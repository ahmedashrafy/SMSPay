package edu.aucegypt.project;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

import org.w3c.dom.Text;

public class SendActivity extends AppCompatActivity implements View.OnClickListener
{

    //TextEdits
    EditText SendPhone;
    EditText SendAmount;


    //TextViews
    TextView SelectPhone;
    TextView SelectGroup;

    //Button
    Button SendConfirm;

    //String
    String Operation = "1";

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send);
        configureNavBar();

        //Get Views
        SelectPhone = (TextView) findViewById(R.id.select_phone);
        SelectGroup = (TextView) findViewById(R.id.select_group);

        SendPhone = (EditText) findViewById(R.id.send_phone);
        SendAmount = (EditText) findViewById(R.id.send_amount);
        SendConfirm = (Button) findViewById(R.id.send_confirm);


        //Check the intents
        Intent Caller = getIntent();

        if (Caller.hasExtra("phone"))
        {
            SendPhone.setText (getIntent().getExtras().getString("phone"));
        }

        if (Caller.hasExtra("amount"))
        {
            SendAmount.setText (getIntent().getExtras().getString("amount"));
        }

        //Configure the button
        SendConfirm.setOnClickListener(this);
        SelectPhone.setOnClickListener(this);
        SelectGroup.setOnClickListener(this);

    }

    public void onClick(View v)
    {


        switch (v.getId())
        {
            case R.id.send_confirm:
            {
                Intent i = new Intent(this, SMSActivity.class);
                i.putExtra("op", "Send");
                i.putExtra("message", Operation + ","+SendAmount.getText()+","+SendPhone.getText());
                startActivity(i);
                break;
            }

            case R.id.select_group:
            {
                Operation = "3";
                SelectPhone.setTextColor(getResources().getColor(R.color.grey_dark));
                SelectGroup.setTextColor(getResources().getColor(R.color.blue_light));
                break;
            }

            case R.id.select_phone:
            {
                Operation = "1";
                SelectGroup.setTextColor(getResources().getColor(R.color.grey_dark));
                SelectPhone.setTextColor(getResources().getColor(R.color.blue_light));
                break;
            }
        }


    }

    protected  void configureNavBar()
    {
        BottomNavigationView NavBar = (BottomNavigationView) findViewById(R.id.bottom_navigation_send);
        NavBar.setSelectedItemId(R.id.send);
        NavBar.setOnItemSelectedListener (new NavigationBarView.OnItemSelectedListener(){

            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item)
            {
                switch (item.getItemId())
                {
                    case R.id.send:
                        return true;
                    case R.id.balance:
                        startActivity (new Intent(getApplicationContext(), MainActivity.class));
                        overridePendingTransition(0, 0);
                        return true;
                    case R.id.invoice:
                        startActivity (new Intent(getApplicationContext(),InvoiceActivity.class));
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