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
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.GridLayoutManager;
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
import com.store.inventory.adapters.DashboardAdapter;
import com.store.inventory.adapters.TabsAdapter;
import com.store.inventory.models.Product;

import java.util.ArrayList;
import java.util.List;

public class MyDashboardActivity extends AppCompatActivity {

    private RecyclerView listView;
    private SQLiteDatabase db;
    private LinearLayout splash;
    private RelativeLayout emptyView;
    private List<Product> product_list;
    private DashboardAdapter productAdapter;
    private ProgressBar progressBar;
    private TextView textView;
    private String category;
    private TabsAdapter t;
    private FirebaseAuth mAuth;
    private FirebaseUser mCurrentUser;
    private DatabaseReference mDatabase;
    private StorageReference storageRef;
    private String type;
    private TextView profit_textView;
    private int bq = 10;
    private int bp = 10000;
    private int ttl_profit = 0;
    private CardView ttl_profit_View;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAuth = FirebaseAuth.getInstance();
        mCurrentUser = mAuth.getCurrentUser();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        storageRef = FirebaseStorage.getInstance().getReference();

        category = mCurrentUser.getUid();

        setContentView(R.layout.activity_my_dashboard);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setTitle("Dashboard");

        progressBar = findViewById(R.id.my_progress_bar);
        textView = findViewById(R.id.my_textView);
        splash = findViewById(R.id.splash);
        listView = findViewById(R.id.list_view);
        emptyView = findViewById(R.id.empty_view);
        profit_textView = findViewById(R.id.profit);
        ttl_profit_View = findViewById(R.id.total_profit);
        listView.setVisibility(View.INVISIBLE);
        emptyView.setVisibility(View.INVISIBLE);
        ttl_profit_View.setVisibility(View.INVISIBLE);
        splash.setVisibility(View.VISIBLE);
        product_list = new ArrayList<>();
        //profit_textView.setMovementMethod(new ScrollingMovementMethod());

        listView.setLayoutManager(new GridLayoutManager(getApplicationContext(),2));
        listView.setHasFixedSize(true);

        getAllProducts(category);
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

    private void showProducts(){

        splash.setVisibility(View.INVISIBLE);
        ttl_profit_View.setVisibility(View.VISIBLE);

        //showMessage(String.valueOf(product_list.size()));
        //showMessage("c");

        if(product_list.size() == 0){
            listView.setVisibility(View.GONE);
            emptyView.setVisibility(View.VISIBLE);
            profit_textView.setText("0");
        }
        else {
            emptyView.setVisibility(View.GONE);
            listView.setVisibility(View.VISIBLE);
            profit_textView.setText(String.valueOf(ttl_profit));
            productAdapter = new DashboardAdapter(getApplicationContext(),product_list);
            listView.setAdapter(productAdapter);
        }

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
                    if (product != null) {
                        ttl_profit += bq * (Integer.parseInt(product.getProduct_selling_price())
                                - Integer.parseInt(product.getProduct_buying_price()));
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
