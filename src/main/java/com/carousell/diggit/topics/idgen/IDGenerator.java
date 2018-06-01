package com.carousell.diggit.topics.idgen;

import io.vertx.core.Future;

/**
 * This interface defines an API to generate a unique id.
 *
 * @author Moath
 */
public interface IDGenerator {
    Future<String> generateID();
}
