package com.carousell.diggit.topics.store;

import com.carousell.diggit.topics.Topic;
import io.vertx.core.Future;

import java.util.List;

/**
 * This interface define the APIs to deal with topics.
 *
 * @author Moath
 */
public interface TopicsStore {

    /**
     * Get topics
     *
     * @param howMany
     * @return
     */
    Future<List<Topic>> getTopics(int howMany);

    /**
     * Register a topic and return it's ID.
     *
     * @param text
     * @return
     */
    Future<String> addTopic(String text);

    /**
     * Up vote a topic and reorder based on up votes descending.
     *
     * @param id
     * @return
     */
    Future<Void> upVoteTopic(String id);

    /**
     * Down vote a topic and reorder based on up votes descending.
     *
     * @param id
     * @return
     */
    Future<Void> downVoteTopic(String id);
}
