package com.osapps.chat.application;

import android.app.Application;

import com.rocketchat.common.network.ReconnectionStrategy;
import com.rocketchat.common.utils.Logger;
import com.rocketchat.common.utils.Utils;
import com.rocketchat.core.RocketChatClient;
import com.squareup.picasso.Picasso;

/**
 * Created by sachin on 13/8/17.
 */

public class RocketChatApplication extends Application {

    RocketChatClient client;

    //shabat server url server server
    //private static String serverurl = "http://weedleapps.co.il:3000/websocket";

//   private static String serverurl = "http://34.214.236.242:32771/websocket";
  //  private static String baseUrl = "http://weedle-caht-alb-external-304626034.us-west-2.elb.amazonaws.com/api/v1/";




    private static String serverurl = "wss://demo.rocket.chat/websocket";
    private static String baseUrl = "https://demo.rocket.chat/api/v1/";

    public String token;

    @Override
    public void onCreate() {
        super.onCreate();
        client = new RocketChatClient.Builder()
                .websocketUrl(serverurl)
                .restBaseUrl(baseUrl)
                .logger(logger)
                .build();

        Utils.DOMAIN_NAME = "https://demo.rocket.chat";

        client.setReconnectionStrategy(new ReconnectionStrategy(20, 3000));


        Picasso.Builder builder = new Picasso.Builder(this);
        Picasso built = builder.build();
//        built.setIndicatorsEnabled(true);
        built.setLoggingEnabled(true);
        Picasso.setSingletonInstance(built);
    }

    public RocketChatClient getRocketChatAPI() {
        return client;
    }

    Logger logger = new Logger() {
        @Override
        public void info(String format, Object... args) {
            System.out.println(format + " " +  args);
        }

        @Override
        public void warning(String format, Object... args) {
            System.out.println(format + " " +  args);
        }

        @Override
        public void debug(String format, Object... args) {
            System.out.println(format + " " +  args);
        }
    };

    public void setToken(String token) {
        this.token = token;
    }

    public String getToken() {
        return token;
    }
}
