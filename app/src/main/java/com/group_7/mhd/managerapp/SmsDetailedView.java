package com.group_7.mhd.managerapp;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.view.View;

import com.group_7.mhd.managerapp.ReadSms.adapters.SingleGroupAdapter;
import com.group_7.mhd.managerapp.ReadSms.constants.Constants;
import com.group_7.mhd.managerapp.ReadSms.constants.SmsContract;
import com.group_7.mhd.managerapp.ReadSms.services.UpdateSMSService;

public class SmsDetailedView extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor>, View.OnClickListener {

    private String contact;
    private SingleGroupAdapter singleGroupAdapter;
    private RecyclerView recyclerView;
    /*private EditText etMessage;
    private ImageView btSend;*/
    private String message;
    private boolean from_reciever;
    private long _Id;
    private int color;
    private String read = "1";

    private Cursor dataCursor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sms_detailed_view);
        /*getSupportActionBar().setDisplayHomeAsUpEnabled(true);*/
        if(getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            /*getSupportActionBar().setHomeButtonEnabled(true);*/
        }
        /*getSupportActionBar().setTitle(mTitles);*/
        /*getSupportActionBar().setDisplayHomeAsUpEnabled(true);*/

        init();
    }


    private void init() {

        Intent intent = getIntent();

        contact = intent.getStringExtra(Constants.CONTACT_NAME);
        _Id = intent.getLongExtra(Constants.SMS_ID,-123);
        color = intent.getIntExtra(Constants.COLOR,0);
        read = intent.getStringExtra(Constants.READ);

        from_reciever = intent.getBooleanExtra(Constants.FROM_SMS_RECIEVER, false);

        if (getSupportActionBar() != null)
            getSupportActionBar().setTitle(contact);

        recyclerView = (RecyclerView) findViewById(R.id.recyclerview);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        recyclerView.setLayoutManager(linearLayoutManager);

        setRecyclerView(null);

        if (read!=null && read.equals("0"))
            setReadSMS();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == android.R.id.home) // Press Back Icon
        {
            onBackPressed();
        }

        return super.onOptionsItemSelected(item);
    }


    private void setRecyclerView(Cursor cursor) {
        /*dataCursor.moveToPosition(position);
        String readMessage = dataCursor.getString(dataCursor.getColumnIndexOrThrow("body"));
        String compareMessage = "Dear Customer,you received";

        if (readMessage.startsWith(compareMessage)) {
            singleGroupAdapter = new SingleGroupAdapter(this, cursor, color);
            recyclerView.setAdapter(singleGroupAdapter);
        }*/
        singleGroupAdapter = new SingleGroupAdapter(this, cursor, color);
        recyclerView.setAdapter(singleGroupAdapter);

    }

    @Override
    protected void onResume() {
        super.onResume();
        getSupportLoaderManager().initLoader(Constants.CONVERSATION_LOADER, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        String[] selectionArgs = {contact};

        return new CursorLoader(this,
                SmsContract.ALL_SMS_URI,
                null,
                SmsContract.SMS_SELECTION,
                selectionArgs,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {

        if (cursor != null && cursor.getCount() > 0) {
            singleGroupAdapter.swapCursor(cursor);
        } else {
            //no sms
        }

    }


    private void setReadSMS() {

        Intent intent = new Intent(this, UpdateSMSService.class);
        intent.putExtra("id", _Id);
        startService(intent);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        singleGroupAdapter.swapCursor(null);
    }

    @Override
    public void onClick(View view) {

}

}
