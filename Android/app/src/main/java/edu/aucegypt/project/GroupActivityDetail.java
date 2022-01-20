package edu.aucegypt.project;

import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;

public class GroupActivityDetail extends AppCompatActivity {


    TextView groupMembersList;
    TextView groupDetailName;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_detail);
        groupMembersList = (TextView) findViewById(R.id.groupMembersList);
        groupDetailName = (TextView) findViewById(R.id.groupDetailName);
        Bundle bundle = getIntent().getExtras();
        SharedPreferences UserAccount = getSharedPreferences("UserAccount", 0);
        QueryTask Task = new QueryTask();
        Task.execute("http://10.0.2.2:3000/getGroupInfo", "?phone="+UserAccount.getString("Number","N/A")+"&group=" +bundle.getString("groupName"));

        groupDetailName.setText(bundle.getString("groupName"));

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
            String Members = "";
            for (int i = 0; i<Results.length(); i++)
            {
                try {
                    Members = Members + Results.getJSONObject(i).getString("DestinationPhone") + new String ("\n");
                }
                catch (Exception E)
                {
                    E.printStackTrace();
                }
            }
            groupMembersList.setText (Members);


        }

        protected JSONArray GETRequest (String Link, String Data)
        {
            try
            {
                //Open the connection
                URL link        = new URL (Link+Data);
                HttpURLConnection connection  = (HttpURLConnection) link.openConnection();


                //If there is an error, return
                if (connection.getResponseCode() != HttpURLConnection.HTTP_OK)
                    return new JSONArray("[{'Status': 'ERROR'}]");

                //Else, read the response
                InputStream in                  = connection.getInputStream();
                byte[]              ResponseText        = new byte[2000];

                in.read(ResponseText);

                //Return the response
                return new JSONObject(new String(ResponseText)).getJSONArray("body");
            }
            catch (Exception e)
            {
                e.printStackTrace();
                return new JSONArray();
            }

        }
    }

}