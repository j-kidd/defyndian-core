# Defyndian Core

## Overview
  Defyndian is a personal home automation project in Java, the goal is that several different
  parts can act in a publish/subscribe manner to automate various tasks. The development system currently
  consists of [Raspberry Pis](https://www.raspberrypi.org/) running in several places around the house.
  
  There is also an Android app in development which will act to provide notifications from the system, and 
  allow interaction with various parts of the system.
  
## Technologies
  The system an AMQP broker for communications, currently [RabbitMQ](https://www.rabbitmq.com/), with a single topic exchange.
  There is the option to use a database for state/object and config storage, this is setup to use [MariaDB](https://mariadb.org/).
  The Jackson Databinding library is also used heavily to map messages being sent as JSON to/from objects.
  These technologies are available in the standard repos.
 
## Current State
  This is very much a project under development, the core is there but will most likely receive serious revision, as well as expansion. One major planned change is the development of a Node master which will manage all Defyndian Nodes running on a single system; this should allow sharing of resources (ie. Database connections). 

## Goals
  A stable finished core library which will allow the easy development of nodes which perform specific tasks in the house currently planned:
  * Lights/Heating control ([LightwaveRF](http://lightwaverf.com/))
  * Calendar Integration, adding/removing events and notifications
  * A set of nodes specifically for receiving notifications, an Android app, desktop 'System Tray' type application
  
Several additional elements for system usability are:
  * Voice control integration, possibly?
  * Web interface for monitoring/controlling the system
  * REST API for each node (embedded Jetty container?)
    

  
 
  
