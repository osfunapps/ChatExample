package com.osapps.chat.socket.callback;

import com.rocketchat.common.listener.Callback;
import com.rocketchat.core.model.Token;

import java.lang.reflect.Type;

/**
 * Created by sachin on 18/7/17.
 */
public interface ChannelCreationCallback extends Callback {
    void onChannelCreated();
}
