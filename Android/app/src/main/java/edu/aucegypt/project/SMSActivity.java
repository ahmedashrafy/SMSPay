package edu.aucegypt.project;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.provider.Telephony;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.progressindicator.CircularProgressIndicator;

import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import android.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class SMSActivity extends AppCompatActivity {

    //Resources
    SmsManager SMSSendManager;
    SMSReceiver SMSReceiveManager;

    //Encryption
    String EncryptionKey = "STLy5Oct30mk7Lawj0CldQ==";

    //Control
    String GatewayNumber = "5556";

    //String
    String Operation;
    String Message;

    //Views
    TextView OperationInfo;
    TextView OperationStatus;
    ProgressBar OperationProgress;
    LinearLayout OperationBackground;

    //Preferences
    SharedPreferences UserAccount;
    SharedPreferences.Editor UserAccountEditor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_smsactivity);

        //Preferences
        UserAccount = getSharedPreferences("UserAccount", 0);
        UserAccountEditor = UserAccount.edit();

        //Created SMS Managers
        SMSReceiveManager = new SMSReceiver();
        SMSSendManager = SmsManager.getDefault();

        //Register Recieve Manager
        Log.i ("SMSActivity", "Inside");
        IntentFilter SMSReceiverFilter = new IntentFilter(Telephony.Sms.Intents.SMS_RECEIVED_ACTION);
        registerReceiver(SMSReceiveManager, SMSReceiverFilter);


        //Get the bundle data
        Bundle bundle = getIntent().getExtras();
        Operation = bundle.getString("op");
        Message = bundle.getString("message");



        //Get the views
        OperationInfo = (TextView) findViewById(R.id.OperationInfo);
        OperationStatus = (TextView) findViewById(R.id.OperationInfo);
        OperationProgress = (ProgressBar) findViewById(R.id.OperationProgress);
        OperationBackground = (LinearLayout) findViewById(R.id.OperationBackground);

        OperationProgress.setVisibility(View.VISIBLE);



        //Update Screen;
        displayOperation();

        //Send SMS
        sendSMS(Message);

    }

    private void displayOperation() {
        switch (Operation) {
            case "Register": {
                OperationInfo.setText("Registering user \n Please Wait");
                break;
            }

            case "Send": {
                OperationInfo.setText("Processing Transaction \n Please Wait");
                break;
            }

            case "Balance": {
                OperationInfo.setText("Retrieving Balance \n Please Wait");
                break;
            }
            case "Create Group": {
                OperationInfo.setText("Creating Groups \n Please Wait");
                break;
            }


        }
    }

    private void resolveOperation(String Response) {
        OperationProgress.setVisibility(View.GONE);

        switch (Operation) {
            case "Balance": {
                switch (Response.split(",", 0)[0]) {
                    case "ERR_USER_NOT_REGISTERED": {
                        OperationInfo.setText("Registration Failed:\nUser Already Exists.");
                        OperationBackground.setBackgroundResource(R.color.red);
                        break;
                    }
                    case "CONF_TX_SUCCESS": {
                        OperationInfo.setText("Balance Retrieved!");
                        OperationBackground.setBackgroundResource(R.color.green);
                        UserAccountEditor.putString("Balance", Response.split(",", 0)[1]);
                        UserAccountEditor.commit();
                        break;
                    }
                }
            }

            case "Register": {
                switch (Response.split(",", 0)[0]) {
                    case "ERR_USER_ALREADY_EXISTS": {
                        OperationInfo.setText("Info:\nUser Already Exists.");
                        UserAccountEditor.putBoolean("Registered", true);
                        UserAccountEditor.putString("Balance", "0.00");
                        UserAccountEditor.commit();
                        break;
                    }

                    case "CONF_USER_CREATED": {
                        OperationInfo.setText("Registration Success");
                        OperationBackground.setBackgroundResource(R.color.green);
                        UserAccountEditor.putString("Balance", "0.00");
                        UserAccountEditor.putBoolean("Registered", true);
                        UserAccountEditor.commit();
                        break;
                    }

                    case "ERR_RUNTIME_ERROR": {
                        OperationInfo.setText("Registration Failed:\nRunetime Error");
                        OperationBackground.setBackgroundResource(R.color.red);
                        break;
                    }
                }
                break;
            }

            case "Send": {
                switch (Response.split(",", 0)[0]) {
                    case "ERR_SRC_NOT_REGISTERED": {
                        OperationInfo.setText("Transaction Failed:\nUser Not Registered");
                        OperationBackground.setBackgroundResource(R.color.red);
                        break;
                    }

                    case "ERR_INSUF_BALANCE": {
                        OperationInfo.setText("Transaction Failed:\nInsufficient Balance");
                        OperationBackground.setBackgroundResource(R.color.red);
                        UserAccountEditor.putString("Balance", Response.split(",", 0)[1]);
                        UserAccountEditor.commit();
                        break;
                    }

                    case "ERR_UNKNOWN": {
                        OperationInfo.setText("Transaction Failed:\nUnknown Error");
                        OperationBackground.setBackgroundResource(R.color.red);
                        break;
                    }

                    case "ERR_DEST_NOT_REGISTERED": {
                        OperationInfo.setText("Transaction Failed:\nRecepient Not Registered");
                        OperationBackground.setBackgroundResource(R.color.red);

                        break;
                    }

                    case "ERROR_TX_FAILED": {
                        OperationInfo.setText("Transaction Failed:\nRuntime Error");
                        OperationBackground.setBackgroundResource(R.color.red);
                        break;
                    }

                    case "CONF_TX_SUCCESS": {
                        OperationInfo.setText("Transaction Success!");
                        OperationBackground.setBackgroundResource(R.color.green);
                        UserAccountEditor.putString("Balance", Response.split(",", 0)[1]);
                        UserAccountEditor.commit();
                        break;
                    }

                }
                break;
            }

            case "Create Group": {
                switch (Response.split(",", 0)[0]) {
                    case "ERR_RUNTIME_ERROR": {
                        OperationInfo.setText("Group Creation Failed:\nRuntime Error");
                        OperationBackground.setBackgroundResource(R.color.red);
                        break;
                    }

                    case "CONF_REQUEST_SENT": {
                        OperationInfo.setText("Group Creation Successful!");
                        OperationBackground.setBackgroundResource(R.color.green);
                        break;
                    }

                }
                break;
            }
        }

    }


    private class SMSReceiver extends BroadcastReceiver {
        //Resolve pending transaction
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle bundle = intent.getExtras();

            Object[] Objects = (Object[]) bundle.get("pdus");
            SmsMessage[] SMS = new SmsMessage[Objects.length];

            for (int i = 0; i < Objects.length; i++) {
                SMS[i] = SmsMessage.createFromPdu(((byte[]) Objects[i]));
            }

            for (SmsMessage Message : SMS) {
                resolveOperation(decryptSMS(Message.getMessageBody()));
            }
        }
    }

    private void sendSMS(String smsBody)
    {
        SMSSendManager.sendTextMessage(GatewayNumber, null, encryptSMS(smsBody), null, null);
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
