package com.carousell.diggit.topics;

import io.vertx.core.buffer.impl.BufferImpl;
import io.vertx.core.json.JsonObject;

import java.io.Serializable;

public class Topic extends BufferImpl implements Serializable {

    private String text;
    private String id;
    private int upVotes;
    private int downVotes;

    public Topic(String text, String id) {
        this.text = text;
        this.id = id;
        setValue();
    }

    public String getText() {
        return text;
    }

    public String getId() {
        return id;
    }

    public int getUpVotes() {
        return upVotes;
    }

    public int getDownVotes() {
        return downVotes;
    }

    public void upVotes() {
        upVotes++;
        setValue();
    }

    public void downVotes() {
        downVotes++;
        setValue();
    }

    private void setValue() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(id).append(text).append(upVotes).append(downVotes);
        setString(0, stringBuilder.toString());
    }

    public JsonObject toJsonObject() {
        return new JsonObject().put("id", id).put("text", text).put("upVotes", upVotes).put("downVotes", downVotes);
    }

}
