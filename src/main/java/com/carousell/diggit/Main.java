package com.carousell.diggit;

import com.carousell.diggit.verticle.TopicsManager;
import com.carousell.diggit.verticle.WebServer;
import io.vertx.config.ConfigRetriever;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import org.apache.log4j.Logger;

/**
 * This class is the starting point of the application.
 * Here the main verticle (Web Server) will be deployed, upon deployment success, the Topics Manager verticle
 * will be deployed.
 *
 * @author Moath
 */
public class Main {

    private final static Logger LOG = Logger.getLogger(Main.class);

    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();
        ConfigRetriever retriever = ConfigRetriever.create(vertx);
        retriever.getConfig(configHandler -> {
            if (configHandler.failed()) {
                LOG.fatal("Failed to load configurations!", configHandler.cause());
                return;
            }

            deployVerticles(vertx, configHandler.result());
        });
    }

    private static void deployVerticles(Vertx vertx, JsonObject config) {
        DeploymentOptions deploymentOptions = new DeploymentOptions().setConfig(config);

        vertx.deployVerticle(new WebServer(), deploymentOptions, deployWebServerEvent -> {
            if (deployWebServerEvent.succeeded()) {
                vertx.deployVerticle(new TopicsManager(), deploymentOptions, deployTopicsManagerEvent -> {
                    if (deployTopicsManagerEvent.failed()) {
                        LOG.error("Failed to deploy Topics Manager verticle!", deployTopicsManagerEvent.cause());
                    } else {
                        LOG.info("Topics Manager verticle deployed successfully.");
                    }
                });

                LOG.info("Web Server verticle deployed successfully.");
            } else {
                LOG.fatal("Failed to deploy Web Server verticle!", deployWebServerEvent.cause());
            }
        });
    }
}
