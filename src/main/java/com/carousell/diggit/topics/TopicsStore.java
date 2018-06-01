package com.carousell.diggit.topics;

import io.vertx.core.Future;

/**
 * @author Moath
 */
public interface TopicsStore {
    Future<String> addTopic(String text);

    Future<Void> upVoteTopic(String id);

    Future<Void> downVoteTopic(String id);
}
