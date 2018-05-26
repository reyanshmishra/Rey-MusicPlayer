/*
package com.reyansh.audio.audioplayer.free.LauncherActivity;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.anjlab.android.iab.v3.BillingProcessor;
import com.anjlab.android.iab.v3.SkuDetails;
import com.anjlab.android.iab.v3.TransactionDetails;
import com.reyansh.audio.audioplayer.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

*/
/**
 * Created by reyansh on 1/14/18.
 *//*


public class DonateActivity extends AppCompatActivity implements BillingProcessor.IBillingHandler {
    private BillingProcessor bp;
    private TextView mStatus;
    private static final String DONATION_1 = "com.reyansh.audio.audioplayer.free_donate1";
    private ProgressBar progressBar;
    private boolean readyToPurchase = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_donate);


        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Support development");
        bp = new BillingProcessor(this, "", this);
        mStatus = findViewById(R.id.donation_status);
        productListView = findViewById(R.id.product_list);
        progressBar = findViewById(R.id.progressBar);
    }


    @Override
    public void onProductPurchased(String productId, TransactionDetails details) {
        readyToPurchase = true;
        runOnUiThread(() -> Toast.makeText(DonateActivity.this, "Thanks for your support!", Toast.LENGTH_SHORT).show());
    }

    @Override
    public void onBillingError(int errorCode, Throwable error) {
        runOnUiThread(() -> Toast.makeText(DonateActivity.this, "Unable to process purchase", Toast.LENGTH_SHORT).show());

    }


    private void checkStatus() {
        progressBar.setVisibility(View.VISIBLE);

        new AsyncTask<Void, Void, Boolean>() {
            @Override
            protected Boolean doInBackground(Void... voids) {
                List<String> owned = bp.listOwnedProducts();
                return owned != null && owned.size() != 0;
            }

            @Override
            protected void onPostExecute(Boolean b) {
                super.onPostExecute(b);
                if (b) {
                    mStatus.setText("Thanks for your support!");
                } else {
                    mStatus.setText("No previous purchase found");
                    getProducts();
                }
            }
        }.execute();
    }

    private LinearLayout productListView;

    private void getProducts() {

        new AsyncTask<Void, Void, List<SkuDetails>>() {
            @Override
            protected List<SkuDetails> doInBackground(Void... voids) {

                ArrayList<String> products = new ArrayList<>();

                products.add(DONATION_1);

                return bp.getPurchaseListingDetails(products);
            }

            @Override
            protected void onPostExecute(List<SkuDetails> productList) {
                super.onPostExecute(productList);

                if (productList == null)
                    return;

                Collections.sort(productList, (skuDetails, t1) -> {
                    if (skuDetails.priceValue >= t1.priceValue)
                        return 1;
                    else if (skuDetails.priceValue <= t1.priceValue)
                        return -1;
                    else return 0;
                });
                for (int i = 0; i < productList.size(); i++) {
                    final SkuDetails product = productList.get(i);
                    View rootView = LayoutInflater.from(DonateActivity.this).inflate(R.layout.item_donate_product, productListView, false);

                    TextView detail = rootView.findViewById(R.id.product_detail);
                    detail.setText(product.priceText);

                    rootView.findViewById(R.id.btn_donate).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if (readyToPurchase)
                                bp.purchase(DonateActivity.this, product.productId);
                            else
                                Toast.makeText(DonateActivity.this, "Unable to initiate purchase", Toast.LENGTH_SHORT).show();
                        }
                    });

                    productListView.addView(rootView);

                }
                progressBar.setVisibility(View.GONE);
            }
        }.execute();
    }

    @Override
    public void onPurchaseHistoryRestored() {

    }


    @Override
    public void onBillingInitialized() {
        readyToPurchase = true;
        getProducts();
    }
}
*/
