package com.carousell.diggit.verticle;

import io.vertx.config.ConfigRetriever;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.bridge.PermittedOptions;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.StaticHandler;
import io.vertx.ext.web.handler.TemplateHandler;
import io.vertx.ext.web.handler.sockjs.BridgeOptions;
import io.vertx.ext.web.handler.sockjs.SockJSHandler;
import io.vertx.ext.web.templ.FreeMarkerTemplateEngine;
import io.vertx.ext.web.templ.TemplateEngine;
import org.apache.log4j.Logger;

/**
 * This is the main verticle, here an implementation of a web server will be done.
 *
 * @author Moath
 */
public class WebServer extends AbstractVerticle {

    private final static Logger LOG = Logger.getLogger(WebServer.class);

    private static final String SYS_PROPERTY_HTTP_PORT = "http.port";
    private static final int DEFAULT_HTTP_PORT = 8080;
    private static final String ROOT_PATH = "/";
    public static final String PATH_EVENT_BUS = "/eventbus/*";

    @Override
    public void start(Future<Void> future) {
        ConfigRetriever retriever = ConfigRetriever.create(vertx);
        retriever.getConfig(configHandler -> {
            if (configHandler.failed()) {
                future.fail("Failed to retrieve config.");
            } else {
                startWebServer(future, configHandler);
            }
        });
    }

    private void startWebServer(Future<Void> future, AsyncResult<JsonObject> configHandler) {
        Router router = Router.router(vertx);

        TemplateEngine engine = FreeMarkerTemplateEngine.create();
        //TODO: Remove for production
        ((FreeMarkerTemplateEngine) engine).setMaxCacheSize(0);
        TemplateHandler handler = TemplateHandler.create(engine);
        router.get(ROOT_PATH).handler(handler);

        final StaticHandler staticHandler = StaticHandler.create();
        //TODO: Remove for production
        staticHandler.setMaxCacheSize(1);
        staticHandler.setCacheEntryTimeout(1);
        staticHandler.setCachingEnabled(false);
        router.route("/*").handler(staticHandler);

        SockJSHandler sockJSHandler = SockJSHandler.create(vertx);
        BridgeOptions options = new BridgeOptions();
        options.addOutboundPermitted(new PermittedOptions().setAddress(TopicsManager.TOPICS_MANAGER_ADDRESS_OUTBOUND_NEW));
        options.addInboundPermitted(new PermittedOptions().setAddress(TopicsManager.TOPICS_MANAGER_ADDRESS_INBOUND_ADD));
        options.addInboundPermitted(new PermittedOptions().setAddress(TopicsManager.TOPICS_MANAGER_ADDRESS_INBOUND_LIST));
        options.addInboundPermitted(new PermittedOptions().setAddress(TopicsManager.TOPICS_MANAGER_ADDRESS_INBOUND_VOTE));
        sockJSHandler.bridge(options);
        router.route(PATH_EVENT_BUS).handler(sockJSHandler);


        vertx.createHttpServer().requestHandler(router::accept).listen(Integer.getInteger(SYS_PROPERTY_HTTP_PORT, DEFAULT_HTTP_PORT), result -> {
            if (result.failed()) {
                LOG.error("Web Server failed to start!", result.cause());
                future.fail(result.cause());
            } else {
                LOG.info("Web Server started successfully.");
                future.complete();
            }
        });
    }
}
