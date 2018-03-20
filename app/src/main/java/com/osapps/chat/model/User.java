package com.osapps.chat.model;

import com.rocketchat.common.utils.Utils;
import com.stfalcon.chatkit.commons.models.IUser;

/*
 * Created by sachin76 on 04.04.17.
 */
public class User implements IUser {

    private String id;
    private String name;
    private String avatar;
    private boolean online;

    public User(String id, String name, String avatar, boolean online) {
        this.id = id;
        this.name = name;
        this.avatar = avatar;
        this.online = online;
    }

    public User (com.rocketchat.common.data.model.User user) {
        this.id = user.id();
        this.name = user.username();
        this.avatar = Utils.getAvatar(user.username());
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getAvatar() {
        return avatar;
    }

    public boolean isOnline() {
        return online;
    }

    @Override
    public String toString() {
        return "User{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", avatar='" + avatar + '\'' +
                ", online=" + online +
                '}';
    }
}
