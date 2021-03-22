package com.store.inventory.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.store.inventory.R;
import com.store.inventory.activities.DetailsActivity;
import com.store.inventory.models.Product;

import java.util.List;

public class RecommendedProductAdapter extends RecyclerView.Adapter<RecommendedProductAdapter.ProductsViewHolder> {

    private Context mContext;
    private List<Product> products;


    public RecommendedProductAdapter(Context mContext, List<Product> products) {
        setHasStableIds(true);
        this.mContext = mContext;
        this.products = products;
    }

    @NonNull
    @Override
    public RecommendedProductAdapter.ProductsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View row = LayoutInflater.from(mContext).inflate(R.layout.recommended_list_item,parent,false);
        return new ProductsViewHolder(row);
    }

    @Override
    public void onBindViewHolder(@NonNull final ProductsViewHolder holder, int position) {

        String s = products.get(position).getProduct_id();

        DatabaseReference mDatabaseUsers = FirebaseDatabase.getInstance().getReference("products").child(s);
        mDatabaseUsers.keepSynced(true);

        mDatabaseUsers.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Product product = dataSnapshot.getValue(Product.class);

                if (product == null) {

                    Toast.makeText(
                            mContext,
                            "Product data is null!",
                            Toast.LENGTH_SHORT)
                            .show();
                }
                else {
                    holder.product_name.setText(product.product_name);
                    holder.product_qauntity.setText(product.product_qauntity);
                    holder.product_price.setText(product.product_selling_price);
                    if(product.product_image != null){
                        Glide.with(mContext).load(product.product_image).into(holder.product_image);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        holder.product_name.setText(products.get(position).getProduct_name());
        holder.product_qauntity.setText(String.valueOf(products.get(position).getProduct_qauntity()));
        holder.product_price.setText(String.valueOf(products.get(position).getProduct_selling_price()));

        if(products.get(position).getProduct_image() != null){
            Glide.with(mContext).load(products.get(position).getProduct_image()).into(holder.product_image);
        }

    }

    @Override
    public int getItemCount() {
        return products.size();
    }

    public class ProductsViewHolder extends RecyclerView.ViewHolder{

        LinearLayout product_layout;
        ImageView product_image;
        TextView product_qauntity, product_price, product_name;
        int position;
        FirebaseAuth mAuth;

        public ProductsViewHolder(View itemView) {
            super(itemView);
            product_image = itemView.findViewById(R.id.product_image);
            product_qauntity = itemView.findViewById(R.id.product_quantity);
            product_price = itemView.findViewById(R.id.product_price);
            product_name = itemView.findViewById(R.id.product_name);
            product_layout = itemView.findViewById(R.id.product_layout);
            mAuth = FirebaseAuth.getInstance();

            product_layout.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {

                    Intent i = new Intent(mContext, DetailsActivity.class);
                    position = getAdapterPosition();
                    i.putExtra("productId", products.get(position).getProduct_id());
                    //i.putExtra("supplierId", products.get(position).getSupplier_id());
                    mContext.startActivity(i);

                }

            });

        }

    }

}
