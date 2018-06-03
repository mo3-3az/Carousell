package com.carousell.diggit.topics;

import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import junit.framework.TestCase;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(VertxUnitRunner.class)
public class TopicTest extends TestCase {

    /**
     * Test goes as follows:
     * <ol>
     * <li>Create a new topic.</li>
     * <li>Up vote the topic.</li>
     * <li>Check if upVotes equal to 1.</li>
     * </ol>
     */
    @Test
    public void testUpVotes() {
        final Topic topic = new Topic("1", "1");
        topic.upVotes();
        assertEquals(topic.getUpVotes(), 1);
    }

    /**
     * Test goes as follows:
     * <ol>
     * <li>Create a new topic.</li>
     * <li>Down vote the topic.</li>
     * <li>Check if down votes equal to 1.</li>
     * </ol>
     */
    @Test
    public void testDownVotes() {
        final Topic topic = new Topic("1", "1");
        topic.downVotes();
        assertEquals(topic.getDownVotes(), 1);
    }

    /**
     * Test goes as follows:
     * <ol>
     * <li>Create a new topic with HTML markups as topic text.</li>
     * <li>Check if text was escaped.</li>
     * </ol>
     */
    @Test
    public void testEscapeHTML() {
        final Topic topic = new Topic("<h1>Header</h1> \" &", "1");
        assertEquals(topic.getText(), "&lt;h1&gt;Header&lt;/h1&gt; &quot; &amp;");
    }

    /**
     * Test goes as follows:
     * <ol>
     * <li>Create a new topic.</li>
     * <li>Up vote the topic.</li>
     * <li>Down vote the topic.</li>
     * <li>Convert to a Json object and create a Json object with same values and compare.</li>
     * </ol>
     */
    @Test
    public void testToJsonObject() {
        final Topic topic = new Topic("1", "1");
        topic.upVotes();
        topic.downVotes();
        assertEquals(topic.toJsonObject(), new JsonObject()
                .put(Topic.JSON_PROPERTY_TEXT, "1")
                .put(Topic.JSON_PROPERTY_ID, "1")
                .put(Topic.JSON_PROPERTY_UP_VOTES, 1)
                .put(Topic.JSON_PROPERTY_DOWN_VOTES, 1));
    }
}