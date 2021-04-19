package com.example.fitnessapp;

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

import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class WeightLossChallengeInitActivity extends AppCompatActivity {

    static final int REQUEST_IMAGE_CAPTURE = 1;
    ImageView weightImg, gestureImg;
    EditText enter_weight_et;
    TextView randomGestureTV;
    Button takePhotoBTN, submitBTN;

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
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            weightImg.setImageBitmap(imageBitmap);
        }
    }

    public void takePhoto(View view){

        dispatchTakePictureIntent();

    }

    public void submit(View view){

        String weight = enter_weight_et.getText().toString();

        if(weight.isEmpty())return;



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
}