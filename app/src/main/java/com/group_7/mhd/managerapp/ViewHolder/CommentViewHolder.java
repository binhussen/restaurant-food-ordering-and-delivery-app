package com.group_7.mhd.managerapp.ViewHolder;

import android.support.v7.widget.RecyclerView;
import android.view.ContextMenu;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.group_7.mhd.managerapp.Common.Common;
import com.group_7.mhd.managerapp.Interface.ItemClickListener;
import com.group_7.mhd.managerapp.R;

public class CommentViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener,
        View.OnCreateContextMenuListener{

    public TextView txtphone, txtname, txttitle, txtcomment;

    private ItemClickListener itemClickListener;
    public ImageView btn_delete;
    public void setItemClickListener(ItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    public CommentViewHolder(View itemView) {
        super(itemView);

        txtphone = itemView.findViewById(R.id.cphone);
        txtname = itemView.findViewById(R.id.cname);
        txttitle = itemView.findViewById(R.id.ctitles);
        txtcomment = itemView.findViewById(R.id.ccomment);

        btn_delete = itemView.findViewById(R.id.btn_delete);

        itemView.setOnClickListener(this);
        itemView.setOnCreateContextMenuListener(this);
    }

    @Override
    public void onClick(View v) {
        //itemClickListener.onClick(v, getAdapterPosition(), false);
    }

    @Override
    public void onCreateContextMenu(ContextMenu contextMenu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        contextMenu.setHeaderTitle("Select The Action");

        contextMenu.add(0,1,getAdapterPosition(),Common.DELETE);
    }
}
