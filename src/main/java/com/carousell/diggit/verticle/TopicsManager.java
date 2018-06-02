package com.carousell.diggit.verticle;

import com.carousell.diggit.topics.Topic;
import com.carousell.diggit.topics.store.InMemTopicStore;
import com.carousell.diggit.topics.store.TopicsStore;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Context;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.apache.log4j.Logger;

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
    static final String TOPICS_MANAGER_ADDRESS_INBOUND_ADD = "topics.manager.in.add";
    static final String TOPICS_MANAGER_ADDRESS_INBOUND_VOTE = "topics.manager.in.vote";
    static final String TOPICS_MANAGER_ADDRESS_OUTBOUND_NEW = "topics.manager.out.new";

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
    public void start(Future<Void> future) throws Exception {
        final EventBus eventBus = vertx.eventBus();

        eventBus.localConsumer(TOPICS_MANAGER_ADDRESS_INBOUND_LIST, handler -> {
            publishTopics(eventBus);
        });

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
                handler.reply("Topic wasn't registered, topic text exceds " + TOPIC_TEXT_MAX + "!");
                return;
            }

            topicsStore.addTopic(escapeHTML(topicText)).setHandler(event -> {
                if (event.succeeded()) {
                    publishTopics(eventBus);
                    handler.reply("Topic added.");
                } else {
                    LOG.warn("Topic wasn't added!", event.cause());
                }
            });
        });

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

            final Future<Void> voteTopic;
            if (voteUp) {
                voteTopic = topicsStore.upVoteTopic(id);
            } else {
                voteTopic = topicsStore.downVoteTopic(id);
            }

            voteTopic.setHandler(event -> {
                if (event.succeeded()) {
                    publishTopics(eventBus);
                    LOG.info("Topic voted successfully.");
                } else {
                    LOG.warn("Topic wasn't voted!", event.cause());
                }
            });
        });
    }

    private void publishTopics(EventBus eventBus) {
        topicsStore.getTopics(TOP).setHandler(event -> {
            if (event.succeeded()) {
                final List<Topic> result = event.result();
                JsonArray topics = new JsonArray();
                result.forEach(topic -> {
                    topics.add(topic.toJsonObject());
                });

                eventBus.publish(TOPICS_MANAGER_ADDRESS_OUTBOUND_NEW, topics);
            } else {
                LOG.warn("Unable to get top " + TOP + "topics!", event.cause());
            }
        });
    }

    private String escapeHTML(String text) {
        final String[] toEscape = {"&", "\"", "<", ">"};
        final String[] replaceWith = {"&amp;", "&quot;", "&lt;", "&gt;"};

        for (int i = 0; i < toEscape.length; i++) {
            text = text.replaceAll(toEscape[i], replaceWith[i]);
        }

        return text;
    }
}
