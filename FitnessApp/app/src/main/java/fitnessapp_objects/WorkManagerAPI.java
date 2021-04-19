package fitnessapp_objects;

import android.util.Log;

import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import com.google.common.util.concurrent.ListenableFuture;

import java.util.List;
import java.util.concurrent.ExecutionException;

public class WorkManagerAPI {

    final String TAG = "WorkManagerAPI";
    static WorkManagerAPI workManagerAPI = null;

    private WorkManagerAPI(){}

    public static WorkManagerAPI getInstance(){

        if(workManagerAPI==null)workManagerAPI = new WorkManagerAPI();
        return workManagerAPI;

    }

    public void cancelAllTask(WorkManager workManager, String tag){

        workManager.cancelAllWorkByTag(tag);

    }

    public void viewAllWork(WorkManager workManager, String tag){

        ListenableFuture<List<WorkInfo>> listListenableFuture = workManager.getWorkInfosByTag(tag);

        try {
            for(WorkInfo w: listListenableFuture.get()){

                Log.i(TAG, w.toString());

            }
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

}
