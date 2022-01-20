package edu.aucegypt.project;

import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.ImageView;

import com.google.zxing.BarcodeFormat;
import com.journeyapps.barcodescanner.BarcodeEncoder;

public class QRActivity extends AppCompatActivity
{

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qractivity);
        ImageView QRCode = (ImageView) findViewById(R.id.QRCode);
        Bundle bundle = getIntent().getExtras();

        try
        {
            BarcodeEncoder Encoder = new BarcodeEncoder();
            QRCode.setImageBitmap(Encoder.encodeBitmap(bundle.getString("content"), BarcodeFormat.QR_CODE, 1000, 1000));
        }

        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

}
