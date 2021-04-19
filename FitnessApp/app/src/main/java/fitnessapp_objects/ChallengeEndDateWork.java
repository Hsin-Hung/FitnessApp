package fitnessapp_objects;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

public class ChallengeEndDateWork extends Worker {

    private static final String BACKEND_URL = "https://fitnessapp501.herokuapp.com/";
    private OkHttpClient httpClient = new OkHttpClient();
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
        MediaType mediaType = MediaType.get("application/json; charset=utf-8");
        String json = "{"
                + "\"roomID\":" + "\"" + roomIDInput + "\""
                + "}";
        System.out.println(json);
        RequestBody body = RequestBody.create(json, mediaType);
        Request request = new Request.Builder()
                .url(BACKEND_URL + "bet-payout")
                .post(body)
                .build();
        try {
            httpClient.newCall(request)
            .execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
        // Indicate whether the work finished successfully with the Result

        return Result.success();
    }



}
