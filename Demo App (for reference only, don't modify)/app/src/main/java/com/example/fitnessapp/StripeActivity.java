package com.example.fitnessapp;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.stripe.android.PaymentConfiguration;
import com.stripe.android.paymentsheet.PaymentResult;
import com.stripe.android.paymentsheet.PaymentSheet;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class StripeActivity extends AppCompatActivity {

    private final String DEFAULT_PAYMENT_VALUE = "10.99"; // $10.99

    private static final String BACKEND_URL = "https://fitnessapp501.herokuapp.com/";
    private static final String STRIPE_PUBLISHABLE_KEY = "pk_test_51IYoiTG4EHiTjcivFbozomjXG35nqPyiiYYFKlwEMGE4YgBuccRWmph6vGhLjaURKkMloROx88jky9YLILUyNbDA00akeGEl3N";

    private PaymentSheet paymentSheet;

    private String paymentIntentClientSecret;
    private String customerId;
    private String ephemeralKeySecret;

    private Button checkoutBT;
    private EditText amountPayET;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stripe);

        amountPayET = (EditText) findViewById(R.id.amountPayET);
        checkoutBT = findViewById(R.id.checkoutBT);

        // create the payment configuration with the Stripe public key
        PaymentConfiguration.init(this, STRIPE_PUBLISHABLE_KEY);
        paymentSheet = new PaymentSheet(this, result -> {
            onPaymentSheetResult(result);
        });


    }


    // fetch the data needed to call the stripe API : Payment Intent, Customer, Ephemeral Key by
    // creating a post request to backend server hosted on Heroku
    public void fetchInitData(View view) {



        if(amountPayET.getText().toString().isEmpty()){
            amountPayET.setText(DEFAULT_PAYMENT_VALUE);
            Toast.makeText(
                    this,
                    "We chose one for you!",
                    Toast.LENGTH_SHORT
            ).show();
        }else{
            checkoutBT.setText("Processing");
            checkoutBT.setEnabled(false);

            //process the number (e.g. from 10.99 to 1099)
            Long amount = Math.round(Double.parseDouble(amountPayET.getText().toString())*100);
            System.out.println(amount);
            // request the pay amount entered

            final String requestJson = "{\"amount\":"+amount+"}";

            // create the request body
            final RequestBody requestBody = RequestBody.create(
                    requestJson,
                    MediaType.get("application/json; charset=utf-8")
            );

            final Request request = new Request.Builder()
                    .url(BACKEND_URL + "payment-sheet")
                    .post(requestBody)
                    .build();

            // init the post request
            new OkHttpClient()
                    .newCall(request)
                    .enqueue(new Callback() {
                        @Override
                        public void onFailure(@NotNull Call call, @NotNull IOException e) {
                            // Handle failure
                            System.out.println(e);
                            resetCheckoutButton();
                        }

                        @Override
                        public void onResponse(
                                @NotNull Call call,
                                @NotNull Response response
                        ) throws IOException {
                            if (response.isSuccessful()) {
                                final JSONObject responseJson = parseResponse(response.body());
                                paymentIntentClientSecret = responseJson.optString("paymentIntent");
                                customerId = responseJson.optString("customer");
                                ephemeralKeySecret = responseJson.optString("ephemeralKey");

                                runOnUiThread(() -> presentPaymentSheet());


                            } else {
                                // Handle failure
                                System.out.println("not successful");
                            }

                            resetCheckoutButton();
                        }
                    });
        }


    }

    // helper function to parse JSON
    private JSONObject parseResponse(ResponseBody responseBody) {
        if (responseBody != null) {
            try {
                return new JSONObject(responseBody.string());
            } catch (IOException | JSONException e) {
                Log.e("App", "Error parsing response", e);
            }
        }
        return new JSONObject();
    }


    // present the Pre-build UI payment sheet with the retrieved objects (Payment Intent, Customer, Ephemeral Key)
    private void presentPaymentSheet() {

        paymentSheet.present(
                paymentIntentClientSecret,
                new PaymentSheet.Configuration(
                        "FitnessApp, Inc.",
                        new PaymentSheet.CustomerConfiguration(
                                customerId,
                                ephemeralKeySecret
                        )
                )
        );
    }

    // after payment, it shows the result of the payment
    private void onPaymentSheetResult(
            final PaymentResult paymentResult
    ) {
        if (paymentResult instanceof PaymentResult.Canceled) {
            Toast.makeText(
                    this,
                    "Payment Canceled",
                    Toast.LENGTH_LONG
            ).show();
        } else if (paymentResult instanceof PaymentResult.Failed) {
            Toast.makeText(
                    this,
                    "Payment Failed. See logcat for details.",
                    Toast.LENGTH_LONG
            ).show();

            Log.e("App", "Got error: ", ((PaymentResult.Failed) paymentResult).getError());
        } else if (paymentResult instanceof PaymentResult.Completed) {
            Toast.makeText(
                    this,
                    "Payment Complete",
                    Toast.LENGTH_LONG
            ).show();
        }
    }

    private void resetCheckoutButton(){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                checkoutBT.setEnabled(true);
                checkoutBT.setText("CHECKOUT");
            }
        });
    }


}