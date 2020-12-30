package com.store.inventory.activities;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.store.inventory.R;
import com.store.inventory.utils.HelperClass;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.PushbackInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class SetProfileData extends AppCompatActivity {
    private EditText name;
    private CircleImageView imageView;
    private DatabaseReference mDatabase;
    String phone;
    private StorageReference storageRef;
    private Uri outputFileUri;

    String[] PERMISSIONS = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA
    };
    private Uri mImageUri =null;
    private FirebaseAuth mAuth;
    private FirebaseUser mCurrentUser;
    private Button buy;
    private Button sell;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_profile_data);
        mDatabase = FirebaseDatabase.getInstance().getReference();
        storageRef = FirebaseStorage.getInstance().getReference();

        mAuth = FirebaseAuth.getInstance();
        mCurrentUser = mAuth.getCurrentUser();

        Intent intent = getIntent();
        phone = (String) intent.getSerializableExtra("phone");
        name = (EditText) findViewById(R.id.name);
        imageView = (CircleImageView) findViewById(R.id.photo);
        buy = findViewById(R.id.buy);
        sell = findViewById(R.id.sell);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!HelperClass.hasPermissions(getApplicationContext(), PERMISSIONS)) {
                    ActivityCompat.requestPermissions(SetProfileData.this, PERMISSIONS, 1);
                }
                else openImageIntent();
            }
        });
        buy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                next(true);
            }
        });
        sell.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                next(false);
            }
        });
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 1:
                if (HelperClass.hasPermissions(getApplicationContext(), PERMISSIONS)) {
                    openImageIntent();
                }
                else Toast.makeText(this, "You need to give permission!", Toast.LENGTH_SHORT).show();
        }
    }


    private void openImageIntent() {

        final File root = new File(getApplicationContext().getExternalCacheDir() + File.separator + "InventoryDir" + File.separator);
        root.mkdirs();
        final String fname = "img_"+ System.currentTimeMillis() + ".jpg";
        final File sdImageMainDirectory = new File(root, fname);
        outputFileUri = Uri.fromFile(sdImageMainDirectory);

        // Camera.
        final List<Intent> cameraIntents = new ArrayList<Intent>();
        final Intent captureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE_SECURE);
        final PackageManager packageManager = getPackageManager();
        final List<ResolveInfo> listCam = packageManager.queryIntentActivities(captureIntent, 0);
        for (ResolveInfo res : listCam) {
            final String packageName = res.activityInfo.packageName;
            final Intent intent = new Intent(captureIntent);
            intent.setComponent(new ComponentName(packageName, res.activityInfo.name));
            intent.setPackage(packageName);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);
            cameraIntents.add(intent);
        }

        final Intent galleryIntent = new Intent();
        galleryIntent.setType("image/*");
        galleryIntent.setAction(Intent.ACTION_PICK);

        // Chooser of filesystem options.
        final Intent chooserIntent = Intent.createChooser(galleryIntent, "Select Source");

        // Add the camera options.
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, cameraIntents.toArray(new Parcelable[cameraIntents.size()]));

        startActivityForResult(chooserIntent, 1);
    }

    private void next(final Boolean b) {
        BitmapDrawable drawable = (BitmapDrawable) imageView.getDrawable();
        if(drawable == null) {
            Toast.makeText(this, "Must attach a Profile Photo", Toast.LENGTH_SHORT).show();
            return;
        }
        final Bitmap photo = drawable.getBitmap();
        final String nam = name.getText().toString();
        if(nam.length() == 0) {
            Toast.makeText(this, "Must input Name", Toast.LENGTH_SHORT).show();
            return;
        }

        if(mImageUri!=null) {

            final StorageReference filepath = storageRef.child("profilePictures/" + mCurrentUser.getUid() + ".jpg");

            /*from google doc*/
            Log.d("image name", String.valueOf(mImageUri));

            final ProgressDialog dialog = ProgressDialog.show(this, "Please wait", "Uploading Image...", true);
            UploadTask uploadTask = filepath.putFile(mImageUri);

            Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }

                    // Continue with the task to get the download URL
                    return filepath.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()) {
                        final Uri downloadUri = task.getResult();
                        //userRef.child("photo").setValue(downloadUri.toString());
                        try {
                            dialog.dismiss();
                        }catch
                        (Exception ex) {}

                        Map<String, String> map = new HashMap<>();
                        map.put("name", nam);
                        map.put("phone",phone);
                        map.put("photo", downloadUri.toString());
                        if(b){
                            map.put("type","buyer");
                        }
                        else {
                            map.put("type","seller");
                        }
                        mDatabase.child("users").child(mCurrentUser.getUid()).setValue(map);
                        startActivity(new Intent(getApplicationContext(), MainActivity.class));
                        finish();
                    }
                }
            });
        }

    }


//    public void nextButton(View view) {
//        BitmapDrawable drawable = (BitmapDrawable) imageView.getDrawable();
//        if(drawable == null) {
//            Toast.makeText(this, "Must attach a Profile Photo", Toast.LENGTH_SHORT).show();
//            return;
//        }
//        final Bitmap photo = drawable.getBitmap();
//        final String nam = name.getText().toString();
//        if(nam.length() == 0) {
//            Toast.makeText(this, "Must input Name", Toast.LENGTH_SHORT).show();
//            return;
//        }
//
//        if(mImageUri!=null) {
//
//            final StorageReference filepath = storageRef.child("profilePictures/" + mCurrentUser.getUid() + ".jpg");
//
//            /*from google doc*/
//            Log.d("image name", String.valueOf(mImageUri));
//
//            final ProgressDialog dialog = ProgressDialog.show(this, "Please wait", "Uploading Image...", true);
//            UploadTask uploadTask = filepath.putFile(mImageUri);
//
//            Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
//                @Override
//                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
//                    if (!task.isSuccessful()) {
//                        throw task.getException();
//                    }
//
//                    // Continue with the task to get the download URL
//                    return filepath.getDownloadUrl();
//                }
//            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
//                @Override
//                public void onComplete(@NonNull Task<Uri> task) {
//                    if (task.isSuccessful()) {
//                        final Uri downloadUri = task.getResult();
//                        //userRef.child("photo").setValue(downloadUri.toString());
//                        try {
//                            dialog.dismiss();
//                        }catch
//                        (Exception ex) {}
//
//                        Map<String, String> map = new HashMap<>();
//                        map.put("name", nam);
//                        map.put("phone",phone);
//                        map.put("photo", downloadUri.toString());
//                        mDatabase.child("users").child(mCurrentUser.getUid()).setValue(map);
//                        startActivity(new Intent(getApplicationContext(), MainActivity.class));
//                        finish();
//                    }
//                }
//            });
//        }
//
//    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == 1) {
                final boolean isCamera;
                if (data == null) {
                    isCamera = true;
                }
                else {
                    final String action = data.getAction();
                    if (action == null) {
                        isCamera = false;
                    } else {
                        isCamera = action.equals(MediaStore.ACTION_IMAGE_CAPTURE_SECURE);
                    }
                }

                if (isCamera) {
                    mImageUri = outputFileUri;
                    imageView.setImageURI(mImageUri);
                }
                else {
                    mImageUri = data.getData();
                    imageView.setImageURI(mImageUri);
                }
            }
        }
    }
}
