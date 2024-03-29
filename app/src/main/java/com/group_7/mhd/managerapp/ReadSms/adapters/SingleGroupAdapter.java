package com.group_7.mhd.managerapp.ReadSms.adapters;

import android.content.Context;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.amulyakhare.textdrawable.TextDrawable;
import com.group_7.mhd.managerapp.R;
import com.group_7.mhd.managerapp.ReadSms.utils.Helpers;

public class SingleGroupAdapter extends RecyclerView.Adapter<SingleGroupAdapter.MyViewHolder> {

    private Context context;
    private Cursor dataCursor;
    private int color;

    public SingleGroupAdapter(Context context, Cursor dataCursor, int color) {

        this.context = context;
        this.dataCursor = dataCursor;
        this.color = color;

    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.single_sms_detailed, parent, false);
        SingleGroupAdapter.MyViewHolder myHolder = new SingleGroupAdapter.MyViewHolder(view);
        return myHolder;
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {

        /*dataCursor.moveToPosition(position);
        String readMessage=dataCursor.getString(dataCursor.getColumnIndexOrThrow("body"));
        String compareMessage="Dear Customer, you received";

        if (readMessage.startsWith(compareMessage)){*//*
            holder.message.setText(readMessage);*/

        dataCursor.moveToPosition(position);
            String readMessage=dataCursor.getString(dataCursor.getColumnIndexOrThrow("body"));
            holder.message.setText(readMessage);

            long time = dataCursor.getLong(dataCursor.getColumnIndexOrThrow("date"));
            holder.time.setText(Helpers.getDate(time));

            String name = dataCursor.getString(dataCursor.getColumnIndexOrThrow("address"));
            String firstChar = String.valueOf(name.charAt(0));

            TextDrawable drawable = TextDrawable.builder().buildRound(firstChar, color);
            holder.image.setImageDrawable(drawable);


    }

    public Cursor swapCursor(Cursor cursor) {
        if (dataCursor == cursor) {
            return null;
        }
        Cursor oldCursor = dataCursor;
        this.dataCursor = cursor;
        if (cursor != null) {
            this.notifyDataSetChanged();
        }
        return oldCursor;
    }

    @Override
    public int getItemCount() {
        return (dataCursor == null) ? 0 : dataCursor.getCount();
    }


    public class MyViewHolder extends RecyclerView.ViewHolder {

        private TextView message;
        private ImageView image;
        private TextView time;

        public MyViewHolder(View itemView) {
            super(itemView);

            message = (TextView) itemView.findViewById(R.id.message);
            image = (ImageView) itemView.findViewById(R.id.smsImage);
            time = (TextView) itemView.findViewById(R.id.time);

        }

    }
}
