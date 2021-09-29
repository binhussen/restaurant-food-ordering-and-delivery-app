package com.group_7.mhd.managerapp;

import android.Manifest;
import android.app.SearchManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.group_7.mhd.managerapp.ReadSms.SMS;
import com.group_7.mhd.managerapp.ReadSms.adapters.AllConversationAdapter;
import com.group_7.mhd.managerapp.ReadSms.adapters.ItemCLickListener;
import com.group_7.mhd.managerapp.ReadSms.constants.Constants;
import com.group_7.mhd.managerapp.ReadSms.constants.SmsContract;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;


public class Payment extends AppCompatActivity implements View.OnClickListener,
        ItemCLickListener, LoaderManager.LoaderCallbacks<Cursor>, SearchView.OnQueryTextListener {

    private RecyclerView recyclerView;
    private AllConversationAdapter allConversationAdapter;
    private String TAG = MainActivity.class.getSimpleName();
    private String mCurFilter;
    private List<SMS> data;
    private LinearLayoutManager linearLayoutManager;
    private BroadcastReceiver mReceiver;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Payments");

        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_black_24dp);
        init();

    }

    private void init() {

        recyclerView = (RecyclerView) findViewById(R.id.recyclerview);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);

        /*if (checkDefaultSettings())*/
        checkPermissions();
    }

    private void checkPermissions() {

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_SMS)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.READ_SMS)) {
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_SMS},
                        Constants.MY_PERMISSIONS_REQUEST_READ_SMS);
            }

        }


        int permissionCheck = ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_CONTACTS);
        if (permissionCheck==0)
            getSupportLoaderManager().initLoader(Constants.ALL_SMS_LOADER, null, this);




    }
    private void setRecyclerView(List<SMS> data) {
        allConversationAdapter = new AllConversationAdapter(this, data);
        allConversationAdapter.setItemClickListener(this);
        recyclerView.setAdapter(allConversationAdapter);
    }

    @Override
    public void onClick(View view) { }


    @Override
    protected void onResume() {
        super.onResume();

        IntentFilter intentFilter = new IntentFilter(
                "android.intent.action.MAIN");

        mReceiver = new BroadcastReceiver() {

            @Override
            public void onReceive(Context context, Intent intent) {

                boolean new_sms = intent.getBooleanExtra("new_sms", false);

                if (new_sms)
                    getSupportLoaderManager().restartLoader(Constants.ALL_SMS_LOADER, null, Payment.this);

            }
        };

        this.registerReceiver(mReceiver, intentFilter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.home_menu, menu);

        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.ic_search).getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setIconifiedByDefault(true);
        searchView.setOnQueryTextListener(this);

        return true;

    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case Constants.MY_PERMISSIONS_REQUEST_READ_SMS:
            {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ContextCompat.checkSelfPermission(this,
                            Manifest.permission.READ_CONTACTS)
                            != PackageManager.PERMISSION_GRANTED) {
                        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                                Manifest.permission.READ_CONTACTS)) {
                        } else {
                            ActivityCompat.requestPermissions(this,
                                    new String[]{Manifest.permission.READ_CONTACTS},
                                    Constants.MY_PERMISSIONS_REQUEST_READ_CONTACTS);
                        }

                    } else


                        getSupportLoaderManager().initLoader(Constants.ALL_SMS_LOADER, null, this);









                } else {
                    Toast.makeText(getApplicationContext(),
                            "Can't access messages.", Toast.LENGTH_LONG).show();
                    return;
                }
            }
            case Constants.MY_PERMISSIONS_REQUEST_READ_CONTACTS:
            {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    getSupportLoaderManager().initLoader(Constants.ALL_SMS_LOADER, null, this);

                } else {
                    Toast.makeText(getApplicationContext(),
                            "Can't access messages.", Toast.LENGTH_LONG).show();
                    return;
                }
            }
        }
    }


    @Override
    public void itemClicked(int color, String contact, long id,String read) {

        Intent intent = new Intent(this, SmsDetailedView.class);
        intent.putExtra(Constants.CONTACT_NAME, contact);
        intent.putExtra(Constants.COLOR, color);
        intent.putExtra(Constants.SMS_ID, id);
        intent.putExtra(Constants.READ, read);
        startActivity(intent);

    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        String selection = null;
        String[] selectionArgs = null;

        if (mCurFilter != null) {
            selection = SmsContract.SMS_SELECTION_SEARCH;
            selectionArgs = new String[]{"%" + mCurFilter + "%", "%" + mCurFilter + "%"};
        }

        return new CursorLoader(this,
                SmsContract.ALL_SMS_URI,
                null,
                selection,
                selectionArgs,
                SmsContract.SORT_DESC);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {

        progressBar.setVisibility(View.GONE);

        if (cursor != null && cursor.getCount() > 0) {

            //allConversationAdapter.swapCursor(cursor);
            getAllSmsToFile(cursor);

        } else {
            //no sms
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

        data = null;
        allConversationAdapter.notifyDataSetChanged();
        //allConversationAdapter.swapCursor(null);
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        mCurFilter = !TextUtils.isEmpty(query) ? query : null;
        getSupportLoaderManager().restartLoader(Constants.ALL_SMS_LOADER, null, this);
        return true;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        mCurFilter = !TextUtils.isEmpty(newText) ? newText : null;
        getSupportLoaderManager().restartLoader(Constants.ALL_SMS_LOADER, null, this);
        return true;
    }

    @Override
    protected void onPause() {
        super.onPause();

        this.unregisterReceiver(this.mReceiver);
        getSupportLoaderManager().destroyLoader(Constants.ALL_SMS_LOADER);
    }


    public void getAllSmsToFile(Cursor c) {

        List<SMS> lstSms = new ArrayList<SMS>();
        SMS objSMS = null;
        int totalSMS = c.getCount();
        String cbe="CBE BIRR";
        if (c.moveToFirst()) {
            for (int i = 0; i < totalSMS; i++) {
                try {

                    objSMS = new SMS();
                    objSMS.setId(c.getLong(c.getColumnIndexOrThrow("_id")));
                    String num = c.getString(c.getColumnIndexOrThrow("address"));
                    objSMS.setAddress(num);
                    objSMS.setMsg(c.getString(c.getColumnIndexOrThrow("body")));
                    objSMS.setReadState(c.getString(c.getColumnIndex("read")));
                    objSMS.setTime(c.getLong(c.getColumnIndexOrThrow("date")));
                    if (c.getString(c.getColumnIndexOrThrow("type")).contains("1")) {
                        objSMS.setFolderName("inbox");
                    } else {
                        objSMS.setFolderName("sent");
                    }



                } catch (Exception e) {

                } finally {
                    if ((c.getString(c.getColumnIndexOrThrow("address"))).equalsIgnoreCase(cbe)) {
                        lstSms.add(objSMS);
                    }
                    c.moveToNext();
                }
            }
        }
        c.close();

        data = lstSms;

        //Log.d(TAG,"Size before "+data.size());
        sortAndSetToRecycler(lstSms);

    }

    private void sortAndSetToRecycler(List<SMS> lstSms) {

        Set<SMS> s = new LinkedHashSet<>(lstSms);
        data = new ArrayList<>(s);
        setRecyclerView(data);

        convertToJson(lstSms);
    }

    private void convertToJson(List<SMS> lstSms) {

        Type listType = new TypeToken<List<SMS>>() {
        }.getType();
        Gson gson = new Gson();
        String json = gson.toJson(lstSms, listType);

        SharedPreferences sp = getSharedPreferences(Constants.PREF_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(Constants.SMS_JSON, json);
        editor.apply();

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