package com.carousell.diggit.topics.idgen;

import io.vertx.core.Future;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Moath
 */
public class InMemoryIDGenerator implements IDGenerator {
    AtomicInteger id;

    public InMemoryIDGenerator() {
        id = new AtomicInteger(0);
    }

    @Override
    public Future<String> generateID() {
        return Future.succeededFuture(String.valueOf(id.addAndGet(1)));
    }
}
