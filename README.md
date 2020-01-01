# BWMatcher
===========================================

The purpose of this project is to provide the core features, orchestrade and manage the processing flow. 


Technologies
------------

+ java
+ Spring
+ docker
+ gradle
+ elastic search

Prerequisites
--------------
You will need Docker in order to successfully run this project.

https://docs.docker.com/install/


How To Compile
--------------

The service can be compiled with:

```
gradle clean build
```


How To Build the Docker Image
--------------

Use this command to build the docker image:

```
docker build .
```


How To start docker compose
--------------

The service can be initiated with:

```
docker-compose up --build
```

How To get Access
--------------

The service can be compiled with:

```
docker-compose up --build
```

Successful compilation conditions
--------------
This project uses pmd, findbugs, jacoco to guaranty the quality of the code.

In addition there is a jacoco task that is attached to the build lifecycle that prevents the successful compilation of the project if there is no enought unit test code coverage.

The current minimun coverage percentage is: 80 %