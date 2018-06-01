package com.carousell.diggit.topics.store;

import com.carousell.diggit.topics.Topic;
import com.carousell.diggit.topics.idgen.IDGenerator;
import com.carousell.diggit.topics.idgen.InMemoryIDGenerator;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.shareddata.AsyncMap;
import org.apache.log4j.Logger;

import java.util.*;

/**
 * In-memory implementation of the Topic Store.
 *
 * @author Moath
 */
public class InMemTopicStore implements TopicsStore {

    private final static Logger LOG = Logger.getLogger(InMemTopicStore.class);

    private static final String TOPICS_MAP = "topicsMap";

    private IDGenerator idGenerator;
    private AsyncMap<String, Topic> topics;

    public InMemTopicStore(Vertx vertx) {
        this(vertx, TOPICS_MAP);
    }

    public InMemTopicStore(Vertx vertx, String mapName) {
        this.idGenerator = new InMemoryIDGenerator();
        vertx.sharedData().<String, Topic>getAsyncMap(TOPICS_MAP, res -> {
            if (res.succeeded()) {
                topics = res.result();
            } else {
                LOG.error("Something went wrong initializing the topics map!", res.cause());
            }
        });
    }

    @Override
    public Future<List<Topic>> getTopics(int top) {
        Future<List<Topic>> future = Future.future();
        topics.entries(event -> {
            if (event.succeeded()) {
                final Map<String, Topic> result = event.result();
                final List<Topic> topicsList = new ArrayList<>(result.values()).subList(0, top);
                topicsList.sort(Comparator.comparingInt(Topic::getVotes));
                future.complete(topicsList);
            } else {
                future.fail(event.cause());
                LOG.error("Failed to get topics!", event.cause());
            }
        });

        return future;
    }

    @Override
    public Future<String> addTopic(String text) {
        final Future<String> future = Future.future();
        final Future<String> id = idGenerator.generateID();
        id.setHandler(idHandler -> {
            if (idHandler.succeeded()) {
                final String newTopicID = idHandler.result();
                topics.put(newTopicID, new Topic(text, newTopicID), resPut -> {
                    if (resPut.succeeded()) {
                        future.complete(newTopicID);
                        LOG.info("New topic added. ID: " + newTopicID);
                    } else {
                        future.fail(resPut.cause());
                        LOG.error("Failed to add new topic!", resPut.cause());
                    }
                });
            } else {
                future.fail(idHandler.cause());
                LOG.error("Failed to add new topic!", idHandler.cause());
            }
        });

        return future;
    }

    @Override
    public Future<Void> upVoteTopic(String id) {
        final Future<Void> future = Future.future();
        topics.get(id, resGet -> {
            if (resGet.succeeded()) {
                Topic topic = resGet.result();
                topic.upVotes();

                topics.replace(id, topic, resReplace -> {
                    if (resReplace.succeeded()) {
                        future.complete();
                        LOG.info("Topic up voted. ID: " + id);
                    } else {
                        future.fail(resReplace.cause());
                        LOG.error("Failed to up vote topic! ID: " + id, resReplace.cause());
                    }
                });
            } else {
                future.fail(resGet.cause());
                LOG.error("Failed to up vote topic! ID: " + id, resGet.cause());
            }
        });

        return future;
    }

    @Override
    public Future<Void> downVoteTopic(String id) {
        final Future<Void> future = Future.future();
        topics.get(id, resGet -> {
            if (resGet.succeeded()) {
                Topic topic = resGet.result();
                topic.downVotes();

                topics.replace(id, topic, resReplace -> {
                    if (resReplace.succeeded()) {
                        future.complete();
                        LOG.info("Topic down voted. ID: " + id);
                    } else {
                        future.fail(resReplace.cause());
                        LOG.error("Failed to down vote topic! ID: " + id, resReplace.cause());
                    }
                });
            } else {
                future.fail(resGet.cause());
                LOG.error("Failed to down vote topic! ID: " + id, resGet.cause());
            }
        });

        return future;
    }
}
