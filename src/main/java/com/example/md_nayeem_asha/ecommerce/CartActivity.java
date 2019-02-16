package com.example.md_nayeem_asha.ecommerce;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.md_nayeem_asha.ecommerce.Model.Cart;
import com.example.md_nayeem_asha.ecommerce.Prevalent.Prevalent;
import com.example.md_nayeem_asha.ecommerce.ViewHolder.CartViewHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class CartActivity extends AppCompatActivity
{

    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;
    private Button NextProcessButton;
    private TextView txtTotalAmount, txtMsd1;

    int  overTotalPrise = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        recyclerView = findViewById(R.id.cart_list);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        NextProcessButton = findViewById(R.id.next_process_button);
        txtTotalAmount = findViewById(R.id.total_price);
        txtMsd1 = findViewById(R.id.msd1);

        NextProcessButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {

                txtTotalAmount.setText("Total Price "+String.valueOf(overTotalPrise)+" /-");

                Intent intent = new Intent(CartActivity.this, ConfirmFinalOrderActivity.class);
                intent.putExtra("Total Price", String.valueOf(overTotalPrise));
                startActivity(intent);
                finish();
            }
        });

    }


    @Override
    protected void onStart()
    {
        super.onStart();

        CheckOrderState();

        final DatabaseReference cartListRef = FirebaseDatabase.getInstance().getReference().child("Cart List");

        FirebaseRecyclerOptions<Cart> options = new FirebaseRecyclerOptions.Builder<Cart>()
                .setQuery(cartListRef.child("User View")
                        .child(Prevalent.currentOnlineUser.getPhone())
                        .child("Products"), Cart.class)
                        .build();


        FirebaseRecyclerAdapter<Cart, CartViewHolder> adapter = new FirebaseRecyclerAdapter<Cart, CartViewHolder>(options)
        {
            @Override
            protected void onBindViewHolder(@NonNull CartViewHolder holder, int position, @NonNull final Cart model)
            {
                holder.txtProductQuantity.setText("Quantity: "+model.getQuantity());
                holder.txtProductPrice.setText("Per "+model.getPrice());
                holder.txtProductName.setText(model.getPname());

                int  oneTypeProductPrice = (Integer.parseInt(model.getPrice().replaceAll("[\\D]", ""))) * Integer.parseInt(model.getQuantity().replaceAll("[\\D]", ""));
                overTotalPrise = overTotalPrise + oneTypeProductPrice;

                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v)
                    {
                        CharSequence options[] = new CharSequence[]
                                {
                                        "Edit",
                                        "Remove"
                                };
                        AlertDialog.Builder builder = new AlertDialog.Builder(CartActivity.this);
                        builder.setTitle("Cart Options:");

                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i)
                            {
                                if (i == 0)
                                {
                                    Intent intent = new Intent(CartActivity.this, ProductDetailsActivity.class);
                                    intent.putExtra("pid", model.getPid());
                                    startActivity(intent);
                                }
                                if (i == 1)
                                {
                                    cartListRef.child("User View")
                                            .child(Prevalent.currentOnlineUser.getPhone())
                                            .child("Products")
                                            .child(model.getPid())
                                            .removeValue()
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task)
                                                {
                                                    if (task.isSuccessful())
                                                    {
                                                        Toast.makeText(CartActivity.this, "Item removed Successfully..!", Toast.LENGTH_SHORT).show();
                                                        startActivity(new Intent(CartActivity.this, HomeActivity.class));
                                                    }
                                                }
                                            });
                                }
                            }
                        });
                        builder.show();
                    }
                });
            }

            @NonNull
            @Override
            public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
            {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.cart_items_layout, parent, false);
                CartViewHolder holder = new CartViewHolder(view);
                return holder;
            }
        };

        recyclerView.setAdapter(adapter);
        adapter.startListening();
    }

    private void CheckOrderState()
    {
        DatabaseReference ordersRef = FirebaseDatabase.getInstance().getReference().child("Orders").child(Prevalent.currentOnlineUser.getPhone());

        ordersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                if (dataSnapshot.exists())
                {
                    String shippingState = dataSnapshot.child("state").getValue().toString();
                    String userName = dataSnapshot.child("name").getValue().toString();

                    if (shippingState.equals("shipped"))
                    {
                        txtTotalAmount.setText("Dear " + userName +"\n order is shipped successfully.");
                        recyclerView.setVisibility(View.GONE);

                        txtMsd1.setVisibility(View.VISIBLE);
                        txtMsd1.setText("Congratulation, your final order has been shipped successfully. Soon you will received your order at your door step.");
                        NextProcessButton.setVisibility(View.GONE);

                        Toast.makeText(CartActivity.this, "You can purchase more products, once you received your pending final order.", Toast.LENGTH_SHORT).show();
                    }
                    else if (shippingState.equals("not shipped"))
                    {
                        txtTotalAmount.setText("Shipping State = Not Shipped.");
                        recyclerView.setVisibility(View.GONE);

                        txtMsd1.setVisibility(View.VISIBLE);
                        NextProcessButton.setVisibility(View.GONE);

                        Toast.makeText(CartActivity.this, "You can purchase more products, once you received your pending final order.", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}
