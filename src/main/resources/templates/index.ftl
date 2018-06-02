<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>diggit</title>

    <meta name="viewport" content="width=device-width, initial-scale=1">

    <link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/css/bootstrap.min.css">
    <link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/css/bootstrap.min.css">

    <script src="https://cdn.jsdelivr.net/sockjs/0.3.4/sockjs.min.js"></script>
    <script src='https://cdnjs.cloudflare.com/ajax/libs/vertx/3.5.0/vertx-eventbus.js'></script>
    <script src="https://ajax.googleapis.com/ajax/libs/jquery/3.2.1/jquery.min.js"></script>
    <script src="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/js/bootstrap.min.js"></script>
    <script src="js/main.js"></script>

</head>
<body>
    <div class="container">
        <div class="span12">
            <h1 class="h2 text-center">diggit (<b>digg</b>redd<b>it</b>)</h1>
        </div>
    </div>
    <div class="container">
        <div class="panel panel-default">
            <div class="panel-body">
                    <div class="form-group">
                        <label for="topicText">Topic</label>
                        <textarea class="form-control" id="topicText" rows="3" maxlength="255"></textarea>
                        <p id="topicAddedInfo" class="text-muted">Topic text must be under 255 characters.</p>
                        <button type="button" class="btn btn-info pull-right" onclick="registerTopic()">
                            ADD TOPIC
                        </button>
                    </div>
            </div>
        </div>

    </div>
    <br>
    <div class="container instance">
        <div class="table-responsive">
            <h3 class="module">Top Topics</h3>
            <table class="table table-striped" id="topTopics">
                <thead>
                    <tr>
                        <th class="col-xs-1 name">ID</th>
                        <th class="col-xs-4 name">Topic</th>
                        <th class="col-xs-2">Up Votes</th>
                        <th class="col-xs-2">Down Votes</th>
                        <th class="col-xs"></th>
                    </tr>
                </thead>
                <tbody>
                </tbody>
            </table>
        </div>
    </div>
    <br>
    <br>
    <div class="container instance">
        <div class="table-responsive">
            <h3 class="module">Topics</h3>
            <table class="table table-striped" id="topics">
                <thead>
                <tr>
                    <th class="col-xs-1 name">ID</th>
                    <th class="col-xs-4 name">Topic</th>
                    <th class="col-xs-2">Up Votes</th>
                    <th class="col-xs-2">Down Votes</th>
                    <th class="col-xs"></th>
                </tr>
                </thead>
                <tbody>
                </tbody>
            </table>
        </div>
    </div>
</body>
</html>