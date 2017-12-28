package com.ram.letschat;

import android.app.ProgressDialog;
import android.content.Intent;
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
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class RegisterActivity extends AppCompatActivity {

    private EditText mName;
    private EditText mEmail;
    private EditText mPassword;
    private Button mRegisterButton;

    private Toolbar mToolbar;

    private FirebaseAuth mAuth;
    private DatabaseReference mReference;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mName = (EditText) findViewById(R.id.reg_name);
        mEmail = (EditText) findViewById(R.id.reg_email);
        mPassword = (EditText) findViewById( R.id.reg_password);

        mRegisterButton = (Button) findViewById(R.id.registerBtn);

        progressDialog = new ProgressDialog(this);

        //Toolbar
        mToolbar = (Toolbar) findViewById(R.id.register_appbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Register");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mAuth = FirebaseAuth.getInstance();

        mRegisterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String name = mName.getText().toString().trim();
                String email = mEmail.getText().toString().trim();
                String pass = mPassword.getText().toString().trim();

                progressDialog.setTitle("Registering user");
                progressDialog.setMessage("Please Wait...");
                progressDialog.setCanceledOnTouchOutside(false);
                progressDialog.show();
                register_User(name,email,pass);
            }
        });
    }

    private void register_User(final String name, String email, String pass) {

        mAuth.createUserWithEmailAndPassword(email,pass).addOnCompleteListener(
                this,new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {

                if(task.isSuccessful())
                {

                    FirebaseUser current_user = FirebaseAuth.getInstance().getCurrentUser();
                    String uid = current_user.getUid();
                    mReference = FirebaseDatabase.getInstance().getReference().child("Users").child(uid);

                    HashMap<String,String> userMap = new HashMap<>();
                    userMap.put("name",name);
                    userMap.put("status","Hi there I'm using Let's Chat");
                    userMap.put("image","default");
                    userMap.put("thumb_image","default");
                    mReference.setValue(userMap);

                    progressDialog.dismiss();


                    Intent mainIntent = new Intent(RegisterActivity.this,MainActivity.class);
                    startActivity(mainIntent);
                    finish();
                }else {
                    progressDialog.hide();
                    Toast.makeText(RegisterActivity.this,"Something Went wrong..Please Check",Toast.LENGTH_SHORT).show();
                }
            }
        });

    }
}
