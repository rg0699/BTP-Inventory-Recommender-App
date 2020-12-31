package com.store.inventory.activities;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NavUtils;

import com.bumptech.glide.Glide;
import com.store.inventory.R;
import com.store.inventory.models.Product;
import com.store.inventory.models.User;
import com.store.inventory.utils.HelperClass;
import com.google.android.gms.tasks.Continuation;
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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class EditActivity extends AppCompatActivity {

    private static final int CAMERA_REQUEST = 1888;
    private static final int SELECT_PHOTO = 100;

    String[] PERMISSIONS = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA
    };

    ImageView productImage;
    EditText productNameEditText;
    EditText productQuantityEditText;
    EditText productSellingPriceEditText;
    TextView supplierNameEditText;
    TextView supplierPhoneEditText;

    Bitmap imageBitmap;
    private boolean isProductEdited = false;
    private String what;
    private SQLiteDatabase db;
    private String productId;
    private Spinner categorySpinner;
    private ArrayList<String> categories;
    private FirebaseAuth mAuth;
    private FirebaseUser mCurrentUser;
    private DatabaseReference mDatabase;
    private StorageReference storageRef;
    private ValueEventListener listener;
    private Bitmap bitmap;
    private Button photoButton;
    private Uri outputFileUri;
    private Uri mImageUri;
    private String supplierId;
    private EditText productBuyingPriceEditText;

    public static byte[] getBytes(Bitmap bitmap) {
        if (bitmap != null) {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 50, stream);
            return stream.toByteArray();
        } else {
            return null;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_edit);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mAuth = FirebaseAuth.getInstance();
        mCurrentUser = mAuth.getCurrentUser();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        storageRef = FirebaseStorage.getInstance().getReference();

        productNameEditText = findViewById(R.id.edit_product_name);
        productQuantityEditText = findViewById(R.id.edit_quantity);
        productSellingPriceEditText = findViewById(R.id.edit_selling_price);
        productBuyingPriceEditText = findViewById(R.id.edit_buying_price);
        supplierNameEditText = findViewById(R.id.edit_supplier_name);
        supplierPhoneEditText = findViewById(R.id.edit_supplier_phone);
        photoButton = findViewById(R.id.photo_button);
        productImage = findViewById(R.id.product_image);

        categorySpinner = findViewById(R.id.categorySpinner);
        categories = new ArrayList<>();
        categories.addAll(MainActivity.categoriesList);
        categorySpinner.setAdapter(new ArrayAdapter<>(getApplicationContext(),
                android.R.layout.simple_list_item_1, categories));

        what = getIntent().getStringExtra("what");
        supplierId = getIntent().getStringExtra("supplierId");

        assert what != null;
        if (what.equals("add")) {
            setTitle("Add Product");
        } else {
            setTitle("Edit Product");
            productId = getIntent().getStringExtra("productId");
            getProductDetails();
        }

        photoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!HelperClass.hasPermissions(getApplicationContext(), PERMISSIONS)) {
                    ActivityCompat.requestPermissions(EditActivity.this, PERMISSIONS, 1);
                }
                else openImageIntent();
            }
        });

        getSupplierDetails();

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

    private void getSupplierDetails(){
        mDatabase.child("users").child(supplierId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                if (user == null) {
                    showMessage("User data is null!");
                }
                else {
                    supplierNameEditText.setText(user.name);
                    supplierPhoneEditText.setText(user.phone);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                showMessage("Failed to read user!"
                        + " Please try again later");
            }
        });
    }


    private void getProductDetails() {

        mDatabase.child("products").child(productId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Product product = dataSnapshot.getValue(Product.class);
                if(product!=null){
                    productNameEditText.setText(product.getProduct_name());
                    productQuantityEditText.setText(product.getProduct_qauntity());
                    productSellingPriceEditText.setText(product.getProduct_selling_price());
                    productBuyingPriceEditText.setText(product.getProduct_buying_price());

                    if(product.getProduct_image()!=null){
                        Glide.with(getApplicationContext()).load(product.getProduct_image()).into(productImage);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

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
                    productImage.setImageURI(mImageUri);
                }
                else {
                    mImageUri = data.getData();
                    productImage.setImageURI(mImageUri);
                }
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_activity_edit, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.save:
                String productNameString = productNameEditText.getText().toString().trim();
                String quantityString = productQuantityEditText.getText().toString().trim();
                String SellPriceString = productSellingPriceEditText.getText().toString().trim();
                String BuyPriceString = productBuyingPriceEditText.getText().toString().trim();
                getBytes(imageBitmap);

                if (TextUtils.isEmpty(productNameString) || TextUtils.isEmpty(SellPriceString)
                        || TextUtils.isEmpty(quantityString) || TextUtils.isEmpty(BuyPriceString)) {
                    Toast.makeText(this, getString(R.string.empty_field_toast), Toast.LENGTH_LONG).show();
                } else {
                    //saveProduct();
                    sendToFirebase();
                    finish();
                }
                return true;
            case android.R.id.home:
                if (!isProductEdited) {
                    finish();
//                    NavUtils.navigateUpFromSameTask(EditActivity.this);
                    return true;
                }
                DialogInterface.OnClickListener buttonClick =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                NavUtils.navigateUpFromSameTask(EditActivity.this);
                            }
                        };
                unsavedDataDialog(buttonClick);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (!isProductEdited) {
            super.onBackPressed();
            return;
        }
        DialogInterface.OnClickListener buttonClick =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                };
        unsavedDataDialog(buttonClick);
    }

    private void unsavedDataDialog(DialogInterface.OnClickListener buttonClick) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Discard Changes");
        builder.setPositiveButton("Yes", buttonClick);
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void sendToFirebase() {
        final String stringProductName = productNameEditText.getText().toString().trim();
        final String stringQuantity = productQuantityEditText.getText().toString().trim();
        final String stringSellPrice = productSellingPriceEditText.getText().toString().trim();
        final String stringBuyPrice = productBuyingPriceEditText.getText().toString().trim();
        //byte[] imageByte = getBytes(imageBitmap);
        final String category = categories.get(categorySpinner.getSelectedItemPosition());

        DatabaseReference newProduct;
        String message;
        final String message1;
        if(what.equals("add")){
            newProduct = mDatabase.child("products").push();
            message = "Adding Inventory...";
            message1 = "Inventory Added Successfully!!";
        }
        else {
            newProduct = mDatabase.child("products").child(productId);
            message = "Updating Inventory...";
            message1 = "Inventory Updated Successfully!!";
        }

        if(mImageUri!=null) {

            final StorageReference filepath = storageRef.child("productPictures/" + supplierId)
                    .child(newProduct.getKey() + ".jpg");

            /*from google doc*/
            Log.d("image name", String.valueOf(mImageUri));

            final ProgressDialog dialog = ProgressDialog.show(this,
                    "Please wait!", message,
                    true);
            UploadTask uploadTask = filepath.putFile(mImageUri);

            final DatabaseReference finalNewProduct = newProduct;
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

                        Product product = new Product(stringProductName, stringQuantity, stringSellPrice, stringBuyPrice,
                                downloadUri.toString(), category, mCurrentUser.getUid());
                        if(what.equals("add")){
                            String key = finalNewProduct.getKey();
                            product.setProduct_id(key);
                        }
                        else {
                            product.setProduct_id(productId);
                        }

                        finalNewProduct.setValue(product).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                dialog.dismiss();
                                if (task.isSuccessful()) {
                                    showMessage(message1);
                                } else {
                                    String error = task.getException().getMessage();
                                    showMessage("Error" + error);
                                }
                            }
                        });
                    }
                }
            });
        }
        else {
            final ProgressDialog dialog = ProgressDialog.show(this,
                    "Please wait!", message,
                    true);

            Product product = new Product(stringProductName, stringQuantity,stringSellPrice,stringBuyPrice, category,
                    mCurrentUser.getUid());

            String key = newProduct.getKey();
            product.setProduct_id(key);

            newProduct.setValue(product).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    dialog.dismiss();
                    if (task.isSuccessful()) {
                        showMessage(message1);
                    } else {
                        String error = task.getException().getMessage();
                        showMessage("Error" + error);
                    }
                }
            });
        }

    }

    private void showMessage(String s){
        Toast.makeText(getApplicationContext(), s, Toast.LENGTH_SHORT)
                .show();
    }
}