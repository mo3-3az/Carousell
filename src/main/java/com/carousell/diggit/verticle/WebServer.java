package com.carousell.diggit.verticle;

import io.vertx.config.ConfigRetriever;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.TemplateHandler;
import io.vertx.ext.web.templ.FreeMarkerTemplateEngine;
import io.vertx.ext.web.templ.TemplateEngine;

/**
 * This is the main verticle, here an implementation of a web server will be done.
 *
 * @author Moath
 */
public class WebServer extends AbstractVerticle {

    public static final String SYS_PROPERTY_HTTP_PORT = "http.port";
    public static final int DEFAULT_HTTP_PORT = 8080;

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
        TemplateHandler handler = TemplateHandler.create(engine);
        router.get("/").handler(handler);

        vertx.createHttpServer().requestHandler(router::accept).listen(Integer.getInteger(SYS_PROPERTY_HTTP_PORT, DEFAULT_HTTP_PORT), result -> {
            if (result.succeeded()) {
                future.complete();
            } else {
                future.fail(result.cause());
            }
        });
    }
}
