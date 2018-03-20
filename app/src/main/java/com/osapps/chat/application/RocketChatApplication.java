package com.osapps.chat.application;

import android.app.Application;

import com.osapps.chat.socket.RocketChatClient;
import com.rocketchat.common.network.ReconnectionStrategy;
import com.rocketchat.common.utils.Logger;
import com.rocketchat.common.utils.Utils;
import com.squareup.picasso.Picasso;

/**
 * Created by sachin on 13/8/17.
 */

public class RocketChatApplication extends Application {

    RocketChatClient client;

    //todo: ozvi server address
    //private static String serverurl = "http://weedleapps.co.il:3000/websocket";

   private static String serverurl = "https://chat.weedleapps.co.il/websocket";
    private static String baseUrl = "https://chat.weedleapps.co.il/api/v1/";



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
