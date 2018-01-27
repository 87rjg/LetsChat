package com.ram.letschat;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class FriendsFragment extends Fragment {


    private RecyclerView mListView;
    private DatabaseReference mFriendsDatabase;
    private DatabaseReference mUsersDatabase;
    private FirebaseAuth mAuth;
    private View mView;
    private FirebaseRecyclerAdapter<Friends,ViewHolder> firebaseRecyclerAdapter;
    public FriendsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mView =  inflater.inflate(R.layout.fragment_friends, container, false);


        mListView = (RecyclerView) mView.findViewById(R.id.friends_fragmnet_id);

        mAuth = FirebaseAuth.getInstance();

        String current_user_id = mAuth.getCurrentUser().getUid();
        mFriendsDatabase = FirebaseDatabase.getInstance().getReference().child("friends").child(current_user_id);
        mFriendsDatabase.keepSynced(true);
        mUsersDatabase = FirebaseDatabase.getInstance().getReference().child("Users");
        mUsersDatabase.keepSynced(true);
        mListView.setHasFixedSize(true);
        mListView.setLayoutManager(new LinearLayoutManager(getContext()));


        Query query = mFriendsDatabase.orderByChild("date");

        FirebaseRecyclerOptions options = new FirebaseRecyclerOptions.Builder<Friends>().setQuery(query,Friends.class).build();

        firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Friends, ViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final ViewHolder holder, int position, @NonNull Friends model) {
                holder.setDate(model.getDate());

                final String user_list_id = getRef(position).getKey();

                mUsersDatabase.child(user_list_id).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        final String userName = dataSnapshot.child("name").getValue().toString();
                        final String user_image = dataSnapshot.child("thumb_image").getValue().toString();

                        holder.setName(userName);
                        holder.setImage(user_image,container.getContext());

                        if(dataSnapshot.hasChild("online")){
                            Boolean online_status =(Boolean) dataSnapshot.child("online").getValue();
                            holder.setOnlineSatus(online_status);

                        }

                        holder.view.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                CharSequence AlertOption[] = new CharSequence[]{"View Profile", "Send Message"};

                                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                                builder.setTitle("Select Option");
                                builder.setItems(AlertOption, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {

                                        if(i == 0){

                                            Intent profile_intent = new Intent(container.getContext(),ProfileActivity.class);
                                            profile_intent.putExtra("user_id",user_list_id);

                                            startActivity(profile_intent);

                                        }
                                        if(i==1){
                                            Intent chatIntent = new Intent(container.getContext(),ChatActivity.class);
                                            chatIntent.putExtra("user_id",user_list_id);
                                            chatIntent.putExtra("user_name",userName);
                                            chatIntent.putExtra("user_image",user_image);
                                            startActivity(chatIntent);
                                        }

                                    }
                                });
                                builder.show();

                            }
                        });
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

            }



            @Override
            public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.single_user_layout,parent,false);
                return new ViewHolder(view);
            }
        };

        mListView.setAdapter(firebaseRecyclerAdapter);

        return  mView;
    }

    @Override
    public void onStart() {
        super.onStart();
        firebaseRecyclerAdapter.startListening();

    }

    @Override
    public void onStop() {
        super.onStop();
        firebaseRecyclerAdapter.stopListening();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{
        View view;

        public ViewHolder(View itemView) {
            super(itemView);
            view = itemView;
        }

        public void setDate(String date) {
            TextView friendshipDate = (TextView) view.findViewById(R.id.single_user_status);
            friendshipDate.setText(date);
        }

        public void setName(String userName) {
            TextView name = (TextView) view.findViewById(R.id.single_user_name);
            name.setText(userName);
        }

        public void setImage(String user_image, Context context) {
            CircleImageView image = (CircleImageView) view.findViewById(R.id.single_user_image);

            Picasso.with(context).load(user_image).placeholder(R.drawable.user1).into(image);
        }

        public void setOnlineSatus(Boolean online_status) {
            ImageView onlineImage = (ImageView) view.findViewById(R.id.sinlge_online_icon);

            if(online_status == true){
                onlineImage.setVisibility(View.VISIBLE);
            }else{
                onlineImage.setVisibility(View.INVISIBLE);
            }
        }
    }
}
