package fitnessapp_objects;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.FitnessOptions;
import com.google.android.gms.fitness.data.Bucket;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.request.DataReadRequest;
import com.google.android.gms.fitness.result.DataReadResponse;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Timestamp;

import java.util.Date;
import java.util.concurrent.TimeUnit;

public class WeightChallengePeriodicWork extends Worker {


    private final String TAG = "ChallengeWork";
    private Database db;
    private long currentTime;
    private Context context;

    public WeightChallengePeriodicWork(
            @NonNull Context context,
            @NonNull WorkerParameters params) {
        super(context, params);
        db = Database.getInstance();
        currentTime = new Timestamp(new Date()).toDate().getTime();
        this.context = context;
    }
    @NonNull
    @Override
    public Result doWork() {

        String roomIDInput = getInputData().getString("roomID");
        if(roomIDInput == null) {
            return Result.failure();
        }
        db.setWeightPrompt(roomIDInput);

        return Result.success();
    }
}
