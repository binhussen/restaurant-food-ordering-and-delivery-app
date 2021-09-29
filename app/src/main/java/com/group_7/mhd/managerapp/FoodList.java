package com.group_7.mhd.managerapp;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.group_7.mhd.managerapp.Common.Common;
import com.group_7.mhd.managerapp.Interface.ItemClickListener;
import com.group_7.mhd.managerapp.Model.Category;
import com.group_7.mhd.managerapp.Model.Food;
import com.group_7.mhd.managerapp.ViewHolder.FoodViewHolder;

import com.mancj.materialsearchbar.MaterialSearchBar;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class FoodList extends AppCompatActivity {

    RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;

    LinearLayout rootLayout;

    FloatingActionButton fab;

    //Firebase
    FirebaseDatabase db;
    DatabaseReference foodList, category;
    FirebaseStorage storage;
    StorageReference storageReference;

    String categoryId = "";

    FirebaseRecyclerAdapter<Food, FoodViewHolder> adapter;

    FirebaseRecyclerAdapter<Food, FoodViewHolder> searchadapter;
    List<String> suggestList = new ArrayList<>();
    MaterialSearchBar materialSearchBar;
    //Add new food
    EditText editName, editDescription, editPrice, editDiscount;
    Button btnUpload;
    ImageView btnSelect;

    Food newFood;

    Uri saveUri;

    TextView cat_name;
    ImageView cat_image;
    Category currentCategory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food_list);

        //Firebase
        db = FirebaseDatabase.getInstance();
        foodList = db.getReference(Common.FOOD_TABLE);
        category = db.getReference(Common.CATEGORY_TABLE);

        cat_name = findViewById(R.id.cat_name);
        cat_image = findViewById(R.id.cat_image);

        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        //Init
        recyclerView = findViewById(R.id.recycler_food);
        /*recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);*/
        recyclerView.setLayoutManager(new GridLayoutManager(this,2));

        rootLayout = findViewById(R.id.root_Layout);

        fab = findViewById(R.id.fab_foodList);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(FoodList.this, "FoodList" + this.getClass().getName(), Toast.LENGTH_SHORT).show();
                showAddFoodDialog();
            }
        });

        if (getIntent() != null) {
            categoryId = getIntent().getStringExtra("CategoryId");
        }
        if (!categoryId.isEmpty()) {
            loadListFood(categoryId);
            getCategoryDetail(categoryId);
        }

        //search
        materialSearchBar = (MaterialSearchBar)findViewById(R.id.searchBar);
        materialSearchBar.setHint("Enter your food");
        //materialSearchBar.setSpeechMode(false);

        LoadSuggest();
        materialSearchBar.setLastSuggestions(suggestList);
        // materialSearchBar =(MaterialSearchBar) findViewById(R.id.searchBar);
        materialSearchBar.setCardViewElevation(10);
        materialSearchBar.addTextChangeListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                List<String> suggest = new ArrayList<>();
                for(String search:suggestList){

                    if(search.toLowerCase().contains(materialSearchBar.getText().toLowerCase()))
                        suggest.add(search);

                }
                materialSearchBar.setLastSuggestions(suggest);

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        materialSearchBar.setOnSearchActionListener(new MaterialSearchBar.OnSearchActionListener() {
            @Override
            public void onSearchStateChanged(boolean enabled) {

                if(!enabled)
                    recyclerView.setAdapter(adapter);
            }

            @Override
            public void onSearchConfirmed(CharSequence text) {

                startSearch(text);
            }

            private void startSearch(CharSequence text) {

                searchadapter = new FirebaseRecyclerAdapter<Food, FoodViewHolder>(
                        Food.class,
                        R.layout.food_item,
                        FoodViewHolder.class,
                        foodList.orderByChild("name").equalTo(text.toString())
                ) {
                    @Override
                    protected void populateViewHolder(FoodViewHolder viewHolder, Food model, int position) {

                        viewHolder.foodName.setText(model.getName());
                        Picasso.get().load(model.getImage()).into(viewHolder.foodImage);

                        final Food local = model;
                        viewHolder.setItemClickListener(new ItemClickListener() {
                            @Override
                            public void onClick(View view, int position, boolean isLongClick) {
                                //Start new Activity
                                //Intent foodDetail = new Intent(FoodList.this, FoodDetails.class);
                                //Save food id to activity
                                // foodDetail.putExtra("FoodId", searchadapter.getRef(position).getKey());
                                // startActivity(foodDetail);
                            }
                        });
                    }
                };

                recyclerView.setAdapter(searchadapter);
            }

            @Override
            public void onButtonClicked(int buttonCode) {

            }
        });

    }

    private void LoadSuggest() {
        foodList.orderByChild("menuId").equalTo(categoryId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for(DataSnapshot postSnapshot : dataSnapshot.getChildren())
                        {
                            Food item = postSnapshot.getValue(Food.class);
                            //assert item != null;
                            suggestList.add(item.getName());
                        }
                        materialSearchBar.setLastSuggestions(suggestList);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }

    //showAddFoodDialog() method
    private void showAddFoodDialog() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(FoodList.this);
        alertDialog.setTitle(R.string.addfood);
        alertDialog.setMessage(R.string.fill);

        LayoutInflater inflater = this.getLayoutInflater();
        View add_menu_layout = inflater.inflate(R.layout.add_new_food_layout, null);

        editName = add_menu_layout.findViewById(R.id.edit_name_anf);
        editDescription = add_menu_layout.findViewById(R.id.edit_description_anf);
        editPrice = add_menu_layout.findViewById(R.id.edit_price_anf);
        editDiscount = add_menu_layout.findViewById(R.id.edit_discount_anf);

        btnSelect = add_menu_layout.findViewById(R.id.btn_select_anf);
        btnUpload = add_menu_layout.findViewById(R.id.btn_upload_anf);

        //Event for button
        btnSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chooseImage(); //Copy from HomeActivity
            }
        });

        btnUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadImage(); //Copy from HomeActivity
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
                    if (newFood != null) {
                        foodList.push().setValue(newFood);
                        Snackbar.make(rootLayout, R.string.newcate +"  "+ newFood.getName() + " "+R.string.added, Snackbar.LENGTH_SHORT).show();
                        Common.CHKIMG = "";
                        dialog.dismiss();
                    }
                }
            }

            private boolean validate() {
                boolean valid = true;

                String name = editName.getText().toString().trim();
                String disc = editDescription.getText().toString().trim();
                String pric = editPrice.getText().toString().trim();
                String discou = editDiscount.getText().toString().trim();

                double pr = 0;
                double dc = 0;
                if (!pric.isEmpty()){
                    pr= Double.parseDouble(pric);
                }
                if (!discou.isEmpty()){
                    dc= Double.parseDouble(discou);
                }

                if (name.isEmpty()||(name.length()>50||name.length()<2)){
                    editName.setError(getString(R.string.err_name));
                    valid = false;
                }
                if (disc.isEmpty()||(disc.length()>300||disc.length()<2)){
                    editDescription.setError(getString(R.string.err_dicription));
                    valid = false;
                }
                if (pric.isEmpty()){
                    editPrice.setError(getString(R.string.err_price));
                    valid = false;
                }
                if (discou.isEmpty()||dc>=pr){
                    editDiscount.setError(getString(R.string.err_dicount));
                    valid = false;
                }
                if (Common.CHKIMG.isEmpty()){
                    Toast.makeText(FoodList.this,"Please Upload Image",Toast.LENGTH_SHORT).show();
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

       /* //setButton
        alertDialog.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();

                //Here just Create new Category
                if (newFood != null) {
                    foodList.push().setValue(newFood);
                    Snackbar.make(rootLayout, R.string.newcate +"  "+ newFood.getName() + " "+R.string.added, Snackbar.LENGTH_SHORT).show();
                }
            }
        });

        alertDialog.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        alertDialog.show();*/
    }

    //chooseImage() method
    private void chooseImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, getString(R.string.selectpic)), Common.PIC_IMAGE_REQUEST);
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
                            Toast.makeText(FoodList.this, R.string.uploadsucc, Toast.LENGTH_SHORT).show();
                            imageFolder.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    //set value for newCategory if image upload and we can get download link
                                    newFood = new Food();
                                    newFood.setName(editName.getText().toString());
                                    newFood.setDescription(editDescription.getText().toString());
                                    newFood.setPrice(editPrice.getText().toString());
                                    newFood.setDiscount(editDiscount.getText().toString());
                                    newFood.setMenuId(categoryId);
                                    newFood.setImage(uri.toString());
                                    Common.CHKIMG = "Uploaded";
                                }
                            });
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            mDialog.dismiss();
                            Toast.makeText(FoodList.this, R.string.error + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            int progress = (int) (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
                            mDialog.setMessage(R.string.uploaded+": " + progress + "%");
                        }
                    });
        }
        Toast.makeText(FoodList.this, "Please Choose Image First", Toast.LENGTH_SHORT).show();
    }

    //loadListFood() method
    private void loadListFood(String categoryId) {
        adapter = new FirebaseRecyclerAdapter<Food, FoodViewHolder>(
                Food.class,
                R.layout.food_item,
                FoodViewHolder.class,
                foodList.orderByChild("menuId").equalTo(categoryId)
        ) {
            @Override
            protected void populateViewHolder(FoodViewHolder viewHolder, Food model,final int position) {
                viewHolder.foodName.setText(model.getName());
                Picasso.get().load(model.getImage()).into(viewHolder.foodImage);

                viewHolder.setItemClickListener(new ItemClickListener() {
                    @Override
                    public void onClick(View view, int position, boolean isLongClick) {
                        //Code late
                    }
                });
                viewHolder.btn_delete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        deleteFood(adapter.getRef(position).getKey());
                    }
                });
                viewHolder.btn_update.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //update food
                        showUpdateFoodDialog(adapter.getRef(position).getKey(), adapter.getItem(position));
                    }
                });
            }
        };
        adapter.notifyDataSetChanged();
        recyclerView.setAdapter(adapter);
    }

    //Press Ctrl+O

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == Common.PIC_IMAGE_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null) {

            saveUri = data.getData();


            if(saveUri!=null)
            {
                String legimage = saveUri.toString();
                byte[] im = legimage.getBytes();
                Bitmap bitmap = BitmapFactory.decodeByteArray(im,0,legimage.length());
                btnSelect.setImageBitmap(bitmap);
            }
        }
    }


    //Method for delete and update food item
    //Press Ctrl+o
    @Override
    public boolean onContextItemSelected(MenuItem item) {

        if (item.getTitle().equals(Common.UPDATE)) {
            //update food
            showUpdateFoodDialog(adapter.getRef(item.getOrder()).getKey(), adapter.getItem(item.getOrder()));
        }
        else if (item.getTitle().equals(Common.DELETE)) {
            //delete food
            deleteFood(adapter.getRef(item.getOrder()).getKey());
        }
        return super.onContextItemSelected(item);
    }

    //deleteFood() method
    private void deleteFood(final String key) {
        android.support.v7.app.AlertDialog.Builder CheckBuild = new android.support.v7.app.AlertDialog.Builder(FoodList.this);
        CheckBuild.setIcon(R.drawable.no);
        CheckBuild.setTitle(R.string.newcate);
        CheckBuild.setMessage(R.string.fooddelete);

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

                foodList.child(key).removeValue();
            }

        });
        android.support.v7.app.AlertDialog alertDialog = CheckBuild.create();
        alertDialog.show();
    }

    //showUpdateFoodDialog() method
    private void showUpdateFoodDialog(final String key, final Food item) {

        //just copy code from showAddFoodDialog() method
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(FoodList.this);
        alertDialog.setTitle(R.string.editfood);
        alertDialog.setMessage(R.string.fill);
        alertDialog.setIcon(R.drawable.ic_edit_black_24dp);

        LayoutInflater inflater = this.getLayoutInflater();
        View add_menu_layout = inflater.inflate(R.layout.add_new_food_layout, null);

        editName = add_menu_layout.findViewById(R.id.edit_name_anf);
        editDescription = add_menu_layout.findViewById(R.id.edit_description_anf);
        editPrice = add_menu_layout.findViewById(R.id.edit_price_anf);
        editDiscount = add_menu_layout.findViewById(R.id.edit_discount_anf);

        //Set default value for View
        editName.setText(item.getName());
        editDescription.setText(item.getDescription());
        editPrice.setText(item.getPrice());
        editDiscount.setText(item.getDiscount());

        btnSelect = add_menu_layout.findViewById(R.id.btn_select_anf);
        btnUpload = add_menu_layout.findViewById(R.id.btn_upload_anf);

        //Event for button
        btnSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chooseImage();
            }
        });

        btnUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeImage(item);
            }
        });

        alertDialog.setView(add_menu_layout);

        //setButton

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
                    item.setDescription(editDescription.getText().toString());
                    item.setPrice(editPrice.getText().toString());
                    item.setDiscount(editDiscount.getText().toString());

                    foodList.child(key).setValue(item);

                    Snackbar.make(rootLayout, R.string.catagory + item.getName() + R.string.editedsuccessfully, Snackbar.LENGTH_SHORT).show();
                    Common.CHKIMG = "";
                    dialog.dismiss();
                }
            }

            private boolean validate() {
                boolean valid = true;

                String name = editName.getText().toString().trim();
                String disc = editDescription.getText().toString().trim();
                String pric = editPrice.getText().toString().trim();
                String discou = editDiscount.getText().toString().trim();

                double pr = 0;
                double dc = 0;
                if (!pric.isEmpty()){
                    pr= Double.parseDouble(pric);
                }
                if (!discou.isEmpty()){
                    dc= Double.parseDouble(discou);
                }

                if (name.isEmpty()||(name.length()>50||name.length()<2)){
                    editName.setError(getString(R.string.err_name));
                    valid = false;
                }
                if (disc.isEmpty()||(disc.length()>300||disc.length()<2)){
                    editDescription.setError(getString(R.string.err_dicription));
                    valid = false;
                }
                if (pric.isEmpty()){
                    editPrice.setError(getString(R.string.err_price));
                    valid = false;
                }
                if (discou.isEmpty()||dc>=pr){
                    editDiscount.setError(getString(R.string.err_dicount));
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

        /*alertDialog.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();

            }
        });

        alertDialog.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        alertDialog.show();*/
    }


    //changeImage() method
    private void changeImage(final Food item) {
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
                            Toast.makeText(FoodList.this, R.string.uploadsucc, Toast.LENGTH_SHORT).show();
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
                            Toast.makeText(FoodList.this, R.string.error + e.getMessage(), Toast.LENGTH_SHORT).show();
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
            Toast.makeText(FoodList.this,R.string.err_image, Toast.LENGTH_SHORT).show();
    }
    // getCatagory detail() method
    private void getCategoryDetail(String categoryId) {
        category.child(categoryId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                currentCategory = dataSnapshot.getValue(Category.class);

                Picasso.get().load(currentCategory.getImage()).into(cat_image);
                cat_name.setText(currentCategory.getName());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}