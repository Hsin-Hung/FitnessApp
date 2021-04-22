package com.example.fitnessapp;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import java.util.HashMap;
import java.util.Map;

/**
 * this class represents the info of the challenge which include, name, description, end data, bet amount, and etc.
 */
public class ChallengeInfoActivity extends AppCompatActivity {

    private TextView descrypTV, typeTV, endDateTV, betAmountTV;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_challenge_info);

        descrypTV = (TextView) findViewById(R.id.chall_descrp_info_tv);
        typeTV = (TextView) findViewById(R.id.chall_type_info_tv);
        endDateTV = (TextView) findViewById(R.id.end_date_info_tv);
        betAmountTV = (TextView) findViewById(R.id.bet_amount_info_tv);

        //noinspection unchecked
        Map<String,String> challengeInfo = (HashMap<String,String>) getIntent().getSerializableExtra("challengeInfo");

        descrypTV.setText(challengeInfo.get("description"));
        typeTV.setText(challengeInfo.get("type"));
        endDateTV.setText(challengeInfo.get("endDate"));
        betAmountTV.setText(challengeInfo.get("betAmount"));

    }

    public void back(View view){
        finish();
    }
}