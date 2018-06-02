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
     * Get top topics ordered by up votes descending.
     *
     * @param top Number of top topics to get.
     */
    Future<List<Topic>> getTopTopics(int top);

    /**
     * Get all topics.
     */
    Future<List<Topic>> getTopics();

    /**
     * Register a topic.
     *
     * @param text
     * @return Newly added topic.
     */
    Future<Topic> addTopic(String text);

    /**
     * Up vote a topic.
     *
     * @param id
     */
    Future<Topic> upVoteTopic(String id);

    /**
     * Down vote a topic.
     *
     * @param id
     */
    Future<Topic> downVoteTopic(String id);
}
