package com.group_7.mhd.managerapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.group_7.mhd.managerapp.Common.Common;
import com.group_7.mhd.managerapp.Interface.ItemClickListener;
import com.group_7.mhd.managerapp.Model.Category;
import com.group_7.mhd.managerapp.Model.Token;
import com.group_7.mhd.managerapp.ViewHolder.MenuViewHolder;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;

import io.paperdb.Paper;

public class Home extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private static final Pattern PASSWORD_PATTERN =
            Pattern.compile("^"+
                    "(?=.*[a-z])" +     //at least one lowercase
                    "(?=.*[A-Z])" +     //at least one upercase
                    "(?=.*[0-9])" +     //at least one digit
                    "(?=.*[@#$%^&+=])" +    //at least one special character
                    "(?=\\S+$)" +          //no white space
                    ".{6,}" +               //at least six digit
                    "$");
    TextView txtFullName,textphone;

    //Firebase
    FirebaseDatabase database;
    DatabaseReference categories;

    //FirebaseStorage
    FirebaseStorage storage;
    StorageReference storageReference;

    FirebaseRecyclerAdapter<Category, MenuViewHolder> adapter;

    //View
    RecyclerView recycler_menu;
    RecyclerView.LayoutManager layoutManager;

    //Add New Menu Layout
    EditText editName;
    Button btnUpload;
    ImageView btnSelect;

    Category newCategory;

    Uri saveUri;

    DrawerLayout drawer;

    SwipeRefreshLayout swipeRefreshLayout;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Manager");
        setSupportActionBar(toolbar);

        //int paper
        Paper.init(this);

        //Init Firebase
        database = FirebaseDatabase.getInstance();
        categories = database.getReference(Common.CATEGORY_TABLE);

        //Init FirebaseStorage
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();


        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDialog();
            }
        });

        //swipeRefreshLayout = findViewById(R.id.swipeHome);
        drawer =(DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView =(NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        //Set Nme for User
        View headerView = navigationView.getHeaderView(0);
        txtFullName = headerView.findViewById(R.id.text_fullName);
        txtFullName.setText(/*Common.currentUser.getName()*/"Manager");

        textphone = headerView.findViewById(R.id.text_phone);
        textphone.setText(/*Common.currentUser.getPhone()*/"");

        //Init View
        recycler_menu = findViewById(R.id.recycler_menu);
        /*recycler_menu.setHasFixedSize(true);
        *//*layoutManager = new LinearLayoutManager(this);
        recycler_menu.setLayoutManager(layoutManager);*//*
        recycler_menu.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));*/

        recycler_menu.setLayoutManager(new GridLayoutManager(this,2));


        loadMenu();
        /*swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
               // adapter.stopListening();
                loadMenu();
                adapter.startListening();
                swipeRefreshLayout.setRefreshing(false);
            }
        });*/

       //send token
        updateToken(FirebaseInstanceId.getInstance().getToken());
    }

    private void updateToken(String token) {

        FirebaseDatabase db = FirebaseDatabase.getInstance();
        DatabaseReference tokens = db.getReference(Common.TOKEN_TABLE);
        Token data = new Token(token,true);
        tokens.child(Common.currentUser).setValue(data);
    }

    //showDialog() method
    private void showDialog() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(Home.this);
        alertDialog.setTitle(R.string.newcate);
        alertDialog.setMessage(R.string.fill);

        LayoutInflater inflater = this.getLayoutInflater();
        View add_menu_layout = inflater.inflate(R.layout.add_new_menu_layout, null);

        editName = add_menu_layout.findViewById(R.id.edit_name);

        btnSelect = add_menu_layout.findViewById(R.id.btn_select);
        btnUpload = add_menu_layout.findViewById(R.id.btn_upload);

        //Event for button
        btnSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chooseImage(); //Let user select image from gallery and save Uri of this image
            }
        });

        btnUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadImage();
            }
        });

        alertDialog.setView(add_menu_layout);
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
                    //Here just Create new Category
                    if (newCategory != null) {
                        categories.push().setValue(newCategory);
                        Snackbar.make(drawer,  R.string.newcate + newCategory.getName() + R.string.added, Snackbar.LENGTH_SHORT).show();
                        Common.CHKIMG = "";
                        dialog.dismiss();
                    }
                }
            }

            private boolean validate() {
                boolean valid = true;

                String name = editName.getText().toString().trim();

                if (name.isEmpty()||(name.length()>50||name.length()<2)){
                    editName.setError(getString(R.string.err_name));
                    valid = false;
                }
                if (Common.CHKIMG.isEmpty()){
                    Toast.makeText(Home.this,"Please Upload Image",Toast.LENGTH_SHORT).show();
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

    //uploadImage() method
    private void uploadImage() {
        if (saveUri != null) {
            final ProgressDialog mDialog = new ProgressDialog(this);
            mDialog.setMessage(getString(R.string.upload));
            mDialog.show();

            //create random string
            String imageName = UUID.randomUUID().toString();
            final StorageReference imageFolder = storageReference.child("images/" + imageName);
            imageFolder.putFile(saveUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            mDialog.dismiss();
                            Toast.makeText(Home.this, R.string.uploadsucc, Toast.LENGTH_SHORT).show();
                            imageFolder.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    //set value for newCategory if image upload and we can get download link
                                    newCategory = new Category(editName.getText().toString(), uri.toString());

                                    Common.CHKIMG = "Uploaded";
                                }
                            });
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            mDialog.dismiss();
                            Toast.makeText(Home.this, R.string.error + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            int progress = (int) (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
                            mDialog.setMessage(R.string.uploaded + progress + "%");
                        }
                    });
        }else
            Toast.makeText(Home.this,R.string.err_image, Toast.LENGTH_SHORT).show();
    }

    //Press Ctrl+O
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Common.PIC_IMAGE_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null) {

            saveUri = data.getData();/*
            btnSelect.setText(R.string.imgselected);*/
        }
    }

    //chooseImage() method
    private void chooseImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, getString(R.string.selectpic)), Common.PIC_IMAGE_REQUEST);
    }

    //loadMenu() method
    private void loadMenu() {

        adapter = new FirebaseRecyclerAdapter<Category, MenuViewHolder>(
                Category.class,
                R.layout.menu_item,
                MenuViewHolder.class,
                categories
        ) {
            @Override
            protected void populateViewHolder(MenuViewHolder viewHolder, Category model,final int position) {
                viewHolder.txtMenuName.setText(model.getName());
                Picasso.get().load(model.getImage()).into(viewHolder.imageView);

                //ClickListener for MenuItem
                viewHolder.setItemClickListener(new ItemClickListener() {
                    @Override
                    public void onClick(View view, int position, boolean isLongClick) {
                        //Send Category ID and Start new Activity
                        Intent foodList = new Intent(Home.this, FoodList.class);
                        foodList.putExtra("CategoryId", adapter.getRef(position).getKey());
                        startActivity(foodList);
                    }
                });
                viewHolder.btn_delete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        deleteCategory(adapter.getRef(position).getKey());
                    }
                });
                viewHolder.btn_update.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //update Category
                        showUpdateDialog(adapter.getRef(position).getKey(), adapter.getItem(position));
                    }
                });
            }
        };

        //Refresh Data if data have changed in database
        adapter.notifyDataSetChanged();
        recycler_menu.setAdapter(adapter);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_order) {
            Intent orders=new Intent(this,OrderStatus.class);
            startActivity(orders);
        }
        else if (id == R.id.nav_signout) {
            //Delete Rmwmber user password
            Paper.book().destroy();
            //Logout
            FirebaseAuth.getInstance().signOut();
            finish();
            Intent signIn = new Intent(Home.this, SignIn.class);
            signIn.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(signIn);
        }
        else if (id == R.id.nav_comment){
            //int paper
            Intent comment=new Intent(this,Comments.class);
             startActivity(comment);
        }
        else if (id == R.id.nav_shipper){
            Intent driver=new Intent(this,ShipperManagement.class);
            startActivity(driver);
        }
        else if (id == R.id.nav_sheaf){
            Intent sheaf=new Intent(this,SeafManagement.class);
            startActivity(sheaf);
        }
        else if (id == R.id.nav_language){
            showChangeLanguageDialog();
        }
        else if (id == R.id.nav_payment){
            Intent payment=new Intent(this,Payment.class);
            startActivity(payment);
        }
        else if (id == R.id.nav_user){
            Intent user=new Intent(this,User_Management.class);
            startActivity(user);
        }else if (id == R.id.nav_change_password){

            showChangePasswordDialog();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


    //Update|Delete
    //Pres Ctrl+O
    @Override
    public boolean onContextItemSelected(MenuItem item) {

        if (item.getTitle().equals(Common.UPDATE)) {
            showUpdateDialog(adapter.getRef(item.getOrder()).getKey(), adapter.getItem(item.getOrder()));
        } else if (item.getTitle().equals(Common.DELETE)) {
            deleteCategory(adapter.getRef(item.getOrder()).getKey());
        }

        return super.onContextItemSelected(item);
    }

    //deleteCategory() method
    private void deleteCategory(final String key) {
        android.support.v7.app.AlertDialog.Builder CheckBuild = new android.support.v7.app.AlertDialog.Builder(Home.this);
        CheckBuild.setIcon(R.drawable.no);
        CheckBuild.setTitle(R.string.error);
        CheckBuild.setMessage(R.string.catagorydelete);

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
                categories.child(key).removeValue();
            }

        });
        android.support.v7.app.AlertDialog alertDialog = CheckBuild.create();
        alertDialog.show();


    }


    //showUpdateDialog() method
    private void showUpdateDialog(final String key, final Category item) {

        //Just copy & past showDialog() and modify
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(Home.this);
        alertDialog.setTitle(R.string.editcate);
        alertDialog.setMessage(R.string.fill);
        alertDialog.setIcon(R.drawable.ic_edit_black_24dp);

        LayoutInflater inflater = this.getLayoutInflater();
        View add_menu_layout = inflater.inflate(R.layout.add_new_menu_layout, null);

        editName = add_menu_layout.findViewById(R.id.edit_name);

        btnSelect = add_menu_layout.findViewById(R.id.btn_select);
        btnUpload = add_menu_layout.findViewById(R.id.btn_upload);

        //set default name
        editName.setText(item.getName());

        //Event for button
        btnSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chooseImage(); //Let user select image from gallery and save Uri of this image
            }
        });

        btnUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeImage(item);
            }
        });

        alertDialog.setView(add_menu_layout);

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
                    //Update information
                    item.setName(editName.getText().toString());
                    categories.child(key).setValue(item);
                    Common.CHKIMG = "";
                    dialog.dismiss();
                }
            }

            private boolean validate() {
                boolean valid = true;

                String name = editName.getText().toString().trim();

                if (name.isEmpty()||(name.length()>50||name.length()<2)){
                    editName.setError(getString(R.string.err_name));
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

    //uploadImage() method
    private void changeImage(final Category item) {
        if (saveUri != null) {
            final ProgressDialog mDialog = new ProgressDialog(this);
            mDialog.setMessage(getString(R.string.upload));
            mDialog.show();

            //create random string
            String imageName = UUID.randomUUID().toString();
            final StorageReference imageFolder = storageReference.child("images/" + imageName);
            imageFolder.putFile(saveUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            mDialog.dismiss();
                            Toast.makeText(Home.this, R.string.uploadsucc, Toast.LENGTH_SHORT).show();
                            imageFolder.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    //set value for newCategory if image upload and we can get download link
                                    item.setImage(uri.toString());
                                    Common.CHKIMG = "Uploaded";
                                }
                            });
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            mDialog.dismiss();
                            Toast.makeText(Home.this, R.string.error + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            int progress = (int) (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
                            mDialog.setMessage(R.string.uploaded + progress + "%");
                        }
                    });
        }else
            Toast.makeText(Home.this,R.string.err_image, Toast.LENGTH_SHORT).show();
    }

    private void showChangePasswordDialog() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setTitle(R.string.changep);
        alertDialog.setMessage(R.string.fill);


        alertDialog.setIcon(R.drawable.ic_security_black_24dp);

        LayoutInflater inflater = LayoutInflater.from(this);
        View layout_pwd = inflater.inflate(R.layout.change_password_layout,null);

        final MaterialEditText edtPassword = (MaterialEditText) layout_pwd.findViewById(R.id.edtPassword);
        final MaterialEditText edtNewPassword = (MaterialEditText) layout_pwd.findViewById(R.id.edtNewPassword);
        final MaterialEditText edtRepeatPassword = (MaterialEditText) layout_pwd.findViewById(R.id.edtRepeatPassword);

        alertDialog.setView(layout_pwd);

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
                    //change password here

                    //check old password
                    if (edtPassword.getText().toString().equals(Common.current_password))
                    {
                        //check old password
                        FirebaseUser userm = FirebaseAuth.getInstance().getCurrentUser();
                        if (userm!=null)
                        {
                            //check new password and repeat password
                            if (edtNewPassword.getText().toString().equals(edtRepeatPassword.getText().toString()))
                            {
                                Paper.book().write(Common.PWD_KEY,edtNewPassword.getText().toString());

                                userm.updatePassword(edtNewPassword.getText().toString())
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()){
                                                    dialog.dismiss();
                                                    Toast.makeText(Home.this,R.string.passu,Toast.LENGTH_SHORT).show();
                                                }else {
                                                    Toast.makeText(Home.this,task.getException().getMessage(),Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });
                            }
                            else
                            {
                                Toast.makeText(Home.this,R.string.newpassd,Toast.LENGTH_SHORT).show();
                            }
                        }
                        else
                        {
                            Toast.makeText(Home.this,R.string.wropass,Toast.LENGTH_SHORT).show();
                        }
                    }else
                    {
                        Toast.makeText(Home.this,R.string.wropass,Toast.LENGTH_SHORT).show();
                    }

                }
            }

            private boolean validate() {
                boolean valid = true;

                String oldpass = edtPassword.getText().toString().trim();
                String newpass = edtNewPassword.getText().toString().trim();
                String reppass = edtRepeatPassword.getText().toString().trim();

                if (!PASSWORD_PATTERN.matcher(oldpass).matches()){
                    edtPassword.setError(getString(R.string.wrongpass));
                    valid = false;
                }if (!PASSWORD_PATTERN.matcher(newpass).matches()){
                    edtNewPassword.setError(getString(R.string.err_password));
                    valid = false;
                }if (!reppass.equals(newpass)){
                    edtRepeatPassword.setError(getString(R.string.err_password_confirmation));
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

    //langs
    private void showChangeLanguageDialog() {
        final String[] listItems = {"English","አማርኛ"};
        android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(Home.this);
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
