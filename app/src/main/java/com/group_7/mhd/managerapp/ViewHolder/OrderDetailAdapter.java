package com.group_7.mhd.managerapp.ViewHolder;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.group_7.mhd.managerapp.Model.Order;
import com.group_7.mhd.managerapp.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

class MyViewHolder extends RecyclerView.ViewHolder {

    public TextView name,quantity,price,discount;
    public ImageView food_image;

    public MyViewHolder(@NonNull View itemView) {
        super(itemView);
        name = (TextView)itemView.findViewById(R.id.product_name);
        quantity = (TextView)itemView.findViewById(R.id.product_quantity);
        price = (TextView)itemView.findViewById(R.id.product_price);
        discount = (TextView)itemView.findViewById(R.id.product_discount);
        food_image = itemView.findViewById(R.id.cart_image);
    }
}

public class OrderDetailAdapter extends RecyclerView.Adapter<MyViewHolder>{

    List<Order> myOrders;

    public OrderDetailAdapter(List<Order> myOrders) {
        this.myOrders = myOrders;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.order_detail_layout,parent,false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Order order = myOrders.get(position);
        Picasso.get(/*cart.getBaseContext()*/)
                .load(myOrders.get(position).getImage())
                .resize(70,70)
                .centerCrop()
                .into(holder.food_image);

        holder.name.setText(String.format("Name : %s",order.getProductName()));
        holder.quantity.setText(String.format("Quantity : %s",order.getQuantity()));
        holder.price.setText(String.format("Price : %s",order.getPrice()));
        holder.discount.setText(String.format("Discount : %s",order.getDiscount()));
    }

    @Override
    public int getItemCount() {
        return myOrders.size();
    }
}
