package com.example.fitnessapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
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

/**
 * this class represents the screen that prompts the participant for weight image, which the backend will verify to prevent cheating
 */
public class WeightLossChallengeInitActivity extends AppCompatActivity implements Database.UIUpdateCompletionHandler {

    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final String BACKEND_URL = "https://fitnessapp501.herokuapp.com/";
    private String TAG = "WeightChallengeInitActivity";
    private final long TIME_TO_SUBMIT = 30000;

    private OkHttpClient httpClient = new OkHttpClient();
    private ImageView weightImg, gestureImg;
    private EditText enter_weight_et;
    private Button takePhotoBTN, submitBTN;
    private Bitmap imageBitmap;
    private HashMap<String,String> challengeInfo;
    private long endDate;
    private Database db;
    private FirebaseStorage storage = FirebaseStorage.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weight_loss_challenge_init);

        weightImg = (ImageView) findViewById(R.id.weight_img);
        gestureImg = (ImageView) findViewById(R.id.gesture_img);
        enter_weight_et = (EditText) findViewById(R.id.enter_weight_et);
        takePhotoBTN = (Button) findViewById(R.id.take_photo_btn);
        submitBTN = (Button) findViewById(R.id.submit_btn);

        takePhotoBTN.setEnabled(false);
        submitBTN.setEnabled(false);
        //noinspection unchecked
        challengeInfo = (HashMap<String,String>) getIntent().getSerializableExtra("challengeInfo");
        endDate = getIntent().getLongExtra("endDate",0);
        db = Database.getInstance();

    }

    /**
     * take a picture of your scale with the generated gesture
     */
    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        try {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        } catch (ActivityNotFoundException e) {
            // display error state to the user
            Log.i(TAG, "Error taking photo");
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
        if (this.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)){
            // this device has a camera
            dispatchTakePictureIntent();
        } else {
            // no camera on this device
            Log.i(TAG, "No camera");
        }
    }

    /**
     * submit the photo and the input weight
     */
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
        }, TIME_TO_SUBMIT);



    }

    @Override
    public void updateUI(boolean isSuccess, Map<String, String> data, int callbackCode) {
        if(isSuccess){
            FirebaseAuth mAuth = FirebaseAuth.getInstance();
            FirebaseUser user = mAuth.getCurrentUser();
            String imgPath = data.get("imgPath"), weight = data.get("weight");

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
        intent.putExtra("endDate", endDate);
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