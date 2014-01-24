FishStore
=========

An Example app prepared for the kickoff meeting of the Denver Meetup group: 
**[Reactive Programming Enthusiasts Denver](http://www.meetup.com/Reactive-Programming-Enthusiasts-Denver)**

This example uses
--------------

* Twitter Bootstrap 3
* JQuery
* AngularJs 1.1
* Scala 2.10.2
* Playframework 2.2
* Akka 2.2
* Reactive Mongodb 0.10.0


About the App
--------------

This application is an exercise in thorough non-blocking.

The Fish store allows two primary actions, catch fish and deliver fish.

The application presents two versions, store one and store two.  

Store one presents

* Play! Action
* Play! Action.async
* Play! Json
* Scala Future
* Scala Future.map
* Akka tell
* Akka ActorLogging

Store two introduces some additional functionality namely:

* Akka ask
* Akka pipeTo
* Akka context.become
* Scala Future.flatMap
* Play! Concurrent.broadcast
* ReactiveMongo concurrent db insert


Requirements
--------------

Required is scala, sbt and mongodb


License
--------------
This application is provided under the Apache 2 License



