package com.group_7.mhd.managerapp;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.group_7.mhd.managerapp.Common.Common;
import com.group_7.mhd.managerapp.Interface.ItemClickListener;
import com.group_7.mhd.managerapp.Model.Comment;
import com.group_7.mhd.managerapp.ViewHolder.CommentViewHolder;
import com.group_7.mhd.managerapp.ViewHolder.OrderDetailAdapter;
import com.group_7.mhd.managerapp.ViewHolder.OrderViewHolder;
import com.jaredrummler.materialspinner.MaterialSpinner;

public class Comments extends AppCompatActivity {

    TextView phone, name, title, comment;
    String comment_id_value = "";
    RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;

    FirebaseRecyclerAdapter<Comment, CommentViewHolder> adapter;

    FirebaseDatabase db;
    DatabaseReference requests;

    MaterialSpinner spinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comments);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Comments");

        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_black_24dp);


        phone = (TextView) findViewById(R.id.cphone);
        name = (TextView) findViewById(R.id.cname);
        title = (TextView) findViewById(R.id.ctitles);
        comment = (TextView) findViewById(R.id.ccomment);

        //init firebase database
        db = FirebaseDatabase.getInstance();
        requests = db.getReference(Common.COMMENT_TABLE);

        recyclerView = (RecyclerView) findViewById(R.id.listComment);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        //if we start Orderstatus acctivity from Home
        //we will not put any extra
            loadComment();
    }

    private void loadComment() {
        adapter = new FirebaseRecyclerAdapter<Comment, CommentViewHolder>(
                Comment.class,
                R.layout.comment,
                CommentViewHolder.class,
                requests/*.orderByChild("phone")
                .equalTo(phone)*/

        ) {
            @Override
            protected void populateViewHolder(final CommentViewHolder viewHolder, final Comment model, final int position) {

                viewHolder.txtphone.setText(getString(R.string.phone)+" : "+model.getPhone());
                viewHolder.txtname.setText(getString(R.string.name)+" : "+model.getName());
                viewHolder.txttitle.setText(getString(R.string.about)+" : "+model.getCommentt());
                viewHolder.txtcomment.setText(getString(R.string.comment)+" : "+model.getCommentd());

                viewHolder.btn_delete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        DeleteComment(adapter.getRef(position).getKey());
                    }
                });
            }
            };
        adapter.notifyDataSetChanged();
        recyclerView.setAdapter(adapter);
        }

    public boolean onContextItemSelected(MenuItem item){
        if (item.getTitle().equals(Common.DELETE))
            DeleteComment(adapter.getRef(item.getOrder()).getKey());
        return super.onContextItemSelected(item);
    }
    private void DeleteComment(final String key) {

        AlertDialog.Builder CheckBuild = new AlertDialog.Builder(Comments.this);
        CheckBuild.setIcon(R.drawable.no);
        CheckBuild.setTitle(R.string.error);
        CheckBuild.setMessage(R.string.commentdelete);

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
                requests.child(key)
                        .removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(Comments.this,new StringBuilder(getString(R.string.comment)+" ")
                                .append(key)
                                .append(" "+getString(R.string.deletex)),Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(Comments.this,e.getMessage(),Toast.LENGTH_SHORT).show();
                    }
                });
            }

        });
        AlertDialog alertDialog = CheckBuild.create();
        alertDialog.show();

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


