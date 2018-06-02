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
        this.text = escapeHTML(text);
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

    private String escapeHTML(String text) {
        final String[] toEscape = {"&", "\"", "<", ">"};
        final String[] replaceWith = {"&amp;", "&quot;", "&lt;", "&gt;"};

        for (int i = 0; i < toEscape.length; i++) {
            text = text.replaceAll(toEscape[i], replaceWith[i]);
        }

        return text;
    }

    private void setValue() {
        setString(0, id + text + upVotes + downVotes);
    }

    public JsonObject toJsonObject() {
        return new JsonObject().put("id", id).put("text", text).put("upVotes", upVotes).put("downVotes", downVotes);
    }

}
