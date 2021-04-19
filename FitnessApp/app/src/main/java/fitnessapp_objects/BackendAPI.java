package fitnessapp_objects;

import com.example.fitnessapp.PurchaseCoinActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

public class BackendAPI {

    private static final String BACKEND_URL = "https://fitnessapp501.herokuapp.com/";

    private OkHttpClient httpClient = new OkHttpClient();
    static BackendAPI backendAPI = null;
    private BackendAPI(){}

    public static BackendAPI getInstance(){

        if(backendAPI==null)backendAPI = new BackendAPI();
        return backendAPI;

    }

    public boolean endChallenge(String roomID){


        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();

        MediaType mediaType = MediaType.get("application/json; charset=utf-8");
        String json = "{"
                + "\"roomID\":" + "\"" + roomID + "\""
                + "}";
        System.out.println(json);
        RequestBody body = RequestBody.create(json, mediaType);
        Request request = new Request.Builder()
                .url(BACKEND_URL + "bet-payout")
                .post(body)
                .build();
        httpClient.newCall(request);


        return true;


    }

    public boolean placeBet(int amount){

//        FirebaseAuth mAuth = FirebaseAuth.getInstance();
//        FirebaseUser user = mAuth.getCurrentUser();
//
//        MediaType mediaType = MediaType.get("application/json; charset=utf-8");
//        String json = "{"
//                + "\"betAmount\":" + amount + ","
//                + "\"userID\":" + "\"" + user.getUid() + "\""
//                + "}";
//        System.out.println(json);
//        RequestBody body = RequestBody.create(json, mediaType);
//        Request request = new Request.Builder()
//                .url(BACKEND_URL + "create-payment-intent")
//                .post(body)
//                .build();
//        httpClient.newCall(request)
//                .enqueue(new PurchaseCoinActivity.PayCallback(this));


        return true;
    }


}
