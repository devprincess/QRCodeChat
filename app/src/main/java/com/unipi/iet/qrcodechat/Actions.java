package com.unipi.iet.qrcodechat;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.firebase.client.Firebase;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 *  Class related to the activity Actions that shows 2 different actions: creation of the qr code
 *  and scanning of the qr code.
 *
 */
public class Actions extends AppCompatActivity {

    Button createQRCodeButton;
    Button scanQRCodeButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_actions);

        createQRCodeButton = findViewById(R.id.btnCreateQRCode);
        scanQRCodeButton = findViewById(R.id.btnScanQRCode);

        createQRCodeButton.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){

                Intent i = new Intent(Actions.this, CreateQRCode.class);
                i.putExtra("randomkey", Constants.myKey);
                startActivity(i);
            }
        });


        scanQRCodeButton.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                IntentIntegrator integrator = new IntentIntegrator(Actions.this);
                integrator.initiateScan();
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        IntentResult intentResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (intentResult != null) {
            if (intentResult.getContents() == null) {
                Log.d("ActionsActivity", "Cancelled");
                Toast.makeText(this, "Cancelled", Toast.LENGTH_LONG).show();

            } else {
                Log.d("ActionsActivity", "Scanned");
                Toast.makeText(this, "Scanned: " + intentResult.getContents(), Toast.LENGTH_LONG).show();

                //do an split operation to get the username from the string of the qr code
                String[] arrExchange = intentResult.getContents().split("=");
                Constants.hisKey = arrExchange[0];
                Constants.exchangeUsername = arrExchange[1];

                Log.i("His key:",Constants.hisKey);
                Log.i("His username:", Constants.exchangeUsername);

                if ((!Constants.hisKey.isEmpty())&&(!Constants.myKey.isEmpty())){

                    String url = "https://qrcodechat-ca31a.firebaseio.com/exchanges.json";

                    StringRequest request = new StringRequest(Request.Method.GET, url, new Response.Listener<String>(){
                        @Override
                        public void onResponse(String s) {
                            Firebase reference = new Firebase("https://qrcodechat-ca31a.firebaseio.com/exchanges");

                            Utils u = new Utils();
                            String finalKey = u.xorKeys(Constants.myKey, Constants.hisKey);
                            String[] finalKeyArr = finalKey.split("=");
                            finalKey = finalKeyArr[0];

                            Log.i("username:", UserDetails.username);
                            Log.i("exchange usr:", Constants.exchangeUsername);
                            Log.i("key", finalKey);

                            if(s.equals("null")) {
                                Log.i("info:","New registration of a key exchange");

                                //HashMap<String, Object> users = new HashMap<>();
                                HashMap<String, String> keyEntry = new HashMap<>();
                                keyEntry.put("key", finalKey);

                                //users.put(Constants.exchangeUsername, keyEntry);
                                reference.child(UserDetails.username).child(Constants.exchangeUsername).setValue(keyEntry);

                                Toast.makeText(Actions.this, "Key exchange successful", Toast.LENGTH_LONG).show();
                            }else{
                                try {
                                    Log.i("info:","When user already exists in the exchange list");
                                    JSONObject obj = new JSONObject(s);

                                    //HashMap<String, Object> users = new HashMap<>();
                                    HashMap<String, String> keyEntry = new HashMap<>();
                                    keyEntry.put("key", finalKey);

                                    //users.put(Constants.exchangeUsername, keyEntry);
                                    reference.child(UserDetails.username).child(Constants.exchangeUsername).setValue(keyEntry);
                                    Toast.makeText(Actions.this, "Key exchange successful", Toast.LENGTH_LONG).show();


                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }

                        }

                    },new Response.ErrorListener(){
                        @Override
                        public void onErrorResponse(VolleyError volleyError) {
                            System.out.println("" + volleyError );
                        }
                    });

                    RequestQueue rQueue = Volley.newRequestQueue(Actions.this);
                    rQueue.add(request);

                }

            }
        }
    }
}