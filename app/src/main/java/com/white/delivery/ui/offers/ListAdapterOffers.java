package com.white.delivery.ui.offers;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.white.delivery.R;

public class ListAdapterOffers extends RecyclerView.Adapter<ListAdapterOffers.ViewHolder>{
    private ListDataOffers[] listdata;

    // RecyclerView recyclerView;
    public ListAdapterOffers(ListDataOffers[] listdata) {
        this.listdata = listdata;
    }

    @NonNull
    @Override
    public ListAdapterOffers.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View listItem= layoutInflater.inflate(R.layout.list_item_offers, parent, false);
        ListAdapterOffers.ViewHolder viewHolder = new ListAdapterOffers.ViewHolder(listItem);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ListAdapterOffers.ViewHolder holder, int position) {
        final ListDataOffers myListData = listdata[position];
        holder.textView1.setText(listdata[position].getHeading());
        holder.textView2.setText(listdata[position].getSubHeading());
        holder.relativeLayout.setBackgroundResource((R.drawable.ic_launcher_background));
        holder.relativeLayout.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                Toast.makeText(view.getContext(),"click on item: "+myListData.getHeading(),Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return listdata.length;
    }
    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView textView1,textView2;
        public RelativeLayout relativeLayout;
        public ViewHolder(View itemView) {
            super(itemView);
            this.textView1 = itemView.findViewById(R.id.heading);
            this.textView2 = itemView.findViewById(R.id.subHeading);
            relativeLayout = (RelativeLayout) itemView.findViewById(R.id.relativelay);
        }
    }
}
