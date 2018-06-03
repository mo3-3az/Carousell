package com.carousell.diggit.topics.store;

import com.carousell.diggit.topics.Topic;
import io.vertx.core.Future;

import java.util.List;

/**
 * This interface defines the APIs to deal with topics.
 *
 * @author Moath
 */
public interface TopicsStore {

    /**
     * Get top topics ordered by up votes descending.
     *
     */
    Future<List<Topic>> getTopTopics();

    /**
     * Get all topics.
     */
    Future<List<Topic>> getTopics();

    /**
     * Register a topic.
     *
     * @return Newly added topic.
     */
    Future<Topic> addTopic(String text);

    /**
     * Up vote a topic.
     *
     * @return THe voted Topic.
     */
    Future<Topic> upVoteTopic(String id);

    /**
     * Down vote a topic.
     *
     * @return THe voted Topic.
     */
    Future<Topic> downVoteTopic(String id);
}
