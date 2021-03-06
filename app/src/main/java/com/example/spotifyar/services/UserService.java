package com.example.spotifyar.services;

import android.content.SharedPreferences;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.example.spotifyar.interfaces.VolleyCallBack;
import com.example.spotifyar.models.User;
import com.google.gson.Gson;

import java.util.HashMap;
import java.util.Map;

public class UserService {
    private static final String ENDPOINT = "https://api.spotify.com/v1/me";
    private SharedPreferences sharedPreferences;
    private RequestQueue queue;
    private User user;

    public UserService(RequestQueue queue, SharedPreferences sharedPreferences) {
        this.sharedPreferences = sharedPreferences;
        this.queue = queue;
    }

    public User getUser() {
        return user;
    }

    // Create JSONObjectRequest and add to Volley Request Queue
    // Request Queue will execute the JSONObjectRequest for us
    public void get(final VolleyCallBack callBack) {
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(ENDPOINT, null, response -> {
            Gson gson = new Gson();
            user = gson.fromJson(response.toString(), User.class); // Put our json info into a User class object
            callBack.onSuccess(); // use our volley callback
        }, error -> get(() -> {Log.v("Fail", "WE FAILED SO SAD XDDDDDDDDDDDDD");})) {
            // Add our authorization token to the jsonObjectRequst
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                String token = sharedPreferences.getString("token", "");
                String auth = "Bearer " + token;
                headers.put("Authorization", auth);
                return headers;
            }
        };
        queue.add(jsonObjectRequest);
    }
}
