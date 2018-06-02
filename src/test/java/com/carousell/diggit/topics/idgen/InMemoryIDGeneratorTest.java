package com.carousell.diggit.topics.idgen;

import io.vertx.core.CompositeFuture;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import junit.framework.TestCase;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Arrays;

@RunWith(VertxUnitRunner.class)
public class InMemoryIDGeneratorTest extends TestCase {
    private InMemoryIDGenerator inMemoryIDGenerator;

    public InMemoryIDGeneratorTest() {
        inMemoryIDGenerator = new InMemoryIDGenerator();
    }

    /**
     * Test goes as follows:
     * <ol>
     * <li>Generate 3 ids.</li>
     * <li>Generate 1 id and check if the value is 4.</li>
     * </ol>
     */
    @Test
    public void testGenerateID(TestContext testContext) {
        CompositeFuture.join(Arrays.asList(
                inMemoryIDGenerator.generateID(),
                inMemoryIDGenerator.generateID(),
                inMemoryIDGenerator.generateID()
        )).setHandler(event -> {
            if (event.succeeded()) {
                inMemoryIDGenerator.generateID().setHandler(event1 -> {
                    if (event1.succeeded()) {
                        testContext.assertEquals(event1.result(), "4");
                    } else {
                        fail();
                    }

                    testContext.async().complete();
                });
            } else {
                fail();
            }
        });
    }
}