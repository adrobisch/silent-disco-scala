Silent Disco Scala
==================

This is a alternative server for [nikku/silent-disco](https://github.com/nikku/silent-disco) implemented with play2 and scala.

Get your own instance in the cloud
==================================

Use the clickstart to install silent disco at cloudbees:

<a href="https://grandcentral.cloudbees.com/?#CB_clickstart=http://unkonstant.de/dev/repository/silent-disco/clickstart.json"><img src="https://d3ko533tu1ozfq.cloudfront.net/clickstart/deployInstantly.png"/></a>


You will need the [cloudbees sdk](https://wiki.cloudbees.com/bin/view/RUN/BeesSDK) command line to complete the setup. 
After installing it, you need to execute the following two commands:

    bees app:proxy:update -a yourAppId httpVersion=1.1

Which will enable the websocket support in the cloudbees proxy.

    bees config:set -a as2 SC_CLIENT_ID=<your own client id here>

To set a valid soundcloud sdk client id for the JavaScript client code. 
You can get a client id at http://soundcloud.com/you/apps/ by creating a new application and looking into its details.
