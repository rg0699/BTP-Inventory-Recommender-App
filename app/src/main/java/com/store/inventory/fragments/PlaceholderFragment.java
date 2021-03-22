package com.store.inventory.fragments;

import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.store.inventory.R;
import com.store.inventory.adapters.ProductAdapter;
import com.store.inventory.adapters.RecommendedProductAdapter;
import com.store.inventory.adapters.TabsAdapter;
import com.store.inventory.models.Pair;
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

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static java.lang.Math.min;

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
    private List<Pair> recommended_product_list;
    private List<Product> recommended_product_list1;
    private TextView mpp;
    private TextView txt;
    private NestedScrollView scrollView;
    private RecommendedProductAdapter recommendedProductAdapter;


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
        recommended_product_list1 = new ArrayList<>();

        listView.setLayoutManager(new LinearLayoutManager(requireContext()));
        listView.setNestedScrollingEnabled(false);
        recommended_product_view.setLayoutManager(new LinearLayoutManager(requireContext(),
                LinearLayoutManager.HORIZONTAL,false));
        listView.setHasFixedSize(true);

        getAllProducts(category);
        //getAllRecommendedProducts(category);

        return root;
    }

    private void getAllRecommendedProducts(String category) {

        mDatabase.child("product_ratings").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot snap: snapshot.getChildren()) {
                    final String productId = snap.getKey();
                    //showMessage(productId);
                    //getProductDetails(productId);
                    final Product[] product = new Product[1];

                    mDatabase.child("products").child(productId).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            product[0] = dataSnapshot.getValue(Product.class);
                            //getRatings(productId,product);

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });

                    final double[] x = new double[1];
                    final double[] var = new double[1];

                    mDatabase.child("product_ratings").child(productId).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            for (DataSnapshot snap: snapshot.getChildren()) {
                                x[0] += Double.parseDouble(String.valueOf(snap.getValue()));
                            }
                            double y = (double)x[0]/snapshot.getChildrenCount();
                            DecimalFormat df = new DecimalFormat("###.##");
                            y = Double.parseDouble(df.format(y));
                            //showMessage(String.valueOf(y));
                            var[0] = (0.6)*snapshot.getChildrenCount() + (0.4)*y;

                            //showMessage(String.valueOf(recommended_product_list.size()));

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });

                    Pair z = new Pair(var[0], product[0]);
                    recommended_product_list.add(z);

                }

                Collections.sort(recommended_product_list, new Comparator<Pair>() {
                    @Override
                    public int compare(Pair p1, Pair p2) {
                        return p1.getKey().compareTo(p2.getKey());
                    }
                });

                //showMessage(String.valueOf(recommended_product_list.size()));

                for (int i=0;i<min(10,recommended_product_list.size());i++){
                    recommended_product_list1.add(recommended_product_list.get(i).getProduct());
                }

                showProducts();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


    }

    private void getProductDetails(final String productId) {

        mDatabase.child("products").child(productId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                final Product product = dataSnapshot.getValue(Product.class);
                //getRatings(productId,product);
                final double[] x = new double[1];

                mDatabase.child("product_ratings").child(productId).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot snap: snapshot.getChildren()) {
                            x[0] += Double.parseDouble(String.valueOf(snap.getValue()));
                        }
                        double y = (double)x[0]/snapshot.getChildrenCount();
                        DecimalFormat df = new DecimalFormat("###.##");
                        y = Double.parseDouble(df.format(y));
                        //showMessage(String.valueOf(y));
                        double var = (0.6)*snapshot.getChildrenCount() + (0.4)*y;
                        Pair x = new Pair(var,product);
                        recommended_product_list.add(x);
                        //showMessage(String.valueOf(recommended_product_list.size()));

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void getRatings(String productId, final Product product) {

        final double[] x = new double[1];

        mDatabase.child("product_ratings").child(productId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot snap: snapshot.getChildren()) {
                    x[0] += Double.parseDouble(String.valueOf(snap.getValue()));
                }
                double y = (double)x[0]/snapshot.getChildrenCount();
                DecimalFormat df = new DecimalFormat("###.##");
                y = Double.parseDouble(df.format(y));
                //showMessage(String.valueOf(y));
                double var = (0.6)*snapshot.getChildrenCount() + (0.4)*y;
                Pair x = new Pair(var,product);
                recommended_product_list.add(x);
                //showMessage(String.valueOf(recommended_product_list.size()));

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

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
            recommendedProductAdapter = new RecommendedProductAdapter(requireContext(),product_list);
            listView.setAdapter(productAdapter);
            recommended_product_view.setAdapter(recommendedProductAdapter);
        }

    }

    private void getAllProducts(final String category) {

        Query query = mDatabase.child("products").orderByChild("product_category").equalTo(category);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                product_list.clear();
                for (DataSnapshot productSnap: dataSnapshot.getChildren()) {

                    Product product = productSnap.getValue(Product.class);
                    product_list.add(product);

                }

                //getAllRecommendedProducts(category);

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