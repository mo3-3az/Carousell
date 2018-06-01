var eventBus;
$( document ).ready(function() {
    eventBus = new EventBus('/eventbus');

    eventBus.onopen = function() {
        eventBus.registerHandler('topics.manager.publish', function(error, message) {
            if(!error){
                reloadTopics(message);
            }
        });
    }
});

function registerTopic(){
    var topicText = $("#topicText").val();

    eventBus.send("topics.manager", topicText, function (reply){
        if(!reply){
            $("#topicText").val("");
        }
    });
}

function upVote(id){
}

function downTopics(id){
}

function reloadTopics(message){
}