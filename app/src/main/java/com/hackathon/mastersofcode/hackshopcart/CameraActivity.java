package com.hackathon.mastersofcode.hackshopcart;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.view.Menu;
import android.view.MenuItem;

import Domain.Item;
import Domain.Cart;
import WebServiceApi.WebServiceGateway;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import google.zxing.integration.android.IntentIntegrator;
import google.zxing.integration.android.IntentResult;

import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.HashMap;

public class CameraActivity extends Activity implements OnClickListener {

    private Button scanButton, payButton;
    private TextView formatTxt, contentTxt;
    private WebServiceGateway gateway = new WebServiceGateway();
    private HashMap<String, Item> itemsMap;
    private double subTotal, tax = 0.00;
    private String total = "0.00";
    Firebase firebaseRef = new Firebase("https://hackshopcart.firebaseio.com/");


    private final Cart cart = new Cart();
    private final String TAG = "Camera Activity";
    private final String TRANSACTION_ID = Calendar.getInstance().SECOND + java.util.UUID.randomUUID().toString().replaceAll("-", "");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        Firebase.setAndroidContext(this);

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

        //TODO: Add transaction ID to DB, pull total price
        firebaseRef.setValue(TRANSACTION_ID);
        firebaseRef.push();
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
                    //Send authorization to process payment, return to home screen

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
            String scanFormat = scanningResult.getFormatName();

            if (itemsMap.containsKey(scanContent)) {
                final Item item = itemsMap.get(scanContent);
                item.addOne();
                cart.addItem(item);
                gateway.addTransactionElement(TRANSACTION_ID, item);
                subTotal += item.getPrice();

            }

            for (Item item: cart.getItemsList()) {
                formatTxt.setText("Item Name: " + item.getName());
                contentTxt.setText("Price: " + item.getPrice());
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {}
            }

        } else {
            Toast toast = Toast.makeText(getApplicationContext(),
                    "No scan data received!", Toast.LENGTH_SHORT);
            toast.show();
        }
    }
}
