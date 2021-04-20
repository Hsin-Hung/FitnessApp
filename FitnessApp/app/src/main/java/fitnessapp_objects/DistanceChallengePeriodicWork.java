package fitnessapp_objects;

import android.content.Context;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.FitnessOptions;
import com.google.android.gms.fitness.data.Bucket;
import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.fitness.request.DataReadRequest;
import com.google.firebase.Timestamp;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class DistanceChallengePeriodicWork extends Worker {

    private final String TAG = "ChallengeWork";
    private Database db;
    private long currentTime;
    private Context context;

    public DistanceChallengePeriodicWork(
            @NonNull Context context,
            @NonNull WorkerParameters params) {
        super(context, params);
        db = Database.getInstance();
        currentTime = new Timestamp(new Date()).toDate().getTime();
        this.context = context;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public Result doWork() {

        String roomIDInput = getInputData().getString("roomID");
        Long startTimeInput = getInputData().getLong("startDate", currentTime);
        if(roomIDInput == null) {
            return Result.failure();
        }

        // Read the data that's been collected throughout the past week.
        long endTime = new Timestamp(new Date()).toDate().getTime();
        long startTime = startTimeInput;

        DataReadRequest readRequest = new DataReadRequest.Builder()
                // The data request can specify multiple data types to return,
                // effectively combining multiple data queries into one call.
                // This example demonstrates aggregating only one data type.
                .aggregate(DataType.AGGREGATE_DISTANCE_DELTA)
                .bucketByTime(1, TimeUnit.DAYS)
                .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
                .build();

        FitnessOptions fitnessOptions = AuthPermission.getInstance().getFitnessOption();

        Fitness.getHistoryClient(context, GoogleSignIn.getAccountForExtension(context, fitnessOptions))
                .readData(readRequest)
                .addOnSuccessListener (response -> {
                    // The aggregate query puts datasets into buckets, so convert to a
                    // single list of datasets
                    float totalDistance = 0;
                    for (Bucket bucket : response.getBuckets()) {
                        for (DataSet dataSet : bucket.getDataSets()) {
                            totalDistance += dumpDataSet(dataSet);
                        }
                    }

                    db.updateChallengeStats(roomIDInput, totalDistance, ChallengeType.DISTANCE);

                })
                .addOnFailureListener(e ->
                        Log.w(TAG, "There was an error reading data from Google Fit", e));


        return Result.success();
    }

    private float dumpDataSet(DataSet dataSet) {

        float distance = 0;

        Log.i(TAG, "Data returned for Data type: " + dataSet.getDataType().getName());
        for (DataPoint dp : dataSet.getDataPoints()) {
            Log.i(TAG,"Data point:");
            Log.i(TAG,"\tType:" + dp.getDataType().getName());
            Log.i(TAG,"\tStart: " + dp.getStartTime(TimeUnit.MILLISECONDS));
            Log.i(TAG,"\tEnd: " + dp.getEndTime(TimeUnit.MILLISECONDS));
            for (Field field : dp.getDataType().getFields()) {
                distance += dp.getValue(field).asFloat();
                Log.i(TAG,"\tField: "+field.getName()+" Value: " + dp.getValue(field));
            }
        }

        return distance;
    }
}
