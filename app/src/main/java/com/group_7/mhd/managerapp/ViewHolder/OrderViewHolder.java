package com.group_7.mhd.managerapp.ViewHolder;

import android.support.v7.widget.RecyclerView;
import android.view.ContextMenu;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.group_7.mhd.managerapp.Common.Common;
import com.group_7.mhd.managerapp.Interface.ItemClickListener;
import com.group_7.mhd.managerapp.R;
import com.rey.material.widget.CheckBox;

/**
 * Created by Guest User on 4/16/2018.
 */

public class OrderViewHolder extends RecyclerView.ViewHolder /*implements View.OnClickListener,
        View.OnLongClickListener,
        View.OnCreateContextMenuListener*/{

    public TextView txtOrderId,txtOrderStatus,txtOrderAddress,txtOrderPhone,txtOrderDate;
   // ContextMenu contextMenu;
    public ImageView btnEdit,btnRemove,btnDetail,imglogo;

    public CheckBox chkpayemnt;

    private ItemClickListener itemClickListner;
    public OrderViewHolder(View itemView) {
        super(itemView);

        txtOrderAddress=itemView.findViewById(R.id.order_address);
        txtOrderStatus=itemView.findViewById(R.id.order_status);
        txtOrderPhone=itemView.findViewById(R.id.order_phone);
        txtOrderId=itemView.findViewById(R.id.order_name);
        txtOrderDate=itemView.findViewById(R.id.order_date);

        btnEdit = (ImageView) itemView.findViewById(R.id.btnEdit);
        btnRemove = (ImageView) itemView.findViewById(R.id.btnRemove);
        btnDetail = (ImageView) itemView.findViewById(R.id.btnDetail);

        imglogo = (ImageView) itemView.findViewById(R.id.imagelogo);

        chkpayemnt = itemView.findViewById(R.id.chkpayment);

        /*itemView.setOnClickListener(this);
        itemView.setOnLongClickListener(this);
        itemView.setOnCreateContextMenuListener(this);*/
    }

 /*   public void setItemClickListner(ItemClickListener itemClickListner) {
        this.itemClickListner = itemClickListner;
    }

    @Override
    public void onClick(View view) {
        itemClickListner.onClick(view,getAdapterPosition(),false);

    }

    @Override
    public void onCreateContextMenu(ContextMenu contextMenu, View view, ContextMenu.ContextMenuInfo contextMenuInfo) {
        contextMenu.setHeaderTitle("Select The Action");

        contextMenu.add(0,0,getAdapterPosition(),Common.UPDATE);
        contextMenu.add(0,1,getAdapterPosition(),Common.DELETE);
    }

    @Override
    public boolean onLongClick(View view) {
        itemClickListner.onClick(view,getAdapterPosition(),true);
        return true;
    }*/
}
