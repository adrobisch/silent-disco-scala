Silent Disco Scala
==================

This is a alternative server for [nikku/silent-disco](https://github.com/nikku/silent-disco) implemented with play2 / scala / mongodb.

Checkout the [demo installation](http://silent-disco-scala.drobisch.cloudbees.net/).

Get your own instance in the cloud
==================================

Use the clickstart to install silent disco at cloudbees (you will need an free account):

<a href="https://grandcentral.cloudbees.com/?#CB_clickstart=http://unkonstant.de/dev/repository/silent-disco/clickstart.json"><img src="https://d3ko533tu1ozfq.cloudfront.net/clickstart/deployInstantly.png"/></a>

During the process you will be asked to provide a name / id for your silent disco app.

You will need the [cloudbees sdk](https://wiki.cloudbees.com/bin/view/RUN/BeesSDK) command line to complete the setup. 
After installing it, execute the following two commands:

    bees app:proxy:update -a <yourappid> httpVersion=1.1

Which will enable the websocket support in the cloudbees proxy.

    bees config:set -a <yourappid> SC_CLIENT_ID=<your own client id here>
    bees app:restart as2

To set a valid soundcloud sdk client id for the JavaScript client code and to trigger a restart of the app. 
You can get a client id at http://soundcloud.com/you/apps/ by creating a new application and looking into its details.

License
=======

You may use Silent Disco Scala under the terms of the MIT License.


