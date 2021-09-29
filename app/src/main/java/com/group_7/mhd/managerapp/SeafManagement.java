package com.group_7.mhd.managerapp;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
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
import com.group_7.mhd.managerapp.Model.Chaf;
import com.group_7.mhd.managerapp.Model.Shipper;
import com.group_7.mhd.managerapp.ViewHolder.ChafViewHolder;
import com.group_7.mhd.managerapp.ViewHolder.ShipperViewHolder;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class SeafManagement extends AppCompatActivity {

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
    DatabaseReference chafs;
    DatabaseReference driver;

    FirebaseRecyclerAdapter<Chaf, ChafViewHolder> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seaf_management);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Chafes");

        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_black_24dp);
        //init view
        fabAdd = (FloatingActionButton)findViewById(R.id.fab_add);
        fabAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCreateShipperLayout();
            }
        });

        recyclerView = (RecyclerView)findViewById(R.id.recycler_chaf);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        //firebase
        database = FirebaseDatabase.getInstance();
        chafs = database.getReference(Common.CHAFS_TABLE);
        driver = database.getReference(Common.SHIPPERS_TABLE);

        //load all shippers
        loadChafs();
    }

    private void loadChafs() {
        adapter = new FirebaseRecyclerAdapter<Chaf, ChafViewHolder>(
                Chaf.class,
                R.layout.create_chaf_layout,
                ChafViewHolder.class,
                chafs

        ) {
            @Override
            public ChafViewHolder onCreateViewHolder(ViewGroup parent, int viewType){

                View itemView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.chaf_layout,parent,false);
                return new ChafViewHolder(itemView);

            }

            @Override
            protected void populateViewHolder(ChafViewHolder viewHolder, final Chaf model, final int position) {

                viewHolder.chaf_name.setText(model.getName());
                viewHolder.chaf_phone.setText(model.getPhone());

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
        };
        adapter.startListening();
        recyclerView.setAdapter(adapter);

    }

    private void showEditDialog(String key, Chaf model) {
        //Just copy & past showDialog() and modify
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(SeafManagement.this);
        alertDialog.setTitle(R.string.updatesheaf);
        alertDialog.setMessage(R.string.fill);

        LayoutInflater inflater = this.getLayoutInflater();
        View chaf = inflater.inflate(R.layout.create_chaf_layout, null);

        final EditText editName = chaf.findViewById(R.id.edt_names);
        final EditText editPhone = chaf.findViewById(R.id.edt_phones);
        final EditText editPassword = chaf.findViewById(R.id.edt_passwords);
        final EditText editrepeat = chaf.findViewById(R.id.edt_passwordrepeat);
        final EditText editsecure = chaf.findViewById(R.id.edt_secure);

        //setdate
        editName.setText(model.getName());
        editPhone.setText(model.getPhone());

        alertDialog.setView(chaf);
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
                    if (phoneLength == 10) {
                        phoneformat = (editPhone.getText().toString()).substring(1);
                    } else if (phoneLength == 13) {
                        phoneformat = (editPhone.getText().toString()).substring(4);
                    } else if (phoneLength == 14) {
                        phoneformat = (editPhone.getText().toString()).substring(5);
                    }

                    Map<String,Object> update = new HashMap<>();
                    update.put("name",editName.getText().toString());
                    update.put("phone",phoneformat);
                    update.put("password",editPassword.getText().toString());
                    update.put("secureCode",editsecure.getText().toString());

                    chafs.child(phoneformat)
                            .setValue(update)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Toast.makeText(SeafManagement.this, R.string.sheafupdated, Toast.LENGTH_SHORT).show();
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(SeafManagement.this,e.getMessage(),Toast.LENGTH_SHORT).show();
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
                }
                if (reapeat.isEmpty()||!(pass.equals(reapeat))){
                    editPassword.setError(getString(R.string.err_password));
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

    private void removeShipper(final String key) {
        android.support.v7.app.AlertDialog.Builder CheckBuild = new android.support.v7.app.AlertDialog.Builder(SeafManagement.this);
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
                chafs.child(key)
                        .removeValue()
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(SeafManagement.this,"Removed Success", Toast.LENGTH_SHORT).show();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(SeafManagement.this,e.getMessage(),Toast.LENGTH_SHORT).show();
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
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(SeafManagement.this);
        alertDialog.setTitle(R.string.createsheaf);
        alertDialog.setMessage(R.string.fill);

        LayoutInflater inflater = this.getLayoutInflater();
        View chaf = inflater.inflate(R.layout.create_chaf_layout, null);

        final EditText editName = chaf.findViewById(R.id.edt_names);
        final EditText editPhone = chaf.findViewById(R.id.edt_phones);
        final EditText editPassword = chaf.findViewById(R.id.edt_passwords);
        final EditText editrepeat = chaf.findViewById(R.id.edt_passwordrepeat);
        final EditText editsecure = chaf.findViewById(R.id.edt_secure);

        alertDialog.setView(chaf);
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
                    dialog.dismiss();
                    //Update information

                    chafs.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                            int phoneLength = (editPhone.getText().toString()).length();
                            String phoneformat = editPhone.getText().toString();
                            if (phoneLength == 10) {
                                phoneformat = (editPhone.getText().toString()).substring(1);
                            } else if (phoneLength == 13) {
                                phoneformat = (editPhone.getText().toString()).substring(4);
                            } else if (phoneLength == 14) {
                                phoneformat = (editPhone.getText().toString()).substring(5);
                            }

                            if (!dataSnapshot.child(phoneformat).exists()) {
                                driver.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                        int phoneLength = (editPhone.getText().toString()).length();
                                        String phoneformat = editPhone.getText().toString();
                                        if (phoneLength == 10) {
                                            phoneformat = (editPhone.getText().toString()).substring(1);
                                        } else if (phoneLength == 13) {
                                            phoneformat = (editPhone.getText().toString()).substring(4);
                                        } else if (phoneLength == 14) {
                                            phoneformat = (editPhone.getText().toString()).substring(5);
                                        }

                                        if (!dataSnapshot.child(phoneformat).exists()) {
                                            Chaf cheaf = new Chaf();
                                            cheaf.setName(editName.getText().toString());
                                            cheaf.setPassword(editPassword.getText().toString());
                                            cheaf.setSecureCode(editsecure.getText().toString());
                                            cheaf.setPhone(phoneformat);

                                            chafs.child(phoneformat)
                                                    .setValue(cheaf)
                                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void aVoid) {
                                                            Toast.makeText(SeafManagement.this, R.string.sheafcreated, Toast.LENGTH_SHORT).show();
                                                        }
                                                    }).addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    Toast.makeText(SeafManagement.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                        } else
                                            Toast.makeText(SeafManagement.this,"There is Driver Registered Driver",Toast.LENGTH_SHORT).show();

                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });


                            }else
                                Toast.makeText(SeafManagement.this, "the user already ", Toast.LENGTH_SHORT).show();

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });


                }
            }

            private boolean validate() {
                boolean valid = true;

                String name = editName.getText().toString().trim();
                String phone = editPhone.getText().toString().trim();
                String pass = editPassword.getText().toString().trim();
                String repeat = editrepeat.getText().toString().trim();
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
                }
                if (repeat.isEmpty()||!(pass.equals(repeat))){
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
