package com.hackathon.mastersofcode.hackshopcart;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.simplify.android.sdk.CardEditor;
import com.simplify.android.sdk.CardToken;
import com.simplify.android.sdk.Simplify;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

public class AddCardActivity extends Activity {

    private static final String TAG = "AddCardActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_card);

        Simplify.init("sbpb_MDQxYTE2NjEtYTJiMS00M2IwLTlhMDktNzczOGEzMGUxZDlj");

        // init card editor
        final CardEditor cardEditor = (CardEditor) findViewById(R.id.card_editor);
        final Button addCardButton = (Button) findViewById(R.id.bAddCardWithDetails);

// add state change listener
        cardEditor.addOnStateChangedListener(new CardEditor.OnStateChangedListener() {
            @Override
            public void onStateChange(CardEditor cardEditor) {
                // true: card editor contains valid and complete card information
                addCardButton.setEnabled(cardEditor.isValid());
            }
        });

// add checkout button click listener
        addCardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // create a card token
                Simplify.createCardToken(cardEditor.getCard(), new CardToken.Callback() {
                    @Override
                    public void onSuccess(CardToken cardToken) {
                        new AsyncTask<CardToken,Void,Boolean>(){
                            @Override
                            protected void onPostExecute(Boolean aBoolean) {
                                super.onPostExecute(aBoolean);
                                if(aBoolean)
                                    finish();
                            }

                            @Override
                            protected Boolean doInBackground(CardToken... params) {
                                try {
                                    URL url = new URL("https://glacial-bastion-6427.herokuapp.com/add_card");
                                    HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();

                                    String urlParameters = "email=" + MoCUtils.user_email + "&card_token=" + params[0].getId();
                                    connection.setRequestMethod("POST");

                                    connection.setDoOutput(true);
                                    DataOutputStream dStream = new DataOutputStream(connection.getOutputStream());
                                    dStream.writeBytes(urlParameters);
                                    dStream.flush();
                                    dStream.close();

                                    int responseCode = connection.getResponseCode();
                                    String output = "Request URL " + url;
                                    output += System.getProperty("line.separator") + "Request Parameters " + urlParameters;
                                    output += System.getProperty("line.separator") + "Response Code " + responseCode;

                                    BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                                    String line = "";
                                    StringBuilder responseOutput = new StringBuilder();

                                    while ((line = br.readLine()) != null) {
                                        responseOutput.append(line);
                                    }

                                    br.close();
                                    output += System.getProperty("line.separator") + responseOutput.toString();
                                    Log.d(TAG, output);
                                    Log.d(TAG, "email=" + MoCUtils.user_email + "&card_token=" + params[0].getId());

                                    if(responseCode == 200){
                                        return true;
                                    }

                                } catch (MalformedURLException e) {
                                    e.printStackTrace();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }


                                return false;
                            }
                        }.execute(cardToken);


                    }

                    @Override
                    public void onError(Throwable throwable) {
                        // ...
                    }
                });
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_add_card, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

}
