package com.carousell.diggit.topics.store;

import com.carousell.diggit.topics.Topic;
import com.carousell.diggit.topics.idgen.IDGenerator;
import com.carousell.diggit.topics.idgen.InMemoryIDGenerator;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.shareddata.AsyncMap;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * In-memory implementation of the Topic Store.
 * This class utilizes #AsyncMap as the data structure.
 *
 * @author Moath
 */
public class InMemTopicStore implements TopicsStore {

    private final static Logger LOG = Logger.getLogger(InMemTopicStore.class);

    private static final String TOPICS_MAP = "topicsMap";

    private IDGenerator idGenerator;
    private AsyncMap<String, Topic> topics;

    public InMemTopicStore(Vertx vertx) {
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
    public Future<List<Topic>> getTopTopics(int top) {
        Future<List<Topic>> future = Future.future();
        topics.values(event -> {
            if (event.succeeded()) {
                final Collection<Topic> topicCollection = event.result();
                final int topicsSize = topicCollection.size();
                final List<Topic> topicsList = new ArrayList<>(topicCollection);
                topicsList.sort((topic1, topic2) -> Integer.compare(topic2.getUpVotes(), topic1.getUpVotes()));
                future.complete(topicsList.subList(0, top > topicsSize ? topicsSize : top));
            } else {
                future.fail(event.cause());
                LOG.error("Failed to get topics!", event.cause());
            }
        });

        return future;
    }

    @Override
    public Future<List<Topic>> getTopics() {
        Future<List<Topic>> future = Future.future();
        topics.values(event -> {
            if (event.succeeded()) {
                future.complete(new ArrayList<>(event.result()));
            } else {
                future.fail(event.cause());
                LOG.error("Failed to get topics!", event.cause());
            }
        });

        return future;
    }

    @Override
    public Future<Topic> addTopic(String text) {
        final Future<Topic> future = Future.future();
        final Future<String> id = idGenerator.generateID();
        id.setHandler(idHandler -> {
            if (idHandler.succeeded()) {
                final String newTopicID = idHandler.result();
                final Topic newTopic = new Topic(text, newTopicID);
                topics.put(newTopicID, newTopic, resPut -> {
                    if (resPut.succeeded()) {
                        future.complete(newTopic);
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
    public Future<Topic> upVoteTopic(String id) {
        final Future<Topic> future = Future.future();
        topics.get(id, resGet -> {
            if (resGet.succeeded()) {
                Topic topic = resGet.result();
                topic.upVotes();

                topics.replace(id, topic, resReplace -> {
                    if (resReplace.succeeded()) {
                        future.complete(topic);
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
    public Future<Topic> downVoteTopic(String id) {
        final Future<Topic> future = Future.future();
        topics.get(id, resGet -> {
            if (resGet.succeeded()) {
                Topic topic = resGet.result();
                topic.downVotes();

                topics.replace(id, topic, resReplace -> {
                    if (resReplace.succeeded()) {
                        future.complete(topic);
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
