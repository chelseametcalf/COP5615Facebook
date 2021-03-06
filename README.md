# Scala Facebook API Simulator
For University of Florida COP5615 (Fall 2015).
By Chelsea Metcalf and Grant Hernandez.

For security model, see Report-security.pdf.

## Usage

SERVER:  
usage: sbt "fbapi/run"

CLIENT:
```
usage: sbt "fbclient/run [hostname] [num users] [num pages] [load]" 
 hostname: a domain name
 num users: number of users to simulate
 num pages: number of pages to create
 load: time slider for content creation rate (must be >= 1)
```

Example:   
  `sbt "fbapi/run" # starts the server`   
  `sbt "fbclient/run localhost 200 20 1" # runs a client that creates 200 users and 20 pages with a load factor of 1`

Example run
===========
<pre>
COP5615Facebook/ $ sbt "fbapi/run"
[info] Running FBAPI 
[INFO] [11/30/2015 20:08:29.081] [fbapi-akka.actor.default-dispatcher-3] [akka://fbapi/user/IO-HTTP/listener-0] Bound to localhost/127.0.0.1:8080
[INFO] [11/30/2015 20:08:29.082] [fbapi-akka.actor.default-dispatcher-2] [akka://fbapi/user/http-binder] REST interface bound to /127.0.0.1:8080
</pre>

<pre>
COP5615Facebook/ $ sbt "fbclient/run localhost 200 20 1"
info] Running FBClient localhost 200 20 1
Target host: http://localhost:8080
Testing with 200 users, 20 pages, and a load of 1
[INFO] [11/30/2015 20:10:07.106] [Facebook-System-akka.actor.default-dispatcher-4] [akka://Facebook-System/user/master] Tasking for state CreateUsers
[INFO] [11/30/2015 20:10:10.235] [Facebook-System-akka.actor.default-dispatcher-6] [akka://Facebook-System/user/master/create-users] Created 200 users
[INFO] [11/30/2015 20:10:10.243] [Facebook-System-akka.actor.default-dispatcher-4] [akka://Facebook-System/user/master] Tasking for state CreatePages
[INFO] [11/30/2015 20:10:10.345] [Facebook-System-akka.actor.default-dispatcher-4] [akka://Facebook-System/user/master/create-pages] Created 20 pages
[INFO] [11/30/2015 20:10:10.347] [Facebook-System-akka.actor.default-dispatcher-8] [akka://Facebook-System/user/master] Tasking for state LinkFriends
</pre>

File Structure
==============
COP5615Facebook/ - IntelliJ IDEA project root and SBT build root (code)  
README.md - this file

Build Environment
=================
Java SDK 1.8  
Scala SDK 2.11.7  
SBT version 0.13.8

Build Tool
==========
This project uses sbt for dependency management, building, and running. To run
this project, type `sbt run` in the COP5615Facebook/ directory (the same directory
as build.sbt).
