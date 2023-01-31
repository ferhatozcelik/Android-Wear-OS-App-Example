package com.ferhatozcelik.mycodeswear;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.support.wearable.activity.WearableActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.ferhatozcelik.mycodeswear.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.wearable.DataClient;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends WearableActivity implements
    DataClient.OnDataChangedListener {

    private final static String TAG = "Wear MainActivity";
    private TextView mTextView;
    private RecyclerView mRecyclerView;
    String datapath = "/data_path";
    ArrayList<Data> itemList;
    ArrayList<DataMap> itemArray ;

    private LinearLayoutManager mLayoutManager;
    private ListRecyclerViewAdapter mAdapter;
    SharedPreferences sharedPreferences;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTextView = findViewById(R.id.text);
        sharedPreferences = getSharedPreferences("Shared_Preferences", MODE_PRIVATE);
        itemList = new ArrayList<>();

        Gson gson = new Gson();

        String json = sharedPreferences.getString("dataList", null);

        Type type = new TypeToken<ArrayList<Data>>() {}.getType();
        itemList = gson.fromJson(json, type);

        if (itemList == null) {
            itemList = new ArrayList<>();
        }

        mRecyclerView = findViewById(R.id.codesList);
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mAdapter = new ListRecyclerViewAdapter(this,itemList);
        mRecyclerView.setAdapter(mAdapter);
        mAdapter.notifyDataSetChanged();

        setAmbientEnabled();
    }

    @Override
    public void onResume() {
        super.onResume();
        Wearable.getDataClient(this).addListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        Wearable.getDataClient(this).removeListener(this);
    }
    @SuppressLint({"SetTextI18n", "NotifyDataSetChanged"})
    @Override
    public void onDataChanged(@NonNull DataEventBuffer dataEventBuffer) {
        Log.d(TAG, "onDataChanged: " + dataEventBuffer);
        for (DataEvent event : dataEventBuffer) {
            if (event.getType() == DataEvent.TYPE_CHANGED) {
                String path = event.getDataItem().getUri().getPath();
                if (datapath.equals(path)) {
                    ReadDataSync(event);
                }
            } else if (event.getType() == DataEvent.TYPE_DELETED) {
                ReadDataSync(event);
            } else {
            }
        }
    }

    private void ReadDataSync(DataEvent event) {

        Toast.makeText(MainActivity.this, "Data Syncing...", Toast.LENGTH_SHORT).show();
        itemList.clear();
        DataMapItem dataMapItem = DataMapItem.fromDataItem(event.getDataItem());
        itemArray = dataMapItem.getDataMap().getDataMapArrayList("message");
        for(DataMap item : itemArray) {
            Data data = new Data();
            data.setTitle(item.getString("title"));
            data.setData(item.getString("data"));
            data.setImage(item.getString("image"));
            itemList.add(data);
        }
        Gson gson = new Gson();
        SharedPreferences.Editor editor = sharedPreferences.edit();
        String json = gson.toJson(itemList);
        editor.putString("dataList", json);
        editor.apply();
        mAdapter.notifyDataSetChanged();

    }


    private void sendData(String message) {
        PutDataMapRequest dataMap = PutDataMapRequest.create(datapath);
        dataMap.getDataMap().putString("message", message);
        PutDataRequest request = dataMap.asPutDataRequest();
        request.setUrgent();

        Task<DataItem> dataItemTask = Wearable.getDataClient(this).putDataItem(request);
        dataItemTask
            .addOnSuccessListener(new OnSuccessListener<DataItem>() {
                @Override
                public void onSuccess(DataItem dataItem) {
                    Log.d(TAG, "Sending message was successful: " + dataItem);
                }
            })
            .addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.e(TAG, "Sending message failed: " + e);
                }
            })
        ;
    }
}
