package com.osapps.chat.socket.callback;

import com.rocketchat.common.listener.Callback;
import com.rocketchat.core.model.Token;

import java.lang.reflect.Type;

/**
 * Created by sachin on 18/7/17.
 */
public interface RegisterCallback extends Callback {
    /**
     * Called when the registration process was successful. The callback may proceed to read the {@link Token}
     */
    void onRegisterSuccess(Token token);
}
