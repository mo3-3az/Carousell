package com.carousell.diggit.topics;

import io.vertx.core.buffer.impl.BufferImpl;

public class Topic extends BufferImpl {

    private String text;
    private String id;
    int votes;

    public Topic(String text, String id) {
        this.text = text;
        this.id = id;
        setValue();
    }


    public String getId() {
        return id;
    }

    public int getVotes() {
        return votes;
    }

    public void upVotes() {
        votes++;
        setValue();
    }

    public void downVotes() {
        if (votes == 0) {
            return;
        }

        votes--;
        setValue();
    }

    private void setValue() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(id).append(text).append(votes);
        appendString(stringBuilder.toString());
    }

}
