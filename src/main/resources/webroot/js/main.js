var eventBus;
$( document ).ready(function() {
    eventBus = new EventBus('/eventbus');

    eventBus.onopen = function() {
        eventBus.registerHandler('topics.manager.out.new', function(error, message) {
            if(error == null){
                topicAdded(message);
            }
        });

        eventBus.registerHandler('topics.manager.out.vote', function(error, message) {
            if(error == null){
                topicVoted(message);
            }
        });

        eventBus.registerHandler('topics.manager.out.list.top', function(error, message) {
            if(error == null){
                reloadTopTopics(message);
            }
        });

        eventBus.send("topics.manager.in.list", null, function (error, message){
            if(error == null){
                reloadTopics(message);
            }
        });

        eventBus.send("topics.manager.in.list.top", null, function (error, message){
            if(error == null){
                reloadTopTopics(message);
            }
        });
    }
});

//////////////////////////////////////////////

function registerTopic(){
    var topicText = $("#topicText").val();

    eventBus.send("topics.manager.in.add", topicText, function (ar_error, ar){
        if(ar_error == null){
            if(ar.body.success){
                $("#topicText").val("");
            }
            $("#topicAddedInfo").html(ar.body.msg);
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

//////////////////////////////////////////////

function topicAdded(message){
    if(message.body == null){
        return;
    }

   var item = message.body;
   $('#topics').find('tbody').append("<tr id='topic" + item.id + "'>"
   + " <td class='id'>" + item.id + "</td> "
   + " <td class='text'>" + item.text + "</td> "
   + " <td class='upVotes'>" + item.upVotes + "</td> "
   + " <td class='downVotes'>" + item.downVotes + "</td> "
   + " <td class='pull-right'> "
   + " <button type='button' class='btn btn-info' onclick='upVote(" + item.id + ")'>UP VOTE</button> "
   + " <button type='button' class='btn btn-info' onclick='downVote(" + item.id + ")'>DOWN VOTE</button> </td> </tr>");
}

function topicVoted(message){
    if(message.body == null){
        return;
    }

    var item = message.body;
    $('#topics').find("tbody").find("#topic" + item.id + " .upVotes").html(item.upVotes);
    $('#topics').find("tbody").find("#topic" + item.id + " .downVotes").html(item.downVotes);
}

function reloadTopTopics(message){
    if(message.body == null){
        return;
    }

    $("#topTopics tbody tr").remove();

    $.each(message.body, function(i, item) {
            $('#topTopics').find('tbody').append("<tr>"
            + " <td class='text'>" + item.text + "</td> "
            + " <td class='upVotes'>" + item.upVotes + "</td> "
            + " <td class='downVotes'>" + item.downVotes + "</td> "
            + " </tr>");
    });
}

function reloadTopics(message){
    if(message.body == null){
        return;
    }

    $("#topics tbody tr").remove();

    $.each(message.body, function(i, item) {
        $('#topics').find('tbody').append("<tr id='topic" + item.id + "'>"
        + " <td class='no'>" + item.id + "</td> "
        + " <td class='text'>" + item.text + "</td> "
        + " <td class='upVotes'>" + item.upVotes + "</td> "
        + " <td class='downVotes'>" + item.downVotes + "</td> "
        + " <td class='pull-right'> "
        + " <button type='button' class='btn btn-info' onclick='upVote(" + item.id + ")'>UP VOTE</button> "
        + " <button type='button' class='btn btn-info' onclick='downVote(" + item.id + ")'>DOWN VOTE</button> </td> </tr>");
    });
}