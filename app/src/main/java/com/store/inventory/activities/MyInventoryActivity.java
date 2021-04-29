package com.store.inventory.activities;

import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.store.inventory.R;
import com.store.inventory.adapters.ProductAdapter;
import com.store.inventory.adapters.RecommendedProductAdapter;
import com.store.inventory.adapters.TabsAdapter;
import com.store.inventory.models.Product;

import java.util.ArrayList;
import java.util.List;

public class MyInventoryActivity extends AppCompatActivity {


    private RecyclerView listView;
    private SQLiteDatabase db;
    private LinearLayout splash;
    private RelativeLayout emptyView;
    private List<Product> product_list;
    private ProductAdapter productAdapter;
    private ProgressBar progressBar;
    private TextView textView;
    private String category;
    private TabsAdapter t;
    private FirebaseAuth mAuth;
    private FirebaseUser mCurrentUser;
    private DatabaseReference mDatabase;
    private StorageReference storageRef;
    private String type;
    private NestedScrollView scrollView;
    private RecyclerView lowStock_product_view;
    private List<Product> lowStock_product_list;
    private RecommendedProductAdapter lowStockProductAdapter;
    private LinearLayout lowStockView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAuth = FirebaseAuth.getInstance();
        mCurrentUser = mAuth.getCurrentUser();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        storageRef = FirebaseStorage.getInstance().getReference();

        category = mCurrentUser.getUid();

        setContentView(R.layout.activity_my_inventory);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        type = getIntent().getStringExtra("type");
        setTitle(type);

        progressBar = findViewById(R.id.my_progress_bar);
        textView = findViewById(R.id.my_textView);
        splash = findViewById(R.id.splash);
        listView = findViewById(R.id.list_view);
        scrollView = findViewById(R.id.scrollView);
        lowStockView = findViewById(R.id.lowStock_view);
        emptyView = findViewById(R.id.empty_view);
        lowStock_product_view = findViewById(R.id.lowStock_product_view);
        scrollView.setVisibility(View.INVISIBLE);
        emptyView.setVisibility(View.INVISIBLE);
        splash.setVisibility(View.VISIBLE);

        product_list = new ArrayList<>();
        lowStock_product_list = new ArrayList<>();

        listView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        listView.setHasFixedSize(true);
        lowStock_product_view.setLayoutManager(new LinearLayoutManager(getApplicationContext(),
                LinearLayoutManager.HORIZONTAL,false));

        if(type.equals("My Inventory")){
            getAllProducts(category);
        }
        else {
            getAllWishlist(category);
        }
    }

    private void showProducts(){

        splash.setVisibility(View.INVISIBLE);

        //showMessage(String.valueOf(product_list.size()));
        //showMessage("c");

        if(product_list.size() == 0){
            scrollView.setVisibility(View.GONE);
            //listView.setVisibility(View.GONE);
            emptyView.setVisibility(View.VISIBLE);
        }
        else {
            emptyView.setVisibility(View.GONE);
            scrollView.setVisibility(View.VISIBLE);
            //listView.setVisibility(View.VISIBLE);
            if(lowStock_product_list.size() == 0){
                lowStockView.setVisibility(View.GONE);
            }
            else {
                lowStockProductAdapter = new RecommendedProductAdapter(getApplicationContext(),lowStock_product_list);
                lowStock_product_view.setAdapter(lowStockProductAdapter);
            }
            productAdapter = new ProductAdapter(getApplicationContext(),product_list);
            listView.setAdapter(productAdapter);

        }

    }

    private void getAllWishlist(String category) {
        Query query = mDatabase.child("wishList").child(category);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                //product_list.clear();
                for (DataSnapshot productSnap: dataSnapshot.getChildren()) {

                    //showMessage(productSnap.getKey());
                    Product product = productSnap.getValue(Product.class);
                    product_list.add(product);

                    //getProductDetails(productSnap.getKey());

                }
                showProducts();

            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void getAllProducts(String category) {

        Query query = mDatabase.child("products").orderByChild("supplier_id").equalTo(category);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                //product_list.clear();
                for (DataSnapshot productSnap: dataSnapshot.getChildren()) {

                    Product product = productSnap.getValue(Product.class);
                    product_list.add(product);
                    if (product != null && Integer.parseInt(product.getProduct_qauntity()) < 100) {
                        lowStock_product_list.add(product);
                    }

                }
                showProducts();

            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }


    private void showMessage(String s){
        Toast.makeText(getApplicationContext(), s, Toast.LENGTH_SHORT)
                .show();
    }
}
