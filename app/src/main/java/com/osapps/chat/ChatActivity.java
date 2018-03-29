package com.osapps.chat;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.osapps.chat.activity.MyAdapterActivity;
import com.osapps.chat.socket.ChatRoom;
import com.osapps.chat.socket.RocketChatClient;
import com.osapps.chat.utils.views.messageslist.MessagesList;
import com.osapps.chat.utils.views.messageslist.MessagesListAdapter;
import com.rocketchat.common.RocketChatException;
import com.rocketchat.common.data.lightdb.collection.Collection;
import com.rocketchat.common.data.lightdb.document.UserDocument;
import com.rocketchat.common.listener.SimpleListCallback;
import com.rocketchat.common.network.Socket;
import com.rocketchat.core.callback.HistoryCallback;
import com.rocketchat.core.callback.LoginCallback;
import com.rocketchat.core.callback.MessageCallback;
import com.rocketchat.core.model.Subscription;
import com.rocketchat.core.model.Token;
import com.stfalcon.chatkit.messages.MessageInput;
import com.stfalcon.chatkit.utils.DateFormatter;


import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import com.osapps.chat.application.RocketChatApplication;
import com.osapps.chat.model.Message;
import com.osapps.chat.model.User;
import com.osapps.chat.utils.AppUtils;

/**
 * Created by aniket on 05/09/17.
 */

public class ChatActivity extends MyAdapterActivity implements
        MessagesListAdapter.SelectionListener,
        MessagesListAdapter.OnLoadMoreListener,
        MessageInput.InputListener,
        DateFormatter.Formatter,
        MessageInput.AttachmentsListener {

    MessagesList messagesList;

    MessageInput input;

    RocketChatClient api;
    ChatRoom chatRoom;
    String userId;
    /**
     * This will restrict total messages to 1000
     */
    private static final int TOTAL_MESSAGES_COUNT = 1000;

    /**
     * Variables for storing temporary references
     */
    private Menu menu;
    private int selectionCount;

    /**
     * MessageAdapter for loading messages, has 2 callbacks (on selection and onloadmore)
     */
    protected MessagesListAdapter<Message> messagesAdapter;

    Handler Typinghandler = new Handler();
    Boolean typing = false;

    private Date lastTimestamp;
    private Handler mainThreadHanlder;
    private Handler mainLooperHandler;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_chat);
        super.onCreate(savedInstanceState);

        mainLooperHandler = new Handler(getMainLooper());
        input = findViewById(R.id.input);
        messagesList = findViewById(R.id.messagesList);
        mainThreadHanlder = new Handler(getMainLooper());
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        api = ((RocketChatApplication) getApplicationContext()).getRocketChatAPI();
        getSupportActionBar().setTitle("Chat Activity");

        api.getWebsocketImpl().getConnectivityManager().register(this);
        api.subscribeActiveUsers(null);
        api.subscribeUserData(null);
        api.setReconnectionStrategy(null);
        api.connect(this);
    }

    @Override
    public void onConnect(String sessionID) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {

                if (api.getWebsocketImpl().getSocket().getState() == Socket.State.CONNECTED) {
                    api.login("itztik2", "esofesof", new LoginCallback() {
                        @Override
                        public void onLoginSuccess(Token token) {
                            ChatActivity.this.onLoginSuccess();

                            //Log.i(TAG, "onLoginSuccess: ");
                            //LoginActivity.this.justNowLoginApproved(token);
                        }

                        @Override
                        public void onError(RocketChatException error) {
                         //   Log.i(TAG, "onError: " + error.getLocalizedMessage());
                           // Log.i(TAG, "onError: " + error.getMessage());
                        }
                    });
                }

            }
        });
        /*String token = ((RocketChatApplication)getApplicationContext()).getToken();
        api.loginUsingToken(token, new LoginCallback() {
            @Override
            public void onLoginSuccess(Token token) {
                ChatActivity.this.onLoginSuccess();
            }

            @Override
            public void onError(RocketChatException error) {

            }
        });*/

    }

    void onLoginSuccess()  {
        System.out.println("connected!");
        api.subscribeActiveUsers(null);
        api.subscribeUserData(null);




        //ADDED get all rooms
        api.getSubscriptions(new SimpleListCallback<Subscription>() {
            @Override
            public void onSuccess(List<Subscription> list) {
                ChatActivity.this.onSubscriptionsGetSuccess(list);
            }

            @Override
            public void onError(RocketChatException error) {
                System.out.println(error);
            }
        });

      /*  Snackbar
                .make(findViewById(R.id.chat_activity), R.string.connected, Snackbar.LENGTH_LONG)
                .show();*/
    }

    private void onSubscriptionsGetSuccess(List<Subscription> list) {
        onGetSubscriptions(list);
    }


    //ADDED get all rooms
    public void onGetSubscriptions(final List<Subscription> list) {

        mainLooperHandler.post(new Runnable() {
            @Override
            public void run() {

                api.subscribeActiveUsers(null);
                api.subscribeUserData(null);


                api.getChatRoomFactory().createChatRooms(list);


                //getting the general room id
                 String roomId = api.getChatRoomFactory().getChatRoomByName("general").getRoomData().roomId();


                //stopped here! need to know if works
                //userId = roomId.replace("ScA8kHMaxGF9TC4iX", "");


                //todo: emable this!
                 userId = roomId.replace(api.getWebsocketImpl().getMyUserId(), "");

                chatRoom = api.getChatRoomFactory().getChatRoomById(roomId);
                chatRoom.subscribeRoomMessageEvent(null, ChatActivity.this);
                chatRoom.subscribeRoomTypingEvent(null, ChatActivity.this);

                //todo: enable this!
                // getSupportActionBar().setTitle(chatRoom.getRoomData().name());
                if (getCurrentUser() !=null) {
                    updateUserStatus(getCurrentUser().status().toString());
                }

                api.getDbManager().getUserCollection().register(userId, new Collection.Observer<UserDocument>() {
                    @Override
                    public void onUpdate(Collection.Type type, UserDocument document) {
                        switch (type) {
                            case ADDED:
                                updateUserStatus(document.status().toString());
                                break;
                            case CHANGED:
                                updateUserStatus(document.status().toString());
                                break;
                            case REMOVED:
                                updateUserStatus("UNAVAILABLE");
                                break;
                        }
                    }
                });

                chatRoom.subscribeRoomMessageEvent(null, ChatActivity.this);
                chatRoom.subscribeRoomTypingEvent(null, ChatActivity.this);
                chatRoom.getChatHistory(50, lastTimestamp, null, new HistoryCallback() {
                    @Override
                    public void onLoadHistory(List<com.rocketchat.core.model.Message> list, int unreadNotLoaded) {
                        ChatActivity.this.onLoadHistory(list, unreadNotLoaded);
                    }

                    @Override
                    public void onError(RocketChatException error) {

                    }
                });

                afterViewsSet();
            }
        });

    }



    void updateUserStatus(final String status) {
        mainThreadHanlder.post(new Runnable() {
            @Override
            public void run() {
                getSupportActionBar().setSubtitle(status.substring(0,1)+status.substring(1).toLowerCase());
            }
        });

    }

    void afterViewsSet() {
        input.setInputListener(this);
        input.setAttachmentsListener(this);
        input.getInputEditText().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (!typing) {
                    typing = true;
                    chatRoom.sendIsTyping(true);
                }
                Typinghandler.removeCallbacks(onTypingTimeout);
                Typinghandler.postDelayed(onTypingTimeout, 600);
            }

            Runnable onTypingTimeout = new Runnable() {
                @Override
                public void run() {
                    typing = false;
                    chatRoom.sendIsTyping(false);
                }
            };

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        initAdapter();
    }

    private void initAdapter() {
        messagesAdapter = new MessagesListAdapter<>(api.getWebsocketImpl().getMyUserId());
        messagesAdapter.enableSelectionMode(this);
        messagesAdapter.setLoadMoreListener(this);
        messagesAdapter.setDateHeadersFormatter(this);
        messagesList.setAdapter(messagesAdapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.menu = menu;
        getMenuInflater().inflate(R.menu.chat_actions_menu, menu);
        onSelectionChanged(0);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int i = item.getItemId();
        if (i == R.id.action_copy) {
            messagesAdapter.copySelectedMessagesText(this, getMessageStringFormatter(), true);
            AppUtils.showToast(this, R.string.copied_message, true);
        } else if (i == android.R.id.home) {
            onBackPressed();
        }
        return true;
    }

    private MessagesListAdapter.Formatter<Message> getMessageStringFormatter() {
        return new MessagesListAdapter.Formatter<Message>() {
            @Override
            public String format(Message message) {
                String createdAt = new SimpleDateFormat("MMM d, EEE 'at' h:mm a", Locale.getDefault())
                        .format(message.getCreatedAt());

                String text = message.getText();
                if (text == null) text = "[attachment]";

                return String.format(Locale.getDefault(), "%s: %s (%s)",
                        message.getUser().getName(), text, createdAt);
            }
        };
    }



    void updateMessage(final ArrayList<Message> messages) {
        mainThreadHanlder.post(new Runnable() {
            @Override
            public void run() {
                messagesAdapter.addToEnd(messages,false);
            }
        });
    }

    @Override
    public void onMessage(String roomId, final com.rocketchat.core.model.Message message) {
        mainThreadHanlder.post(new Runnable() {
            @Override
            public void run() {
                messagesAdapter.addToStart(new Message(message.id(), new User(message.sender().id(), message.sender().username(), null, true), message.message(), new Date(message.timestamp())), true);
            }
        });

    }

    @Override
    public void onTyping(String roomId, final String user, final Boolean istyping) {
        mainThreadHanlder.post(new Runnable() {
            @Override
            public void run() {
                if (istyping) {
                    getSupportActionBar().setSubtitle(user + " is typing...");
                } else {
                    if (getCurrentUser() != null) {
                        updateUserStatus(getCurrentUser().status().toString());
                    } else {
                        getSupportActionBar().setSubtitle("");
                    }
                }
            }
        });
    }




    @Override
    public void onDisconnect(boolean closedByServer) {
        mainThreadHanlder.post(new Runnable() {
            @Override
            public void run() {


        chatRoom.unSubscribeAllEvents();
     /*   AppUtils.getSnackbar(findViewById(R.id.chat_activity), R.string.disconnected_from_server)
                .setAction("RETRY", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        api.getWebsocketImpl().getSocket().reconnect();
                    }
                })
                .show();*/

            }
        });
    }


    @Override
    public void onConnectError(final Throwable websocketException) {
        mainThreadHanlder.post(new Runnable() {
            @Override
            public void run() {
                chatRoom.unSubscribeAllEvents();
                System.out.println(websocketException.getLocalizedMessage());
                System.out.println(websocketException.getMessage());
      /*  AppUtils.getSnackbar(findViewById(R.id.chat_activity), R.string.connection_error)
                .setAction("RETRY", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        api.getWebsocketImpl().getSocket().reconnect();

                    }
                })
                .show();*/
            }
        });

    }



    @Override
    protected void onDestroy() {
        api.getWebsocketImpl().getConnectivityManager().unRegister(this);
        super.onDestroy();
    }

    @Override
    public void onAddAttachments() {

    }

    @Override
    public boolean onSubmit(CharSequence input) {
        chatRoom.sendMessage(input.toString(), new MessageCallback.MessageAckCallback() {
            @Override
            public void onMessageAck(com.rocketchat.core.model.Message message) {

            }

            @Override
            public void onError(RocketChatException error) {

            }
        });
        return true;
    }

    public void onLoadHistory(final List<com.rocketchat.core.model.Message> list, int unreadNotLoaded) {
        mainThreadHanlder.post(new Runnable() {
            @Override
            public void run() {

        if (list.size() > 0) {
            lastTimestamp = new Date(list.get(list.size() - 1).timestamp());
            final ArrayList<Message> messages = new ArrayList<>();
            for (com.rocketchat.core.model.Message message : list) {
                switch (message.getMsgType()) {
                    case TEXT:
                        messages.add(new Message(message.id(), new User(message.sender().id(), message.sender().username(), null, true), message.message(), new Date(message.timestamp())));
                        break;
                }
            }
            updateMessage(messages);
        }

            }
        });
    }

    @Override
    public void onLoadMore(int page, int totalItemsCount) {
        if (totalItemsCount < TOTAL_MESSAGES_COUNT) {
            chatRoom.getChatHistory(50, lastTimestamp, null, new HistoryCallback() {
                @Override
                public void onLoadHistory(List<com.rocketchat.core.model.Message> list, int unreadNotLoaded) {
                    ChatActivity.this.onLoadHistory(list, unreadNotLoaded);
                }

                @Override
                public void onError(RocketChatException error) {

                }
            });
        }
    }

    @Override
    public void onSelectionChanged(int count) {
        this.selectionCount = count;
        menu.findItem(R.id.action_copy).setVisible(count > 0);
    }

    @Override
    public String format(Date date) {
        if (DateFormatter.isToday(date)) {
            return getString(R.string.date_header_today);
        } else if (DateFormatter.isYesterday(date)) {
            return getString(R.string.date_header_yesterday);
        } else {
            return DateFormatter.format(date, DateFormatter.Template.STRING_DAY_MONTH_YEAR);
        }
    }

    UserDocument getCurrentUser() {
        return api.getDbManager().getUserCollection().get(userId);
    }

    @Override
    public void onBackPressed() {
        if (selectionCount == 0) {
            chatRoom.unSubscribeAllEvents();
            super.onBackPressed();
        } else {
            messagesAdapter.unselectAllItems();
        }
    }
}
