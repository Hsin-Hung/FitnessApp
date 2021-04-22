package com.example.fitnessapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.stripe.android.ApiResultCallback;
import com.stripe.android.PaymentIntentResult;
import com.stripe.android.Stripe;
import com.stripe.android.model.ConfirmPaymentIntentParams;
import com.stripe.android.model.PaymentIntent;
import com.stripe.android.model.PaymentMethodCreateParams;
import com.stripe.android.view.CardInputWidget;

import org.w3c.dom.Text;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.Objects;

import fitnessapp_objects.Database;
import fitnessapp_objects.UserAccount;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * this class represents the screen for purchasing fitness coins with credit/debit cards
 */
public class PurchaseCoinActivity extends AppCompatActivity implements Database.UIUpdateCompletionHandler {

    private static final String BACKEND_URL = "https://fitnessapp501.herokuapp.com/";

    private OkHttpClient httpClient = new OkHttpClient();
    private String paymentIntentClientSecret;
    private Stripe stripe;
    private int amount;
    private EditText numCoinET;
    private FirebaseAuth mAuth;
    private Database db;
    private UserAccount userAccount;
    private TextView showCoinTV;
    private Button payBTN;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_purchase_coin);

        numCoinET = (EditText) findViewById(R.id.num_coin_et);
        showCoinTV = (TextView) findViewById(R.id.show_coin_tv);
        payBTN = (Button) findViewById(R.id.payButton);

        db = Database.getInstance();
        userAccount = UserAccount.getInstance();
        String displayCoin = "My Coins: "+ userAccount.getCoin();
        showCoinTV.setText(displayCoin);
        db.startCoinChangeListener(this);
    }

    @Override
    protected void onDestroy() {
        db.removeCoinChangeListener();
        super.onDestroy();
    }

    /**
     * start the buying and payment process
     */
    private void startCheckout() {

        mAuth = FirebaseAuth.getInstance();

        // Request a PaymentIntent from your server and store its client secret in paymentIntentClientSecret
        // Create a PaymentIntent by calling the sample server's /create-payment-intent endpoint.
        MediaType mediaType = MediaType.get("application/json; charset=utf-8");
        String json = "{"
                + "\"currency\":\"usd\","
                + "\"amount\":" + amount + ","
                + "\"userID\":" + "\"" + mAuth.getUid() + "\""
                + "}";
        System.out.println(json);
        RequestBody body = RequestBody.create(json, mediaType);
        Request request = new Request.Builder()
                .url(BACKEND_URL + "create-payment-intent")
                .post(body)
                .build();
        httpClient.newCall(request)
                .enqueue(new PayCallback(this));


    }

    public void pay(View view){

        String numCoin = numCoinET.getText().toString();

        // check if the user enters a valid number of coins to purchase. (at least 1 coin)
        if(numCoin.isEmpty() || Integer.parseInt(numCoin)<=0){
            return;
        }

        amount = Integer.parseInt(numCoin);
        payBTN.setEnabled(false);
        startCheckout();


    }

    private void proceedPayCallback(){

        CardInputWidget cardInputWidget = findViewById(R.id.cardInputWidget);
        PaymentMethodCreateParams params = cardInputWidget.getPaymentMethodCreateParams();
        if (params != null) {
            ConfirmPaymentIntentParams confirmParams = ConfirmPaymentIntentParams
                    .createWithPaymentMethodCreateParams(params, paymentIntentClientSecret);
            stripe.confirmPayment(this, confirmParams);
        }

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Handle the result of stripe.confirmPayment
        stripe.onPaymentResult(requestCode, data, new PaymentResultCallback(this));
    }

    private void onPaymentSuccess(@NonNull final Response response) throws IOException {
        Gson gson = new Gson();
        Type type = new TypeToken<Map<String, String>>(){}.getType();
        Map<String, String> responseMap = gson.fromJson(
                Objects.requireNonNull(response.body()).string(),
                type
        );

        // The response from the server includes the Stripe publishable key and
        // PaymentIntent details.
        // For added security, our sample app gets the publishable key from the server
        String stripePublishableKey = responseMap.get("publishableKey");
        paymentIntentClientSecret = responseMap.get("clientSecret");

        // Configure the SDK with your Stripe publishable key so that it can make requests to the Stripe API
        stripe = new Stripe(
                getApplicationContext(),
                Objects.requireNonNull(stripePublishableKey)
        );
        proceedPayCallback();
    }

    @Override
    public void updateUI(boolean isSuccess, Map<String, String> data) {

        if(isSuccess){

            String displayCoin = "My Coins: " + userAccount.getCoin();
            showCoinTV.setText(displayCoin);

        }

    }

    /**
     * PayCallback for the request to our backend server
     */
    private static final class PayCallback implements Callback {
        @NonNull private final WeakReference<PurchaseCoinActivity> activityRef;

        PayCallback(@NonNull PurchaseCoinActivity activity) {
            activityRef = new WeakReference<>(activity);
        }

        @Override
        public void onFailure(@NonNull Call call, @NonNull IOException e) {
            final PurchaseCoinActivity activity = activityRef.get();
            if (activity == null) {
                return;
            }
            Button payBTN = (Button) activity.findViewById(R.id.payButton);
            payBTN.setEnabled(true);
            activity.runOnUiThread(() ->
                    Toast.makeText(
                            activity, "Error: " + e.toString(), Toast.LENGTH_LONG
                    ).show()
            );
        }

        @Override
        public void onResponse(@NonNull Call call, @NonNull final Response response)
                throws IOException {
            final PurchaseCoinActivity activity = activityRef.get();
            if (activity == null) {
                return;
            }

            if (!response.isSuccessful()) {
                activity.runOnUiThread(() ->
                        Toast.makeText(
                                activity, "Error: " + response.toString(), Toast.LENGTH_LONG
                        ).show()
                );
            } else {
                activity.onPaymentSuccess(response);
            }
        }
    }

    /**
     * stripe payment callback
     */
    private static final class PaymentResultCallback
            implements ApiResultCallback<PaymentIntentResult> {
        @NonNull private final WeakReference<PurchaseCoinActivity> activityRef;

        PaymentResultCallback(@NonNull PurchaseCoinActivity activity) {
            activityRef = new WeakReference<>(activity);
        }

        @Override
        public void onSuccess(@NonNull PaymentIntentResult result) {
            final PurchaseCoinActivity activity = activityRef.get();
            if (activity == null) {
                return;
            }
            Button payBTN = (Button) activity.findViewById(R.id.payButton);
            payBTN.setEnabled(true);
            PaymentIntent paymentIntent = result.getIntent();
            PaymentIntent.Status status = paymentIntent.getStatus();
            if (status == PaymentIntent.Status.Succeeded) {
                // Payment completed successfully
                Toast.makeText(
                        activity, "Payment completed", Toast.LENGTH_LONG
                ).show();

            } else if (status == PaymentIntent.Status.RequiresPaymentMethod) {
                // Payment failed
                Toast.makeText(
                        activity, "Payment failed", Toast.LENGTH_LONG
                ).show();
            }
        }

        @Override
        public void onError(@NonNull Exception e) {
            final PurchaseCoinActivity activity = activityRef.get();
            if (activity == null) {
                return;
            }
            Button payBTN = (Button) activity.findViewById(R.id.payButton);
            payBTN.setEnabled(true);
            // Payment request failed – allow retrying using the same payment method
            Toast.makeText(
                    activity, "Payment request failed", Toast.LENGTH_LONG
            ).show();
        }
    }
}