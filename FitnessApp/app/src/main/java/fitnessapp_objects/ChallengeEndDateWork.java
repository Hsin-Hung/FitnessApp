package fitnessapp_objects;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

public class ChallengeEndDateWork extends Worker {

    Context context;
    Database db;
    BackendAPI backendAPI;

    public ChallengeEndDateWork(
            @NonNull Context context,
            @NonNull WorkerParameters params) {
        super(context, params);
        this.context = context;
        db = Database.getInstance();
        backendAPI = BackendAPI.getInstance();
    }

    @Override
    public Result doWork() {

        // Do the work here--in this case, upload the images.
        String roomIDInput = getInputData().getString("roomID");
        if(roomIDInput == null) {
            return Result.failure();
        }
        backendAPI.endChallenge(roomIDInput);
        // Indicate whether the work finished successfully with the Result

        return Result.success();
    }

}
