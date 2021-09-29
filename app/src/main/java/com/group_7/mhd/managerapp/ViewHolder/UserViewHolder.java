package com.group_7.mhd.managerapp.ViewHolder;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.group_7.mhd.managerapp.Interface.ItemClickListener;
import com.group_7.mhd.managerapp.R;

import info.hoang8f.widget.FButton;

public class UserViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    public TextView user_name,user_phone, user_address,user_password,user_secure;
    public ImageView btn_delete;
    private ItemClickListener itemClickListener;

    public UserViewHolder(@NonNull View itemView) {
        super(itemView);

        user_name = (TextView)itemView.findViewById(R.id.user_name);
        user_phone = (TextView)itemView.findViewById(R.id.user_phone);
        user_password = (TextView) itemView.findViewById(R.id.user_password);
        user_secure = (TextView) itemView.findViewById(R.id.user_SecureCode);

        btn_delete = (ImageView) itemView.findViewById(R.id.btn_delete);

    }

    public void setItemClickListener(ItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    @Override
    public void onClick(View v) {
        itemClickListener.onClick(v,getAdapterPosition(),false);
    }
}

