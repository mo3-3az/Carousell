package com.carousell.diggit.verticle;

import com.carousell.diggit.config.ConfigKeys;
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
 * This verticle will manage any functionality related to topics.
 * <p>
 * This vertical uses the Event Bus for server/client communications.
 * The following consumers are registered to handle client calls.
 * <ul>
 * <li>List all topics.</li>
 * <li>List top topics.</li>
 * <li>Add a new topic.</li>
 * <li>Vote a topic.</li>
 * </ul>
 * <p>
 * The client on the other hand will be listening for published messages, such as:
 * <ul>
 * <li>List top topics will publish upon any topic addition/voting.</li>
 * <li>Addition of a new topic.</li>
 * <li>Voting of a topic.</li>
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

    private TopicsStore topicsStore;
    private int topicTextMaxSize;
    private int topTopicsSize;

    @Override
    public void init(Vertx vertx, Context context) {
        super.init(vertx, context);
        topicsStore = new InMemTopicStore(vertx);
        topicTextMaxSize = vertx.getOrCreateContext().config().getInteger(ConfigKeys.TOPIC_TEXT_MAX_SIZE);
        topTopicsSize = vertx.getOrCreateContext().config().getInteger(ConfigKeys.TOP_TOPICS_SIZE);
    }

    @Override
    public void start(Future<Void> future) {
        final EventBus eventBus = vertx.eventBus();

        //Those futures are defined to track the completion of registering all the consumers in order to
        //indicate that the verticle is ready.
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
                handler.reply(new JsonObject().put("msg", "Topic wasn't registered, topic text is null!"));
                return;
            }

            final String topicText = handler.body().toString();
            if (topicText.trim().isEmpty()) {
                handler.reply(new JsonObject().put("msg", "Topic wasn't registered, topic text is empty!"));
                return;
            }

            if (topicText.trim().length() > topicTextMaxSize) {
                handler.reply(new JsonObject().put("msg", "Topic wasn't registered, topic text exceeds " + topicTextMaxSize + "!"));
                return;
            }

            topicsStore.addTopic(topicText).setHandler(event -> {
                if (event.succeeded()) {
                    publishTopicAdded(eventBus, event.result());
                    publishTopTopics(eventBus);
                    handler.reply(new JsonObject().put("msg", "Topic added successfully.").put("success", true));
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
            final String id = body.getInteger(Topic.JSON_PROPERTY_ID).toString();
            if (id.trim().isEmpty()) {
                LOG.warn("Topic wasn't voted, topic id is empty!");
                return;
            }

            boolean voteUp = body.getBoolean(Topic.JSON_PROPERTY_VOTE_UP);

            final Future<Topic> voteTopic;
            if (voteUp) {
                voteTopic = topicsStore.upVoteTopic(id);
            } else {
                voteTopic = topicsStore.downVoteTopic(id);
            }

            voteTopic.setHandler(event -> {
                if (event.succeeded()) {
                    publishTopicVoted(eventBus, event.result());
                    publishTopTopics(eventBus);
                } else {
                    LOG.warn("Topic wasn't voted!", event.cause());
                }
            });
        }).completionHandler(getConsumerCompletionHandler(voteTopicConsumer));


        //Whenever all completes the future of this verticle will complete.
        //If any fails, the future will fail.
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

    private void replyTopics(Message<Object> handler) {
        topicsStore.getTopics().setHandler(event -> {
            if (event.succeeded()) {
                JsonArray topics = getTopicsJsonArray(event.result());
                handler.reply(topics);
            } else {
                LOG.warn("Unable to get topics!", event.cause());
            }
        });
    }

    private void replyTopTopics(Message<Object> handler) {
        topicsStore.getTopTopics().setHandler(event -> {
            if (event.succeeded()) {
                JsonArray topics = getTopicsJsonArray(event.result());
                handler.reply(topics);
            } else {
                LOG.warn("Unable to get top " + topTopicsSize + "topics!", event.cause());
            }
        });
    }

    private void publishTopTopics(EventBus eventBus) {
        topicsStore.getTopTopics().setHandler(event -> {
            if (event.succeeded()) {
                JsonArray topics = getTopicsJsonArray(event.result());
                eventBus.publish(TOPICS_MANAGER_ADDRESS_OUTBOUND_LIST_TOP, topics);
            } else {
                LOG.warn("Unable to get top " + topTopicsSize + "topics!", event.cause());
            }
        });
    }

    private JsonArray getTopicsJsonArray(List<Topic> topicsList) {
        JsonArray topics = new JsonArray();
        topicsList.forEach(topic -> topics.add(topic.toJsonObject()));
        return topics;
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

}
