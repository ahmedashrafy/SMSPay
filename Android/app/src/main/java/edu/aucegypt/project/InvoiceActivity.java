package edu.aucegypt.project;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

public class InvoiceActivity extends AppCompatActivity implements  View.OnClickListener
{

    //TextEdits
    EditText InvoiceAmount;

    //Button
    Button InvoiceCreate;
    SharedPreferences UserAccount;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invoice);
        configureNavBar();

        UserAccount = getSharedPreferences("UserAccount", 0);

        //Get Views
        InvoiceAmount = (EditText) findViewById(R.id.invoice_amount);
        InvoiceCreate = (Button) findViewById(R.id.invoice_create);


        //Configure the button
        InvoiceCreate.setOnClickListener(this);
    }

    public void onClick(View v)
    {
        Intent i = new Intent(this, QRActivity.class);
        i.putExtra("content", "Invoice,"+UserAccount.getString("Number", "N/A")+","+InvoiceAmount.getText());
        startActivity(i);
    }

    protected  void configureNavBar()
    {
        BottomNavigationView NavBar = (BottomNavigationView) findViewById(R.id.bottom_navigation_invoice);
        NavBar.setSelectedItemId(R.id.invoice);
        NavBar.setOnItemSelectedListener (new NavigationBarView.OnItemSelectedListener(){

            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item)
            {
                switch (item.getItemId())
                {
                    case R.id.invoice:
                        return true;
                    case R.id.balance:
                        startActivity (new Intent(getApplicationContext(), MainActivity.class));
                        overridePendingTransition(0, 0);
                        return true;
                    case R.id.send:
                        startActivity (new Intent(getApplicationContext(),SendActivity.class));
                        overridePendingTransition(0, 0);
                        return true;

                    case R.id.qrscanner:
                        startActivity (new Intent(getApplicationContext(),QRScanActivity.class));
                        overridePendingTransition(0, 0);
                        return true;

                    case R.id.groups:
                        startActivity (new Intent(getApplicationContext(),GroupActivity.class));
                        overridePendingTransition(0, 0);
                        return true;
                }
                return false;
            }
        });
    }
}