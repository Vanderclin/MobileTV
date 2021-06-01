package com.mobiletv.app;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import java.util.List;

import com.mobiletv.app.R.*;


public class MessageAdapter extends ArrayAdapter<MessageView>
{
    private Activity context;
    private List<MessageView> Messages;

    public MessageAdapter(Activity context, List<MessageView> Messages)
	{
        super(context, R.layout.cardview_message, Messages);
        this.context = context;
        this.Messages = Messages;
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent)
	{
        LayoutInflater inflater = context.getLayoutInflater();
        View itemView = inflater.inflate(R.layout.cardview_message, null, true);

        TextView textViewName = itemView.findViewById(R.id.display_username);
        TextView textDisplayMessage = itemView.findViewById(R.id.display_message);
        TextView textDisplayTime = itemView.findViewById(R.id.display_time);


        MessageView Message = Messages.get(position);

        textViewName.setText(Message.getName());
        textDisplayMessage.setText(Message.getMessage());
        textDisplayTime.setText(Message.getTime());

        return itemView;
    }
}
