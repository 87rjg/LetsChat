package com.ram.letschat;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

public class LoginActivity extends AppCompatActivity {

    private Toolbar mToolbar;

    private EditText mEmail,mPaaword;
    private Button mLoginBtn;
    private TextView mForgotPass;
    private FirebaseAuth mAuth;
    private ProgressDialog mProgress;
    private DatabaseReference mUserDatabase;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);


        mToolbar = (Toolbar) findViewById(R.id.login_appbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Login");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mEmail = (EditText) findViewById(R.id.log1_email);
        mPaaword = (EditText) findViewById(R.id.log_password);
        mLoginBtn =(Button) findViewById(R.id.login_btn);
        mForgotPass =(TextView) findViewById(R.id.forgot_pass);

        mProgress = new ProgressDialog(this);
        mProgress.setMessage("Please Wait...");
        mProgress.setCanceledOnTouchOutside(false);

        mAuth = FirebaseAuth.getInstance();
        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users");

        //----------Click listner to login user-------------
        
       mLoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String email = mEmail.getText().toString().trim();
                String pass = mPaaword.getText().toString().trim();

                if(!TextUtils.isEmpty(email) && !TextUtils.isEmpty(pass))
                {

                    login(email,pass);
                    mProgress.show();
                }
                else
                {
                    Toast.makeText(LoginActivity.this,"Email and Password should not be empty",Toast.LENGTH_SHORT).show();
                    mProgress.hide();
                }

            }
        });

       //-----------Click Listner to reset password-------------

        mForgotPass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                resetPassword();
            }
        });



    }

    //---------------Method to login Users----------------
    private void login(String email, String pass) {

        mAuth.signInWithEmailAndPassword(email, pass).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {

                if(task.isSuccessful())
                {
                    mProgress.dismiss();
                    String user_id = mAuth.getCurrentUser().getUid();

                    String device_token = FirebaseInstanceId.getInstance().getToken();

                    mUserDatabase.child(user_id).child("device_token").setValue(device_token).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {

                            Intent intent = (new Intent(LoginActivity.this,MainActivity.class));
                            intent.addFlags(intent.FLAG_ACTIVITY_NEW_TASK | intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            finish();
                        }
                    });

                }
                else{
                    mProgress.hide();
                    Toast.makeText(LoginActivity.this,"Login failed..Check your Details",Toast.LENGTH_SHORT).show();
                }

            }
        });


    }


    //-------------Method to reset password ---------------------
    private void resetPassword() {
        LayoutInflater inflater = LayoutInflater.from(this);
        View alertPrompt = inflater.inflate(R.layout.alert_prompt,null);

        final AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);

        alertBuilder.setView(alertPrompt);
        final EditText userInput = (EditText) alertPrompt
                .findViewById(R.id.email_box);
        // alertBuilder.setTitle("Enter your email");

        alertBuilder.setCancelable(false).setPositiveButton("OK",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        String emailAddress =  userInput.getText().toString();
                        if(!TextUtils.isEmpty(emailAddress))
                        {
                            mAuth.sendPasswordResetEmail(emailAddress)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                Toast.makeText(LoginActivity.this, "Reset Link set to your email", Toast.LENGTH_SHORT).show();

                                                //AlertDialog alert = new AlertDialog(this);
                                                // Log.d(TAG, "Email sent.");
                                            }else{

                                                Toast.makeText(LoginActivity.this, "Check your email", Toast.LENGTH_SHORT).show();
                                            }

                                        }
                                    });
                        }else{
                            Toast.makeText(LoginActivity.this, "Enter your email id", Toast.LENGTH_SHORT).show();
                        }




                    }
                }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                dialogInterface.cancel();
            }
        });

        AlertDialog alertDialog = alertBuilder.create();
        alertDialog.show();

    }


}
