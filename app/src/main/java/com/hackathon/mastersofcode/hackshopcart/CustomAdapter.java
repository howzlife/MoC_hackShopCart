package com.hackathon.mastersofcode.hackshopcart;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import Domain.Item;
import WebServiceApi.WebServiceGateway;

import java.util.List;

/**
 * Created by nicolasdubus on 15-09-26.
 */
class CustomAdapter extends ArrayAdapter<Item>{
    public CustomAdapter(Context context, List<Item> items) {
        super(context, R.layout.cart_element_row, items);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View customView = inflater.inflate(R.layout.cart_element_row, parent, false);

        String itemQuantity = String.valueOf(getItem(position).getQuantity());
        String itemName = getItem(position).getName();
        String itemPrice = String.valueOf(getItem(position).getPrice());

        TextView quantity = (TextView) customView.findViewById(R.id.quantity);
        quantity.setText(itemQuantity);
        TextView name = (TextView) customView.findViewById(R.id.itemName);
        name.setText(itemName);
        TextView price = (TextView) customView.findViewById(R.id.price);
        double totalItemPrice = Double.valueOf(itemPrice) * Double.valueOf(itemQuantity);
        price.setText(String.valueOf(totalItemPrice));

        return customView;
    }
}
