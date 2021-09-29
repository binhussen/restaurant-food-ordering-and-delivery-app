package com.group_7.mhd.managerapp;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthSettings;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.group_7.mhd.managerapp.Common.Common;
import com.group_7.mhd.managerapp.Model.User;

import io.netopen.hotbitmapgg.library.view.RingProgressBar;
import io.paperdb.Paper;

public class MainActivity extends AppCompatActivity {

    //setting up a finite time for screen delay
    private static int splash_time_Out=2000;
    private Context context;

    RingProgressBar ringProgressBar;
    int progress=0;
    Handler myhandler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            if(msg.what==0)
            {
                if(progress<100)
                {
                    progress+=5;
                    ringProgressBar.setProgress(progress);
                }
            }
        }
    };

    SharedPreferences sharedPreferences;
    Boolean save_login,skip;
    SharedPreferences.Editor edit;
    String type,name,resname;

    FirebaseAuth firebaseAuth;
    FirebaseAuthSettings firebaseAuthSettings;

    FirebaseDatabase db;
    DatabaseReference users;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = this;

        //init paper
        Paper.init(this);


        firebaseAuth = FirebaseAuth.getInstance();
        firebaseAuthSettings = firebaseAuth.getFirebaseAuthSettings();

        //Init Firebase
        db = FirebaseDatabase.getInstance();
        users = db.getReference(Common.USER_TABLE);

        ringProgressBar = (RingProgressBar) findViewById(R.id.ringProgress);
        ringProgressBar.setOnProgressListener(new RingProgressBar.OnProgressListener() {
            @Override
            public void progressToComplete() {
                okDone();
            }
        });
        new Thread(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < 100; i++) {
                    try {
                        Thread.sleep(100);
                        myhandler.sendEmptyMessage(0);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    public void okDone(){
        if (!isNetworkAvilabe()) {
            //Creating an Alertdialog
            AlertDialog.Builder CheckBuild = new AlertDialog.Builder(MainActivity.this);
            CheckBuild.setIcon(R.drawable.no);
            CheckBuild.setTitle(R.string.error);
            CheckBuild.setMessage(R.string.check);

            //Builder Retry Button

            CheckBuild.setPositiveButton(R.string.skip, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int id) {
                    //creating intent and going to the home activity
                    Intent newintent = new Intent(MainActivity.this, SignIn.class);
                    //starting the activity
                    startActivity(newintent);

//                    Intent intent = new Intent(context, First_Activity.class);
//                    context.startActivity(intent);
                    //when intent is start and go to home class then main activity will finish
                    finish();

                }

            });
            /*CheckBuild.setPositiveButton("Retry", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int id) {
                    //Restart The Activity
                    Intent intent = getIntent();
                    finish();
                    startActivity(intent);
                }

            });*/
            CheckBuild.setNegativeButton(R.string.exit, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int which) {
                    //Exit The Activity
                    finish();
                }

            });
            AlertDialog alertDialog = CheckBuild.create();
            alertDialog.show();
        } else {
            //login automatcally
            String user = Paper.book().read(Common.USER_KEY);
            String pwd = Paper.book().read(Common.PWD_KEY);
            if (user != null && pwd != null){
                if (!user.isEmpty() && !pwd.isEmpty()){
                    signInUser(user,pwd);
                }
            }else {
                //creating intent and going to the home activity
                Intent newintent = new Intent(MainActivity.this, SignIn.class);
                //starting the activity
                startActivity(newintent);
                //when intent is start and go to home class then main activity will finish
                finish();
            }

        }
    }

    private void signInUser(final String phone, final String password) {
        final ProgressDialog mDialog = new ProgressDialog(MainActivity.this);
        mDialog.setMessage(getString(R.string.please_wait));
        mDialog.show();

        //phone and password from user editText field
        final String localPhone = phone;
        final String localPassword = password;

        Common.current_password = password;

        firebaseAuth.signInWithEmailAndPassword(phone,password)
                .addOnCompleteListener(MainActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()){

                            //////////
                            users.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {

                                    Paper.book().write(Common.USER_KEY,phone);
                                    Paper.book().write(Common.PWD_KEY,password);

                                    mDialog.dismiss();
                                    /*User user = dataSnapshot.child("923692424").getValue(User.class);
                                    user.setPhone("923692424");*/
                                    Intent intentHome = new Intent(MainActivity.this, Home.class);
                                    /*Common.currentUser = user;*/
                                    startActivity(intentHome);
                                    finish();
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });
                            ////////////
                        }else {
                            mDialog.dismiss();
                            Toast.makeText(MainActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });

    }

    private boolean isNetworkAvilabe()
    {
        ConnectivityManager connectivityManager = (ConnectivityManager)this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
}