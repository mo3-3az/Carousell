package com.carousell.diggit.verticle;

import com.carousell.diggit.topics.InMemTopicStore;
import com.carousell.diggit.topics.Topic;
import com.carousell.diggit.topics.TopicsStore;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Context;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.JsonObject;
import org.apache.log4j.Logger;

/**
 * This verticle will manage any functionality realted to topics, such as:
 * <ol>
 * <li>Register Topics</li>
 * <li>Retrieve Topics</li>
 * <li>Vote Topics</li>
 * </ol>
 *
 * @author Moath
 */
public class TopicsManager extends AbstractVerticle {

    public static final String TOPICS_MANAGER_ADDRESS = "topics.manager";
    public static final String TOPICS_MANAGER_ADDRESS_PUBISH = "topics.manager.publish";

    private final static Logger LOG = Logger.getLogger(TopicsManager.class);

    private TopicsStore topicsStore;

    @Override
    public void init(Vertx vertx, Context context) {
        super.init(vertx, context);
        topicsStore = new InMemTopicStore();
    }

    @Override
    public void start(Future<Void> future) throws Exception {
        final EventBus eventBus = vertx.eventBus();
        eventBus.localConsumer(TOPICS_MANAGER_ADDRESS, handler -> {
            if (handler.body() == null) {
                LOG.warn("Topic wasn't registered, topic text null!");
                return;
            }

            final String topicText = handler.body().toString();
            if (topicText.trim().isEmpty()) {
                LOG.warn("Topic wasn't registered, topic text is empty!");
                return;
            }

            topicsStore.addTopic(topicText).setHandler(event -> {
                if (event.succeeded()) {
                    eventBus.publish(TOPICS_MANAGER_ADDRESS_PUBISH, JsonObject.mapFrom(new Topic(topicText, event.result())));
                } else {
                    LOG.warn("Topic wasn't registered!", event.cause());
                }
            });
        }).completionHandler(event -> {
            if (event.failed()) {
                future.fail(event.cause());
                LOG.error("Registering Topics Manager failed.", event.cause());
            } else {
                LOG.info("Topics Manager registered successfully.");
                future.complete();
            }
        });
    }
}
