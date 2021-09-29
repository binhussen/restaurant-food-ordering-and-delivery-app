package com.group_7.mhd.managerapp;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.group_7.mhd.managerapp.Common.Common;
import com.group_7.mhd.managerapp.Interface.ItemClickListener;
import com.group_7.mhd.managerapp.Model.Chaf;
import com.group_7.mhd.managerapp.Model.DataMessage;
import com.group_7.mhd.managerapp.Model.MyResponse;
import com.group_7.mhd.managerapp.Model.Notification;
import com.group_7.mhd.managerapp.Model.Request;
import com.group_7.mhd.managerapp.Model.Sender;
import com.group_7.mhd.managerapp.Model.Shipper;
import com.group_7.mhd.managerapp.Model.Token;
import com.group_7.mhd.managerapp.Remote.APIService;
import com.group_7.mhd.managerapp.ViewHolder.ChafViewHolder;
import com.group_7.mhd.managerapp.ViewHolder.OrderViewHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.jaredrummler.materialspinner.MaterialSpinner;
import com.mancj.materialsearchbar.MaterialSearchBar;
import com.squareup.picasso.Picasso;

import java.sql.Driver;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OrderStatus extends AppCompatActivity {

    public RecyclerView recyclerView;
    public RecyclerView.LayoutManager layoutManager;

    FirebaseRecyclerAdapter<Request,OrderViewHolder> adapter;

    FirebaseRecyclerAdapter<Chaf, ChafViewHolder> chafadapter;

    FirebaseDatabase db;
    DatabaseReference requests,chafs;

    MaterialSpinner spinner, shipperSpinner;

    APIService mService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_status);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Order Status");

        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_black_24dp);

        //init service
        mService = Common.getFCMClient();

        //init firebase database
        db=FirebaseDatabase.getInstance();
        requests=db.getReference(Common.ORDER_TABLE);

        recyclerView=(RecyclerView) findViewById(R.id.listorders);
        recyclerView.setHasFixedSize(true);
        layoutManager=new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        chafs = db.getReference(Common.CHAFS_TABLE);

        //if we start Orderstatus acctivity from Home
        //we will not put any extra
        if(getIntent().getExtras()==null)
        {
            loadOrders(Common.currentUser);


        }else
        {
            loadOrders(getIntent().getStringExtra("userPhone"));
        }




    }

    private void loadOrders(String phone) {
        adapter=new FirebaseRecyclerAdapter<Request, OrderViewHolder>(
                Request.class,
                R.layout.order_layout,
                OrderViewHolder.class,
                requests/*.orderByChild("phone")
                .equalTo(phone)*/

        ) {
            @Override
            protected void populateViewHolder(final OrderViewHolder viewHolder, final Request model, final int position) {

                viewHolder.txtOrderId.setText(adapter.getRef(position).getKey());
                viewHolder.txtOrderStatus.setText(Common.convertCodeToStatus(model.getStatus()));
                viewHolder.txtOrderAddress.setText(model.getAddresslat()+" , "+model.getAddresslon());
                viewHolder.txtOrderPhone.setText(model.getPhone());
                viewHolder.txtOrderDate.setText(Common.getDate(Long.parseLong(adapter.getRef(position).getKey())));

                if (getItem(position).getPaymentMethod().equals("COD")){
                    viewHolder.chkpayemnt.setCheckedImmediately(true);
                }

                if (getItem(position).getTackAway().equals("false")) {
                    Picasso.get(/*cart.getBaseContext()*/)
                            .load(R.drawable.table)
                            .resize(70,70)
                            .centerCrop()
                            .into(viewHolder.imglogo);

                }else if (getItem(position).getTackAway().equals("true")){
                    Picasso.get(/*cart.getBaseContext()*/)
                            .load(R.drawable.shipper)
                            .resize(70,70)
                            .centerCrop()
                            .into(viewHolder.imglogo);
                }


                viewHolder.btnEdit.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ShowUpdateDialog(adapter.getRef(position).getKey(),adapter.getItem(position),model.getStatus(), model.getTackAway());
                    }
                });

                viewHolder.btnRemove.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        DeleteOrder(adapter.getRef(position).getKey());
                    }
                });

                viewHolder.btnDetail.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent orderDetail = new Intent(OrderStatus.this, OrderDetail.class);
                        Common.currentRequest = model;
                        orderDetail.putExtra("OrderId",adapter.getRef(position).getKey());
                        startActivity(orderDetail);
                    }
                });

                /*viewHolder.btnDirection.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent traking = new Intent(OrderStatus.this,TrackingOrder.class);
                        Common.currentRequest = model;
                        startActivity(traking);
                    }
                });*/

               /*viewHolder.setItemClickListner(new ItemClickListener() {
                   @Override
                   public void onClick(View view, int position, boolean isLongClick) {
                       //
                       if (!isLongClick)
                       {
                           *//*Intent tra = new Intent(OrderStatus.this, TrackingOrder.class);
                           Common.currentRequest = model;
                           startActivity(tra);*//*
                       }
                       else
                       {
                           Intent orderDetail = new Intent(OrderStatus.this, OrderDetail.class);
                           Common.currentRequest = model;
                           orderDetail.putExtra("OrderId",adapter.getRef(position).getKey());
                           startActivity(orderDetail);
                       }

                       //Toast.makeText(OrderStatus.this, "your order status is: "+Common.convertCodeToStatus(model.getStatus()), Toast.LENGTH_SHORT).show();
                   }
               });*/
            }
        };
        adapter.notifyDataSetChanged();
        recyclerView.setAdapter(adapter);

    }

   /* public boolean onContextItemSelected(MenuItem item){

        if(item.getTitle().equals(Common.UPDATE))
            ShowUpdateDialog(adapter.getRef(item.getOrder()).getKey(),adapter.getItem(item.getOrder()));
        else if (item.getTitle().equals(Common.DELETE))
            DeleteOrder(adapter.getRef(item.getOrder()).getKey());
        return super.onContextItemSelected(item);
    }*/

    private void DeleteOrder(final String key) {
            AlertDialog.Builder CheckBuild = new AlertDialog.Builder(OrderStatus.this);
            CheckBuild.setIcon(R.drawable.no);
            CheckBuild.setTitle(R.string.error);
            CheckBuild.setMessage(R.string.oredrdelete);

            //Builder Retry Button

            CheckBuild.setPositiveButton(R.string.no, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int id) {
                    //Exit The Activity
                    finish();
                }

            });
            CheckBuild.setNegativeButton(R.string.yes, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int which) {
                    requests.child(key).removeValue();
                    adapter.notifyDataSetChanged();
                }

            });
            AlertDialog alertDialog = CheckBuild.create();
            alertDialog.show();
    }

    private void ShowUpdateDialog(String key, final Request item, final String validate, final String tackaway) {

        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(OrderStatus.this);
        alertDialog.setTitle(R.string.oredrupd);
        alertDialog.setMessage(R.string.statusupdate);
       // alertDialog.setIcon(R.drawable.ic_access_time_black_24dp);

        LayoutInflater inflater=this.getLayoutInflater();
        final View view = inflater.inflate(R.layout.update_order_layout, null);


         spinner= (MaterialSpinner) view.findViewById(R.id.statusSpinner);
         spinner.setItems(getString(R.string.placed), getString(R.string.payed), getString(R.string.delivering));

        shipperSpinner= (MaterialSpinner) view.findViewById(R.id.shipperSpinner);
        final List<String> shipperList = new ArrayList<>();
        FirebaseDatabase.getInstance().getReference(Common.SHIPPERS_TABLE)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot shipperSnapshot:dataSnapshot.getChildren()){
                            Shipper shipper = dataSnapshot.child(shipperSnapshot.getKey()).getValue(Shipper.class);

                            if (tackaway.equals("true")){
                                if (shipper.getType().equals("Driver")){
                                    shipperList.add(shipperSnapshot.getKey());
                                }
                            }else {
                                if (shipper.getType().equals("Waiter")){
                                    shipperList.add(shipperSnapshot.getKey());
                                }
                            }

                        }
                        shipperSpinner.setItems(shipperList);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

        alertDialog.setView(view);

        //spinner.setSelectedIndex(Integer.parseInt(item.getStatus()));

        final  String localKey = key;

        alertDialog.setPositiveButton(R.string.yes,new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();

                int SPINNER_VALIDATE = spinner.getSelectedIndex();
                if (spinner.getSelectedIndex()==2){
                    item.setStatus(String.valueOf(spinner.getSelectedIndex()+1));
                    SPINNER_VALIDATE = spinner.getSelectedIndex()+1;
                }else {
                    item.setStatus(String.valueOf(spinner.getSelectedIndex()));

                }

                if (SPINNER_VALIDATE>Integer.parseInt(validate)) {

                    if ((Integer.parseInt(validate)==0||Integer.parseInt(validate)==1)&&((spinner.getSelectedIndex()+1)==3)) {
                        Toast.makeText(OrderStatus.this,"You Can't send to delivery before cooked",Toast.LENGTH_SHORT).show();
                    }
                    else{
                        if (item.getStatus().equals("3")) {
                            FirebaseDatabase.getInstance().getReference(Common.ORDER_NEED_SHIPPERS_TABLE)
                                    .child(shipperSpinner.getItems().get(shipperSpinner.getSelectedIndex()).toString())
                                    .child(localKey)
                                    .setValue(item);
                            requests.child(localKey).setValue(item);
                            adapter.notifyDataSetChanged();//add to update item size

                            sendOrderStatusToUser(localKey,item);
                            sendOrderRequestToDriver(shipperSpinner.getItems().get(shipperSpinner.getSelectedIndex()).toString(), item);
                        }
                        else if(item.getStatus().equals("1")) {

                            item.setStatus(String.valueOf(spinner.getSelectedIndex()));
                            requests.child(localKey).setValue(item);

                            adapter.notifyDataSetChanged();//add to update item size
                            sendOrderStatusToUser(localKey,item);

                            sendOrderStatusToUser(localKey,item);
                        }
                        else {
                            requests.child(localKey).setValue(item);
                            adapter.notifyDataSetChanged();//add to update item size

                            sendOrderStatusToUser(localKey,item);
                        }
                    }
                }
                else{
                    Toast.makeText(OrderStatus.this,"You Can't Update from Current Statues to Previos",Toast.LENGTH_SHORT).show();
                }

            }

        });

        alertDialog.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });

        alertDialog.show();
    }

    private void sendOrderRequestToDriver(String shipperPhone, Request item) {
        DatabaseReference tokens = db.getReference("Tokens");

        tokens.child(shipperPhone)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        /*for (DataSnapshot postSnapShot : dataSnapshot.getChildren()) {*/
                            Token serverToken = dataSnapshot.getValue(Token.class);

                            Map<String, String> content = new HashMap<>();
                            content.put("title","Kana Restaurant");
                            content.put("Message","Your Order need Driver");
                            DataMessage dataMessage = new DataMessage(serverToken.getToken(),content);

                            String test = new Gson().toJson(dataMessage);
                            Log.d("content ",test);

                            mService.sendNotification(dataMessage)
                                    .enqueue(new Callback<MyResponse>() {
                                        @Override
                                        public void onResponse(Call<MyResponse> call, Response<MyResponse> response) {

                                            if (response.body().success == 1) {
                                                Toast.makeText(OrderStatus.this, "Sent to Shipper.", Toast.LENGTH_SHORT).show();
                                            } else {
                                                Toast.makeText(OrderStatus.this, "Failed to send notification !!!", Toast.LENGTH_SHORT).show();
                                            }
                                        }

                                        @Override
                                        public void onFailure(Call<MyResponse> call, Throwable t) {

                                            Log.e("ERROR ", t.getMessage());
                                        }
                                    });
                        }
                    /*}*/
                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                    }
                });
    }

    private void sendOrderRequestToChafe(String chafePhone, Request item) {
        DatabaseReference tokens = db.getReference("Tokens");

        tokens.child(chafePhone)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        /*for (DataSnapshot postSnapShot : dataSnapshot.getChildren()) {*/
                        Token serverToken = dataSnapshot.getValue(Token.class);

                        Map<String, String> content = new HashMap<>();
                        content.put("title","Kana Restaurant");
                        content.put("Message","Thisn Order has to Cooke");
                        DataMessage dataMessage = new DataMessage(serverToken.getToken(),content);

                        String test = new Gson().toJson(dataMessage);
                        Log.d("content ",test);

                        mService.sendNotification(dataMessage)
                                .enqueue(new Callback<MyResponse>() {
                                    @Override
                                    public void onResponse(Call<MyResponse> call, Response<MyResponse> response) {

                                        if (response.body().success == 1) {
                                            Toast.makeText(OrderStatus.this, "Sent to Chafe.", Toast.LENGTH_SHORT).show();
                                        } else {
                                            Toast.makeText(OrderStatus.this, "Failed to send notification !!!", Toast.LENGTH_SHORT).show();
                                        }
                                    }

                                    @Override
                                    public void onFailure(Call<MyResponse> call, Throwable t) {

                                        Log.e("ERROR ", t.getMessage());
                                    }
                                });
                    }
                    /*}*/
                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                    }
                });
    }

    private void sendOrderStatusToUser(final String key, final Request item) {

        DatabaseReference tokens = db.getReference("Tokens");
        tokens.child(item.getPhone())/*orderByKey().equalTo(item.getPhone())*/
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        /*for (DataSnapshot postSnapShot : dataSnapshot.getChildren()) {*/
                        if (dataSnapshot.exists())
                        {
                            Token serverToken = dataSnapshot.getValue(Token.class);

                            //create pay laoad
                            /*Notification notification = new Notification("MHD", " Your order " + key + " was updated");
                            Sender content = new Sender(serverToken.getToken(), notification);
*/
                            Map<String, String> content = new HashMap<>();
                            content.put("title","Kana Restaurant");
                            content.put("Message","Your Order need Driver");
                            DataMessage dataMessage = new DataMessage(serverToken.getToken(),content);

                            String test = new Gson().toJson(dataMessage);
                            Log.d("content ",test);

                            mService.sendNotification(dataMessage)
                                    .enqueue(new Callback<MyResponse>() {
                                        @Override
                                        public void onResponse(Call<MyResponse> call, Response<MyResponse> response) {
                                            if (response.body()!=null) {
                                                if (response.body().success == 1) {
                                                    Toast.makeText(OrderStatus.this, "Order was Updated.", Toast.LENGTH_SHORT).show();
                                                } else {
                                                    Toast.makeText(OrderStatus.this, "Order was updated bu Failed to send notification !!!", Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        }

                                        @Override
                                        public void onFailure(Call<MyResponse> call, Throwable t) {

                                            Log.e("ERROR ", t.getMessage());
                                        }
                                    });
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                    }
                });
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) // Press Back Icon
        {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }
}
