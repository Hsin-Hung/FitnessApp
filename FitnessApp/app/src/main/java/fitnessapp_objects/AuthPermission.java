package fitnessapp_objects;

import com.example.fitnessapp.R;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.fitness.FitnessOptions;
import com.google.android.gms.fitness.data.DataType;

/**
 * helper class for reusing authentication related methods
 */
public class AuthPermission {

    public static AuthPermission authPermission = null;


    private AuthPermission(){}

    public static AuthPermission getInstance(){

        if(authPermission==null) authPermission = new AuthPermission();
        return authPermission;

    }

    /**
     * get the fitness options needed for this app to pass to google sign in
     */
    public FitnessOptions getFitnessOption(){

        FitnessOptions fitnessOptions =
                FitnessOptions.builder()
                        .addDataType(DataType.TYPE_DISTANCE_DELTA, FitnessOptions.ACCESS_READ)
                        .addDataType(DataType.AGGREGATE_DISTANCE_DELTA, FitnessOptions.ACCESS_READ)
                        .addDataType(DataType.TYPE_WEIGHT, FitnessOptions.ACCESS_READ)
                        .build();

        return fitnessOptions;
    }


    }

