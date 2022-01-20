package edu.aucegypt.project;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.ListActivity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Telephony;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import android.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class MainActivity extends ListActivity
{
    //Text Views
    TextView Balance;

    //Preferences
    SharedPreferences UserAccount;
    SharedPreferences.Editor UserAccountEditor;

    //Global Array;
    JSONArray Transactions;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        //Configure Nav Bar
        configureNavBar();
        configureButtons();

        //Check for permissions
        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED)
            getPermissions();

        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.RECEIVE_SMS) != PackageManager.PERMISSION_GRANTED)
            getPermissions();

        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED)
            getPermissions();

        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED)
            getPermissions();

        //Views
        Balance = (TextView) findViewById(R.id.BalanceView);

        //Preferences
        UserAccount = getSharedPreferences("UserAccount", 0);
        UserAccountEditor = UserAccount.edit();
        UserAccountEditor.putBoolean("Handshake", false);
        UserAccountEditor.commit();

        //Get Phone Number
        getNumber();

        if (!UserAccount.getBoolean("Registered", false))
        {
            createKey();
            RegisterAccount();
        }
        //Fetch Transactions
        getTransactions();


    }

    public void createKey ()
    {
        try
        {
            //Create Key Generator
            KeyGenerator KeyGen = KeyGenerator.getInstance("AES");
            KeyGen.init(128);

            //Generate Key
            SecretKey SecKey = KeyGen.generateKey();

            //Store pair in preferences
            UserAccountEditor.putString("EncryptionKey", Base64.encodeToString(SecKey.getEncoded(), Base64.DEFAULT));
            UserAccountEditor.commit();

        }
        catch (Exception E)
        {
            E.printStackTrace();
        }
    }



    void RegisterAccount()
    {
        Intent i = new Intent(this, SMSActivity.class);

        i.putExtra("op", "Register");
        i.putExtra("message", "0,"+UserAccount.getString("EncryptionKey","N/A"));
        startActivity(i);
    }

    public void getTransactions ()
    {
        QueryTask Task = new QueryTask();
        Task.execute("http://10.0.2.2:3000/getTransactions", "?phone="+UserAccount.getString("Number","N/A"));
    }
    public void getNumber()
    {
        TelephonyManager telManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        UserAccountEditor.putString("Number", telManager.getLine1Number());
        UserAccountEditor.commit();
    }

    public void getPermissions()
    {
        String[]    PermissionsList = new String[]{"android.permission.SEND_SMS", "android.permission.RECEIVE_SMS", "android.permission.READ_SMS", "android.permission.READ_PHONE_STATE"};
        int         PermissionsCode = 200;
        ActivityCompat.requestPermissions(this, PermissionsList, PermissionsCode);
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        Balance.setText(UserAccount.getString("Balance", "N/A"));
        BottomNavigationView NavBar = (BottomNavigationView) findViewById(R.id.bottom_navigation_main);
        NavBar.setSelectedItemId(R.id.balance);
        getTransactions ();
    }

    protected void configureList()
    {
        Transaction[] TransactionObjects = new Transaction[Transactions.length()];
        for (int i = 0; i<TransactionObjects.length; i++)
        {
            try
            {
                TransactionObjects[i] = new Transaction (Transactions.getJSONObject(i), UserAccount.getString ("Number", "N/A"));
            }
            catch (Exception e) {}
        }
        setListAdapter(new TransactionAdapter(this, new ArrayList <Transaction> (Arrays.asList(TransactionObjects))));
    }



    protected  void configureNavBar()
    {
        BottomNavigationView NavBar = (BottomNavigationView) findViewById(R.id.bottom_navigation_main);
        NavBar.setSelectedItemId(R.id.balance);
        NavBar.setOnItemSelectedListener (new NavigationBarView.OnItemSelectedListener(){

            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item)
            {
                switch (item.getItemId())
                {
                    case R.id.balance:
                        return true;
                    case R.id.send:
                        startActivity (new Intent(getApplicationContext(), SendActivity.class));
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

    protected void configureButtons()
    {
        ImageButton BalanceRefresh = (ImageButton) findViewById(R.id.BalanceRefresh);
        BalanceRefresh.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                Intent i = new Intent(getApplicationContext(), SMSActivity.class);
                i.putExtra("op", "Balance");
                i.putExtra("message", "2");
                startActivity(i);
            }
        });

        ImageButton PhoneQRGenerate = (ImageButton) findViewById(R.id.PhoneQRGenerate);
        PhoneQRGenerate.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                Intent i = new Intent(getApplicationContext(), QRActivity.class);
                i.putExtra("content", "Phone:"+UserAccount.getString("Number","N/A"));
                startActivity(i);
            }
        });
    }

    private class QueryTask extends AsyncTask<String, Void, JSONArray>
    {
        @Override
        protected JSONArray doInBackground(String... Strings)
        {
            try
            {
                //Get SMS
                JSONArray Response = GETRequest(Strings[0], Strings[1]);
                return Response;

            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(JSONArray Results)
        {
            Transactions = Results;
            configureList();

        }

        protected JSONArray GETRequest (String Link, String Data)
        {
            try
            {
                //Open the connection
                URL                 link        = new URL (Link+Data);
                HttpURLConnection   connection  = (HttpURLConnection) link.openConnection();


                //If there is an error, return
                if (connection.getResponseCode() != HttpURLConnection.HTTP_OK)
                    return new JSONArray("[{'Status': 'ERROR'}]");

                //Else, read the response
                InputStream         in                  = connection.getInputStream();
                byte[]              ResponseText        = new byte[2000];

                in.read(ResponseText);

                //Return the response
                return new JSONArray(new String(ResponseText));
            }
            catch (Exception e)
            {
                e.printStackTrace();
                return new JSONArray();
            }

        }
    }




}