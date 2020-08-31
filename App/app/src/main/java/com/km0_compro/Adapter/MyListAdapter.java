package com.km0_compro.Adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.km0_compro.Model.Product;
import com.km0_compro.MyListActivity;
import com.km0_compro.R;

import org.w3c.dom.Text;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class MyListAdapter extends RecyclerView.Adapter<MyListAdapter.ViewHolder> {

    private Context mContext1;
    private List<Product> mProducts1;
    String sellerid;
    String productId;

    public MyListAdapter(Context mContext, List<Product> mProducts) {
        this.mProducts1 = mProducts;
        this.mContext1 = mContext;
    }



    @NonNull
    @Override
    public MyListAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext1).inflate(R.layout.order_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyListAdapter.ViewHolder holder, int position) {
        final Product product = mProducts1.get(position);
        sellerid = product.getSellerId();
        String description = product.getDescription();
        holder.product_name_mylist.setText("Prodotto: " + description);
        holder.number_item_mylist.setText("Pezzi: " + product.getNumber());
        holder.total_price_mylist.setText("Prezzo (x" + product.getNumber() + "): " + product.getPrice() + "€");

        holder.remove_item.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // get date
                @SuppressLint("SimpleDateFormat") SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy");
                Date c = Calendar.getInstance().getTime();
                String date = df.format(c);

                // get customer's uid
                FirebaseAuth auth;
                auth = FirebaseAuth.getInstance();  // authentication for firebase
                FirebaseUser firebaseUser = auth.getCurrentUser();
                assert firebaseUser != null;
                String userid = firebaseUser.getUid();

                DatabaseReference removeRef = FirebaseDatabase.getInstance().getReference("Orders").child(date).child(sellerid).child(userid);
                removeRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            Product productFor = snapshot.getValue(Product.class);
                            //holder.remove_item.setText(productId);
                            assert productFor != null;
                            if (productFor.getDescription().equals(description)) {
                                productId = snapshot.getKey();
                            }
                        }
                        removeRef.child(productId).removeValue();
                        holder.remove_item.setText("Prodotto rimosso");
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
                //holder.remove_item.setText("Prodotto rimosso");
            }
        });
    }


    @Override
    public int getItemCount() {
        return mProducts1.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder{

        public TextView product_name_mylist;
        public TextView number_item_mylist;
        public TextView total_price_mylist;
        public TextView remove_item;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            product_name_mylist = itemView.findViewById(R.id.product_name_mylist);
            number_item_mylist = itemView.findViewById(R.id.number_item_mylist);
            total_price_mylist = itemView.findViewById(R.id.total_price_mylist);
            remove_item = itemView.findViewById(R.id.remove_item);
        }

    }

}
