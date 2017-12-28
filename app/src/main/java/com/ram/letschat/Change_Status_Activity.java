package com.ram.letschat;

import android.app.ProgressDialog;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


public class Change_Status_Activity extends AppCompatActivity {

    private Toolbar mToolbar;
    private Button mSaveButton;
    private EditText mStatus;

    //Firebase referances
    private DatabaseReference mDatabaseReference;
    private FirebaseUser mCurrentUser;

    //progress
    private ProgressDialog mProgress;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change__status_);

        mToolbar = (Toolbar) findViewById(R.id.change_status_appbar);
        setSupportActionBar(mToolbar);

        getSupportActionBar().setTitle("Account Status");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mStatus = (EditText) findViewById(R.id.status_change_id);
        mSaveButton = (Button) findViewById(R.id.change_status_btn);

        String status_value = getIntent().getStringExtra("status_value");
        mStatus.setText(status_value);

        //Firebase
        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
        String current_uid = mCurrentUser.getUid();

        mDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Users").child(current_uid);




        mSaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Progress
                mProgress = new ProgressDialog(Change_Status_Activity.this);
                mProgress.setTitle("Saving Changes");
                mProgress.setMessage("Please Wait...");
                mProgress.show();

                String status = mStatus.getText().toString();

                mDatabaseReference.child("status").setValue(status).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        if(task.isSuccessful())
                        {
                            mProgress.dismiss();
                            Toast.makeText(Change_Status_Activity.this,"Status Updated",Toast.LENGTH_SHORT).show();
                        }
                        else
                        {
                            Toast.makeText(getApplicationContext(),"Something went wrong! Try again..,",Toast.LENGTH_SHORT).show();

                        }
                    }
                });
            }
        });
    }
}
