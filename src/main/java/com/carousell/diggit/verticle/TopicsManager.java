package com.carousell.diggit.verticle;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
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

    private final static Logger LOG = Logger.getLogger(TopicsManager.class);

    @Override
    public void start(Future<Void> future) throws Exception {
        vertx.eventBus().localConsumer(TOPICS_MANAGER_ADDRESS, handler -> {
            LOG.info("Message received: " + handler.body().toString());
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
