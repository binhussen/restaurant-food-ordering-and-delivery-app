package com.group_7.mhd.managerapp;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
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
import android.widget.EditText;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.group_7.mhd.managerapp.Common.Common;
import com.group_7.mhd.managerapp.Model.Chaf;
import com.group_7.mhd.managerapp.Model.User;
import com.group_7.mhd.managerapp.ViewHolder.ChafViewHolder;
import com.group_7.mhd.managerapp.ViewHolder.UserViewHolder;

import java.util.HashMap;
import java.util.Map;

public class User_Management extends AppCompatActivity {


    public RecyclerView recyclerView;
    public RecyclerView.LayoutManager layoutManager;

    FirebaseDatabase database;
    DatabaseReference users;

    FirebaseRecyclerAdapter<User, UserViewHolder> adapter;

    //forget
    FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    FirebaseUser firebaseUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user__management);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Customers");

        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_black_24dp);

        recyclerView = (RecyclerView)findViewById(R.id.recycler_user);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        //firebase
        database = FirebaseDatabase.getInstance();
        users = database.getReference(Common.USER_TABLE);

        //load all users
        loadUsers();
    }

    private void loadUsers() {
        adapter = new FirebaseRecyclerAdapter<User, UserViewHolder>(
                User.class,
                R.layout.user_layout,
                UserViewHolder.class,
                users

        ) {
            @Override
            protected void populateViewHolder(final UserViewHolder viewHolder, final User model, final int position) {

                viewHolder.user_name.setText(model.getName());
                viewHolder.user_phone.setText(model.getPhone());
                viewHolder.user_password.setText(model.getPassword());
                viewHolder.user_secure.setText(model.getEmail());

                if(!Boolean.parseBoolean(model.getIsStaff())) { //isStaff == true
                        viewHolder.btn_delete.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                removeUser(adapter.getRef(position).getKey());
                            }
                        });
                }else{
                    Toast.makeText(User_Management.this,"You Connot Delete This Account",Toast.LENGTH_SHORT).show();
                }
            }
        };
        adapter.startListening();
        recyclerView.setAdapter(adapter);

    }

    private void removeUser(final String key) {
        android.support.v7.app.AlertDialog.Builder CheckBuild = new android.support.v7.app.AlertDialog.Builder(User_Management.this);
        CheckBuild.setIcon(R.drawable.no);
        CheckBuild.setTitle(R.string.error);
        CheckBuild.setMessage(R.string.customerdelete);

        //Builder Retry Button

        CheckBuild.setPositiveButton(R.string.no, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int id) {
                //Exit The Activity
                dialogInterface.dismiss();
            }

        });
        CheckBuild.setNegativeButton(R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(final DialogInterface dialogInterface, int which) {

                users.child(key)
                        .removeValue()
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                dialogInterface.dismiss();
                                Toast.makeText(User_Management.this,"Removed Success", Toast.LENGTH_SHORT).show();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        dialogInterface.dismiss();
                        Toast.makeText(User_Management.this,e.getMessage(),Toast.LENGTH_SHORT).show();
                    }
                });
                adapter.notifyDataSetChanged();

            }

        });
        android.support.v7.app.AlertDialog alertDialog = CheckBuild.create();
        alertDialog.show();

    }
   /* private void showCreateManagerLayout() {
        //Just copy & past showDialog() and modify
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(User_Management.this);
        alertDialog.setTitle(R.string.createmanager);
        alertDialog.setMessage(R.string.fill);

        LayoutInflater inflater = this.getLayoutInflater();
        View manage = inflater.inflate(R.layout.create_manager_layout, null);

        final EditText editName = manage.findViewById(R.id.edt_name);
        final EditText editPhone = manage.findViewById(R.id.edt_phone);
        final EditText editPassword = manage.findViewById(R.id.edt_password);
        final EditText edit_repeat_Password = manage.findViewById(R.id.edt_repeat_password);
        final EditText edit_secure = manage.findViewById(R.id.edt_securecode);

        alertDialog.setView(manage);
        alertDialog.setIcon(R.drawable.ic_add_black_24dp);

        //setButton
        alertDialog.setPositiveButton(R.string.create, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                dialog.dismiss();
                //Update information

                //forget
                firebaseAuth.createUserWithEmailAndPassword(edit_secure.getText().toString(),
                        editPassword.getText().toString())
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()){
                                    users.addValueEventListener(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {

                                            //Check if user phone number already exist
                                            if (dataSnapshot.child(editPhone.getText().toString()).exists()) {
                                                Toast.makeText(User_Management.this, R.string.exist, Toast.LENGTH_SHORT).show();

                                            } else {
                                                User user = new User(editName.getText().toString(),
                                                        editPassword.getText().toString(),
                                                        editPhone.getText().toString(),
                                                        "true",
                                                        edit_secure.getText().toString()
                                                );
                                                users.child(editPhone.getText().toString()).setValue(user);
                                                Toast.makeText(User_Management.this,R.string.success, Toast.LENGTH_SHORT).show();
                                                finish();
                                            }

                                        }

                                        @Override
                                        public void onCancelled(DatabaseError databaseError) {

                                        }
                                    });
                                    Toast.makeText(User_Management.this, "Manager Created", Toast.LENGTH_SHORT).show();
                                }else {
                                    Toast.makeText(User_Management.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            }
                        });

            }
        });

        alertDialog.setNegativeButton(R.string.cacel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        alertDialog.show();
    }*/
   @Override
   public boolean onOptionsItemSelected(MenuItem item) {
       if (item.getItemId() == android.R.id.home) // Press Back Icon
       {
           onBackPressed();
       }
       return super.onOptionsItemSelected(item);
   }
}
