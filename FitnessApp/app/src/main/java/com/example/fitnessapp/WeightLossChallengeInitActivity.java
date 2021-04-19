package com.example.fitnessapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.okhttp.MultipartBuilder;
import com.stripe.android.model.ConfirmPaymentIntentParams;
import com.stripe.android.model.PaymentMethodCreateParams;
import com.stripe.android.view.CardInputWidget;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import fitnessapp_objects.Database;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class WeightLossChallengeInitActivity extends AppCompatActivity implements Database.UIUpdateCompletionHandler {

    private static final String BACKEND_URL = "http://10.0.2.2:5000/";
    private OkHttpClient httpClient = new OkHttpClient();
    static final int REQUEST_IMAGE_CAPTURE = 1;
    ImageView weightImg, gestureImg;
    EditText enter_weight_et;
    TextView randomGestureTV;
    Button takePhotoBTN, submitBTN;
    private FirebaseAuth mAuth;
    Bitmap imageBitmap;
    HashMap<String,String> challengeInfo;
    long endDate;
    Database db;
    FirebaseStorage storage = FirebaseStorage.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weight_loss_challenge_init);
        weightImg = (ImageView) findViewById(R.id.weight_img);
        gestureImg = (ImageView) findViewById(R.id.gesture_img);
        enter_weight_et = (EditText) findViewById(R.id.enter_weight_et);
        randomGestureTV = (TextView) findViewById(R.id.random_gesture_tv);
        takePhotoBTN = (Button) findViewById(R.id.take_photo_btn);
        submitBTN = (Button) findViewById(R.id.submit_btn);

        takePhotoBTN.setEnabled(false);
        submitBTN.setEnabled(false);
        challengeInfo = (HashMap<String,String>) getIntent().getSerializableExtra("challengeInfo");
        endDate = getIntent().getLongExtra("endDate",0);
        db = Database.getInstance();

    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        try {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        } catch (ActivityNotFoundException e) {
            // display error state to the user
        }
    }

    @SuppressLint("MissingSuperCall")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            imageBitmap = (Bitmap) extras.get("data");
            weightImg.setImageBitmap(imageBitmap);
        }
    }

    public void takePhoto(View view){

        dispatchTakePictureIntent();

    }

    public void submit(View view){

        String weight = enter_weight_et.getText().toString();

        if(weight.isEmpty() || imageBitmap==null )return;

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        String challengeID = challengeInfo.get("roomID");

        // Create a storage reference from our app
        StorageReference storageRef = storage.getReference();

        // Create a reference to 'images/mountains.jpg'
        StorageReference weightImagesRef = storageRef.child(challengeID + "/" + user.getUid() + "/startWeight.jpg");

        StorageMetadata metadata = new StorageMetadata.Builder()
                .setCustomMetadata("weight", weight)
                .setCustomMetadata("userID", user.getUid())
                .setCustomMetadata("challengeID", challengeID)
                .build();

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos);
        byte[] data = bos.toByteArray();

        db.uploadImg(weightImagesRef, metadata, data,  this);

    }

    public void generate(View view){

        int[] images = new int[]{R.drawable.g1, R.drawable.g2, R.drawable.g3};
        Random rand = new Random();
        gestureImg.setImageResource(images[rand.nextInt(images.length)]);
        takePhotoBTN.setEnabled(true);
        submitBTN.setEnabled(true);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        takePhotoBTN.setEnabled(false);
                        submitBTN.setEnabled(false);
                    }
                });
            }
        }, 30000);



    }

    @Override
    public void updateUI(boolean isSuccess, Map<String, String> data) {
        if(isSuccess){
            FirebaseAuth mAuth = FirebaseAuth.getInstance();
            FirebaseUser user = mAuth.getCurrentUser();
            String imgPath = data.get("imgPath"), weight = data.get("weight");


// Request a PaymentIntent from your server and store its client secret in paymentIntentClientSecret
            // Create a PaymentIntent by calling the sample server's /create-payment-intent endpoint.
            MediaType mediaType = MediaType.get("application/json; charset=utf-8");

            String json = "{"
                    + "\"userID\":" + "\"" + user.getUid() + "\","
                    + "\"roomID\":" + "\"" + challengeInfo.get("roomID") + "\","
                    + "\"imgPath\":" + "\"" + imgPath + "\","
                    + "\"weight\":" + Float.parseFloat(weight)
                    + "}";

            RequestBody body = RequestBody.create(json, mediaType);
            Request request = new Request.Builder()
                    .url(BACKEND_URL + "weight-verify")
                    .post(body)
                    .build();
            httpClient.newCall(request)
                    .enqueue(new PayCallback(this));

        }
    }

    private void startWeightChallenge(){

        Intent intent = new Intent(this, WeightChallengeActivity.class);
        intent.putExtra("challengeInfo", challengeInfo);
        startActivity(intent);

    }

    /**
     * PayCallback for the request to our backend server
     */
    private static final class PayCallback implements Callback {
        @NonNull private final WeakReference<WeightLossChallengeInitActivity> activityRef;

        PayCallback(@NonNull WeightLossChallengeInitActivity activity) {
            activityRef = new WeakReference<>(activity);
        }

        @Override
        public void onFailure(@NonNull Call call, @NonNull IOException e) {
            final WeightLossChallengeInitActivity activity = activityRef.get();
            if (activity == null) {
                return;
            }
            activity.runOnUiThread(() ->
                    Toast.makeText(
                            activity, "Error: " + e.toString(), Toast.LENGTH_LONG
                    ).show()
            );
        }

        @Override
        public void onResponse(@NonNull Call call, @NonNull final Response response)
                throws IOException {
            final WeightLossChallengeInitActivity activity = activityRef.get();
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
                activity.startWeightChallenge();
            }
        }
    }
}