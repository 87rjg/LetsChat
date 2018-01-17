package com.ram.letschat;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class AllUsersActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private RecyclerView mListView;
    private DatabaseReference mUserdatabase;
    private FirebaseRecyclerAdapter<Users,UserViewHolder> firebaseRecyclerAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_users);



        mToolbar = (Toolbar) findViewById(R.id.user_appbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("All Users");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mListView = (RecyclerView) findViewById(R.id.userlist_view);
        mListView.setHasFixedSize(true);
        mListView.setLayoutManager(new LinearLayoutManager(this));

        mUserdatabase = FirebaseDatabase.getInstance().getReference().child("Users");
        Query personsQuery = mUserdatabase.orderByChild("name");
                //Firebase UI adapter to populate RecyclerView

        FirebaseRecyclerOptions options = new FirebaseRecyclerOptions.Builder<Users>().setQuery(personsQuery, Users.class).build();

         firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Users, UserViewHolder>(options) {


            @Override
            public UserViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.single_user_layout, parent, false);

                return new UserViewHolder(view);

            }

            @Override
            protected void onBindViewHolder(@NonNull UserViewHolder holder, int position, @NonNull Users model) {

                holder.setName(model.getName());
                holder.setSatus(model.getStatus());
                holder.setUserImage(model.getThumb_image(),getApplicationContext());

                final String user_id = getRef(position).getKey();

                holder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        Intent profile_intent = new Intent(AllUsersActivity.this,ProfileActivity.class);
                        profile_intent.putExtra("user_id",user_id);

                        startActivity(profile_intent);

                    }
                });
            }

        };
        mListView.setAdapter(firebaseRecyclerAdapter);


    }

    @Override
    protected void onStart() {
        super.onStart();

        firebaseRecyclerAdapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();

        firebaseRecyclerAdapter.stopListening();
    }

    public static class UserViewHolder extends RecyclerView.ViewHolder{

         View mView;
        public UserViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
        }

        public  void setName(String name)
        {

            TextView userName = (TextView) mView.findViewById(R.id.single_user_name);
            userName.setText(name);
        }
        public void setSatus(String status)
        {
            TextView userStatus = (TextView) mView.findViewById(R.id.single_user_status);
            userStatus.setText(status);
        }
        public void setUserImage(String thumb_image, Context context)
        {

            CircleImageView userimageView = (CircleImageView) mView.findViewById(R.id.single_user_image);
            Picasso.with(context).load(thumb_image).placeholder(R.drawable.user1).into(userimageView);

        }
    }
}
