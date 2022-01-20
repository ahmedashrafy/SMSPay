package edu.aucegypt.project;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class GroupCreateActivity extends AppCompatActivity
{

    EditText NewMember;
    TextView AddNewMember;
    TextView MembersList;
    Button CreateGroup;
    EditText GroupName;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_create);

        NewMember = (EditText) findViewById(R.id.group_member);
        AddNewMember = (TextView) findViewById(R.id.add_group_member);
        MembersList = (TextView) findViewById(R.id.group_member_list);
        CreateGroup = (Button) findViewById(R.id.create_group);
        GroupName = (EditText) findViewById(R.id.group_name);

        AddNewMember.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MembersList.setText(MembersList.getText()+"\n"+NewMember.getText());
                NewMember.setText("");
            }
        });

        CreateGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getApplicationContext(), SMSActivity.class);
                i.putExtra("op", "Create Group");
                i.putExtra("message", "4,"+GroupName.getText()+","+MembersList.getText().toString().replace("\n",","));
                startActivity(i);
            }
        });

    }
}

