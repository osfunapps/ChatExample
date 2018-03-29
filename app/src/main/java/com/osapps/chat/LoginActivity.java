package com.osapps.chat;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.facebook.AccessToken;
import com.osapps.chat.activity.MyAdapterActivity;
import com.osapps.chat.application.RocketChatApplication;
import com.osapps.chat.login.AuthenticationManager;
import com.osapps.chat.login.LoginViewsManager;
import com.osapps.chat.socket.RocketChatClient;
import com.osapps.chat.utils.AppUtils;
import com.osapps.chat.login.LoginDialog;
import com.rocketchat.common.RocketChatException;
import com.rocketchat.common.network.Socket;
import com.rocketchat.core.callback.LoginCallback;
import com.rocketchat.core.model.Token;
import com.twitter.sdk.android.core.TwitterCore;

public class LoginActivity extends MyAdapterActivity implements AuthenticationManager.AuthenticationManagerCallback {

    RocketChatClient api;

    private SharedPreferences.Editor editor;
    private SharedPreferences sharedPref;

    //on activity result request codes. DO NOT CHANGE!
    private int ACTIVITY_RESULT_FACEBOOK_REQUEST_CODE = 64206;
    private int ACTIVITY_RESULT_TWITTER_REQUEST_CODE = 140;
    private int ACTIVITY_RESULT_GOOGLE_REQUEST_CODE = 150;

    //holds if user connected
    boolean userConnected;
    private Handler uiThread;


    //new instances
    private AuthenticationManager authenticationManager;
    private LoginViewsManager loginViewsManager;
    private String TAG = "ozvi";


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        //connect().create().start();
        setContentView(R.layout.activity_login);
        uiThread = new Handler(getMainLooper());
        //to be set BEFORE setContentView
        //prepareTwitter();
        //checkIfUserConnected();


        getSupportActionBar().setTitle("RocketChat Login");
        api = ((RocketChatApplication) getApplicationContext()).getRocketChatAPI();
        api.setReconnectionStrategy(null);
        api.connect(this);
        sharedPref = getPreferences(MODE_PRIVATE);
        editor = sharedPref.edit();





        //to move to chat activity

        authenticationManager = new AuthenticationManager();
        authenticationManager.popLoginDialog(this, api, this);



        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                onLoginButtonClicked();
            }
        },4000);
    }

    @Override
    public void onLoginApproved() {
        new LoginViewsManager().userLoggedIn(this);
    }

    private void checkIfUserConnected() {

        //check if still connected to facebook
        userConnected = AccessToken.getCurrentAccessToken() != null;

        //check if still connected to twitter
        userConnected = TwitterCore.getInstance().getSessionManager().getActiveSession() != null;

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == ACTIVITY_RESULT_FACEBOOK_REQUEST_CODE) {
            authenticationManager.informFacebookCallback(requestCode, resultCode, data);
        } else if (requestCode == ACTIVITY_RESULT_TWITTER_REQUEST_CODE) {
            authenticationManager.informTwitterCallback(requestCode, resultCode, data);
        }

        super.onActivityResult(requestCode, resultCode, data);
    }



    void onLoginError(RocketChatException error) {
        AppUtils.showToast(LoginActivity.this, error.getMessage(), true);
    }


    @Override
    public void onConnect(String sessionID) {
        Log.i(TAG, "connected: ");
    /*    Snackbar.make(findViewById(R.id.activity_login), R.string.connected, Snackbar.LENGTH_LONG)
                .show();
    */}


    @Override
    public void onDisconnect(boolean closedByServer) {
        Log.i(TAG, "disconnected: ");
        /*AppUtils.getSnackbar(findViewById(R.id.activity_login), R.string.disconnected_from_server)
                .setAction("RETRY", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        api.getWebsocketImpl().getSocket().reconnect();
                    }
                })
                .show();
*/
    }

    @Override
    public void onConnectError(Throwable websocketException) {
        Log.i(TAG, "failed: "+websocketException.getMessage());
     /*   AppUtils.getSnackbar(findViewById(R.id.activity_login), R.string.connection_error)
                .setAction("RETRY", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        api.getWebsocketImpl().getSocket().reconnect();

                    }
                })
                .show();*/
    }

    @Override
    protected void onDestroy() {
        api.getWebsocketImpl().getConnectivityManager().unRegister(this);
        super.onDestroy();
    }

    public void onLoginButtonClicked() {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {

                if (api.getWebsocketImpl().getSocket().getState() == Socket.State.CONNECTED) {
                    api.login("itztik2", "esofesof", new LoginCallback() {
                        @Override
                        public void onLoginSuccess(Token token) {
                            Log.i(TAG, "onLoginSuccess: ");
                            LoginActivity.this.justNowLoginApproved(token);
                        }

                        @Override
                        public void onError(RocketChatException error) {
                            Log.i(TAG, "onError: " + error.getLocalizedMessage());
                            Log.i(TAG, "onError: " + error.getMessage());
                        }
                    });
                }

            }
        });
    }

    private void justNowLoginApproved(final Token token) {
        uiThread.post(new Runnable() {
            @Override
            public void run() {
                ((RocketChatApplication) getApplicationContext()).setToken(token.getAuthToken());
                AppUtils.showToast(LoginActivity.this, "Login successful", true);
                Intent intent = new Intent(LoginActivity.this, RoomActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }


}


