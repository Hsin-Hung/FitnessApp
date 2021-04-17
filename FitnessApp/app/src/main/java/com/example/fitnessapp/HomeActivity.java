package com.example.fitnessapp;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.PopupMenu;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInOptionsExtension;
import com.google.android.gms.fitness.FitnessOptions;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;

import fitnessapp_objects.UserAccount;

public class HomeActivity extends AppCompatActivity {

    private GoogleSignInClient mGoogleSignInClient;
    private final int GOOGLE_FIT_PERMISSIONS_REQUEST_CODE = 1;
    GoogleSignInOptionsExtension fitnessOptions;
    private FirebaseAuth mAuth;
    
    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        mAuth = FirebaseAuth.getInstance();

        System.out.println(UserAccount.getInstance().getEmail());
        System.out.println(UserAccount.getInstance().getName());

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        // Build a GoogleSignInClient with the options specified by gso.
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        fitnessAPIAuth();

        System.out.println("HOME ACTIVITY CREATED");

    }

    public void showMenu(View v) {
        // This method implements OnMenuItemClickListener
        // To add more items in the menu
        // 1. modify res/menu/main_menu.xml for views in the menu
        // 2. add the case in onMenuItemClick method (right after this method)
        PopupMenu popup = new PopupMenu(this, v);

        popup.setOnMenuItemClickListener(this::onMenuItemClick);
        popup.inflate(R.menu.main_menu);
        popup.show();
    }

    @SuppressLint("NonConstantResourceId")
    public boolean onMenuItemClick(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.settings: // open the setting page
                Intent goToSettings_intent = new Intent(getApplicationContext(), SettingsActivity.class);
                startActivity(goToSettings_intent);
                return true;
            case R.id.sign_out: // sign out
                signOut();
                return true;
            case R.id.buy_coin: // open the purchase page
                Intent goToPurchase_intent = new Intent(this, PurchaseCoinActivity.class);
                startActivity(goToPurchase_intent);
                return true;
            default:
                return false;
        }
    }

    public void createChallenge(View view){

        Intent intent = new Intent(this, CreateChallengePresetActivity.class);
        startActivity(intent);

    }

    public void joinChallenge(View view){

        Intent intent = new Intent(this, JoinChallengeActivity.class);
        startActivity(intent);

    }

    public void myChallenges(View view){

        Intent intent = new Intent(this, MyChallengesActivity.class);
        startActivity(intent);

    }



    public void signOut(){

        FirebaseAuth.getInstance().signOut();
        mGoogleSignInClient.signOut().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                UserAccount.getInstance().eraseAccount();
                Intent intent = new Intent(HomeActivity.this, LaunchActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        });
    }


    @Override
    public void onBackPressed() {
        //do nothing
    }


    @RequiresApi(api = Build.VERSION_CODES.Q)
    public void fitnessAPIAuth(){

        fitnessOptions =
                FitnessOptions.builder()
                        .addDataType(DataType.TYPE_DISTANCE_DELTA, FitnessOptions.ACCESS_READ)
                        .addDataType(DataType.AGGREGATE_DISTANCE_DELTA, FitnessOptions.ACCESS_READ)
                        .build();

        GoogleSignInAccount account = GoogleSignIn.getAccountForExtension(this, fitnessOptions);

        if (!GoogleSignIn.hasPermissions(account, fitnessOptions)) {
            GoogleSignIn.requestPermissions(
                    this, // your activity
                    GOOGLE_FIT_PERMISSIONS_REQUEST_CODE, // e.g. 1
                    account,
                    fitnessOptions);
        } else {
            Toast.makeText(HomeActivity.this, "You have gotten permission! Yay",
                    Toast.LENGTH_SHORT).show();
        }

    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        System.out.println("request code: " + requestCode + "result code: " + resultCode);

        if(requestCode==Activity.RESULT_OK){

            switch (requestCode){

                case GOOGLE_FIT_PERMISSIONS_REQUEST_CODE:
                    System.out.println(" Successfully granted permissions !");
//                    Toast.makeText(HomeActivity.this, " Successfully granted permissions !",
//                            Toast.LENGTH_SHORT).show();
                    break;
                default:
                    Toast.makeText(HomeActivity.this, " idk where this from!",
                            Toast.LENGTH_SHORT).show();
            }

        }

    }

    public void revoke(View view){

        mGoogleSignInClient.revokeAccess();

    }

    public void checkPermission(View view){

        fitnessOptions =
                FitnessOptions.builder()
                        .addDataType(DataType.TYPE_DISTANCE_DELTA, FitnessOptions.ACCESS_READ)
                        .addDataType(DataType.AGGREGATE_DISTANCE_DELTA, FitnessOptions.ACCESS_READ)
                        .build();

        GoogleSignInAccount account = GoogleSignIn.getAccountForExtension(this, fitnessOptions);

        boolean check = GoogleSignIn.hasPermissions(account, fitnessOptions);

        Toast.makeText(HomeActivity.this, String.valueOf(check),
                Toast.LENGTH_SHORT).show();
    }


}