package app.android.volleyimage;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.squareup.picasso.Picasso;

public class MainActivity extends AppCompatActivity {

    private static final String URL_PRODUCTS = "http://192.168.43.137/mini/MyApi/myapi.php";

    List<Product> productList;

    RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = findViewById(R.id.recylcerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));

        productList = new ArrayList<>();
        loadProducts();

        if (isConnected()) {
            Toast.makeText(getApplicationContext(), "Internet Connected", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getApplicationContext(), "No Internet Connection", Toast.LENGTH_SHORT).show();
            ImageView imageView = (ImageView)findViewById(R.id.nocon);
            imageView.setImageResource(R.drawable.nocon);

        }
}

    public boolean isConnected() {
        boolean connected = false;
        try {
            ConnectivityManager cm = (ConnectivityManager)getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo nInfo = cm.getActiveNetworkInfo();
            connected = nInfo != null && nInfo.isAvailable() && nInfo.isConnected();
            return connected;
        } catch (Exception e) {
            Log.e("Connectivity Exception", e.getMessage());
        }
        return connected;
    }

    public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {


        private Context mCtx;
        private List<Product> productList;

        public ProductAdapter(Context mCtx, List<Product> productList) {
            this.mCtx = mCtx;
            this.productList = productList;
        }

        @Override
        public ProductAdapter.ProductViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(mCtx);
            View view = inflater.inflate(R.layout.product_list, null);
            return new ProductAdapter.ProductViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ProductAdapter.ProductViewHolder holder, final int position) {
            final Product product = productList.get(position);

            //loading the image
            /*Glide.with(mCtx)
                    .load(product.getImage())
                    .into(holder.imageView);
                    */

            holder.textViewTitle.setText(product.getTitle());
//            holder.textViewShortDesc.setText(product.getShortdesc());
            holder.textViewRating.setText(String.valueOf(product.getRating()));
            holder.textViewPrice.setText(String.valueOf(product.getPrice()));
            Picasso.with(mCtx).load(product.getImage()).resize(200,200).into(holder.imageView);
            holder.llcard.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent i = new Intent(MainActivity.this,Details.class);
                    i.putExtra("title",product.getTitle());
                    i.putExtra("shortdesc",product.getShortdesc());
                    i.putExtra("image",product.getImage());
                    startActivity(i);
                }
            });

        }

        @Override
        public int getItemCount() {
            return productList.size();
        }

        class ProductViewHolder extends RecyclerView.ViewHolder {

            TextView textViewTitle, textViewShortDesc, textViewRating, textViewPrice;
            public CardView llcard;
            ImageView imageView;

            public ProductViewHolder(View itemView) {
                super(itemView);

                llcard=itemView.findViewById(R.id.ll);
                textViewTitle = itemView.findViewById(R.id.textViewTitle);
                textViewShortDesc = itemView.findViewById(R.id.textViewShortDesc);
                textViewRating = itemView.findViewById(R.id.textViewRating);
                textViewPrice = itemView.findViewById(R.id.textViewPrice);
                imageView = itemView.findViewById(R.id.imageView);
            }
        }
    }


    public void loadProducts() {
        StringRequest stringRequest = new StringRequest(Request.Method.GET, URL_PRODUCTS,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            //converting the string to json array object
                            JSONArray array = new JSONArray(response);

                            //traversing through all the object
                            for (int i = 0; i < array.length(); i++) {

                                //getting product object from json array
                                JSONObject product = array.getJSONObject(i);

                                //adding the product to product list
                                productList.add(new Product(
                                        product.getInt("id"),
                                        product.getString("title"),
                                        product.getString("shortdesc"),
                                        product.getDouble("rating"),
                                        product.getDouble("price"),
                                        product.getString("image")
                                ));
                            }

                            //creating adapter object and setting it to recyclerview
                            ProductAdapter adapter = new ProductAdapter(MainActivity.this, productList);
                            recyclerView.setAdapter(adapter);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                    }
                });

        //adding our stringrequest to queue
        Volley.newRequestQueue(this).add(stringRequest);
    }
}