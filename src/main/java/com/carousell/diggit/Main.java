package com.carousell.diggit;

import com.carousell.diggit.verticle.WebServer;
import io.vertx.core.Vertx;

/**
 * This class is the starting point of the application.
 *
 * @author Moath
 */
public class Main {
    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();
        vertx.deployVerticle(new WebServer());
    }
}
