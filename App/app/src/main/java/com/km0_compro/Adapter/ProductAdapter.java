package com.km0_compro.Adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.km0_compro.Model.Product;
import com.km0_compro.PopPresentation;
import com.km0_compro.R;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ViewHolder> implements Filterable {

    private Context mContext;
    private List<Product> mProducts;
    private List<Product> mProductsFull;


    public ProductAdapter(Context mContext, List<Product> mProducts) {
        this.mProducts = mProducts;
        mProductsFull = new ArrayList<>(mProducts);
        this.mContext = mContext;
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.product_item, parent, false);
        return new ViewHolder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        final Product product = mProducts.get(position);

        Float initialPrice = Float.parseFloat(product.getPrice());
        Float discount = Float.parseFloat(product.getDiscount());
        DecimalFormat formatter = new DecimalFormat("0.00");
        String discountedPrice = formatter.format(initialPrice - initialPrice * discount / 100);
        //String discountedPrice = String.valueOf(initialPrice - initialPrice * discount / 100);

        holder.product_name.setText(product.getDescription());
        //holder.product_price.setText(product.getPrice());
        //holder.product_price.setText(discountedPrice);
        if (!product.getDiscount().equals("0")) {
            holder.product_price.setText(product.getPrice());
            holder.product_price.setPaintFlags(holder.product_price.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            holder.discounted_price.setText("€" + discountedPrice);
            holder.discount.setText("-" + product.getDiscount() + "%");
        } else {
            holder.product_price.setText(product.getPrice());
        }

        if (product.getImage().equals("Default")){
            holder.product_image.setImageResource(R.mipmap.ic_launcher);
        } else {
            Glide.with(mContext).load(product.getImage()).into(holder.product_image);
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, PopPresentation.class);
                intent.putExtra("com.km0_vendo.GETIMAGE", product.getImage());
                intent.putExtra("com.km0_vendo.GETNUMBER", product.getNumber());
                intent.putExtra("com.km0_vendo.GETNAME", product.getDescription());
                intent.putExtra("com.km0_vendo.GETPRICE", product.getPrice());
                intent.putExtra("com.km0_vendo.GETDISCOUNTEDPRICE", discountedPrice);
                intent.putExtra("com.km0_vendo.GETDISCOUNT", product.getDiscount());
                intent.putExtra("com.km0_vendo.GETSELLERID", product.getSellerId());
                intent.putExtra("com.km0_vendo.GETSELLERNAME", product.getSellerName());
                mContext.startActivity(intent);
            }
        });

    }


    @Override
    public int getItemCount() {
        return mProducts.size();
    }


    static class ViewHolder extends RecyclerView.ViewHolder{

        public TextView product_name;
        public TextView product_price;
        public ImageView product_image;
        public TextView discount;
        public TextView discounted_price;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            product_name = itemView.findViewById(R.id.product_name);
            product_price = itemView.findViewById(R.id.product_price);
            product_image = itemView.findViewById(R.id.product_image);
            discount = itemView.findViewById(R.id.discount_text);
            discounted_price = itemView.findViewById(R.id.discounted_price);
        }
    }

    @Override                                                                                       /////////////////////////
    public Filter getFilter() {
        return filterProducts;
    }

    private  Filter filterProducts = new Filter() {                                                 /////////////////////////
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            List<Product> filteredList = new ArrayList<>();
            if (constraint == null || constraint.length() == 0) {
                filteredList.addAll(mProductsFull);
            } else {
                String filterPattern = constraint.toString().toLowerCase().trim();
                for (Product product : mProductsFull) {
                    if(product.getDescription().toLowerCase().contains(filterPattern)){
                        filteredList.add(product);
                    }
                }
            }
            FilterResults results = new FilterResults();
            results.values = filteredList;

            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            mProducts.clear();
            mProducts.addAll((List) results.values);
            notifyDataSetChanged();
        }
    };


}
