package com.hackathon.mastersofcode.hackshopcart;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.view.Menu;
import android.view.MenuItem;
import android.content.Intent;

import Domain.Item;
import Domain.Cart;
import WebServiceApi.WebServiceGateway;

import com.firebase.client.DataSnapshot;
import com.firebase.client.AuthData;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import google.zxing.integration.android.IntentIntegrator;
import google.zxing.integration.android.IntentResult;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.HashMap;

import javax.net.ssl.HttpsURLConnection;

public class CameraActivity extends Activity implements OnClickListener {

    private Button scanButton, payButton;
    private TextView formatTxt, contentTxt;
    private WebServiceGateway gateway = new WebServiceGateway();
    private HashMap<String, Item> itemsMap;
    private double subTotal, tax = 0.00;
    private String total = "0.00";
    private FirebaseWrapper wrapper;

    private final Cart cart = new Cart();
    private final String TAG = "Camera Activity";
    private final String TRANSACTION_ID = Calendar.getInstance().SECOND + java.util.UUID.randomUUID().toString().replaceAll("-", "");
    private AllowTransaction allowTransaction = new AllowTransaction(TRANSACTION_ID);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        scanButton = (Button)findViewById(R.id.scanButton);
        payButton = (Button) findViewById(R.id.payButton);
        formatTxt = (TextView)findViewById(R.id.scan_format);
        contentTxt = (TextView)findViewById(R.id.scan_content);

        scanButton.setOnClickListener(this);
        payButton.setOnClickListener(this);

        itemsMap = gateway.getItemsMap();

        ListAdapter listAdapter = new CustomAdapter(this, cart.getItemsList());
        ListView cartItemsListView = (ListView) findViewById(R.id.cartItemsListView);
        cartItemsListView.setAdapter(listAdapter);

        Firebase.setAndroidContext(this);
        Firebase ref = new Firebase("https://pliu-test.firebaseio.com/");
        ref.authWithCustomToken("cP0KXtG09Tw7SIr7gXJMNpfcDChts06sKraqiBNd", new Firebase.AuthResultHandler() {
            @Override
            public void onAuthenticated(AuthData authData) {
                Log.d(TAG, "Successfully logged in with User ID: " + authData.getUid() + ", Provider: " + authData.getProvider());
            }
            @Override
            public void onAuthenticationError(FirebaseError firebaseError) {
                Log.d(TAG, firebaseError.toString());
            }
        });

        wrapper = new FirebaseWrapper();
        wrapper.activate(TRANSACTION_ID, ref);

        wrapper.inititateTransaction();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_camera, menu);
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

    @Override
    public void onClick(View view){
        if(view.getId()==R.id.scanButton){
            IntentIntegrator scanIntegrator = new IntentIntegrator(this);
            scanIntegrator.initiateScan();
        }

        if(view.getId()==R.id.payButton) {
            if(subTotal != 0) {
                DecimalFormat decimalFormat = new DecimalFormat("0.00");
                tax = Double.parseDouble(decimalFormat.format(subTotal * 0.13));
                total = String.valueOf(Double.parseDouble(decimalFormat.format(subTotal + tax)));
            }
            AlertDialog.Builder alert = new AlertDialog.Builder(this);

            alert.setTitle("Click \"Ok\" to authorize payment");
            alert.setMessage("SubTotal: $" + String.valueOf(subTotal) + "\n Tax: $" + String.valueOf(tax) + "\n Total: $" + total);

            alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    allowTransaction.execute();
                }
            });

            alert.setNegativeButton("Cancel",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                        }
                    });

            alert.show();
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
//        get scan results
        IntentResult scanningResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
        if (scanningResult != null) {
            // Display data
            String scanContent = scanningResult.getContents();

            if (itemsMap.containsKey(scanContent)) {
                final Item item = itemsMap.get(scanContent);
                item.addOne();
                cart.addItem(item);
                gateway.addTransactionElement(TRANSACTION_ID, item);
                subTotal += item.getPrice();

                wrapper.addItem(item);
            }

        } else {
            Toast toast = Toast.makeText(getApplicationContext(),
                    "No scan data received!", Toast.LENGTH_SHORT);
            toast.show();
        }
    }

    /**
     * Represents an Async task allowing transaction
     */
    public class AllowTransaction extends AsyncTask<Void, Void, Boolean> {

        private String transactionId;

        AllowTransaction(String transactionId) {
            this.transactionId = transactionId;
        }

        @Override
        protected Boolean doInBackground(Void... params) {

            try {
                URL url = new URL("https://glacial-bastion-6427.herokuapp.com/charge_card");
                HttpsURLConnection connection = (HttpsURLConnection)url.openConnection();

                String urlParameters = "email=nico.dubus@hotmail.com&transID="+TRANSACTION_ID;
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

                BufferedReader br  = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String line = "";
                StringBuilder responseOutput = new StringBuilder();

                while((line = br.readLine()) != null){
                    responseOutput.append(line);
                }

                br.close();
                output += System.getProperty("line.separator") + responseOutput.toString();
                Log.d(TAG, output);

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
//                    //Send authorization to process payment, return to home screen
//                    Intent intent = new Intent(this, ShopperViewActivity.class);
//                    /*EditText editText = (EditText) findViewById(R.id.edit_message);
//                    String message = editText.getText().toString();
//                    intent.putExtra(EXTRA_MESSAGE, message);*/
//                    startActivity(intent);

            return true;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            transactionId = null;

            if (success) {
                Intent databackIntent = new Intent();
                databackIntent.putExtra("login_status", "OK");
                setResult(Activity.RESULT_OK, databackIntent);
                finish();
            } else {

            }
        }

        @Override
        protected void onCancelled() {
            transactionId = null;
        }
    }
}
