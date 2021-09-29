package com.group_7.mhd.managerapp.ViewHolder;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.group_7.mhd.managerapp.Interface.ItemClickListener;
import com.group_7.mhd.managerapp.R;

import info.hoang8f.widget.FButton;

public class ChafViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    public TextView chaf_name,chaf_phone;
    public ImageView btn_edit,btn_remove;
    private ItemClickListener itemClickListener;

    public ChafViewHolder(@NonNull View itemView) {
        super(itemView);

        chaf_name = (TextView)itemView.findViewById(R.id.chaf_name);
        chaf_phone = (TextView)itemView.findViewById(R.id.chaf_phone);
        btn_edit = (ImageView) itemView.findViewById(R.id.btnEdit);
        btn_remove = (ImageView) itemView.findViewById(R.id.btnRemove);

    }

    public void setItemClickListener(ItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    @Override
    public void onClick(View v) {
        itemClickListener.onClick(v,getAdapterPosition(),false);
    }
}

