package com.carousell.diggit.verticle;

import com.carousell.diggit.topics.Topic;
import com.carousell.diggit.topics.store.InMemTopicStore;
import com.carousell.diggit.topics.store.TopicsStore;
import io.vertx.core.*;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.apache.log4j.Logger;

import java.util.Arrays;
import java.util.List;

/**
 * This verticle will manage any functionality related to topics, such as:
 * <ul>
 * <li>Register Topics</li>
 * <li>Retrieve Topics</li>
 * <li>Vote Topics</li>
 * </ul>
 *
 * @author Moath
 */
public class TopicsManager extends AbstractVerticle {

    static final String TOPICS_MANAGER_ADDRESS_INBOUND_LIST = "topics.manager.in.list";
    static final String TOPICS_MANAGER_ADDRESS_INBOUND_LIST_TOP = "topics.manager.in.list.top";
    static final String TOPICS_MANAGER_ADDRESS_INBOUND_ADD = "topics.manager.in.add";
    static final String TOPICS_MANAGER_ADDRESS_INBOUND_VOTE = "topics.manager.in.vote";

    static final String TOPICS_MANAGER_ADDRESS_OUTBOUND_LIST_TOP = "topics.manager.out.list.top";
    static final String TOPICS_MANAGER_ADDRESS_OUTBOUND_NEW = "topics.manager.out.new";
    static final String TOPICS_MANAGER_ADDRESS_OUTBOUND_VOTE = "topics.manager.out.vote";

    private final static Logger LOG = Logger.getLogger(TopicsManager.class);
    private static final int TOP = 20;
    private static final int TOPIC_TEXT_MAX = 255;

    private TopicsStore topicsStore;

    @Override
    public void init(Vertx vertx, Context context) {
        super.init(vertx, context);
        topicsStore = new InMemTopicStore(vertx);
    }

    @Override
    public void start(Future<Void> future) {
        final EventBus eventBus = vertx.eventBus();
        Future listTopicsConsumer = Future.future();
        Future listTopTopicsConsumer = Future.future();
        Future addTopicConsumer = Future.future();
        Future voteTopicConsumer = Future.future();
        CompositeFuture consumersHandlers = CompositeFuture
                .join(Arrays.asList(
                        listTopicsConsumer,
                        listTopTopicsConsumer,
                        addTopicConsumer,
                        voteTopicConsumer
                ));

        eventBus.localConsumer(TOPICS_MANAGER_ADDRESS_INBOUND_LIST, this::replyTopics).completionHandler(getConsumerCompletionHandler(listTopicsConsumer));
        eventBus.localConsumer(TOPICS_MANAGER_ADDRESS_INBOUND_LIST_TOP, this::replyTopTopics).completionHandler(getConsumerCompletionHandler(listTopTopicsConsumer));

        eventBus.localConsumer(TOPICS_MANAGER_ADDRESS_INBOUND_ADD, handler -> {
            if (handler.body() == null) {
                handler.reply("Topic wasn't registered, topic text is null!");
                return;
            }

            final String topicText = handler.body().toString();
            if (topicText.trim().isEmpty()) {
                handler.reply("Topic wasn't registered, topic text is empty!");
                return;
            }

            if (topicText.trim().length() > TOPIC_TEXT_MAX) {
                handler.reply("Topic wasn't registered, topic text exceeds " + TOPIC_TEXT_MAX + "!");
                return;
            }

            topicsStore.addTopic(topicText).setHandler(event -> {
                if (event.succeeded()) {
                    final Topic topic = event.result();
                    publishTopicAdded(eventBus, topic);
                    publishTopTopics(eventBus);
                    handler.reply("Topic added successfully.");
                } else {
                    LOG.warn("Topic wasn't added!", event.cause());
                }
            });
        }).completionHandler(getConsumerCompletionHandler(addTopicConsumer));


        eventBus.localConsumer(TOPICS_MANAGER_ADDRESS_INBOUND_VOTE, handler -> {
            if (handler.body() == null) {
                LOG.warn("Topic wasn't voted, topic id is null!");
                return;
            }

            JsonObject body = (JsonObject) handler.body();
            final String id = body.getInteger("id").toString();
            if (id.trim().isEmpty()) {
                LOG.warn("Topic wasn't voted, topic id is empty!");
                return;
            }

            boolean voteUp = body.getBoolean("up");

            final Future<Topic> voteTopic;
            if (voteUp) {
                voteTopic = topicsStore.upVoteTopic(id);
            } else {
                voteTopic = topicsStore.downVoteTopic(id);
            }

            voteTopic.setHandler(event -> {
                if (event.succeeded()) {
                    final Topic topic = event.result();
                    publishTopicVoted(eventBus, topic);
                    publishTopTopics(eventBus);
                } else {
                    LOG.warn("Topic wasn't voted!", event.cause());
                }
            });
        }).completionHandler(getConsumerCompletionHandler(voteTopicConsumer));


        consumersHandlers.setHandler(event -> {
            if (event.succeeded()) {
                future.complete();
            } else {
                future.fail(event.cause());
            }
        });
    }

    private void publishTopicAdded(EventBus eventBus, Topic addedTopic) {
        eventBus.publish(TOPICS_MANAGER_ADDRESS_OUTBOUND_NEW, addedTopic.toJsonObject());
    }

    private void publishTopicVoted(EventBus eventBus, Topic votedTopic) {
        eventBus.publish(TOPICS_MANAGER_ADDRESS_OUTBOUND_VOTE, votedTopic.toJsonObject());
    }

    private Handler<AsyncResult<Void>> getConsumerCompletionHandler(Future future) {
        return event -> {
            if (event.succeeded()) {
                future.complete();
            } else {
                future.fail(event.cause());
            }
        };
    }

    private void replyTopics(Message<Object> handler) {
        topicsStore.getTopics().setHandler(event -> {
            if (event.succeeded()) {
                JsonArray topics = getTopicsJsonArray(event);
                handler.reply(topics);
            } else {
                LOG.warn("Unable to get topics!", event.cause());
            }
        });
    }

    private void replyTopTopics(Message<Object> handler) {
        topicsStore.getTopTopics(TOP).setHandler(event -> {
            if (event.succeeded()) {
                JsonArray topics = getTopicsJsonArray(event);
                handler.reply(topics);
            } else {
                LOG.warn("Unable to get top " + TOP + "topics!", event.cause());
            }
        });
    }

    private void publishTopTopics(EventBus eventBus) {
        topicsStore.getTopTopics(TOP).setHandler(event -> {
            if (event.succeeded()) {
                JsonArray topics = getTopicsJsonArray(event);

                eventBus.publish(TOPICS_MANAGER_ADDRESS_OUTBOUND_LIST_TOP, topics);
            } else {
                LOG.warn("Unable to get top " + TOP + "topics!", event.cause());
            }
        });
    }

    private JsonArray getTopicsJsonArray(AsyncResult<List<Topic>> event) {
        final List<Topic> result = event.result();
        JsonArray topics = new JsonArray();
        result.forEach(topic -> topics.add(topic.toJsonObject()));
        return topics;
    }

}
