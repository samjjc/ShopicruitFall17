package com.example.johnny.shopicruitfall17.models;


import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by samuelcatherasoo on 2017-04-15.
 */

public class Order {

    @SerializedName("total_price")
    String price;

    @SerializedName("line_items")
    List<Item> lineItems;

    public List<Item> getLineItems(){
        return lineItems;
    }

    public double getPrice(){
        return Double.parseDouble(price);
    }
}
