package com.ram.letschat;

import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.text.Layout;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;

import java.util.List;

/**
 * Created by RAMJEE on 19-01-2018.
 */

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHoler> {

    private List<Messages> mMessageList;
    private FirebaseAuth mAuth;

    public MessageAdapter(List<Messages> mMessageList){
        this.mMessageList = mMessageList;
        mAuth = FirebaseAuth.getInstance();
    }
    @Override
    public MessageViewHoler onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.single_message_layout,parent,false);


        return new MessageViewHoler(view);
    }

    @Override
    public void onBindViewHolder(MessageViewHoler holder, int position) {

        String current_user_id = mAuth.getCurrentUser().getUid();
        Messages message = mMessageList.get(position);

         String messgae_from = message.getFrom();

        if(current_user_id.equals(messgae_from)){


            holder.messageText.setBackgroundResource(R.drawable.message_sender_background);
            holder.messageText.setTextColor(Color.BLACK);
        }else{
            holder.messageText.setBackgroundResource(R.drawable.message_background);
            holder.messageText.setTextColor(Color.BLACK);

        }
        holder.messageText.setText(message.getMessage());
    }

    @Override
    public int getItemCount() {
        return mMessageList.size();
    }


    public class MessageViewHoler extends RecyclerView.ViewHolder {
        public TextView messageText;
        public MessageViewHoler(View itemView) {
            super(itemView);

            messageText = (TextView) itemView.findViewById(R.id.message_id);
        }
    }
}
