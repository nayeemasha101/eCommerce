package com.example.md_nayeem_asha.ecommerce;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.md_nayeem_asha.ecommerce.Prevalent.Prevalent;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class SettingsActivity extends AppCompatActivity
{

    private CircleImageView profileImageView;
    Button close, update, changeProfile;
    private EditText phoneSetting, fullNameSetting,addressSetting;

    private Uri imageUri;
    private String myUrl;
    StorageTask uploadTask;
    private StorageReference storageProfileReference;
    private String checker = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        storageProfileReference = FirebaseStorage.getInstance().getReference().child("Profile pictures");

        close = findViewById(R.id.cancel_account_settings);
        profileImageView = findViewById(R.id.settings_profile_image);
        update = findViewById(R.id.update_account_settings);
        changeProfile = findViewById(R.id.profile_image_change_btn);
        phoneSetting = findViewById(R.id.settings_phone_number);
        fullNameSetting = findViewById(R.id.settings_full_name);
        addressSetting = findViewById(R.id.settings_address);
        
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                finish();
                Toast.makeText(SettingsActivity.this, "We can't update your profile status successfully....!", Toast.LENGTH_SHORT).show();
            }
        });

        update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                if (checker.equals("clicked"))
                {
                    userInfoSave();
                }
                else
                {
                    updateOnlyUserInfo();
                }
            }
        });

        changeProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                checker = "clicked";

                CropImage.activity(imageUri)
                        .setAspectRatio(1,1)
                        .start(SettingsActivity.this);
            }
        });

        usreInfoDisplay(profileImageView, fullNameSetting, phoneSetting, addressSetting);
    }

    private void updateOnlyUserInfo()
    {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("Users");

        HashMap<String, Object> userMap = new HashMap<>();
        userMap.put("name", fullNameSetting.getText().toString());
        userMap.put("address", addressSetting.getText().toString());
        userMap.put("phoneOrder", phoneSetting.getText().toString());

        ref.child(Prevalent.currentOnlineUser.getPhone()).updateChildren(userMap);

        startActivity(new Intent(SettingsActivity.this, HomeActivity.class));
        Toast.makeText(SettingsActivity.this, "profile information update successfully..!", Toast.LENGTH_SHORT).show();
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK && data != null )
        {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            imageUri = result.getUri();

            profileImageView.setImageURI(imageUri);
        }
        else
        {
            Toast.makeText(this, "Error, try again", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(SettingsActivity.this, SettingsActivity.class));
            finish();
        }
    }

    private void userInfoSave()
    {
        if (TextUtils.isEmpty(fullNameSetting.getText().toString()))
        {
            Toast.makeText(this, "Name is mandatory...!", Toast.LENGTH_SHORT).show();
        }
        else if (TextUtils.isEmpty(addressSetting.getText().toString()))
        {
            Toast.makeText(this, "Address is mandatory...!", Toast.LENGTH_SHORT).show();
        }
        else if (TextUtils.isEmpty(phoneSetting.getText().toString()))
        {
            Toast.makeText(this, "Phone Number is mandatory...!", Toast.LENGTH_SHORT).show();
        }
        else if (checker.equals("clicked"))
        {
            uploadImage();
        }
    }

    private void uploadImage()
    {
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("update Image");
        progressDialog.setMessage("Please wait, while we are uploading your account information..!!");
        progressDialog.setCancelable(false);
        progressDialog.show();

        if (imageUri != null)
        {
            final StorageReference fileRef = storageProfileReference
                    .child(Prevalent.currentOnlineUser.getPhone() + ".jpg");
            uploadTask = fileRef.putFile(imageUri);

            uploadTask.continueWithTask(new Continuation() {
                @Override
                public Object then(@NonNull Task task) throws Exception
                {

                    if (!task.isSuccessful())
                    {
                        throw task.getException();
                    }
                    return fileRef.getDownloadUrl();
                }
            })
            .addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful())
                    {
                        Uri downloadUrl = task.getResult();

                        myUrl = downloadUrl.toString();

                        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("Users");

                        HashMap<String, Object> userMap = new HashMap<>();
                        userMap.put("name", fullNameSetting.getText().toString());
                        userMap.put("address", addressSetting.getText().toString());
                        userMap.put("phoneOrder", phoneSetting.getText().toString());
                        userMap.put("image", myUrl);

                        ref.child(Prevalent.currentOnlineUser.getPhone()).updateChildren(userMap);

                        progressDialog.dismiss();

                        startActivity(new Intent(SettingsActivity.this, HomeActivity.class));
                        Toast.makeText(SettingsActivity.this, "profile information update successfully..!", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                    else
                    {
                        progressDialog.dismiss();
                        Toast.makeText(SettingsActivity.this, "Error....", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
        else
        {
            Toast.makeText(this, "Image is not Selected...!", Toast.LENGTH_SHORT).show();
        }
    }

    private void usreInfoDisplay(final CircleImageView profileImageView, final EditText fullNameSetting, final EditText phoneSetting, final EditText addressSetting)
    {
        DatabaseReference UserRef = FirebaseDatabase.getInstance().getReference().child("Users").child(Prevalent.currentOnlineUser.getPhone());

        UserRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                if (dataSnapshot.exists())
                {
                    if (dataSnapshot.child("image").exists())
                    {
                        String image = dataSnapshot.child("image").getValue().toString();
                        String name = dataSnapshot.child("name").getValue().toString();
                        String phone = dataSnapshot.child("phone").getValue().toString();
                        String address = dataSnapshot.child("address").getValue().toString();

                        Picasso.get().load(image).into(profileImageView);

                        fullNameSetting.setText(name);
                        phoneSetting.setText(phone);
                        addressSetting.setText(address);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError)
            {

            }
        });
    }
}
