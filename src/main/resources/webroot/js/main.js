var eventBus;
$( document ).ready(function() {
    eventBus = new EventBus('/eventbus');

    eventBus.onopen = function() {
        eventBus.registerHandler('topics.manager.out.new', function(error, message) {
            if(!error){
                reloadTopics(message);
            }
        });

        eventBus.send("topics.manager.in.list", topicText, function (ar_error, ar){
                if(ar_error == null){
                    reloadTopics(ar);
                }
            });
    }
});

function registerTopic(){
    var topicText = $("#topicText").val();

    eventBus.send("topics.manager.in.add", topicText, function (ar_error, ar){
        if(ar_error == null){
            $("#topicText").val("");
            $("#topicAddedInfo").html(ar.body);
            setTimeout(function() {
               $( "#topicAddedInfo" ).html("Topic text under 255 characters.");
            }, 3000);
        }
    });
}

function upVote(id){
     var obj = {
         id: id,
         up: true
     }
     eventBus.send("topics.manager.in.vote", obj);
}

function downVote(id){
     var obj = {
         id: id,
         up: false
     }
     eventBus.send("topics.manager.in.vote", obj);
}

function reloadTopics(message){
    if(message.body == null){
        return;
    }

    $("#topics tbody tr").remove();

    $.each(message.body, function(i, item) {
            $('#topics').find('tbody').append("<tr class='topic'>"
            + " <td class='no' id='id'>" + item.id + "</td> "
            + " <td id='text'>" + item.text + "</td> "
            + " <td class='no' id='upVotes'>" + item.upVotes + "</td> "
            + " <td class='no' id='downVotes'>" + item.downVotes + "</td> "
            + " <td class='pull-right'> "
            + "<button type='button' class='btn btn-info' onclick='upVote(" + item.id + ")'>UP VOTE</button> "
            + " <button type='button' class='btn btn-info' onclick='downVote(" + item.id + ")'>DOWN VOTE</button> </td> </tr>");
    });
}