package com.osapps.chat.login;

import android.app.Activity;
import android.content.Intent;
import android.widget.Toast;

import com.facebook.login.LoginResult;
import com.osapps.chat.ChatActivity;
import com.osapps.chat.LoginActivity;
import com.osapps.chat.R;
import com.osapps.chat.RoomActivity;
import com.osapps.chat.application.RocketChatApplication;
import com.osapps.chat.socket.RocketChatClient;
import com.osapps.chat.utils.AppUtils;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterSession;

/** this class manages all of the user authentication **/
public class AuthenticationManager implements LoginDialog.LoginDialogCallback, AuthenticationSocketManager.AuthenticationSocketCallback {

    //instances
    private AuthenticationSocketManager socketManager;
    private LoginDialog loginDialog;
    private AuthenticationManagerCallback callback;

    public void popLoginDialog(Activity activity, RocketChatClient api, AuthenticationManagerCallback callback){
        loginDialog = new LoginDialog(activity, this);
        socketManager = new AuthenticationSocketManager(api, this);
        this.callback = callback;
        loginDialog.show();
    }


    public void informFacebookCallback(int requestCode, int resultCode, Intent data) {
        loginDialog.informFacebookCallback(requestCode, resultCode, data);
    }

    public void informTwitterCallback(int requestCode, int resultCode, Intent data) {
        loginDialog.informTwitterCallback(requestCode, resultCode, data);
    }


    @Override
    public void onLoginClicked() {
        socketManager.onLoginButtonClicked();
    }


    @Override
    public void onFacebookLoginDone(LoginResult loginResult) {
        socketManager.facebookLoginDone(loginResult);
    }

    @Override
    public void onTwitterLoginDone(Result<TwitterSession> result) {
        socketManager.twitterLoginDone(result);
    }


    @Override
    public void onLoginApproved() {
        Toast.makeText(loginDialog.getContext(), loginDialog.getContext().getString(R.string.login_approved), Toast.LENGTH_LONG).show();
        callback.onLoginApproved();
    }

    public interface AuthenticationManagerCallback{
        void onLoginApproved();
    }
}
