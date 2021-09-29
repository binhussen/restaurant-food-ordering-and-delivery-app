package com.group_7.mhd.managerapp;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.ImageView;
import android.widget.TextView;

import com.group_7.mhd.managerapp.Common.Common;
import com.group_7.mhd.managerapp.ViewHolder.OrderDetailAdapter;
import com.squareup.picasso.Picasso;

public class OrderDetail extends AppCompatActivity {

    TextView order_id,order_phone,order_address,order_total,order_comment;
    public ImageView food_image;
    String order_id_value="";
    RecyclerView lstFoods;
    RecyclerView.LayoutManager layoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_detail);

        order_id = (TextView)findViewById(R.id.order_id);
        order_phone = (TextView)findViewById(R.id.order_phone);
        order_address = (TextView)findViewById(R.id.order_address);
        order_total = (TextView)findViewById(R.id.order_total);
        //order_comment = (TextView)findViewById(R.id.order_comment);
        food_image = findViewById(R.id.cart_image);

        lstFoods = (RecyclerView)findViewById(R.id.lstFoods);
        lstFoods.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        lstFoods.setLayoutManager(layoutManager);

        if (getIntent()!= null)
            order_id_value = getIntent().getStringExtra("OrderId");

        /*Picasso.get(*//*cart.getBaseContext()*//*)
                .load(listData.get(position).getImage())
                .resize(70,70)
                .centerCrop()
                .into(holder.cart_image);*/
        //set value
        order_id.setText(order_id_value);
        order_phone.setText(Common.currentRequest.getPhone());
        order_total.setText(Common.currentRequest.getTotal());
        order_address.setText(Common.currentRequest.getAddresslat());
        order_address.setText(Common.currentRequest.getAddresslon());

        OrderDetailAdapter adapter = new OrderDetailAdapter(Common.currentRequest.getFoods());
        adapter.notifyDataSetChanged();
        lstFoods.setAdapter(adapter);
    }
}
