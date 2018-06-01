package com.carousell.diggit.topics.store;

import com.carousell.diggit.topics.Topic;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Vertx;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import junit.framework.TestCase;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Arrays;
import java.util.List;

@RunWith(VertxUnitRunner.class)
public class InMemTopicStoreTest extends TestCase {

    private Vertx vertx;
    private InMemTopicStore inMemTopicStore1;
    private InMemTopicStore inMemTopicStore2;
    private InMemTopicStore inMemTopicStore3;
    private InMemTopicStore inMemTopicStore4;

    public InMemTopicStoreTest() {
        vertx = Vertx.vertx();
        inMemTopicStore1 = new InMemTopicStore(vertx, "map1");
        inMemTopicStore2 = new InMemTopicStore(vertx, "map2");
        inMemTopicStore3 = new InMemTopicStore(vertx, "map3");
        inMemTopicStore4 = new InMemTopicStore(vertx, "map4");
    }

    @Test
    public void getTopics(TestContext testContext) {
        CompositeFuture compositeFuture = CompositeFuture.join(Arrays.asList(
                inMemTopicStore1.addTopic("Topic 1"),
                inMemTopicStore1.addTopic("Topic 2"),
                inMemTopicStore1.addTopic("Topic 3"),
                inMemTopicStore1.addTopic("Topic 4")
        ));

        final Async async = testContext.async();

        compositeFuture.setHandler(event -> {
            if (event.succeeded()) {
                inMemTopicStore1.getTopics(4).setHandler(event1 -> {
                    if (event1.succeeded()) {
                        testContext.assertEquals(event.result().size(), 4);
                    } else {
                        fail();
                    }
                });

                async.complete();
            } else {
                fail();
            }
        });
    }

    @Test
    public void testAddTopic(TestContext testContext) {
        final String text = "Topic 1";
        inMemTopicStore2.addTopic(text).setHandler(event -> {
            if (event.succeeded()) {
                inMemTopicStore2.getTopics(1).setHandler(event1 -> {
                    if (event1.succeeded()) {
                        final List<Topic> topics = event1.result();
                        testContext.assertTrue(topics.size() == 1 && topics.get(0).getText().equals(text));
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


    @Test
    public void testUpVoteTopic(TestContext testContext) {
        CompositeFuture compositeFuture = CompositeFuture.join(Arrays.asList(
                inMemTopicStore3.addTopic("Topic 1"),
                inMemTopicStore3.addTopic("Topic 2")
        ));

        final Async async = testContext.async();

        compositeFuture.setHandler(event -> {
            if (event.succeeded()) {
                inMemTopicStore3.upVoteTopic("2").setHandler(event1 -> {
                    if (event1.succeeded()) {
                        inMemTopicStore3.getTopics(2).setHandler(event2 -> {
                            if (event2.succeeded()) {
                                testContext.assertEquals(event2.result().get(1).getId(), "2");
                            } else {
                                fail();
                            }

                            async.complete();
                        });
                    } else {
                        fail();
                    }
                });
            } else {
                fail();
            }
        });
    }

    @Test
    public void testDownVoteTopic(TestContext testContext) {
        CompositeFuture compositeFuture = CompositeFuture.join(Arrays.asList(
                inMemTopicStore4.addTopic("Topic 1"),
                inMemTopicStore4.addTopic("Topic 2"),
                inMemTopicStore4.upVoteTopic("1"),
                inMemTopicStore4.upVoteTopic("2"),
                inMemTopicStore4.upVoteTopic("2")
        ));

        final Async async = testContext.async();

        compositeFuture.setHandler(event -> {
            if (event.succeeded()) {
                inMemTopicStore4.downVoteTopic("2").setHandler(event1 -> {
                    if (event1.succeeded()) {
                        inMemTopicStore4.downVoteTopic("2").setHandler(event2 -> {
                            if (event2.succeeded()) {
                                inMemTopicStore4.getTopics(2).setHandler(event3 -> {
                                    if (event3.succeeded()) {
                                        testContext.assertEquals(event3.result().get(1).getId(), "1");
                                    } else {
                                        fail();
                                    }

                                    async.complete();
                                });
                            } else {
                                fail();
                            }
                        });
                    } else {
                        fail();
                    }
                });
            } else {
                fail();
            }
        });
    }

    @Override
    protected void tearDown() {
        vertx.close();
    }
}