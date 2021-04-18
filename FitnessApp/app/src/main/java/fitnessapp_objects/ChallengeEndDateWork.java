package fitnessapp_objects;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

public class ChallengeEndDateWork extends Worker {

    Context context;
    Database db;

    public ChallengeEndDateWork(
            @NonNull Context context,
            @NonNull WorkerParameters params) {
        super(context, params);
        this.context = context;
        db = Database.getInstance();
    }

    @Override
    public Result doWork() {

        // Do the work here--in this case, upload the images.
        String roomIDInput = getInputData().getString("roomID");
        if(roomIDInput == null) {
            return Result.failure();
        }
        db.endChallenge(roomIDInput, context);
        // Indicate whether the work finished successfully with the Result

        return Result.success();
    }

}
