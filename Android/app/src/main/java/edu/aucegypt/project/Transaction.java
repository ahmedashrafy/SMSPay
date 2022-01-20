package edu.aucegypt.project;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONObject;

import java.sql.Timestamp;
import java.util.ArrayList;


public class Transaction
{
    public String Type ="";
    public String Party ="";
    public String Amount ="";
    public String Timestamp ="";

    public Transaction(JSONObject TransactionItem, String Phone)
    {
        try
        {
            if (Phone.charAt(0) == '+')
                Phone = new String (" ")+Phone.substring(1);


            this.Type = (TransactionItem.getString("SourcePhone").equals(Phone))?("Send to: "):("Receive from: ") ;
            this.Party = (TransactionItem.getString("SourcePhone").equals(Phone))?(TransactionItem.getString("DestinationPhone")):(Phone) ;
            this.Amount = TransactionItem.getString("TransactionAmount");
            this.Timestamp = TransactionItem.getString("TransactionTimestamp");

        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
class TransactionAdapter extends ArrayAdapter<Transaction>
{
    public TransactionAdapter (Context context, ArrayList<Transaction> Transactions)
    {
        super(context, 0, Transactions);
    }

    @Override
    public View getView (int position, View convertView, ViewGroup parent)
    {
        Transaction transaction = getItem(position);

        if (convertView == null)
            convertView = LayoutInflater.from (getContext()).inflate(R.layout.transaction_list_item, parent, false);

        TextView transactionType = (TextView)  convertView.findViewById(R.id.transactionType);
        TextView transactionParty = (TextView)  convertView.findViewById(R.id.transactionParty);
        TextView transactionAmount = (TextView)  convertView.findViewById(R.id.transactionAmount);
        TextView transactionTimestamp = (TextView)  convertView.findViewById(R.id.transactionTimestamp);

        transactionType.setText (transaction.Type);
        transactionParty.setText (transaction.Party);
        transactionAmount.setText (transaction.Amount);
        transactionTimestamp.setText(transaction.Timestamp);
        Log.i ("ts", transaction.Timestamp);

        return convertView;
    }

}
