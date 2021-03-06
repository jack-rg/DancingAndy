/**
 * SplashActivity.java - Where the application starts. Contains credentials for spotify app and
 * will authenticate the user via spotify login. The authorization token in onActivityResult
 * will be stored in SharedPreferences and will be used in {@link com.example.spotifyar.services}.
 * Starts com.example.spotifyar.MainActivity#MainActivity
 */

package com.example.spotifyar;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.Window;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.example.spotifyar.models.User;
import com.example.spotifyar.services.UserService;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.spotify.sdk.android.auth.AuthorizationClient;
import com.spotify.sdk.android.auth.AuthorizationRequest;
import com.spotify.sdk.android.auth.AuthorizationResponse;

/**
 * Starting Activity, authenticates
 */
public class SplashActivity extends AppCompatActivity {

    private SharedPreferences.Editor editor;
    private SharedPreferences sharedPreferences;

    private RequestQueue queue;

    private static final String CLIENT_ID = "730bb52a8e884ac9bb4e03b49856815f";
    private static final String REDIRECT_URI = "https://com.example.spotifyar/callback";
    private static final int REQUEST_CODE = 1337;
    private static final String[] SCOPES =  new String[] {"user-read-currently-playing",
            "user-read-email", "user-read-private", "user-library-read", "user-modify-playback-state",
            "user-read-playback-state"
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_splash);

        sharedPreferences = this.getSharedPreferences("SPOTIFY", 0);
        queue = Volley.newRequestQueue(this);

        ConnectivityManager connectivityManager = (ConnectivityManager) getApplicationContext()
                .getSystemService(Context.CONNECTIVITY_SERVICE);


        //Check wifi and authentication
        NetworkInfo wifiStatus = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

        if (wifiStatus != null && wifiStatus.isConnected()) {
//            Log.d("AUTHENTICATING", String.valueOf(wifiStatus.isConnected()));
//            Log.d("AUTHENTICATING", "begun authentication");
            authenticateSpotify();
        } else {
            new MaterialAlertDialogBuilder(SplashActivity.this)
                    .setCancelable(false)
                    .setTitle(R.string.unable_to_connect)
                    .setMessage(R.string.wifi_dialog_message)
                    .setPositiveButton(R.string.connect_wifi, (dialog, which) -> {
                        startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
                        authenticateSpotify();
                    })
                    .setNegativeButton(R.string.quit, (dialog, which) -> {
                        SplashActivity.this.finish();
                    })
                    .show();
        }
    }


    private void waitForUserInfo() {
        UserService userService = new UserService(queue, sharedPreferences);
        userService.get(() -> {
            User user = userService.getUser();
            Log.d("service_user_name", String.valueOf(user.getId()));
            editor = getSharedPreferences("SPOTIFY", 0).edit();
            editor.putString("userid", user.getId());
            editor.putString("display_name", user.getDisplay_name());
            Log.d("STARTING", "GOT USER INFORMATION");
            // We use commit instead of apply because we need the information stored immediately
            editor.commit();
            startMainActivity();
        });

    }

    private void startMainActivity() {
        Intent intent = new Intent(SplashActivity.this, MainActivity.class);
        startActivity(intent);
    }


    private void authenticateSpotify() {
        AuthorizationRequest.Builder builder = new AuthorizationRequest.Builder(CLIENT_ID,
                AuthorizationResponse.Type.TOKEN, REDIRECT_URI);
        builder.setScopes(SCOPES);
        AuthorizationRequest request = builder.build();
        AuthorizationClient.openLoginActivity(this, REQUEST_CODE, request);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        // Check if result comes from the correct activity
        if (requestCode == REQUEST_CODE) {
            AuthorizationResponse response = AuthorizationClient.getResponse(resultCode, intent);

            switch (response.getType()) {
                // Response was successful and contains auth token
                case TOKEN:
                    editor = getSharedPreferences("SPOTIFY", 0).edit();
                    editor.putString("token", response.getAccessToken());
                    Log.d("STARTING", "GOT AUTH TOKEN");
                    editor.apply();
                    waitForUserInfo();
                    break;

                // Auth flow returned an error
                case ERROR:
                    Log.d("Login Response Error", response.getError());
                    AuthorizationClient.clearCookies(getApplicationContext());

                    // Handle error response
                    break;

                // Most likely auth flow was cancelled
                default:
                    Log.d("Login Response", response.toString());
                    // Handle other cases
            }
        }
    }


}