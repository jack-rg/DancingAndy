package com.example.spotifyar.services;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

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

import java.util.HashMap;
import java.util.Map;

/**
 * Service to play selected songs.
 */
public class PlayerService {
    private static final String PLAY_ENDPOINT = "https://api.spotify.com/v1/me/player/play";
    private static final String PAUSE_ENDPOINT = "https://api.spotify.com/v1/me/player/pause";
    private SharedPreferences sharedPreferences;
    private RequestQueue queue;


    private JSONObject songsToPlay = new JSONObject();

    Track currentPlayingTrack;

    // Creqte JSonObjectRequest and add to volley request queue
    // Request Queue will execute the JSonObjectRequest for us 

    public PlayerService(Context context){
        sharedPreferences = context.getSharedPreferences("SPOTIFY", 0);
        queue = Volley.newRequestQueue(context);
    }

    public void addSongToPlaybackQueue(Track track){
        JSONArray uriArray = new JSONArray();
        uriArray.put(track.uri);
        try {
            songsToPlay.put("uris", uriArray);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void addArtistAlbumToPlaybackQueue(String uri) {
        try {
            songsToPlay.put("context_uri", uri);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void addTrackToPlaybackQueue(String uri) {
        JSONArray uriArray = new JSONArray();
        uriArray.put(uri);
        try {
             songsToPlay.put("uris", uriArray);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void playQueuedSong() {
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.PUT, PLAY_ENDPOINT,
                songsToPlay, response -> {
                    Log.d("Player Service", response.toString());
                }, error -> {
                    Log.d("Player Service", error.toString());
                }) {

           // Add our authorization token to the jsonObjectRequst

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                String token = sharedPreferences.getString("token", "");
                String auth = "Bearer " + token;
                headers.put("Authorization", auth);
                headers.put("Content-Type", "application/json");
                return headers;
            }
        };

        queue.add(jsonObjectRequest);
    }


//
    public void pausePlayback() {
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.PUT, PAUSE_ENDPOINT,
                null, response -> {
                    Log.d("Player Service", response.toString());
                }, error -> {
                    Log.d("Player Service", error.toString());
                }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                String token = sharedPreferences.getString("token", "");
                String auth = "Bearer " + token;
                headers.put("Authorization", auth);
                headers.put("Content-Type", "application/json");
                return headers;
            }
        };

        queue.add(jsonObjectRequest);

    }

    public void resumePlayback() {
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.PUT, PLAY_ENDPOINT,
                null, response -> {
                    Log.d("Player Service", response.toString());
                }, error -> {
                    Log.d("Player Service", error.getCause().toString());
                    Log.d("Player Service", error.getMessage());
                }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                String token = sharedPreferences.getString("token", "");
                String auth = "Bearer " + token;
                headers.put("Authorization", auth);
                headers.put("Content-Type", "application/json");
                return headers;
            }
        };
        queue.add(jsonObjectRequest);
    }

    public Track getCurrentPlayingTrack() {
        return currentPlayingTrack;
    }

    public void loadCurrentPlayingTrack(VolleyCallBack callBack) {
        String endpoint = "https://api.spotify.com/v1/me/player";
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, endpoint, null, response -> {
                    Log.v("response", response.toString());
                    Gson gson = new Gson();
                    currentPlayingTrack = gson.fromJson(response.optJSONObject("item").toString(), Track.class); // Put our json info into a playservice class object
                    callBack.onSuccess(); // use our volley callback
                }, error -> {
                    // TODO: Handle error

                }) {
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
