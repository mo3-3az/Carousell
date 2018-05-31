$( document ).ready(function() {
    var eventBus = new EventBus('/eventbus');

    eventBus.onopen = function() {
        eventBus.send("topics.manager", "Event bus on client is ready.");
    }
});