package com.ferhatozcelik.mycodes;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

import com.ferhatozcelik.mycodes.R;

public class ListRecyclerViewAdapter extends RecyclerView.Adapter<ListRecyclerViewAdapter.ViewHolder> {

    String datapath = "/data_path";
    private List<Data> mData;
    private LayoutInflater mInflater;
    private Context context;
    private SharedPreferences sharedPreferences;
    ListRecyclerViewAdapter(Context context, List<Data> data, SharedPreferences sharedPreferences) {
        this.context = context;
        this.mInflater = LayoutInflater.from(context);
        this.mData = data;
        this.sharedPreferences = sharedPreferences;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.item_list, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        final Data post = mData.get(position);

        holder.myTextView.setText(post.getTitle());

        int drawableId = context.getResources().getIdentifier(post.getImage(), "drawable", context.getPackageName());
        holder.imageView.setImageResource(drawableId);


        holder.btn_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (v.getId() == holder.btn_delete.getId()) {

                    final AlertDialog dialogBuilder = new AlertDialog.Builder(context).create();
                    LayoutInflater inflater = LayoutInflater.from(context);
                    View dialogView = inflater.inflate(R.layout.alert_item_delete_dialog, null);

                    final TextView input_edittitle = dialogView.findViewById(R.id.itemDeleteTitle);
                    input_edittitle.setText( "\"" + holder.myTextView.getText() + "\" Delete From List?");
                    Button btn_delete = dialogView.findViewById(R.id.btn_delete);
                    Button btn_dis = dialogView.findViewById(R.id.btn_dis);

                    btn_delete.setOnClickListener(new View.OnClickListener() {
                        @SuppressLint("NotifyDataSetChanged")
                        @Override
                        public void onClick(View v) {
                            mData.remove(holder.getPosition());
                            Toast.makeText(context, holder.myTextView.getText() + " Has Been Removed From List", Toast.LENGTH_SHORT).show();
                            notifyDataSetChanged();
                            dialogBuilder.dismiss();
                            saveData(mData);
                        }
                    });

                    btn_dis.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            dialogBuilder.dismiss();
                        }
                    });
                    dialogBuilder.setView(dialogView);
                    dialogBuilder.show();
                }
            }
        });

    }

    @SuppressLint("NotifyDataSetChanged")
    private void saveData(List<Data> dataList) {
        Gson gson = new Gson();
        SharedPreferences.Editor editor = sharedPreferences.edit();
        String json = gson.toJson(dataList);
        editor.putString("dataList", json);
        editor.apply();
        sendData(dataList);
        notifyDataSetChanged();
    }


    private void sendData(List<Data> dataList) {
        PutDataMapRequest dataMap = PutDataMapRequest.create(datapath);
        ArrayList<DataMap> itemArray = new ArrayList<>();
        for(Data item : dataList) {
            DataMap dataMapp = new DataMap();
            dataMapp.putString("title", item.getTitle());
            dataMapp.putString("data", item.getData());
            dataMapp.putString("image", item.getImage());
            itemArray.add(dataMapp);
        }

        dataMap.getDataMap().putDataMapArrayList("message", itemArray);
        PutDataRequest request = dataMap.asPutDataRequest();
        request.setUrgent();

        Task<DataItem> dataItemTask = Wearable.getDataClient(context).putDataItem(request);
        dataItemTask
                .addOnSuccessListener(new OnSuccessListener<DataItem>() {
                    @Override
                    public void onSuccess(DataItem dataItem) {
                        Log.d("TAG", "Sending message was successful: " + dataItem);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e("TAG", "Sending message failed: " + e);
                    }
                })
        ;
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder  {
        TextView myTextView;
        ImageView imageView;
        Button btn_delete;


        ViewHolder(View itemView) {
            super(itemView);
            myTextView = itemView.findViewById(R.id.itemTextView);
            imageView = itemView.findViewById(R.id.itemImage);
            btn_delete = itemView.findViewById(R.id.btn_delete);

        }
    }

}