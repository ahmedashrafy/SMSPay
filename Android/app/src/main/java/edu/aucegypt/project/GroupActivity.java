package edu.aucegypt.project;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;

public class GroupActivity extends ListActivity
{
    BottomNavigationView NavBar;
    String[] Groups;
    TextView CreateGroup;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group);
        configureNavBar();

        SharedPreferences UserAccount = getSharedPreferences("UserAccount", 0);
        QueryTask Task = new QueryTask();
        Task.execute("http://10.0.2.2:3000/getGroups", "?phone="+UserAccount.getString("Number","N/A"));


        CreateGroup = (TextView) findViewById(R.id.create_new_group);
        CreateGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent (getApplicationContext(), GroupCreateActivity.class));
            }
        });
    }

    @Override
    protected void onResume ()
    {
        NavBar.setSelectedItemId(R.id.groups);
        super.onResume();
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
            Groups = new String [Results.length()];

            for (int i = 0; i<Results.length(); i++)
            {
                try
                {
                    Groups[i] = Results.getJSONObject(i).getString("GroupName");
                }
                catch (Exception E)
                {
                    E.printStackTrace();
                }
            }

            setListAdapter(new GroupsAdapter(getApplicationContext(), new ArrayList <String> (Arrays.asList(Groups))));
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
                return new JSONArray(new String(ResponseText));
            }
            catch (Exception e)
            {
                e.printStackTrace();
                return new JSONArray();
            }

        }
    }

    protected  void configureNavBar()
    {
        NavBar = (BottomNavigationView) findViewById(R.id.bottom_navigation_group);
        NavBar.setSelectedItemId(R.id.groups);
        NavBar.setOnItemSelectedListener (new NavigationBarView.OnItemSelectedListener(){

            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item)
            {
                switch (item.getItemId())
                {
                    case R.id.invoice:
                        startActivity (new Intent(getApplicationContext(),InvoiceActivity.class));
                        overridePendingTransition(0, 0);
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
                        return true;
                }
                return false;
            }
        });
    }
    @Override
    public void onListItemClick (ListView parent, View v, int position, long id)
    {
        Intent i = new Intent(this, GroupActivityDetail.class);
        i.putExtra("groupName", Groups[position]);
        startActivity(i);
    }

}

class GroupsAdapter extends ArrayAdapter<String>
{
    public GroupsAdapter (Context context, ArrayList<String> Groups)
    {
        super(context, 0, Groups);
    }

    @Override
    public View getView (int position, View convertView, ViewGroup parent)
    {
        String group = getItem(position);

        if (convertView == null)
            convertView = LayoutInflater.from (getContext()).inflate(R.layout.group_list_item, parent, false);

        TextView groupName = (TextView)  convertView.findViewById(R.id.groupName);


        groupName.setText (group);

        return convertView;
    }

}
