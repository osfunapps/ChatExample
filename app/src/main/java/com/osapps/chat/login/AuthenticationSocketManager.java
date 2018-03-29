package com.osapps.chat.login;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.facebook.login.LoginResult;
import com.osapps.chat.LoginActivity;
import com.osapps.chat.socket.RocketChatClient;
import com.osapps.chat.socket.callback.RegisterCallback;
import com.osapps.chat.utils.AppUtils;
import com.rocketchat.common.RocketChatException;
import com.rocketchat.common.network.Socket;
import com.rocketchat.core.callback.LoginCallback;
import com.rocketchat.core.model.Token;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterSession;

/**
 * Created by osapps on 21/03/2018.
 */
public class AuthenticationSocketManager {

    private final AuthenticationSocketCallback callback;
    private RocketChatClient api;
    private String TAG = "ozvi";

    public AuthenticationSocketManager(RocketChatClient api, AuthenticationSocketCallback callback) {
        this.api = api;
        this.callback = callback;
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
                    callback.onLoginApproved();
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

    public void facebookLoginDone(LoginResult loginResult) {
        //todo: set facebook email here
    }

    public void twitterLoginDone(Result<TwitterSession> result) {
        //todo: set twitter email here
    }

    private void signUpUser(String email, String username, String password){
        api.signUp(email, username, password, new RegisterCallback() {
            @Override
            public void onRegisterSuccess(Token token) {
                callback.onLoginApproved();
            }

            @Override
            public void onError(RocketChatException error) {
                Log.i(TAG, "onError: " + error.getMessage());
            }
        });
    }

    public interface AuthenticationSocketCallback{
        void onLoginApproved();
    }
}
