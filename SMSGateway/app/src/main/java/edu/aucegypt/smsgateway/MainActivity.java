package edu.aucegypt.smsgateway;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.provider.Telephony;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class MainActivity extends AppCompatActivity implements View.OnClickListener
{
    //Resources
    SmsManager SMSManager;
    SMSReceiver SMSRecieveManager;

    //Encryption
    String EncryptionKey = "STLy5Oct30mk7Lawj0CldQ==";

    //Control
    boolean Active = false;
    int     Counter = 0;

    //UI
    TextView CounterView;
    Button Start;
    Button Stop;


    private class SMSReceiver extends BroadcastReceiver
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            if (!Active) return;

            Bundle bundle           = intent.getExtras();

            Object[] Objects        = (Object[]) bundle.get("pdus");
            SmsMessage[] SMS   = new SmsMessage[Objects.length];

            for (int i = 0; i<Objects.length; i++)
            {
                SMS[i] = SmsMessage.createFromPdu(((byte[]) Objects[i]));
            }

            for (SmsMessage Message : SMS)
            {
                Toast.makeText(context.getApplicationContext(), "SMS Recieved: " + decryptSMS(Message.getMessageBody()), Toast.LENGTH_SHORT).show();
                SMSTask StartTask = new SMSTask();
                StartTask.execute(SMS);
            }
        }
    }

    private class SMSTask extends AsyncTask<SmsMessage, Void, Void>
    {
        @Override
        protected Void doInBackground(SmsMessage... Messages)
        {
            try
            {
                //Get SMS
                String      Parameters = "?phone="+Messages[0].getOriginatingAddress()+"&message_body=" + decryptSMS(Messages[0].getMessageBody());
                JSONObject  Response = GETRequest("http://10.0.2.2:3000/passSMS", Parameters);


                String SMSDestination   = Response.getString("to");
                String SMSBody          = encryptSMS(Response.getString("body"));

                //Send SMS
                SMSManager.sendTextMessage(SMSDestination,
                        null,
                        SMSBody,
                        null,
                        null);

                //Update User Counter
                Counter++;
                publishProgress();

            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Void... Voids)
        {
            CounterView.setText("Handled SMS Messages: " + String.valueOf(Counter));
        }

        protected JSONObject GETRequest (String Link, String Data)
        {
            try
            {
                //Open the connection
                URL                 link        = new URL (Link+Data);
                HttpURLConnection   connection  = (HttpURLConnection) link.openConnection();


                //If there is an error, return
                if (connection.getResponseCode() != HttpURLConnection.HTTP_OK)
                    return new JSONObject("{'Status': 'ERROR'}");

                //Else, read the response
                InputStream         in                  = connection.getInputStream();
                byte[]              ResponseText        = new byte[250];

                in.read(ResponseText);

                //Return the response
                return new JSONObject(new String(ResponseText));
            }
            catch (Exception e)
            {
                e.printStackTrace();
                return new JSONObject();
            }

        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SMSManager= SmsManager.getDefault();
        SMSRecieveManager = new SMSReceiver();

        CounterView = (TextView)    findViewById(R.id.CounterView);
        Start       = (Button)      findViewById(R.id.StartButton);
        Stop        = (Button)      findViewById(R.id.StopButton);

        Start.setOnClickListener(this);
        Stop.setOnClickListener(this);

        Start.setEnabled(true);
        Stop.setEnabled(false);

        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED)
            getPermissions();

        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED)
            getPermissions();

        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.RECEIVE_SMS) != PackageManager.PERMISSION_GRANTED)
            getPermissions();

        //Register Recieve Manager
        IntentFilter SMSReceiverFilter = new IntentFilter(Telephony.Sms.Intents.SMS_RECEIVED_ACTION);
        registerReceiver(SMSRecieveManager, SMSReceiverFilter);
    }

    public void getPermissions()
    {
        String[]    PermissionsList = new String[]{"android.permission.SEND_SMS", "android.permission.RECEIVE_SMS", "android.permission.READ_SMS"};
        int         PermissionsCode = 200;
        ActivityCompat.requestPermissions(this, PermissionsList, PermissionsCode);
    }

    public void deactivateActivity()
    {
        Start.setEnabled(false);
        Stop.setEnabled(false);
        CounterView.setText("This app can't work without access to SMS.");
    }

    @Override
    public void onRequestPermissionsResult(int permsRequestCode, String[] permissions, int[] gRes)
    {
        super.onRequestPermissionsResult(permsRequestCode, permissions, gRes);
        switch (permsRequestCode)
        {
            case 200:
                if (gRes.length > 0)
                    if(gRes[0] != PackageManager.PERMISSION_GRANTED)
                        deactivateActivity();
        }
    }

    @Override
    public void onClick(View v)
    {
        Button b = (Button) v;

        if (b.getText().equals("Start"))
        {
           Active = true;
           Stop.setEnabled(true);
           Start.setEnabled (false);

        }

        if (b.getText().equals("Stop"))
        {
            Active = false;
            Stop.setEnabled(false);
            Start.setEnabled (true);
        }


    }

    private String encryptSMS(String smsBody)
    {


        try
        {
            //Retrieve the key
            byte[] SecKeyArray      = Base64.decode(EncryptionKey, Base64.NO_WRAP);
            SecretKey SecKey        = new SecretKeySpec(SecKeyArray, 0, SecKeyArray.length, "AES");

            //Set up the cipher
            Cipher Encryptor = Cipher.getInstance("AES");
            Encryptor.init(Cipher.ENCRYPT_MODE, SecKey);


            return new String (Base64.encode(Encryptor.doFinal(smsBody.getBytes(StandardCharsets.UTF_8)), Base64.DEFAULT));
        }
        catch (Exception E)
        {
            E.printStackTrace();
            return new String("ERROR");
        }

    }

    private String decryptSMS(String smsBody)
    {
        try
        {
            //Retrieve the key
            byte[] SecKeyArray      = Base64.decode(EncryptionKey, Base64.NO_WRAP);
            SecretKey SecKey        = new SecretKeySpec(SecKeyArray, 0, SecKeyArray.length, "AES");

            //Set up the cipher
            Cipher Encryptor = Cipher.getInstance("AES");
            Encryptor.init(Cipher.DECRYPT_MODE, SecKey);

            return new String (Encryptor.doFinal(Base64.decode(smsBody.getBytes(StandardCharsets.UTF_8), Base64.DEFAULT)));

        }
        catch (Exception E)
        {
            E.printStackTrace();
            return new String("ERROR");
        }
    }

}