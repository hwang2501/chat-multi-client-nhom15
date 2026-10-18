package com.nhom15.chat.common;

import com.google.gson.Gson;

/** Gói tin JSON trao đổi giữa Client và Server. */
public class Message {
    private static final Gson GSON = new Gson();

    public String action;    // LOGIN, LOGIN_OK, LOGIN_FAIL, USER_LIST, MSG_PRIVATE, MSG_GROUP, LOGOUT...
    public String from;
    public String to;
    public String content;
    public String time;

    public Message() {}

    public Message(String action, String from, String to, String content) {
        this.action = action;
        this.from = from;
        this.to = to;
        this.content = content;
        this.time = java.time.LocalTime.now().withNano(0).toString();
    }

    public String toJson() {
        return GSON.toJson(this);
    }

    public static Message fromJson(String json) {
        return GSON.fromJson(json, Message.class);
    }
}
