package com.osapps.chat.login;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.CountDownTimer;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.osapps.chat.R;
import com.osapps.chat.donotcopytoproject.DialogPopa;
import com.osapps.chat.utils.views.TwitterLoginButton;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.DefaultLogger;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.Twitter;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.twitter.sdk.android.core.TwitterConfig;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;


public class LoginDialog extends DialogPopa {

    //views
    private Button login;

    //sign in instances
    private TwitterLoginButton twitterLoginButton;
    private CallbackManager facebookCallbackManager;

    //etc
    private static final String LOGGER_LOGIN_DIALOG = "ozvi";
    private LoginDialogCallback callback;

    public LoginDialog(Activity activity, LoginDialogCallback callback) {
        super(activity, R.style.FullHeightDialog);
        prePrepareTwitter();
        setContentView(R.layout.login_dialog);
        this.callback = callback;
        prepareDialog();
        setSocialBtns(activity);
        setLoginBtn();
    }

    private void prePrepareTwitter() {
        TwitterConfig config = new TwitterConfig.Builder(getContext())
                .logger(new DefaultLogger(Log.DEBUG))
                .twitterAuthConfig(new TwitterAuthConfig(getContext().getString(R.string.com_twitter_sdk_android_CONSUMER_KEY),
                        getContext().getString(R.string.com_twitter_sdk_android_CONSUMER_SECRET)))
                .debug(true)
                .build();
        Twitter.initialize(config);
    }


    /* twitter setup start **/
    private void setTwitter(Activity activity) {

        twitterLoginButton = findViewById(R.id.login_button_twitter);
        twitterLoginButton.setCallback(new Callback<TwitterSession>() {
            @Override
            public void success(Result<TwitterSession> result) {
                Log.i(LOGGER_LOGIN_DIALOG, "SetupTwitter: login success!");
                callback.onTwitterLoginDone(result);
                dismiss();

            }

            @Override
            public void failure(TwitterException exception) {
                Log.i(LOGGER_LOGIN_DIALOG, "SetupTwitter: login failed!");
                //todo: add crash reporter here!
            }
        });
        twitterLoginButton.setActivity(activity);
    }


    private void prepareDialog() {

        if(getWindow()!=null)
            getWindow().setDimAmount(0.5f);
        setCanceledOnTouchOutside(true);
        setCancelable(false);
    }


    private void setSocialBtns(Activity activity) {
        setUpFacebook();
        setTwitter(activity);
    }


    /* facebook setup start **/
    private void setUpFacebook() {
        facebookCallbackManager = CallbackManager.Factory.create();
        LoginManager.getInstance().registerCallback(facebookCallbackManager,
                new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult loginResult) {
                        callback.onFacebookLoginDone(loginResult);
                        Log.i(LOGGER_LOGIN_DIALOG, "setupfacebook: login success!");
                        dismiss();
                    }

                    @Override
                    public void onCancel() {
                    }

                    @Override
                    public void onError(FacebookException exception) {
                        //todo: add crash reporter here!
                        Log.i(LOGGER_LOGIN_DIALOG, "setupfacebook: login failed");
                    }
                });
    }



    private void setLoginBtn() {
        login = (Button) findViewById(R.id.login);
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callback.onLoginClicked();
            }
        });
    }

    @Override
    public void dismiss() {
        //kill all views here!
        super.dismiss(this);
    }

    public void informFacebookCallback(int requestCode, int resultCode, Intent data) {
        facebookCallbackManager.onActivityResult(requestCode, resultCode, data);
    }

    public void informTwitterCallback(int requestCode, int resultCode, Intent data) {
        twitterLoginButton.onActivityResult(requestCode, resultCode, data);
    }



    public interface LoginDialogCallback{
        void onLoginClicked(); //todo  delete!!!
        void onFacebookLoginDone(LoginResult loginResult);
        void onTwitterLoginDone(Result<TwitterSession> result);
    }
}