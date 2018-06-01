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

/**
 * Note: For every test the store will be re-initialized to avoid any inconsistencies.
 *
 * @author Moath
 */
@RunWith(VertxUnitRunner.class)
public class InMemTopicStoreTest extends TestCase {

    private Vertx vertx;
    private InMemTopicStore inMemTopicStore;

    public InMemTopicStoreTest() {
        vertx = Vertx.vertx();
        inMemTopicStore = new InMemTopicStore(vertx);
    }

    /**
     * Test goes as follows:
     * <ol>
     * <li>Add a topic.</li>
     * <li>Check if you have 1 topic and it's text is what was added.</li>
     * </ol>
     */
    @Test
    public void testAddTopic(TestContext testContext) {
        inMemTopicStore = new InMemTopicStore(vertx);
        final String text = "Topic 1";
        inMemTopicStore.addTopic(text).setHandler(addTopicEvent -> {
            if (addTopicEvent.succeeded()) {
                inMemTopicStore.getTopics(1).setHandler(getTopicsEvent -> {
                    if (getTopicsEvent.succeeded()) {
                        final List<Topic> topics = getTopicsEvent.result();
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

    /**
     * Test goes as follows:
     * <ol>
     * <li>Add 1 topic.</li>
     * <li>Up vote topic 1 twice.</li>
     * <li>Check if topic 1 has 2 vote.</li>
     * </ol>
     */
    @Test
    public void testUpVoteTopic(TestContext testContext) {
        inMemTopicStore = new InMemTopicStore(vertx);

        final String id = "1";
        CompositeFuture compositeFuture = CompositeFuture.join(Arrays.asList(
                inMemTopicStore.addTopic("Topic 1"),
                inMemTopicStore.upVoteTopic(id),
                inMemTopicStore.upVoteTopic(id)
        ));

        final Async async = testContext.async();
        compositeFuture.setHandler(preTestCaseEvents -> {
            if (preTestCaseEvents.succeeded()) {
                inMemTopicStore.getTopics(1).setHandler(getTopicsEvent -> {
                    if (getTopicsEvent.succeeded()) {
                        testContext.assertEquals(getTopicsEvent.result().get(0).getVotes(), 2);
                    } else {
                        fail();
                    }

                    async.complete();
                });
            } else {
                fail();
            }
        });
    }

    /**
     * Test goes as follows:
     * <ol>
     * <li>Add 1 topic.</li>
     * <li>Down vote topic number 1.</li>
     * <li>Up vote topic number 1.</li>
     * <li>Down vote topic number 1.</li>
     * <li>Check if topic 1 has 0. (It shouldn't be -1).</li>
     * </ol>
     */
    @Test
    public void testDownVoteTopic(TestContext testContext) {
        inMemTopicStore = new InMemTopicStore(vertx);

        final String id = "1";
        CompositeFuture compositeFuture = CompositeFuture.join(Arrays.asList(
                inMemTopicStore.addTopic("Topic 1"),
                inMemTopicStore.downVoteTopic(id),
                inMemTopicStore.upVoteTopic(id),
                inMemTopicStore.downVoteTopic(id)
        ));

        final Async async = testContext.async();
        compositeFuture.setHandler(preTestCaseEvents -> {
            if (preTestCaseEvents.succeeded()) {
                inMemTopicStore.getTopics(1).setHandler(getTopicsEvent -> {
                    if (getTopicsEvent.succeeded()) {
                        testContext.assertEquals(getTopicsEvent.result().get(0).getVotes(), 0);
                    } else {
                        fail();
                    }

                    async.complete();
                });
            } else {
                fail();
            }
        });
    }

    /**
     * Test goes as follows:
     * <ol>
     * <li>Add 4 topics.</li>
     * <li>Up vote topic 4.</li>
     * <li>Check if you have 4 topics and the first is topic 4.</li>
     * </ol>
     */
    @Test
    public void getTopics(TestContext testContext) {
        inMemTopicStore = new InMemTopicStore(vertx);

        final String topic4ID = "4";
        CompositeFuture compositeFuture = CompositeFuture.join(Arrays.asList(
                inMemTopicStore.addTopic("Topic 1"),
                inMemTopicStore.addTopic("Topic 2"),
                inMemTopicStore.addTopic("Topic 3"),
                inMemTopicStore.addTopic("Topic 4"),
                inMemTopicStore.upVoteTopic(topic4ID)
        ));

        final Async async = testContext.async();

        compositeFuture.setHandler(event -> {
            if (event.succeeded()) {
                inMemTopicStore.getTopics(4).setHandler(preTestCaseEvents -> {
                    if (preTestCaseEvents.succeeded()) {
                        testContext.assertTrue(preTestCaseEvents.result().size() == 4 && preTestCaseEvents.result().get(0).getId().equals(topic4ID));
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


    @Override
    protected void tearDown() {
        vertx.close();
    }
}