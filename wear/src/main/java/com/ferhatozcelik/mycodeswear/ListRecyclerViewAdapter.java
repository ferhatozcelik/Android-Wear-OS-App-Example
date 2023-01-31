package com.ferhatozcelik.mycodeswear;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.ferhatozcelik.mycodeswear.R;

import java.util.ArrayList;
import java.util.List;

public class ListRecyclerViewAdapter extends RecyclerView.Adapter<ListRecyclerViewAdapter.ViewHolder> {

    private List<Data> mData;
    private LayoutInflater mInflater;
    private ItemClickListener mClickListener;

    private Context context;
    ListRecyclerViewAdapter(Context context, List<Data> data ) {
        this.context = context;
        this.mInflater = LayoutInflater.from(context);
        this.mData = data;
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.item_list, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final Data post = mData.get(position);

        holder.myTextView.setText(post.getTitle());

        int drawableId = context.getResources().getIdentifier(post.getImage(), "mipmap", context.getPackageName());
        holder.imageView.setImageResource(drawableId);



        holder.myTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context,CodeDetails.class);
                intent.putExtra("data",post.getData());
                intent.putExtra("title",post.getTitle());
                context.startActivity(intent);
            }
        });


    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView myTextView;
        ImageView imageView;

        ViewHolder(View itemView) {
            super(itemView);
            myTextView = itemView.findViewById(R.id.itemTextView);
            imageView = itemView.findViewById(R.id.itemImage);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (mClickListener != null) mClickListener.onItemClick(view, getAdapterPosition());
        }
    }


    void setClickListener(ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }

    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }
}