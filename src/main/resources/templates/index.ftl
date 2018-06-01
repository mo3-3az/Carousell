<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>diggit</title>

    <meta name="viewport" content="width=device-width, initial-scale=1">

    <link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/css/bootstrap.min.css">
    <link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/css/bootstrap.min.css">

    <script src="http://cdn.jsdelivr.net/sockjs/0.3.4/sockjs.min.js"></script>
    <script src='https://cdnjs.cloudflare.com/ajax/libs/vertx/3.5.0/vertx-eventbus.js'></script>
    <script src="https://ajax.googleapis.com/ajax/libs/jquery/3.2.1/jquery.min.js"></script>
    <script src="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/js/bootstrap.min.js"></script>
    <script src="js/main.js"></script>

</head>
<body>
    <div class="container">
        <div class="span12">
            <h1 class="h2 text-center">diggit</h1>
        </div>
    </div>
    <div class="container">
        <div class="panel panel-default">
            <div class="panel-body">
                <form class="form">
                    <div class="form-group">
                        <label for="topicText">Topic Text</label>
                        <textarea class="form-control" id="topicText" rows="3"></textarea>
                    </div>
                    <button type="button" class="btn btn-info pull-right" onclick="registerTopic()">
                        REGISTER TOPIC
                    </button>
                </form>
            </div>
        </div>

    </div>
    <br>
    <div class="container instance">
        <div class="table-responsive">
            <h2 class="module"></h2>
            <table class="table table-striped">
                <thead>
                    <tr>
                        <th class="col-xs-1 name">ID</th>
                        <th class="col-xs-5 name">Topic</th>
                        <th class="col-xs-2">Up Votes</th>
                        <th class="col-xs-2">Down Votes</th>
                        <th class="col-xs"></th>
                    </tr>
                </thead>
                <tbody>
                    <tr class="topic">
                        <td class="no" id="id">ID</td>
                        <td id="text">Topic1</td>
                        <td class="no" id="upVotes">10</td>
                        <td class="no" id="downVotes">9</td>
                        <td class="pull-right">
                            <button type="button" class="btn btn-info" onclick="upVote(id);">
                                Up Vote
                            </button>
                            <button type="button" class="btn btn-info" onclick="downVote(id);">
                                Down Vote
                            </button>
                        </td>
                    </tr>
                </tbody>
            </table>
        </div>
    </div>
</body>
</html>