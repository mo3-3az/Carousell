package com.carousell.diggit.topics;

import com.carousell.diggit.verticle.TopicsManager;
import io.vertx.core.Future;

import javax.swing.plaf.FontUIResource;

/**
 * @author Moath
 */
public class InMemTopicStore implements TopicsStore {
    @Override
    public Future<String> addTopic(String text) {
        return Future.succeededFuture("id");
    }

    @Override
    public Future<Void> upVoteTopic(String id) {
        return Future.succeededFuture();
    }

    @Override
    public Future<Void> downVoteTopic(String id) {
        return Future.succeededFuture();
    }
}
