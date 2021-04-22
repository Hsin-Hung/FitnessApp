package com.example.fitnessapp;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Switch;

import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.TextView;

import com.example.fitnessapp.DatePickerFragment.OnDatePickListener;
import com.google.android.gms.auth.api.signin.GoogleSignInOptionsExtension;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Calendar;
import java.util.Date;
import java.util.Map;

import fitnessapp_objects.ChallengeRoom;
import fitnessapp_objects.ChallengeType;
import fitnessapp_objects.Database;
import fitnessapp_objects.Participant;
import fitnessapp_objects.UserAccount;

/**
 * this class represents the screen to enter all the needed info for creating a new challenge
 */
public class CreateChallengePresetActivity extends AppCompatActivity implements OnItemSelectedListener, OnDatePickListener, Database.UIUpdateCompletionHandler {

    private final int PASSWORD_MIN_LEN = 6;

    private EditText roomNameET, challDescrET, betAmountET, passwordET;
    private Button pickDateBTN;
    private Spinner challTypeSpinner;
    private Switch betSwitch;
    private TextView errorTV;

    private DialogFragment dialogFragment;

    private Date endDate;
    private ChallengeType challTypePicked;
    private boolean isBet;
    private ChallengeRoom room;

    private Database db;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_challenge_preset);

        roomNameET = (EditText) findViewById(R.id.room_name_et);
        challDescrET = (EditText) findViewById(R.id.chall_descr_et);
        betAmountET = (EditText) findViewById(R.id.bet_amount_et);
        passwordET = (EditText) findViewById(R.id.room_pass_et);

        challTypeSpinner = (Spinner) findViewById(R.id.chall_type_spinner);
        betSwitch = (Switch) findViewById(R.id.bet_switch);
        pickDateBTN = (Button) findViewById(R.id.pick_date_btn);
        errorTV = (TextView) findViewById(R.id.create_chall_error_tv);

        // initialize some default values
        isBet = false;
        challTypePicked = ChallengeType.DISTANCE;
        betAmountET.setEnabled(false);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.challenge_types, R.layout.challenge_type_spinner_item);

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        challTypeSpinner.setAdapter(adapter);
        challTypeSpinner.setOnItemSelectedListener(this);
        setInitDate();

        db = Database.getInstance();
        mAuth = FirebaseAuth.getInstance();

        betSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                isBet = isChecked;
                if (isChecked) {
                    betAmountET.setVisibility(View.VISIBLE);
                    betAmountET.setEnabled(true);
                } else {
                    betAmountET.setVisibility(View.INVISIBLE);
                    betAmountET.setEnabled(false);
                }
            }
        });

    }

    /**
     * set the date intervals for the date picker fragment
     */
    private void setInitDate() {
        final Calendar c = Calendar.getInstance();
        c.add(Calendar.DAY_OF_YEAR, 1);
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);
        String default_date = year + "/" + (month + 1) + "/" + day;
        pickDateBTN.setText(default_date);

        endDate = new Date(c.getTimeInMillis());
        dialogFragment = new DatePickerFragment(this);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        challTypePicked = ChallengeType.values()[position];
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        challTypePicked = ChallengeType.DISTANCE;
    }

    public void showDatePickerDialog(View v) {
        dialogFragment.show(getSupportFragmentManager(), "datePicker");
    }

    @Override
    public void setDate(int year, int month, int day) {
        String dateString = year + "/" + (month + 1) + "/" + day;
        pickDateBTN.setText(dateString);
        Calendar c = Calendar.getInstance();
        c.set(year, month, day, 0, 0);
        endDate.setTime(c.getTimeInMillis());
    }


    /**
     * check if all the field has valid input
     */
    public boolean checkAllFieldValid() {

        if (roomNameET.getText().toString().isEmpty()) {
            errorTV.setText(getString(R.string.roomNameError));
        } else if (challTypePicked == null) {
            errorTV.setText(getString(R.string.challTypeError));
        } else if (endDate == null) {
            errorTV.setText(getString(R.string.endDateError));
        } else if (betSwitch.isChecked() && betAmountET.getText().toString().isEmpty()) {
            errorTV.setText(getString(R.string.betError));
        } else if (passwordET.getText().toString().length() < PASSWORD_MIN_LEN) {
            errorTV.setText(getString(R.string.passError));
        } else {
            return true;
        }
        return false;
    }

    /**
     * create challenge button to create a challenge
     */
    public void createChallenge(View view) {
        if (checkAllFieldValid()) updateDatabase();
    }

    public boolean updateDatabase() {

        String name = roomNameET.getText().toString(), description = challDescrET.getText().toString(), password = passwordET.getText().toString();
        int betAmount = 0;
        if (!betAmountET.getText().toString().isEmpty()) {
            betAmount = Integer.parseInt(betAmountET.getText().toString());
        }

        room = new ChallengeRoom(name, challTypePicked, description, password, new Timestamp(endDate), isBet, betAmount);

        FirebaseUser user = mAuth.getCurrentUser();
        UserAccount userAccount = UserAccount.getInstance();
        room.addParticipant(new Participant(userAccount.getName(), user.getUid()));
        db.updateChallengeRoom(room, this);
        return true;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void updateUI(boolean isSuccess, Map<String, String> data) {

        if (isSuccess) {

            Intent intent = new Intent(this, ChallengeLobbyActivity.class);

            String roomID = data.get("roomID");
            if (roomID == null) return;
            room.setId(data.get("roomID"));

            intent.putExtra("endDate", room.getEndDate().toDate().getTime());
            intent.putExtra("challengeInfo", room.getFirestoreChallengeRoomMap());
            startActivity(intent);
        }

    }


}