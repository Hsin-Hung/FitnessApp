package fitnessapp_objects;

import android.util.Log;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptionsExtension;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.FitnessOptions;
import com.google.android.gms.fitness.data.DataType;

/**
 * a class that act as an API to access googlefit related methods
 */
public class GoogleFitAPI {

    private final String TAG = "GoogleFitAPI";
    private static GoogleFitAPI googleFitAPI_instance = null;
    private GoogleSignInOptionsExtension fitnessOptions;

    private GoogleFitAPI(){

       fitnessOptions =
                FitnessOptions.builder()
                        .addDataType(DataType.AGGREGATE_DISTANCE_DELTA, FitnessOptions.ACCESS_READ)
                        .build();


    }

    public static GoogleFitAPI getInstance(){

        if(googleFitAPI_instance==null){

            googleFitAPI_instance = new GoogleFitAPI();

        }
        return googleFitAPI_instance;

    }


}
