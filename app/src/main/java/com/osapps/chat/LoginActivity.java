package com.osapps.chat;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.arasthel.asyncjob.AsyncJob;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.osapps.chat.activity.MyAdapterActivity;
import com.osapps.chat.application.RocketChatApplication;
import com.osapps.chat.socket.RocketChatClient;
import com.osapps.chat.socket.callback.RegisterCallback;
import com.osapps.chat.utils.AppUtils;
import com.rocketchat.common.RocketChatException;
import com.rocketchat.common.network.Socket;
import com.rocketchat.core.callback.LoginCallback;
import com.rocketchat.core.model.Token;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.DefaultLogger;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.Twitter;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.twitter.sdk.android.core.TwitterConfig;
import com.twitter.sdk.android.core.TwitterCore;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.identity.TwitterLoginButton;

import java.io.IOException;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.internal.http2.Header;

public class LoginActivity extends MyAdapterActivity {

    RocketChatClient api;

    private SharedPreferences.Editor editor;
    private SharedPreferences sharedPref;
    private String TAG = "LoginActivity";


    //on activity result request codes. DO NOT CHANGE!
    private int ACTIVITY_RESULT_FACEBOOK_REQUEST_CODE = 64206;
    private int ACTIVITY_RESULT_TWITTER_REQUEST_CODE = 140;
    private int ACTIVITY_RESULT_GOOGLE_REQUEST_CODE = 150;


    //sign in instances
    private TwitterLoginButton twitterLoginButton;
    private CallbackManager facebookCallbackManager;

    //holds if user connected
    boolean userConnected;
    private Button login;
    private Handler uiThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        //connect().create().start();
        setContentView(R.layout.activity_login);

        uiThread = new Handler(getMainLooper());
        //to be set BEFORE setContentView
        //prepareTwitter();
        //checkIfUserConnected();

        //setUpFacebook();
        //setUpTwitter();

        login = (Button) findViewById(R.id.login);
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onLoginButtonClicked();
            }
        });

        getSupportActionBar().setTitle("RocketChat Login");
        api = ((RocketChatApplication) getApplicationContext()).getRocketChatAPI();
        api.setReconnectionStrategy(null);
        api.connect(this);
        sharedPref = getPreferences(MODE_PRIVATE);
        editor = sharedPref.edit();
    }

    private void checkIfUserConnected() {

        //check if still connected to facebook
        userConnected = AccessToken.getCurrentAccessToken() != null;

        //check if still connected to twitter
        userConnected = TwitterCore.getInstance().getSessionManager().getActiveSession() != null;

    }


    private void prepareTwitter() {
        TwitterConfig config = new TwitterConfig.Builder(this)
                .logger(new DefaultLogger(Log.DEBUG))
                .twitterAuthConfig(new TwitterAuthConfig(getString(R.string.com_twitter_sdk_android_CONSUMER_KEY), getString(R.string.com_twitter_sdk_android_CONSUMER_SECRET)))
                .debug(true)
                .build();
        Twitter.initialize(config);
    }

    /* facebook setup start **/
    private void setUpFacebook() {
        facebookCallbackManager = CallbackManager.Factory.create();
        LoginManager.getInstance().registerCallback(facebookCallbackManager,
                new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult loginResult) {
                        userConnected = true;
                        //navigate to new menu
                    }

                    @Override
                    public void onCancel() {
                        // App code
                    }

                    @Override
                    public void onError(FacebookException exception) {
                        Log.i(TAG, "setUpFacebook: login failed!");
                    }
                });
    }


    private void setUpTwitter() {
        twitterLoginButton = findViewById(R.id.login_button_twitter);
        twitterLoginButton.setCallback(new Callback<TwitterSession>() {
            @Override
            public void success(Result<TwitterSession> result) {
                Log.i(TAG, "SetupTwitter: login success!");
                userConnected = true;
                //navigate to new menu
            }

            @Override
            public void failure(TwitterException exception) {
                Log.i(TAG, "SetupTwitter: login failed!");
            }
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == ACTIVITY_RESULT_FACEBOOK_REQUEST_CODE) {
            facebookCallbackManager.onActivityResult(requestCode, resultCode, data);
        } else if (requestCode == ACTIVITY_RESULT_TWITTER_REQUEST_CODE) {
            // Pass the activity result to the login button.
            twitterLoginButton.onActivityResult(requestCode, resultCode, data);
        }

        super.onActivityResult(requestCode, resultCode, data);
    }



    void onLoginSuccess(final Token token) {
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


    void onLoginError(RocketChatException error) {
        AppUtils.showToast(LoginActivity.this, error.getMessage(), true);
    }

    void onLoginButtonClicked() {
        if (api.getWebsocketImpl().getSocket().getState() == Socket.State.CONNECTED) {
            api.login("itzik1", "itzik1", new LoginCallback() {
                @Override
                public void onLoginSuccess(Token token) {
                    Log.i(TAG, "onLoginSuccess: ");
                    editor.putString("username", token.getAuthToken().toString());
                    editor.putString("password", token.getAuthToken().toString());
                    editor.commit();
                    LoginActivity.this.onLoginSuccess(token);
                }

                @Override
                public void onError(RocketChatException error) {
                    Log.i(TAG, "onError: "+ error.getLocalizedMessage());
                    Log.i(TAG, "onError: "+ error.getMessage());
                    Log.i(TAG, "onError: "+ error.getStackTrace());
                }
            });
        } else {
            AppUtils.showToast(LoginActivity.this, "Not connected to server", true);
        }
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

}


