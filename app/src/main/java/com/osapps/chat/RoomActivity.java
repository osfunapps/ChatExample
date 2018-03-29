package com.osapps.chat;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.UiThread;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.osapps.chat.socket.RocketChatClient;
import com.osapps.chat.socket.callback.ChannelCreationCallback;
import com.rocketchat.common.RocketChatException;
import com.rocketchat.common.listener.SimpleListCallback;
import com.rocketchat.core.callback.LoginCallback;
import com.rocketchat.core.model.Room;
import com.rocketchat.core.model.Subscription;
import com.rocketchat.core.model.Token;


import java.util.List;

import com.osapps.chat.activity.MyAdapterActivity;
import com.osapps.chat.adapter.RoomAdapter;
import com.osapps.chat.application.RocketChatApplication;
import com.osapps.chat.utils.AppUtils;

public class RoomActivity extends MyAdapterActivity {

    RocketChatClient api;
    private RecyclerView.Adapter adapter;
    private RecyclerView.LayoutManager layoutManager;

    RecyclerView recyclerView;
    private Handler mainLooperHandler;
    private Menu menu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_room);
        recyclerView = findViewById(R.id.my_recycler_view);
        getSupportActionBar().setTitle("Chat Rooms");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        api = ((RocketChatApplication) getApplicationContext()).getRocketChatAPI();
        api.getWebsocketImpl().getConnectivityManager().register(this);
        api.subscribeActiveUsers(null);
        api.subscribeUserData(null);

        mainLooperHandler = new Handler(getMainLooper());
        api.getSubscriptions(new SimpleListCallback<Subscription>() {
            @Override
            public void onSuccess(List<Subscription> list) {
                RoomActivity.this.onGetSubscriptions(list);
            }

            @Override
            public void onError(RocketChatException error) {

            }
        });
        super.onCreate(savedInstanceState);
        afterViewsSet();

    }

    public void onGetSubscriptions(final List<Subscription> list) {
        mainLooperHandler.post(new Runnable() {
            @Override
            public void run() {
                adapter = new RoomAdapter(list, RoomActivity.this);
                api.getChatRoomFactory().createChatRooms(list);
                recyclerView.setAdapter(adapter);
            }
        });

    }

    void afterViewsSet() {
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.clearAnimation();
        //recyclerView.setItemAnimator(new DefaultItemAnimator());
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.menu = menu;
        getMenuInflater().inflate(R.menu.rooms_actions_menu, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int i = item.getItemId();
        if (i == android.R.id.home) {
            onBackPressed();
        }else if(i == R.id.action_add_channel){
            addChannel();
        }
        return true;
    }


    private void addChannel() {
        //create channel
        //admin user "admin" "esof$Rocket5173"
        api.createChannel(getString(R.string.app_short_name),"astroSecondChannel", new ChannelCreationCallback() {
            @Override
            public void onChannelCreated() {
                api.getSubscriptions(new SimpleListCallback<Subscription>() {
                    @Override
                    public void onSuccess(List<Subscription> list) {
                        RoomActivity.this.onGetSubscriptions(list);

                    }

                    @Override
                    public void onError(RocketChatException error) {

                    }
                });
            }


            @Override
            public void onError(RocketChatException error) {

            }
        });
    }


    @UiThread
    void showConnectedSnackbar() {
        Snackbar
                .make(findViewById(R.id.activity_room), R.string.connected, Snackbar.LENGTH_LONG)
                .show();
    }

    @Override
    public void onConnect(String sessionID) {

        String token = ((RocketChatApplication) getApplicationContext()).getToken();
        api.loginUsingToken(token, new LoginCallback() {
            @Override
            public void onLoginSuccess(Token token) {
                api.subscribeActiveUsers(null);
                api.subscribeUserData(null);
            }

            @Override
            public void onError(RocketChatException error) {

            }
        });

        showConnectedSnackbar();
    }

    @UiThread
    @Override
    public void onDisconnect(boolean closedByServer) {
    /*    AppUtils.getSnackbar(findViewById(R.id.activity_room), R.string.disconnected_from_server)
                .setAction("RETRY", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        api.getWebsocketImpl().getSocket().reconnect();
                    }
                })
                .show();*/

    }

    @Override
    public void onConnectError(Throwable websocketException) {
        /*AppUtils.getSnackbar(findViewById(R.id.activity_room), R.string.connection_error)
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
