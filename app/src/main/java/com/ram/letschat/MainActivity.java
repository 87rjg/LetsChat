package com.ram.letschat;

import android.content.Intent;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TableLayout;


import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private DatabaseReference mUserRef;
    private Toolbar mToolbar;
    private ViewPager mViewpager;

    private SectionPagerAdapter mSection;
    private TabLayout mTablayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();


        mToolbar = (Toolbar) findViewById(R.id.main_page_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Let's Chat");

        mViewpager = (ViewPager) findViewById(R.id.tab_pager);
        mSection = new SectionPagerAdapter(getSupportFragmentManager());

        mViewpager.setAdapter(mSection);
        mTablayout = (TabLayout) findViewById(R.id.main_tabs);
        mTablayout.setupWithViewPager(mViewpager);
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if(currentUser == null)
        {
            sendTostart();

        }else{
            mUserRef = FirebaseDatabase.getInstance().getReference().child("Users").child(mAuth.getCurrentUser().getUid());
            mUserRef.child("online").setValue(true);

        }

    }

    @Override
    protected void onStop() {
        super.onStop();
       /* FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser !=null)
        mUserRef.child("online").setValue(false);*/
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser !=null)
            mUserRef.child("online").setValue(false);

    }

    private void sendTostart() {
        Intent intent = new Intent(MainActivity.this,StartActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
         super.onCreateOptionsMenu(menu);


         getMenuInflater().inflate(R.menu.main_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
         super.onOptionsItemSelected(item);

         switch(item.getItemId())
         {
             case R.id.user_logout:

                 FirebaseUser currentUser = mAuth.getCurrentUser();
                 if(currentUser !=null)
                     mUserRef.child("online").setValue(false);
                 FirebaseAuth.getInstance().signOut();
                 sendTostart();
                 return true;
             case R.id.setting_btn:
                 Intent intent = new Intent(MainActivity.this,SettingActivity.class);
                 startActivity(intent);
                 return true;
             case R.id.user_btn:
                 Intent userIntent = new Intent(MainActivity.this,AllUsersActivity.class);
                 startActivity(userIntent);
                 return true;
             default:
                 return false;


         }

    }
}
