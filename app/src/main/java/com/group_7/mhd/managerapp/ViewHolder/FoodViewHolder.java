package com.group_7.mhd.managerapp.ViewHolder;

import android.support.v7.widget.RecyclerView;
import android.view.ContextMenu;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.group_7.mhd.managerapp.Common.Common;
import com.group_7.mhd.managerapp.Interface.ItemClickListener;
import com.group_7.mhd.managerapp.R;

public class FoodViewHolder extends RecyclerView.ViewHolder implements
        View.OnClickListener,
        View.OnCreateContextMenuListener //add for update/delete image
{

    public TextView foodName;
    public ImageView foodImage;
    public ImageView btn_delete,btn_update;

    private ItemClickListener itemClickListener;

    public FoodViewHolder(View itemView) {
        super(itemView);

        foodName = itemView.findViewById(R.id.food_name);
        foodImage = itemView.findViewById(R.id.food_image);

        btn_delete = itemView.findViewById(R.id.btn_delete);
        btn_update = itemView.findViewById(R.id.btn_edit);

        itemView.setOnCreateContextMenuListener(this);
        itemView.setOnClickListener(this);


    }

    public void setItemClickListener(ItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    @Override
    public void onClick(View v) {
        itemClickListener.onClick(v, getAdapterPosition(), false);
    }

    //add for update/delete image
    //Override method for View.OnCreateContextMenuListener
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        menu.setHeaderIcon(R.drawable.firebase);
        menu.setHeaderTitle("Select an Action");

        menu.add(0, 0, getAdapterPosition(), Common.UPDATE);
        menu.add(0, 1, getAdapterPosition(), Common.DELETE);
    }
}
