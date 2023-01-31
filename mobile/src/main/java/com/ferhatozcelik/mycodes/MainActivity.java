package com.ferhatozcelik.mycodes;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

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
import com.google.zxing.BinaryBitmap;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.NotFoundException;
import com.google.zxing.RGBLuminanceSource;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import com.ferhatozcelik.mycodes.R;
import in.goodiebag.carouselpicker.CarouselPicker;



public class MainActivity extends AppCompatActivity implements DataClient.OnDataChangedListener {

    String TAG = "Mobile_MainActivity";
    String datapath = "/data_path";
    Button sendbtn;
    Button addManual;
    Button addTakeCamera;
    Button addGalleryImage;
    ArrayList<Data> itemListMobile;
    Context context = MainActivity.this;


    private RecyclerView mRecyclerView;
    private LinearLayoutManager mLayoutManager;
    private ListRecyclerViewAdapter mAdapter;
    SharedPreferences sharedPreferences;
    private AlertDialog dialogBuilder;

    ArrayList<DataMap> itemArray ;
    private static final int CAMERA_PERMISSION_CODE = 100;
    private static final int STORAGE_PERMISSION_CODE = 101;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sendbtn = findViewById(R.id.sendbtn);
        addManual = findViewById(R.id.addManual);
        addTakeCamera = findViewById(R.id.addTakeCamera);
        addGalleryImage = findViewById(R.id.addGalleryImage);
        mRecyclerView = findViewById(R.id.mRecyclerView);

        itemListMobile = new ArrayList<>();

        sharedPreferences = getSharedPreferences("Shared_Preferences", MODE_PRIVATE);
        Gson gson = new Gson();
        String json = sharedPreferences.getString("dataList", null);

        Type type = new TypeToken<ArrayList<Data>>() {}.getType();
        itemListMobile = gson.fromJson(json, type);

        if (itemListMobile == null) {
            itemListMobile = new ArrayList<>();
        }

        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mAdapter = new ListRecyclerViewAdapter(this,itemListMobile,sharedPreferences);
        mRecyclerView.setAdapter(mAdapter);

        addManual.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                OpenAddManual(null);
            }
        });
        sendbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendData(itemListMobile);
            }
        });


        checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, STORAGE_PERMISSION_CODE);


        checkPermission(Manifest.permission.CAMERA, CAMERA_PERMISSION_CODE);

        addGalleryImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent pickIntent = new Intent(Intent.ACTION_PICK);
                pickIntent.setDataAndType( android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");

                startActivityForResult(pickIntent, 111);

            }
        });
        addTakeCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                IntentIntegrator intentIntegrator = new IntentIntegrator(MainActivity.this);
                intentIntegrator.setPrompt("Scan a Barcode or QR Code");
                intentIntegrator.setOrientationLocked(true);
                intentIntegrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE);
                intentIntegrator.initiateScan();

            }
        });

    }

    public void checkPermission(String permission, int requestCode)
    {
        if (ContextCompat.checkSelfPermission(MainActivity.this, permission) == PackageManager.PERMISSION_DENIED) {

            // Requesting the permission
            ActivityCompat.requestPermissions(MainActivity.this, new String[] { permission }, requestCode);
        }
        else {
            Toast.makeText(MainActivity.this, "Permission already granted", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        IntentResult intentResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);

        if (intentResult != null) {
            if (intentResult.getContents() == null) {
                Toast.makeText(getBaseContext(), "Cancelled", Toast.LENGTH_SHORT).show();
            } else {

                //Toast.makeText(getBaseContext(), intentResult.getContents(), Toast.LENGTH_SHORT).show();
                OpenAddManual(intentResult.getContents());
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }

        switch (requestCode) {
            case 111:
                if(data == null || data.getData()==null) {
                    Log.e("TAG", "The uri is null, probably the user cancelled the image selection process using the back button.");
                    return;
                }
                Uri uri = data.getData();
                try
                {
                    InputStream inputStream = getContentResolver().openInputStream(uri);
                    Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                    if (bitmap == null)
                    {
                        Log.e("TAG", "uri is not a bitmap," + uri.toString());
                        return;
                    }
                    int width = bitmap.getWidth(), height = bitmap.getHeight();
                    int[] pixels = new int[width * height];
                    bitmap.getPixels(pixels, 0, width, 0, 0, width, height);
                    bitmap.recycle();
                    bitmap = null;
                    RGBLuminanceSource source = new RGBLuminanceSource(width, height, pixels);
                    BinaryBitmap bBitmap = new BinaryBitmap(new HybridBinarizer(source));
                    MultiFormatReader reader = new MultiFormatReader();
                    try
                    {
                        Result result = reader.decode(bBitmap);
                        //Toast.makeText(this, "The content of the QR image is: " + result.getText(), Toast.LENGTH_SHORT).show();
                        OpenAddManual(result.getText());

                    }
                    catch (NotFoundException e)
                    {
                        Log.e("TAG", "decode exception", e);
                    }
                }
                catch (FileNotFoundException e)
                {
                    Log.e("TAG", "can not open file" + uri.toString(), e);
                }
                break;
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode,
                permissions,
                grantResults);

        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            }
        }
        else if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            }
        }
    }

    String image_Name = "";
    private void OpenAddManual(String data) {
        dialogBuilder = new AlertDialog.Builder(this).create();
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.alert_dialog, null);


        final EditText input_edittitle = dialogView.findViewById(R.id.input_edittitle);
        final EditText input_editdata = dialogView.findViewById(R.id.input_editdata);
        final ImageButton select_Image = dialogView.findViewById(R.id.select_Image);

        input_editdata.setText(data);

        Button btn_save_dialog = dialogView.findViewById(R.id.btn_save_dialog);
        Button btn_dis = dialogView.findViewById(R.id.btn_dis);

        CarouselPicker carouselPicker = (CarouselPicker) dialogView.findViewById(R.id.carousel);

        final List<CarouselPicker.PickerItem> imageItems = new ArrayList<>();
        imageItems.add(new CarouselPicker.DrawableItem(R.drawable.outline_qr_code_scanner_black_24dp));
        imageItems.add(new CarouselPicker.DrawableItem(R.drawable.instagram));
        imageItems.add(new CarouselPicker.DrawableItem(R.drawable.outline_alternate_email_black_24dp));
        imageItems.add(new CarouselPicker.DrawableItem(R.drawable.outline_android_black_24dp));
        imageItems.add(new CarouselPicker.DrawableItem(R.drawable.outline_call_black_24dp));
        imageItems.add(new CarouselPicker.DrawableItem(R.drawable.outline_contact_mail_black_24dp));
        imageItems.add(new CarouselPicker.DrawableItem(R.drawable.outline_domain_black_24dp));
        imageItems.add(new CarouselPicker.DrawableItem(R.drawable.outline_email_black_24dp));
        imageItems.add(new CarouselPicker.DrawableItem(R.drawable.outline_history_black_24dp));
        imageItems.add(new CarouselPicker.DrawableItem(R.drawable.outline_http_black_24dp));
        imageItems.add(new CarouselPicker.DrawableItem(R.drawable.outline_install_mobile_black_24dp));
        imageItems.add(new CarouselPicker.DrawableItem(R.drawable.outline_lightbulb_black_24dp));
        imageItems.add(new CarouselPicker.DrawableItem(R.drawable.outline_lock_black_24dp));
        imageItems.add(new CarouselPicker.DrawableItem(R.drawable.outline_perm_identity_black_24dp));
        imageItems.add(new CarouselPicker.DrawableItem(R.drawable.outline_qr_code_2_black_24dp));
        imageItems.add(new CarouselPicker.DrawableItem(R.drawable.outline_shopping_cart_black_24dp));
        imageItems.add(new CarouselPicker.DrawableItem(R.drawable.outline_thumb_up_black_24dp));
        imageItems.add(new CarouselPicker.DrawableItem(R.drawable.outline_whatsapp_black_24dp));
        imageItems.add(new CarouselPicker.DrawableItem(R.drawable.outline_whatshot_black_24dp));
        imageItems.add(new CarouselPicker.DrawableItem(R.drawable.outline_work_black_24dp));

        CarouselPicker.CarouselViewAdapter imageAdapter = new CarouselPicker.CarouselViewAdapter(this, imageItems, 0);

        carouselPicker.setAdapter(imageAdapter);

        carouselPicker.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                image_Name = getResources().getResourceEntryName(imageItems.get(position).getDrawable());
                Log.d("AddData", image_Name +"");
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });


        btn_save_dialog.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onClick(View v) {

                String title = input_edittitle.getText().toString();
                String data = input_editdata.getText().toString();

                itemListMobile.add(new Data(title,data, image_Name));

                saveData(itemListMobile);
                sendData(itemListMobile);
                dialogBuilder.dismiss();
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

    @Override
    public void onDataChanged(@NonNull DataEventBuffer dataEventBuffer) {
        Log.d(TAG, "onDataChanged: " + dataEventBuffer);
        for (DataEvent event : dataEventBuffer) {
            if (event.getType() == DataEvent.TYPE_CHANGED) {
                String path = event.getDataItem().getUri().getPath();
                if (datapath.equals(path)) {
                    DataMapItem dataMapItem = DataMapItem.fromDataItem(event.getDataItem());

                } else {
                    Log.e(TAG, "Unrecognized path: " + path);
                }
            } else if (event.getType() == DataEvent.TYPE_DELETED) {
                Log.v(TAG, "Data deleted : " + event.getDataItem().toString());
            } else {
                Log.e(TAG, "Unknown data event Type = " + event.getType());
            }
        }
    }


    @SuppressLint("NotifyDataSetChanged")
    private void saveData(List<Data> dataList) {
        Gson gson = new Gson();
        SharedPreferences.Editor editor = sharedPreferences.edit();
        String json = gson.toJson(dataList);
        editor.putString("dataList", json);
        editor.apply();
        mAdapter.notifyDataSetChanged();
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

        Task<DataItem> dataItemTask = Wearable.getDataClient(this).putDataItem(request);
        dataItemTask
            .addOnSuccessListener(new OnSuccessListener<DataItem>() {
                @Override
                public void onSuccess(DataItem dataItem) {
                    Log.d(TAG, "Sending message was successful: " + dataItem);

                    Toast.makeText(MainActivity.this, "Data Syncing...", Toast.LENGTH_SHORT).show();
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
