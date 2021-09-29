package com.group_7.mhd.managerapp;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.group_7.mhd.managerapp.Common.Common;
import com.group_7.mhd.managerapp.Interface.ItemClickListener;
import com.group_7.mhd.managerapp.Model.Chaf;
import com.group_7.mhd.managerapp.Model.Comment;
import com.group_7.mhd.managerapp.Model.Food;
import com.group_7.mhd.managerapp.Model.Request;
import com.group_7.mhd.managerapp.Model.Shipper;
import com.group_7.mhd.managerapp.Remote.APIService;
import com.group_7.mhd.managerapp.ViewHolder.CommentViewHolder;
import com.group_7.mhd.managerapp.ViewHolder.FoodViewHolder;
import com.group_7.mhd.managerapp.ViewHolder.OrderViewHolder;
import com.group_7.mhd.managerapp.ViewHolder.ShipperViewHolder;
import com.jaredrummler.materialspinner.MaterialSpinner;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class ShipperManagement extends AppCompatActivity {

    private static final Pattern PASSWORD_PATTERN =
            Pattern.compile("^"+
                    "(?=.*[a-z])" +     //at least one lowercase
                    "(?=.*[A-Z])" +     //at least one upercase
                    "(?=.*[0-9])" +     //at least one digit
                    "(?=.*[@#$%^&+=])" +    //at least one special character
                    "(?=\\S+$)" +          //no white space
                    ".{6,}" +               //at least six digit
                    "$");
    private static final Pattern NAME_PATTERN =
            Pattern.compile(new String ("^[a-zA-Z\\s]*$"));

    FloatingActionButton fabAdd;

    public RecyclerView recyclerView;
    public RecyclerView.LayoutManager layoutManager;

    FirebaseDatabase database;
    DatabaseReference shippers;

    FirebaseRecyclerAdapter<Shipper, ShipperViewHolder> adapter;

    MaterialSpinner type;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shipper_management);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Drivers");

        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_black_24dp);

        //init view
        fabAdd = (FloatingActionButton)findViewById(R.id.fab_add);
        fabAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCreateShipperLayout();
            }
        });

        recyclerView = (RecyclerView)findViewById(R.id.recycler_shippers);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        //firebase
        database = FirebaseDatabase.getInstance();
        shippers = database.getReference(Common.SHIPPERS_TABLE);
        
        //load all shippers
        loadShippers();
    }

    private void loadShippers() {
        adapter = new FirebaseRecyclerAdapter<Shipper, ShipperViewHolder>(
                Shipper.class,
                R.layout.create_shipper_layout,
                ShipperViewHolder.class,
                shippers

        ) {
            @Override
            protected void populateViewHolder(ShipperViewHolder viewHolder, final Shipper model, final int position) {

                viewHolder.shipper_name.setText(model.getName());
                viewHolder.shipper_phone.setText(model.getPhone());

                viewHolder.btn_edit.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showEditDialog(adapter.getRef(position).getKey(),model);
                    }
                });
                viewHolder.btn_remove.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        removeShipper(adapter.getRef(position).getKey());
                    }
                });
            }

            @NonNull
            @Override
            public ShipperViewHolder onCreateViewHolder(ViewGroup parent, int viewType){

                View itemView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.shipper_layout,parent,false);
                return new ShipperViewHolder(itemView);

            }
        };
        adapter.startListening();
        recyclerView.setAdapter(adapter);

    }

    private void showEditDialog(String key, Shipper model) {
        //Just copy & past showDialog() and modify
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(ShipperManagement.this);
        alertDialog.setTitle(R.string.updatedriver);
        alertDialog.setMessage(R.string.fill);

        LayoutInflater inflater = this.getLayoutInflater();
        View ship = inflater.inflate(R.layout.create_shipper_layout, null);

        final EditText editName = ship.findViewById(R.id.edt_names);
        final EditText editPhone = ship.findViewById(R.id.edt_phones);
        final EditText editPassword = ship.findViewById(R.id.edt_passwords);
        final EditText editrepeat = ship.findViewById(R.id.edt_passwordrepeat);
        final EditText editSecure = ship.findViewById(R.id.edt_secure);

        type= (MaterialSpinner)ship.findViewById(R.id.type);
        type.setItems("Tackaway", "inside");

        //setdate
        editName.setText(model.getName());
        editPhone.setText(model.getPhone());

        alertDialog.setView(ship);
        alertDialog.setIcon(R.drawable.ic_add_black_24dp);

        //create Dialog and show
        final AlertDialog dialog = alertDialog.create();
        dialog.show();

        //Get AlertDialog from dialog
        final AlertDialog diagview = ((AlertDialog) dialog);
        Button ok = (Button) diagview.findViewById(R.id.ok);
        ok.setText(getString(R.string.update));
        Button cancel = (Button) diagview.findViewById(R.id.cancel);

        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validate()){
                    dialog.dismiss();
                    //Update information

                    int phoneLength = (editPhone.getText().toString()).length();
                    String phoneformat = editPhone.getText().toString();
                    if (phoneLength==10){
                        phoneformat = (editPhone.getText().toString()).substring(1);
                    }else if (phoneLength==13){
                        phoneformat = (editPhone.getText().toString()).substring(4);
                    }else if (phoneLength==14){
                        phoneformat = (editPhone.getText().toString()).substring(5);
                    }

                    String type_input ="";
                    if (type.getSelectedIndex()==0){
                        type_input = "Driver";
                    }else if (type.getSelectedIndex()==1){
                        type_input = "Waiter";
                    }

                    Map<String,Object> update = new HashMap<>();
                    update.put("name",editName.getText().toString());
                    update.put("phone",phoneformat);
                    update.put("password",editPassword.getText().toString());
                    update.put("secureCode",editSecure.getText().toString());
                    update.put("type",type_input);

                    shippers.child(phoneformat)
                            .setValue(update)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Toast.makeText(ShipperManagement.this, R.string.driverupdate, Toast.LENGTH_SHORT).show();
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(ShipperManagement.this,e.getMessage(),Toast.LENGTH_SHORT).show();
                        }
                    });

                }
            }

            private boolean validate() {
                boolean valid = true;

                String name = editName.getText().toString().trim();
                String phone = editPhone.getText().toString().trim();
                String pass = editPassword.getText().toString().trim();
                String reapeat = editrepeat.getText().toString().trim();
                String secure = editSecure.getText().toString().trim();

                if (name.isEmpty()||(name.length()>20||name.length()<2)||!NAME_PATTERN.matcher(name).matches()){
                    editName.setError(getString(R.string.err_name));
                    valid = false;
                }
                int phone_length=13;
                if (phone.startsWith("9")){
                    phone_length=9;
                }
                if (phone.startsWith("09")){
                    phone_length=10;
                }
                if (phone.startsWith("002519")){
                    phone_length=14;
                }
                if (phone.isEmpty()||!(phone.startsWith("9")||phone.startsWith("09")||(phone.startsWith("+2519"))||(phone.startsWith("002519")))||!(phone_length==phone.length())) {
                    editPhone.setError(getString(R.string.err_tel));
                    valid = false;
                }

                if (pass.isEmpty()||!PASSWORD_PATTERN.matcher(pass).matches()){
                    editPassword.setError(getString(R.string.err_password));
                    valid = false;
                }if (reapeat.isEmpty()||!(pass.equals(reapeat))){
                    editPassword.setError(getString(R.string.err_password));
                    valid = false;
                }
                if (secure.isEmpty()||!PASSWORD_PATTERN.matcher(secure).matches()){
                    editSecure.setError(getString(R.string.err_password));
                    valid = false;
                }

                return valid;
            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
    }

    private void removeShipper(final String key) {
        android.support.v7.app.AlertDialog.Builder CheckBuild = new android.support.v7.app.AlertDialog.Builder(ShipperManagement.this);
        CheckBuild.setIcon(R.drawable.no);
        CheckBuild.setTitle("Error!");
        CheckBuild.setMessage("do you went to delete this Driver ?");

        //Builder Retry Button

        CheckBuild.setPositiveButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int id) {
                //Exit The Activity
                finish();
            }

        });
        CheckBuild.setNegativeButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int which) {
                shippers.child(key)
                        .removeValue()
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(ShipperManagement.this,"Removed Success", Toast.LENGTH_SHORT).show();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(ShipperManagement.this,e.getMessage(),Toast.LENGTH_SHORT).show();
                    }
                });
                adapter.notifyDataSetChanged();
            }

        });
        android.support.v7.app.AlertDialog alertDialog = CheckBuild.create();
        alertDialog.show();
    }

    private void showCreateShipperLayout() {
        //Just copy & past showDialog() and modify
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(ShipperManagement.this);
        alertDialog.setTitle(R.string.craetedriver);
        alertDialog.setMessage(R.string.fill);

        LayoutInflater inflater = this.getLayoutInflater();
        final View ship = inflater.inflate(R.layout.create_shipper_layout, null);

        final EditText editName = ship.findViewById(R.id.edt_names);
        final EditText editPhone = ship.findViewById(R.id.edt_phones);
        final EditText editPassword = ship.findViewById(R.id.edt_passwords);
        final EditText editrepeat = ship.findViewById(R.id.edt_passwordrepeat);
        final EditText editsecure = ship.findViewById(R.id.edt_secure);

        type= (MaterialSpinner)ship.findViewById(R.id.type);
        type.setItems("Tackaway", "inside");

        alertDialog.setView(ship);
        alertDialog.setIcon(R.drawable.ic_add_black_24dp);


        //create Dialog and show
        final AlertDialog dialog = alertDialog.create();
        dialog.show();

        //Get AlertDialog from dialog
        final AlertDialog diagview = ((AlertDialog) dialog);
        Button ok = (Button) diagview.findViewById(R.id.ok);
        Button cancel = (Button) diagview.findViewById(R.id.cancel);

        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validate()){

                    int phoneLength = (editPhone.getText().toString()).length();
                    String phoneformat = editPhone.getText().toString();
                    if (phoneLength==10){
                        phoneformat = (editPhone.getText().toString()).substring(1);
                    }else if (phoneLength==13){
                        phoneformat = (editPhone.getText().toString()).substring(4);
                    }else if (phoneLength==14){
                        phoneformat = (editPhone.getText().toString()).substring(5);
                    }

                    String type_input ="";
                    if (type.getSelectedIndex()==0){
                        type_input = "Driver";
                    }else if (type.getSelectedIndex()==1){
                        type_input = "Waiter";
                    }

                    Shipper shipper = new Shipper();
                    shipper.setName(editName.getText().toString());
                    shipper.setPassword(editPassword.getText().toString());
                    shipper.setSecureCode(editsecure.getText().toString());
                    shipper.setPhone(phoneformat);
                    shipper.setType(type_input);

                    shippers.child(phoneformat)
                            .setValue(shipper)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Toast.makeText(ShipperManagement.this, R.string.drivercreated, Toast.LENGTH_SHORT).show();
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(ShipperManagement.this,e.getMessage(),Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }

            private boolean validate() {
                boolean valid = true;

                String name = editName.getText().toString().trim();
                String phone = editPhone.getText().toString().trim();
                String pass = editPassword.getText().toString().trim();
                String reapeat = editrepeat.getText().toString().trim();
                String secure = editsecure.getText().toString().trim();

                if (name.isEmpty()||(name.length()>20||name.length()<2)||!NAME_PATTERN.matcher(name).matches()){
                    editName.setError(getString(R.string.err_name));
                    valid = false;
                }
                int phone_length=13;
                if (phone.startsWith("09")){
                    phone_length=10;
                }
                if (phone.startsWith("002519")){
                    phone_length=14;
                }
                if (phone.isEmpty()||!(phone.startsWith("09")||(phone.startsWith("+2519"))||(phone.startsWith("002519")))||!(phone_length==phone.length())) {
                    editPhone.setError(getString(R.string.err_tel));
                    valid = false;
                }

                if (pass.isEmpty()||!PASSWORD_PATTERN.matcher(pass).matches()){
                    editPassword.setError(getString(R.string.err_password));
                    valid = false;
                }if (reapeat.isEmpty()||!(pass.equals(reapeat))){
                    editrepeat.setError(getString(R.string.err_password_confirmation));
                    valid = false;
                }
                if (secure.isEmpty()||!PASSWORD_PATTERN.matcher(secure).matches()){
                    editsecure.setError(getString(R.string.err_password));
                    valid = false;
                }

                return valid;
            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
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
