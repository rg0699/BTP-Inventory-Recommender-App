package com.store.inventory.fragments;

import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.store.inventory.R;
import com.store.inventory.adapters.ProductAdapter;
import com.store.inventory.adapters.TabsAdapter;
import com.store.inventory.models.Product;
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

import java.util.ArrayList;
import java.util.List;

public class PlaceholderFragment extends Fragment {

    private static final String ARG_SECTION_NUMBER = "section_number";
    private static final String ARG_SECTION_TITLE = "section_title";
    private static final String ARG_SECTION_PRODUCTS = "section_products";

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
    private RecyclerView recommended_product_view;
    private List<Product> recommended_product_list;
    private TextView mpp;
    private TextView txt;
    private ScrollView scrollView;


    public static PlaceholderFragment newInstance(String category, int position) {
        PlaceholderFragment fragment = new PlaceholderFragment();
        Bundle args = new Bundle();
        args.putString(ARG_SECTION_TITLE, category);
        args.putInt(ARG_SECTION_NUMBER, position);
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        int index = 1;
        if (getArguments() != null) {
            index = getArguments().getInt(ARG_SECTION_NUMBER);
            category = getArguments().getString(ARG_SECTION_TITLE);
            //product_list = getArguments().getParcelableArrayList(ARG_SECTION_PRODUCTS);
        }

        setHasOptionsMenu(true);

    }

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_main, container, false);

        progressBar = root.findViewById(R.id.my_progress_bar);
        textView = root.findViewById(R.id.my_textView);
        scrollView = root.findViewById(R.id.scrollView);
        splash = root.findViewById(R.id.splash);
        listView = root.findViewById(R.id.list_view);
        recommended_product_view = root.findViewById(R.id.recommended_product_view);
        emptyView = root.findViewById(R.id.empty_view);
        scrollView.setVisibility(View.GONE);
        emptyView.setVisibility(View.GONE);
        splash.setVisibility(View.VISIBLE);

        mAuth = FirebaseAuth.getInstance();
        mCurrentUser = mAuth.getCurrentUser();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        storageRef = FirebaseStorage.getInstance().getReference();

        product_list = new ArrayList<>();
        recommended_product_list = new ArrayList<>();

        listView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recommended_product_view.setLayoutManager(new LinearLayoutManager(requireContext(),
                LinearLayoutManager.HORIZONTAL,false));
        listView.setHasFixedSize(true);

        getAllProducts(category);
        getAllRecommendedProducts(category);

        return root;
    }

    private void getAllRecommendedProducts(String category) {



    }

    private void showProducts(){

        splash.setVisibility(View.INVISIBLE);

        if(product_list.size()==0){
            scrollView.setVisibility(View.GONE);
            emptyView.setVisibility(View.VISIBLE);
        }
        else {
            emptyView.setVisibility(View.GONE);
            scrollView.setVisibility(View.VISIBLE);
            productAdapter = new ProductAdapter(requireContext(),product_list);
            listView.setAdapter(productAdapter);
            recommended_product_view.setAdapter(productAdapter);
        }

    }

    private void getAllProducts(String category) {

        Query query = mDatabase.child("products").orderByChild("product_category").equalTo(category);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                product_list.clear();
                for (DataSnapshot productSnap: dataSnapshot.getChildren()) {

                    Product product = productSnap.getValue(Product.class);
                    product_list.add(product);

                }

                showProducts();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    private void showMessage(String s){
        Toast.makeText(requireContext(), s, Toast.LENGTH_SHORT)
                .show();
    }

}