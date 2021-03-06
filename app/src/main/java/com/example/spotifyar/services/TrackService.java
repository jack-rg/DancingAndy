package com.example.spotifyar.services;

import android.content.Context;
import android.content.SharedPreferences;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.spotifyar.interfaces.VolleyCallBack;
import com.google.gson.Gson;
import com.spotify.protocol.types.Track;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class TrackService {
    private static final int NUM_TRACKS = 20; // the default number of tracks the api returns
    private ArrayList<Track> recentlyPlayedTracks = new ArrayList<>();
    private Track[] libraryTracks = new Track[NUM_TRACKS];
    private SharedPreferences sharedPreferences;
    private RequestQueue queue;


    // Create JSonObjectRequest and add to volley request queue
    // Request Queue will execute the JSonObjectRequest for us 

    public TrackService(Context context) {
        sharedPreferences = context.getSharedPreferences("SPOTIFY", 0);
        queue = Volley.newRequestQueue(context);
    }

    public ArrayList<Track> getRecentlyPlayedTracks() {
        return recentlyPlayedTracks;
    }

    public Track[] getLibraryTracks() {return libraryTracks; }

    public ArrayList<Track> getRecentlyPlayedTracks(final VolleyCallBack callBack) {
        String endpoint = "https://api.spotify.com/v1/me/player/recently-played";
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, endpoint, null, response -> {
                    Gson gson = new Gson();
                    JSONArray jsonArray = response.optJSONArray("items");
                    for (int n = 0; n < jsonArray.length(); n++) {
                        try {
                            JSONObject object = jsonArray.getJSONObject(n);
                            object = object.optJSONObject("track");

                            Track track = gson.fromJson(object.toString(), Track.class); // Put our json infor into a trackService class object
//                            Log.d("Inside Track Service", track.getUri());
                            recentlyPlayedTracks.add(track);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    callBack.onSuccess(); // Use volley callback 
                }, error -> {
                    // TODO: Handle error

                }) {

            // Add our authorization token to the jsonObjectRequest 
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
        return recentlyPlayedTracks;
    }
    
    public Track[] getLibraryTracks(final VolleyCallBack callBack) {
        String endpoint = "https://api.spotify.com/v1/me/tracks";
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, endpoint, null, response -> {
                    Gson gson = new Gson();
                    JSONArray jsonArray = response.optJSONArray("items");
                    for (int n = 0; n < jsonArray.length(); n++) {
                        try {
                            JSONObject object = jsonArray.getJSONObject(n);
                            object = object.optJSONObject("track");
                            Track track = gson.fromJson(object.toString(), Track.class);
                            libraryTracks[n] = track;
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    callBack.onSuccess();
                }, error -> {}) {
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
        return libraryTracks;
    }
}



