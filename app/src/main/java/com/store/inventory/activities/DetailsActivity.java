package com.store.inventory.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.database.Query;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.store.inventory.R;
import com.store.inventory.models.Product;
import com.store.inventory.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class DetailsActivity extends AppCompatActivity {

    TextView supplierPhoneTextView;
    TextView supplierNameTextView;
    TextView productNameTextView;
    TextView productPriceTextView;
    TextView productQuantityTextView;
    ImageView productImage;

    private Button callSupplierButton;
    private Bitmap bitmap;
    private String productId;
    private SQLiteDatabase db;
    private FirebaseAuth mAuth;
    private FirebaseUser mCurrentUser;
    private DatabaseReference mDatabase;
    private FirebaseAuth.AuthStateListener authListener;
    private String supplierId;
    private StorageReference storageRef;
    private LinearLayout shareLayout;
    private LinearLayout wishListLayout;
    private ImageView btnLike;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_item_details);
        setTitle("Details");

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mAuth = FirebaseAuth.getInstance();
        mCurrentUser = mAuth.getCurrentUser();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        storageRef = FirebaseStorage.getInstance().getReference();

        authListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if(user == null){
                    invalidateOptionsMenu();
                }
            }
        };

        productNameTextView = findViewById(R.id.details_product_name);
        productPriceTextView = findViewById(R.id.details_price);
        productQuantityTextView = findViewById(R.id.details_quantity);
        productImage = findViewById(R.id.details_image);
        supplierNameTextView = findViewById(R.id.details_supplier_name);
        supplierPhoneTextView = findViewById(R.id.details_supplier_phone);
        callSupplierButton = findViewById(R.id.call_button);
        shareLayout = findViewById(R.id.share);
        wishListLayout = findViewById(R.id.wishlist);
        btnLike = findViewById(R.id.btn_like);

        callSupplierButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                String phoneNumber = supplierPhoneTextView.getText().toString().trim();
                Intent callIntent = new Intent(Intent.ACTION_DIAL);
                supplierPhoneTextView.getText().toString().trim();
                callIntent.setData(Uri.parse("tel:" + phoneNumber));
                if (callIntent.resolveActivity(getPackageManager()) != null) {
                    startActivity(callIntent);
                }
            }
        });

        shareLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent sharingIntent = new Intent(Intent.ACTION_SEND);
                sharingIntent.setType("text/plain");
                String shareBody = "Hey, Check this Product Out: " + productNameTextView.getText();
                //sharingIntent.putExtra(Intent.EXTRA_SUBJECT, "App link:");
                sharingIntent.putExtra(Intent.EXTRA_TEXT, shareBody);
                startActivity(Intent.createChooser(sharingIntent, "Share product via"));
            }
        });

        wishListLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mCurrentUser == null){
                    showMessage("Please LogIn to add Product to wishList!");
                }
                else {
                    addToWishlist();
                }
            }
        });

        productId = getIntent().getStringExtra("productId");
        //supplierId = getIntent().getStringExtra("supplierId");
        getProductDetails();
        //getSupplierDetails();

        if(mCurrentUser == null){
            btnLike.setClickable(false);
        }
        else {
            setLikeButton();
        }
    }

    private void addToWishlist() {
        final DatabaseReference likesReference = mDatabase.child("wishList")
                .child(mCurrentUser.getUid());
        Query query = mDatabase.child("wishList")
                .child(mCurrentUser.getUid())
                .orderByChild("product_id").equalTo(productId);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if(dataSnapshot.exists()){
                    likesReference.child(productId).removeValue();
                    btnLike.setImageResource(R.drawable.ic_heart_border);
                }else {
                    likesReference.child(productId).child("product_id").setValue(productId);
                    btnLike.setImageResource(R.drawable.ic_heart_red);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    private void setLikeButton(){

        Query query = mDatabase.child("wishList")
                .child(mCurrentUser.getUid())
                .orderByChild("product_id").equalTo(productId);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if(dataSnapshot.exists()){
                    btnLike.setImageResource(R.drawable.ic_heart_red);
                }else {
                    btnLike.setImageResource(R.drawable.ic_heart_border);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });

    }


    private void getProductDetails() {

        mDatabase.child("products").child(productId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Product product = dataSnapshot.getValue(Product.class);
                if(product!=null){
                    productNameTextView.setText(product.getProduct_name());
                    productQuantityTextView.setText(product.getProduct_qauntity());
                    productPriceTextView.setText(product.getProduct_selling_price());

                    if(product.getProduct_image()!=null){
                        Glide.with(getApplicationContext()).load(product.getProduct_image()).into(productImage);
                    }
                    supplierId = product.getSupplier_id();
                    getSupplierDetails();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

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
                    supplierNameTextView.setText(user.name);
                    supplierPhoneTextView.setText(user.phone);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                showMessage("Failed to read user!"
                        + " Please try again later");
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_activity_details, menu);
        if(mCurrentUser == null || !supplierId.equals(mCurrentUser.getUid())){
            menu.findItem(R.id.edit_product).setVisible(false);
            menu.findItem(R.id.remove_product).setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.remove_product:
                showDeleteConfirmationDialog();
                return true;
            case R.id.edit_product:
                Intent i = new Intent(DetailsActivity.this, EditActivity.class);
                i.putExtra("what", "edit");
                i.putExtra("productId", productId);
                i.putExtra("supplierId", supplierId);
                startActivity(i);
        }
        return super.onOptionsItemSelected(item);
    }

    private void showDeleteConfirmationDialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getString(R.string.remove_product));
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                deleteProduct();
            }
        });
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void deleteProduct() {

        mDatabase.child("products").child(productId).removeValue();
        storageRef.child("productPictures/" + supplierId)
                .child(productId + ".jpg").delete();
        finish();
    }

    private void showMessage(String s){
        Toast.makeText(getApplicationContext(), s, Toast.LENGTH_SHORT)
                .show();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(authListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (authListener != null) {
            mAuth.removeAuthStateListener(authListener);
        }
    }
}
