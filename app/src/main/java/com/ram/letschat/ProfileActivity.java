package com.ram.letschat;

import android.app.ProgressDialog;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class ProfileActivity extends AppCompatActivity {

    private ImageView mUserImage;
    private TextView display_name,mUserStatus;
    private Button mRequestSendBtn,mDeclineRequest;
    private ProgressDialog mProgress;
    private String mCurrentState_type;

    //Firebase
    private DatabaseReference mDatabaseReference;
    private DatabaseReference mFriendReqDatabase;
    private DatabaseReference mFriendsDatabase;
    private DatabaseReference mRootref;
    private DatabaseReference mUserdatabaseRef;
    private FirebaseUser  mCurrent_user;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        mUserImage = (ImageView) findViewById(R.id.user_profile_image);
        display_name = (TextView) findViewById(R.id.display_name);
        mUserStatus = (TextView) findViewById(R.id.user_status);
        mRequestSendBtn = (Button) findViewById(R.id.request_button);
        mDeclineRequest = (Button) findViewById(R.id.decline_button);
        mDeclineRequest.setVisibility(View.INVISIBLE);
        mDeclineRequest.setEnabled(false);

        mCurrentState_type = "not_friends";
;        //Progress Dialog
        mProgress = new ProgressDialog(this);
        mProgress.setTitle("Loading Profile");
        mProgress.setMessage("Please Wait...");
        mProgress.setCanceledOnTouchOutside(false);
        mProgress.show();



        final String user_id = getIntent().getStringExtra("user_id");

        //Firebase
        mDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Users").child(user_id);
        mFriendReqDatabase = FirebaseDatabase.getInstance().getReference().child("friend_request");
        mFriendsDatabase = FirebaseDatabase.getInstance().getReference().child("friends");

        mRootref = FirebaseDatabase.getInstance().getReference();
        mCurrent_user = FirebaseAuth.getInstance().getCurrentUser();

        ///---------------To Load User Profile---------------------
        mDatabaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mProgress.show();
                String name = dataSnapshot.child("name").getValue().toString();
                String status = dataSnapshot.child("status").getValue().toString();
                String image = dataSnapshot.child("thumb_image").getValue().toString();

                display_name.setText(name);
                mUserStatus.setText(status);

                Picasso.with(ProfileActivity.this).load(image).placeholder(R.drawable.default_user_image).into(mUserImage);

                //-------------------------Friend Request Accept / Decline ---------------------

                mFriendReqDatabase.child(mCurrent_user.getUid()).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        if(dataSnapshot.hasChild(user_id))
                        {
                            String req_type = dataSnapshot.child(user_id).child("request_type").getValue().toString();
                            if(req_type.equals("recieved"))
                            {
                                mCurrentState_type = "request_recieved";
                                mRequestSendBtn.setText("Accept Friend Request");

                                mDeclineRequest.setVisibility(View.VISIBLE);
                                mDeclineRequest.setEnabled(true);

                            }else {
                                mCurrentState_type = "request_sent";
                                mRequestSendBtn.setText("Cancel Friend Request");
                               // mDeclineRequest.setVisibility(View.INVISIBLE);
                               // mDeclineRequest.setEnabled(false);
                            }
                            mProgress.dismiss();
                        }
                        else {

                            mFriendsDatabase.child(mCurrent_user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    if(dataSnapshot.hasChild(user_id)){
                                        mCurrentState_type = "friends";
                                        mRequestSendBtn.setText("UnFriend");

                                        //mDeclineRequest.setVisibility(View.INVISIBLE);
                                       // mDeclineRequest.setEnabled(false);
                                    }
                                    mProgress.dismiss();
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                    mProgress.dismiss();
                                }
                            });
                        }
                        mProgress.dismiss();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                        mProgress.dismiss();
                    }
                });

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }

        });

        //------------------Send request/Accept request ----------------

        mRequestSendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mRequestSendBtn.setEnabled(false);

                //---------------------------NOT Friends state -------------------------
                if(mCurrentState_type.equals("not_friends"))
                {
                    DatabaseReference newNotifiaction = mRootref.child("notifications").child(user_id).push();
                    String notificationId = newNotifiaction.getKey();
                    HashMap<String, String> notificationData = new HashMap<>();
                    notificationData.put("from", mCurrent_user.getUid());
                    notificationData.put("type","request");

                    Map requestMap = new HashMap();
                    requestMap.put("friend_request/" + mCurrent_user.getUid()+ "/" + user_id + "/request_type", "sent");
                    requestMap.put("friend_request/" + user_id + "/" +mCurrent_user.getUid()+ "/request_type","recieved");
                    requestMap.put("notifications/" + user_id + "/" + notificationId, notificationData);

                    mRootref.updateChildren(requestMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                            if(databaseError != null)
                            {
                                Toast.makeText(ProfileActivity.this,"Something went wrong !",Toast.LENGTH_SHORT).show();
                            }
                            else{
                                mRequestSendBtn.setEnabled(true);
                                mCurrentState_type = "request_sent";
                                mRequestSendBtn.setText("Cancel Friend Request");

                                Toast.makeText(ProfileActivity.this,"Request has been sent successfully",Toast.LENGTH_SHORT).show();
                            }


                        }
                    });


                }

                //---------------------request_sent state or Cancel Friend Request---------------------
                if(mCurrentState_type == "request_sent")
                {

                    Map cancelRequest = new HashMap();
                    cancelRequest.put(mCurrent_user.getUid() + "/" + user_id,null);
                    cancelRequest.put(user_id + "/" + mCurrent_user.getUid(), null);

                    mFriendReqDatabase.updateChildren(cancelRequest, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                            mRequestSendBtn.setEnabled(true);
                            mCurrentState_type = "not_friends";
                            mRequestSendBtn.setText("Send Friend Request");

                           // mDeclineRequest.setVisibility(View.INVISIBLE);
                          //  mDeclineRequest.setEnabled(false);
                        }
                    });


                }
                //-----------Accept Friend Request----------
               if(mCurrentState_type.equals("request_recieved"))
                {
                   // mDeclineRequest.setVisibility(View.VISIBLE);
                   // mDeclineRequest.setEnabled(true);
                    final String date = DateFormat.getDateTimeInstance().format(new Date());

                    Map acceptRequest = new HashMap();
                    acceptRequest.put("friends/" + mCurrent_user.getUid()+ "/" + user_id + "/date", date);
                    acceptRequest.put("friends/" + user_id+ "/" + mCurrent_user.getUid() + "/date",date);


                    acceptRequest.put( "friend_request/" + mCurrent_user.getUid()+ "/" +user_id,null);
                    acceptRequest.put("friend_request/" +user_id+ "/" +mCurrent_user.getUid(),null);

                    mRootref.updateChildren(acceptRequest, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                            mRequestSendBtn.setEnabled(true);
                            mCurrentState_type = "friends";
                            mRequestSendBtn.setText("UnFriend");

                            mDeclineRequest.setVisibility(View.INVISIBLE);

                        }
                    });
                }

                //----------------------Un-friend User ----------
                if(mCurrentState_type.equals("friends"))
                {


                    Map unFriend  = new HashMap();

                    unFriend.put("friends/" + mCurrent_user.getUid() + "/" +user_id,null);
                    unFriend.put("friends/" + user_id + "/" + mCurrent_user.getUid(),null);

                    mRootref.updateChildren(unFriend, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                            mRequestSendBtn.setEnabled(true);
                            mCurrentState_type = "not_friends";
                            mRequestSendBtn.setText("Send Friend Request");


                        }
                    });

                }

            }
        });
    }


}
