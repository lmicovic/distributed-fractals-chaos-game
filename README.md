# Distributed Fractals Calculation

## Introduction

This document represents technical documentation for distributed system that calculates fractals using [mathematical theory of chaos](http://https://en.wikipedia.org/wiki/Chaos_theory "mathematical theory of chaos").

System support:
- Starting calculations for one or multiple fractals.
- Listing distributed system nodes and their data.
- Getting fractal calculation results in the image form.

In the document is described further:
- Architecture of Projects Distributed System
- Node Types
- Communications between nodes in the system
- Messages types
- How to use system

Distributed fractal calculation system is self-organized and main purpose of the project is distributed calculation of fractals using [mathematical theory of chaos](http://https://en.wikipedia.org/wiki/Chaos_theory "mathematical theory of chaos"). All directories in the system are defined using relative paths.

We can set commands to system using command-line or using script file named `file_input`. Script file is defined as argument during the startup of every node in the system.




## Supported Commands

- `status [x[id]]` – shows state of all started calculations, which includes number of points in every fractal. Presents all system nodes that are working on each fractal and how many points every node has calculated. If X is defined as name of the job, then only calculation data of particular job is shown. If X is defined as name of the job and ID is defined as fractal ID, then only calculation data of particular fractal are shown.

- `start [X]` – start calculation for job X. X is the name of job which has to be unique and has to be defined in configuration directory of Servant node. If we don’t specify X, then command prompt will ask for parameters that are required to start calculations for specific job.

- `result X [id]` – shows fractal results for specific job X in form of .png image. If we don’t specify ID then all results of job X will be shown. If we specify ID then results of fractal ID of Job X will be shown.

- `stop X` – stops executing job X. Removes job from distributed system and active nodes are rescheduled to calculate other jobs.

- `quit` – removes node from distributed system and shut down the node.




## Nodes in the distributed system

Two types of nodes exists in distributed system:
- [Bootstrap node](https://github.com/lmicovic/distributed-fractals-chaos-game/tree/main#:~:text=Servant%20node-,Bootstrap%20node,-There%20is%20only "Bootstrap node")
- [Servant node](https://github.com/lmicovic/distributed-fractals-chaos-game/tree/main#:~:text=of%20Bootstrap%20node.-,Servant%20node,-Distributed%20system%20is "Servant node")


### Bootstrap node
---
There is only one bootstrap node in the distributed system. Bootstrap node is used to support new Servant nodes to connect to the distributed network. Communication between Bootstrap node and Servant nodes are minimal and they communicate with each other only when Servant node has to join or leave distributed network. Bootstrap node don’t know the architecture of distributed and the way Servant nodes communicate with each other. Bootstrap node has to be always online in order to support new Servant node to join or leave distributed network.

#### Bootstrap node configuration:
To function properly we need to supply configuration file to Bootstrap node. Configuration file path should be passed as argument in **command-line** during startup of Bootstrap node. Configuration parameters in configuration file:


    bs.ip = ip address of Bootstrap node.
    bs.port = port number of Bootstrap node.

### Servant node
------------
Distributed system is made of mutually connected Servant nodes. Each Servant node is uniquely identified with ip address, port number and serventID.

#### Servant node configuration
To properly configure Servant node we need to supply configuration file path and serventID as argument in command-line during startup of Servant node. Servant configuration file should contain following properties:

    ip = ip address of Servant node.
    port = port number of Servant node.
    bs.ip = ip address of Bootstrap node in distributed system.
    bs.port = port number of Bootstrap node in distributed system.
    soft_failure_time = weak failure limit in milliseconds.
    hard_failure_time = hard failure limit in milliseconds.
    job_count = number of predefined jobs for current Servant node.

Every Servant job has following parameters:

    job[index].name = name that uniquely represents the job.
    job[index].n = number of fractal points in structure. (int, 3 <= N <= 10)
    job[index].p = distance proportion of current point and destination where new point should appear. (double, 0 <= P <= 1)
    job[index].h = height of image on which fractals are calculated.
    job[index].w = width of image on which fractals are calculated.
    job[index].points = set of X and Y coordinates in format (n= 3-x1,y1,x2,y2,x3,y3)

Example of Servant node configuration properties:

    bs.ip=127.0.0.1
    bs.port=2000
    soft_failure_time=1000
    hard_failure_time=10000
    servent0.ip=127.0.0.1
    servent0.port=1100
    servent1.ip=127.0.0.1
    port=1200
    ip=127.0.0.1
    job_count=3
    job1.name=job1
    job1.n=3
    job1.p=0.5
    job1.w=800
    job1.h=800
    job1.points=200,500,500,200,500,700
    job2.name=job2
    job2.n=4
    job2.p=0.4
    job2.w=800
    job2.h=800
    job2.points=0,0,0,800,800,0,800,800



------------

More about following topics can be found in [wiki page](More about following topics can be found in [wiki](https://github.com/lmicovic/distributed-fractals-chaos-game/wiki "wiki") page: "wiki page"):

- System architecture
- Application commands
- Job scheduling
- System messages



