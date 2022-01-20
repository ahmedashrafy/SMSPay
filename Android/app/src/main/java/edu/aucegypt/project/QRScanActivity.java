package edu.aucegypt.project;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

public class QRScanActivity extends AppCompatActivity {

    IntentIntegrator Scanner;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send);
        Scanner = new  IntentIntegrator(this);
        Scanner.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE);

        Scanner.initiateScan();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        IntentResult Results = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);

        if (Results.getContents()  != null)
            callActivity (Results.getContents());

        else
            startActivity(new Intent(getApplicationContext(), MainActivity.class));

        super.onActivityResult(requestCode, resultCode, data);
    }

    private void callActivity (String Code)
    {
        Intent i = new Intent(getApplicationContext(), SendActivity.class);

        String[] Data = Code.split(",");

        switch (Data [0])
        {
            case "Phone":
            {
                i.putExtra("phone", Data[1]);
                break;
            }
            case "Invoice":
            {
                i.putExtra("phone", Data[1]);
                i.putExtra("amount", Data[2]);
                break;
            }

        }

        startActivity(i);
    }

}

