package com.carousell.diggit;

import com.carousell.diggit.verticle.TopicsManager;
import com.carousell.diggit.verticle.WebServer;
import io.vertx.core.Vertx;
import org.apache.log4j.Logger;

/**
 * This class is the starting point of the application.
 *
 * @author Moath
 */
public class Main {

    private final static Logger LOG = Logger.getLogger(Main.class);

    public static void main(String[] args) {
        //TODO: Remove for production
        System.setProperty("vertx.disableFileCaching", "true");

        Vertx vertx = Vertx.vertx();
        vertx.deployVerticle(new WebServer(), handler -> {
            if (handler.succeeded()) {
                vertx.deployVerticle(new TopicsManager());
            } else {
                LOG.error("Failed to deploy the web server verticle!", handler.cause());
            }
        });
    }
}
