package com.store.inventory.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.store.inventory.R;
import com.store.inventory.activities.DetailsActivity;
import com.store.inventory.models.Product;

import java.util.List;

public class DashboardAdapter extends RecyclerView.Adapter<DashboardAdapter.ProductsViewHolder> {

    private Context mContext;
    private List<Product> products;
    private int bq = 10;
    private int bp = 10000;


    public DashboardAdapter(Context mContext, List<Product> products) {
        setHasStableIds(true);
        this.mContext = mContext;
        this.products = products;
    }

    @NonNull
    @Override
    public DashboardAdapter.ProductsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View row = LayoutInflater.from(mContext).inflate(R.layout.profit_list_item,parent,false);
        return new ProductsViewHolder(row);
    }

    @Override
    public void onBindViewHolder(@NonNull final ProductsViewHolder holder, int position) {

        holder.product_name.setText(products.get(position).getProduct_name());
        holder.product_profit.setText(String.valueOf(bq * (Integer.parseInt(products.get(position).getProduct_selling_price())
                - Integer.parseInt(products.get(position).getProduct_buying_price()))));

        if(products.get(position).getProduct_image() != null){
            Glide.with(mContext).load(products.get(position).getProduct_image()).into(holder.product_image);
        }

    }

    @Override
    public int getItemCount() {
        return products.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    public class ProductsViewHolder extends RecyclerView.ViewHolder{

        LinearLayout product_layout;
        ImageView product_image;
        TextView product_qauntity, product_price, product_name,product_profit;
        int position;
        FirebaseAuth mAuth;

        public ProductsViewHolder(View itemView) {
            super(itemView);
            product_image = itemView.findViewById(R.id.product_image);
            product_name = itemView.findViewById(R.id.product_name);
            product_layout = itemView.findViewById(R.id.product_layout);
            product_profit = itemView.findViewById(R.id.product_profit);
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
