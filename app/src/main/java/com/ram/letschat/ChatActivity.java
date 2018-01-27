package com.ram.letschat;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;


public class ChatActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private String mChatUser, mUser_name, mUser_image;
    private TextView mUserName, mLast_seen;
    private CircleImageView mUserImage;
    private ImageView mImageSendBtn, mTextSendBtn;
    private EditText mTextMesssage;

    private DatabaseReference mRootRef;
    private FirebaseAuth mAuth;
    String mCurrentUser;
    private RecyclerView mMessageList;
    private List<Messages> messagesList = new ArrayList<>();
    private LinearLayoutManager mLinearLayout;
    private MessageAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);


        mChatUser = getIntent().getStringExtra("user_id");
        mUser_name = getIntent().getStringExtra("user_name");
        mUser_image = getIntent().getStringExtra("user_image");






        mToolbar = (Toolbar) findViewById(R.id.chat_app_bar);
        setSupportActionBar(mToolbar);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);
        //actionBar.setTitle(mUser_name);

        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View custom_app_bar = inflater.inflate(R.layout.chat_custom_appbar,null);
        actionBar.setCustomView(custom_app_bar);


        //-------------- custom bar property-------
        mUserName = (TextView) findViewById(R.id.user_name_id);
        mLast_seen = (TextView) findViewById(R.id.user_last_seen);
        mUserImage = (CircleImageView) findViewById(R.id.user_image_id);

        // ------------Firebase property------
        mRootRef = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
        mCurrentUser = mAuth.getCurrentUser().getUid();

        //--------------message/image send property ---------------
        mImageSendBtn = (ImageView) findViewById(R.id.send_image);
        mTextSendBtn = (ImageView) findViewById(R.id.send_btn);
        mTextMesssage= (EditText) findViewById(R.id.message_text);
        mMessageList = (RecyclerView) findViewById(R.id.message_recyler);


        mLinearLayout = new LinearLayoutManager(this);
        mMessageList.setHasFixedSize(true);
        mMessageList.setLayoutManager(mLinearLayout);

        ///-------------To Load Messages to recycler view ----------
        mAdapter = new MessageAdapter(messagesList);

        mMessageList.setAdapter(mAdapter);

        loadMessage();



        mUserName.setText(mUser_name);
        Picasso.with(this).load(mUser_image).placeholder(R.drawable.user1).into(mUserImage);





        mRootRef.child("Chat").child(mChatUser).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if(!dataSnapshot.hasChild(mChatUser)){

                    final String date = DateFormat.getDateTimeInstance().format(new Date());
                    Map chatAddMap = new HashMap();
                    chatAddMap.put("seen",false);
                    chatAddMap.put("timestamp", date);

                    Map chatMap = new HashMap();
                    chatMap.put("Chat/" + mCurrentUser + "/" + mChatUser,chatAddMap);
                    chatMap.put("Chat/" + mChatUser + "/" + mCurrentUser,chatAddMap);

                    mRootRef.updateChildren(chatMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                            if(databaseError != null){
                                Log.d("Chat Log", databaseError.getMessage().toString());
                            }


                        }
                    });

                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        //---------------OnClick method of Text Message send Button --------------
        mTextSendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                sendMessages();
            }
        });





    }

    //----------------method to load message to recyler view ------------
    private void loadMessage() {

        mRootRef.child("messages").child(mCurrentUser).child(mChatUser).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Messages  message = dataSnapshot.getValue(Messages.class);
                messagesList.add(message);
                mAdapter.notifyDataSetChanged();

                mMessageList.scrollToPosition(messagesList.size()-1);
                mTextMesssage.setText("");

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    //---------------text Message send Button -------------

    private void sendMessages() {
        final String textMessage = mTextMesssage.getText().toString().trim();

        if(!TextUtils.isEmpty(textMessage)){

            String current_user_ref = "messages/" + mCurrentUser + "/" + mChatUser;
            String chat_user_ref ="messages/" + mChatUser + "/" + mCurrentUser;

            DatabaseReference user_message_push = mRootRef.child("messages").child(mCurrentUser).child(mChatUser).push();
            String push_id = user_message_push.getKey();

            Map addMessageMap = new HashMap();
            addMessageMap.put("message",textMessage);
            addMessageMap.put("seen",false);
            addMessageMap.put("type","text");
            addMessageMap.put("timestamp",ServerValue.TIMESTAMP);
            addMessageMap.put("from",mCurrentUser);

            Map userMessageMap = new HashMap();
            userMessageMap.put(current_user_ref + "/" + push_id, addMessageMap);
            userMessageMap.put(chat_user_ref + "/" +push_id, addMessageMap);

            mRootRef.updateChildren(userMessageMap, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                    if(databaseError != null){
                        Log.d("Chat Log", databaseError.getMessage().toString());
                    }else{
                        mTextMesssage.setText("");
                    }
                }
            });


         }

    }
}
