package com.ram.letschat;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import id.zelory.compressor.Compressor;

public class SettingActivity extends AppCompatActivity {

   //Firebase
    private StorageReference mStorageReference;
    private DatabaseReference mUserDatabase;
    private FirebaseUser mCurrentUser;
    private FirebaseAuth mAuth;
    private DatabaseReference mUserRef;
    //private FirebaseUser mUsers;

    private CircleImageView mImageView;
    private TextView mUsername,mUserStatus;
    private Button mChangeImage,mChangeStatus;
    private final static int GALLARY_PIC = 1;
    private String current_user;
    private ProgressDialog mProgress;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        //toolbar = (Toolbar) findViewById(R.id.setting_appbar);
       // getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mImageView = (CircleImageView) findViewById(R.id.ImageView_image);
        mUsername = (TextView) findViewById(R.id.user_name);
        mUserStatus= (TextView) findViewById(R.id.user_status);
        mChangeImage = (Button) findViewById(R.id.change_image);
        mChangeStatus = (Button) findViewById(R.id.change_status);

        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
         current_user = mCurrentUser.getUid();

        mStorageReference = FirebaseStorage.getInstance().getReference();
        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(current_user);
        mUserDatabase.keepSynced(true);


        mUserDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String name = dataSnapshot.child("name").getValue().toString();
                final String image = dataSnapshot.child("image").getValue().toString();
                String status = dataSnapshot.child("status").getValue().toString();
                String thumb_image = dataSnapshot.child("thumb_image").getValue().toString();

                mUsername.setText(name);
                mUserStatus.setText(status);

                if(!image.equals("default"))
                {
                    //Picasso.with(SettingActivity.this).load(image).placeholder(R.drawable.user ).into(mImageView);

                    Picasso.with(SettingActivity.this).load(image).networkPolicy(NetworkPolicy.OFFLINE)
                            .placeholder(R.drawable.default_user_image ).into(mImageView, new Callback() {
                        @Override
                        public void onSuccess() {

                        }

                        @Override
                        public void onError() {

                            Picasso.with(SettingActivity.this).load(image).placeholder(R.drawable.default_user_image ).into(mImageView);
                        }
                    });
                }


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mChangeStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String sendStatus = mUserStatus.getText().toString();
                Intent changeStatus = new Intent(SettingActivity.this,Change_Status_Activity.class);

                changeStatus.putExtra("status_value",sendStatus);
                startActivity(changeStatus);
            }
        });

        mChangeImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent galleryIntent = new Intent();
                galleryIntent.setType("image/*");
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(galleryIntent,"Select Image"),GALLARY_PIC);

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == GALLARY_PIC && resultCode == RESULT_OK)
        {
            Uri imageUri = data.getData();

            CropImage.activity(imageUri)
                    .setAspectRatio(1,1)
                    .start(this);

        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {

                mProgress = new ProgressDialog(this);
                mProgress.setTitle("Uploading Image");
                mProgress.setMessage("Please Wait...");
                mProgress.setCanceledOnTouchOutside(false);
                mProgress.show();

                Uri resultUri = result.getUri();

                final File thumb_filePath =  new File(resultUri.getPath());


                Bitmap thumb_bitmap = null;
                try {
                    thumb_bitmap = new Compressor(this)
                            .setMaxWidth(200).setMaxHeight(200).setQuality(75).compressToBitmap(thumb_filePath);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    thumb_bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                    final byte[] thumb_byte = baos.toByteArray();



                StorageReference  filepath = mStorageReference.child("userProfile_images").child(current_user+".jpg");
                final StorageReference thumb_filepath = mStorageReference.child("userProfile_images").child("userThumb").child(current_user+".jpg");


                filepath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                        if(task.isSuccessful())
                        {
                            final String download_uri = task.getResult().getDownloadUrl().toString();

                            UploadTask uploadTask = thumb_filepath.putBytes(thumb_byte);
                            uploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> thumb_task) {

                                    String thumb_download_url = thumb_task.getResult().getDownloadUrl().toString();
                                    if(thumb_task.isSuccessful())
                                    {
                                        Map update_map = new HashMap();
                                        update_map.put("image",download_uri);
                                        update_map.put("thumb_image",thumb_download_url);
                                        mUserDatabase.updateChildren(update_map).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if(task.isSuccessful())
                                                {
                                                    Toast.makeText(SettingActivity.this,"Image Saaved Succesfully",Toast.LENGTH_SHORT).show();
                                                    mProgress.dismiss();
                                                }
                                            }
                                        });
                                    }else{
                                        Toast.makeText(SettingActivity.this,"Error while uploading thumb image!",Toast.LENGTH_SHORT).show();
                                        mProgress.dismiss();
                                    }
                                }
                            });

                        }else{
                            Toast.makeText(SettingActivity.this,"Error while uploading user image!",Toast.LENGTH_SHORT).show();
                            mProgress.dismiss();
                        }
                    }
                });

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }


    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if(currentUser != null){
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid());
            ref.child("online").setValue("true");
        }
    }
}
