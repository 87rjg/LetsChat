package com.ram.letschat;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {

    private Toolbar mToolbar;

    private EditText mEmail,mPaaword;
    private Button mLoginBtn;
    private FirebaseAuth mAuth;
    private ProgressDialog mProgress;
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

        mProgress = new ProgressDialog(this);
        mProgress.setMessage("Please Wait...");
        mProgress.setCanceledOnTouchOutside(false);

        mAuth = FirebaseAuth.getInstance();

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

    }

    private void login(String email, String pass) {

        mAuth.signInWithEmailAndPassword(email, pass).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {

                if(task.isSuccessful())
                {
                    mProgress.dismiss();
                    Intent intent = (new Intent(LoginActivity.this,MainActivity.class));
                    intent.addFlags(intent.FLAG_ACTIVITY_NEW_TASK | intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();

                }
                else{
                    mProgress.hide();
                    Toast.makeText(LoginActivity.this,"Login failed..Check your Details",Toast.LENGTH_SHORT).show();
                }

            }
        });
    }
}
