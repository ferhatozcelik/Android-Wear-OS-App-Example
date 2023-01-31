package com.ferhatozcelik.mycodeswear;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.support.wearable.activity.WearableActivity;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.ferhatozcelik.mycodeswear.R;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import androidmads.library.qrgenearator.QRGContents;
import androidmads.library.qrgenearator.QRGEncoder;

public class CodeDetails extends WearableActivity {

    ImageView codeImageView;
    TextView codeTitle;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_code_details);
        codeImageView = findViewById(R.id.codeImageView);
        codeTitle = findViewById(R.id.codeTitle);

        Intent intent = getIntent();
        String data = intent.getStringExtra("data");
        String title = intent.getStringExtra("title");
        codeTitle.setText(title);

        QRCodeWriter multiFormatWriter = new QRCodeWriter();
        try{
            BitMatrix bitMatrix = multiFormatWriter.encode(data, BarcodeFormat.QR_CODE,500,500);
            BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
            Bitmap bitmap = barcodeEncoder.createBitmap(bitMatrix);
            codeImageView.setImageBitmap(bitmap);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

}