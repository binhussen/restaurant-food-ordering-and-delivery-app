package com.group_7.mhd.managerapp;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskExecutors;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthSettings;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.group_7.mhd.managerapp.Common.Common;
import com.group_7.mhd.managerapp.Model.User;
import com.rengwuxian.materialedittext.MaterialEditText;

import java.util.Locale;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import io.paperdb.Paper;

public class SignIn extends AppCompatActivity {

    private static final Pattern PASSWORD_PATTERN =
            Pattern.compile("^"+
                    "(?=.*[a-z])" +     //at least one lowercase
                    "(?=.*[A-Z])" +     //at least one upercase
                    "(?=.*[0-9])" +     //at least one digit
                    "(?=.*[@#$%^&+=])" +    //at least one special character
                    "(?=\\S+$)" +          //no white space
                    ".{6,}" +               //at least six digit
                    "$");

    EditText editPhone, editPassword;
    Button buttonSignIn;

    FirebaseDatabase db;
    DatabaseReference users;

    //forget
    FirebaseAuth firebaseAuth;
    TextView txtForgetPwd, txtlang;
    FirebaseAuthSettings firebaseAuthSettings;
    private String mVerificationId;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        editPhone = findViewById(R.id.edit_Phone);
        editPassword = findViewById(R.id.edit_Password);
        buttonSignIn = findViewById(R.id.button_signin);
        txtlang = (TextView) findViewById(R.id.txtLanguage);
        txtlang.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showChangeLanguageDialog();
            }
        });

        //int paper
        Paper.init(this);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseAuthSettings = firebaseAuth.getFirebaseAuthSettings();

        //forget
        txtForgetPwd = (TextView) findViewById(R.id.txtForgetPwd);
        txtForgetPwd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showForgetPwdDialog();
            }
        });

        //Init Firebase
        db = FirebaseDatabase.getInstance();
        users = db.getReference(Common.USER_TABLE);

        buttonSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validate()){
                    signInUser(editPhone.getText().toString(), editPassword.getText().toString());
                }
            }
        });
    }

    private boolean validate() {

            boolean valid = true;
            String phone_validate = editPhone.getText().toString().trim();
            String password_validate = editPassword.getText().toString().trim();

        if (phone_validate.isEmpty()||!Patterns.EMAIL_ADDRESS.matcher(phone_validate).matches()){
            editPhone.setError(getString(R.string.err_email));
            valid = false;
        }
            if (password_validate.isEmpty()||!PASSWORD_PATTERN.matcher(password_validate).matches()){
                editPassword.setError(getString(R.string.err_password));
                valid = false;
            }

            return valid;

    }

    private void showForgetPwdDialog() {
        firebaseAuth.sendPasswordResetEmail(editPhone.getText().toString())
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){
                            Toast.makeText(SignIn.this,R.string.passsent,Toast.LENGTH_SHORT).show();
                        }else {
                            Toast.makeText(SignIn.this,task.getException().getMessage(),Toast.LENGTH_SHORT).show();
                        }
                    }
                });
        }

    //signInUser() method
    private void signInUser(final String phone, final String password) {
        final ProgressDialog mDialog = new ProgressDialog(SignIn.this);
        mDialog.setMessage(getString(R.string.please_wait));
        mDialog.show();

        //phone and password from user editText field
        final String localPhone = phone;
        final String localPassword = password;
        Common.current_password = password;

        firebaseAuth.signInWithEmailAndPassword(phone,password)
                .addOnCompleteListener(SignIn.this, new OnCompleteListener<AuthResult>() {
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
                                            user.setPhone("923692424");
                                                    */
                                            Intent intentHome = new Intent(SignIn.this, Home.class);
                                            /*        Common.currentUser = user;
                                                    */
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
                            Toast.makeText(SignIn.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });

    }

    //langs
    private void showChangeLanguageDialog() {
        final String[] listItems = {"English","አማርኛ"};
        android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(SignIn.this);
        builder.setTitle(R.string.choosel);
        builder.setSingleChoiceItems(listItems, -1, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if(i==0){
                    setLocale("en");
                    recreate();
                }
                else if(i==1){
                    setLocale("am");
                    recreate();
                }
                dialogInterface.dismiss();
            }
        });
        android.support.v7.app.AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }
    private void setLocale(String langs) {
        Locale locale = new Locale(langs);
        locale.setDefault(locale);
        Configuration config = new Configuration();
        config.locale = locale;
        getBaseContext().getResources().updateConfiguration(config,getBaseContext().getResources().getDisplayMetrics());

        SharedPreferences.Editor editor = getSharedPreferences("Settings",MODE_PRIVATE).edit();
        editor.putString("My_Lang",langs);
        editor.apply();
    }
    public void loadLocale(){
        SharedPreferences prefs = getSharedPreferences("Settings", Activity.MODE_PRIVATE);
        String language = prefs.getString("My_Lang","");
        setLocale(language);
    }
}
