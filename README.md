# Distributed calculation of fractals

## Introduction
This document represents technical documentation for distributed system that calculates [fractals](https://www.britannica.com/science/fractal "fractals") using [mathematical theory of chaos](https://en.wikipedia.org/wiki/Chaos_theory "mathematical theory of chaos").

System supports:
- Starting calculations for one or multiple fractals.
- Listing distributed system nodes and their data.
- Getting fractal calculation results in the image form.

In the document is described further:
- Architecture of Projects Distributed System
- Node Types
- Communications between nodes in the system
- Messages types
- How to use system

Distributed fractal calculation system is self-organized and main purpose of the project is distributed calculation of fractals using [mathematical theory of chaos](https://en.wikipedia.org/wiki/Chaos_theory "mathematical theory of chaos"). All directories in the system are defined using relative paths.

We can set commands to system using **command-line** or using **script file** named *file_input*. Script file is defined as argument during the startup of every node in the system.

## Supported Commands
- **status [x[id]]** – shows state of all started calculations, which includes number of points in every fractal. Presents all system nodes that are working on each fractal and how many points every node has calculated. If X is defined as name of the job, then only calculation data of particular job is shown. If X is defined as name of the job and ID is defined as fractal ID, then only calculation data of particular fractal are shown.

- **start [X]** – start calculation for job X. X is the name of job which has to be unique and has to be defined in configuration directory of Servant node. If we don’t specify X, then command prompt will ask for parameters that are required to start calculations for specific job.

- **result X [id]** – shows fractal results for specific job X in form of .png image. If we don’t specify ID then all results of job X will be shown. If we specify ID then results of fractal ID of Job X will be shown.

- **stop X** – stops executing job X. Removes job from distributed system and active nodes are rescheduled to calculate other jobs.

- **quit** – removes node from distributed system and shut down the node.

## Nodes in the distributed system
Two types of nodes exists in distributed system:
- [Bootstrap node](#bootstrap-node)
- [Servant node](#servant-node)

### Bootstrap node
---
There is only one bootstrap node in the distributed system. [Bootstrap node]($bootstrap-node) is used to support new [Servant nodes]($servant-node) to connect to the distributed network. Communication between [Bootstrap node]($bootstrap-node) and [Servant nodes]($servant-node) are minimal and they communicate with each other only when [Servant node]($servant-node) has to join or leave distributed network. [Bootstrap node]($bootstrap-node) do not know the architecture of distributed and the way [Servant nodes]($servant-node) communicate with each other. [Bootstrap node]($bootstrap-node) has to be always online in order to support new [Servant node]($servant-node) to join or leave distributed network.

#### Bootstrap node configuration:
To function properly we need to supply configuration file to [Bootstrap node]($bootstrap-node). Configuration file path should be passed as argument in command-line during startup of [Bootstrap node]($bootstrap-node). Configuration parameters in configuration file:

    bs.ip = ip address of Bootstrap node.
    bs.port = port number of Bootstrap node.


### Servant node
---
Distributed system is made of mutually connected [Servant nodes]($servant-node). Each [Servant node]($servant-node) is uniquely identified with *ip address*, *port number* and *servantID*.

#### Servant node configuration
To properly configure [Servant node]($servant-node) we need to supply configuration *file path* and *servantID* as argument in command-line during startup of [Servant node]($servant-node). Servant configuration file should contain following properties:

    ip = ip address of Servant node.
    port = port number of Servant node.
    bs.ip = ip address of Bootstrap node in distributed system.
    bs.port = port number of Bootstrap node in distributed system.
    soft_failure_time = weak failure limit in milliseconds.
    hard_failure_time = hard failure limit in milliseconds.
    job_count = number of predefined jobs for current Servant node.


Every Servant job has following parameter:

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
    servant0.ip=127.0.0.1
    servant0.port=1100
    servant1.ip=127.0.0.1
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

---
## Distributed System Architecture
Architecture of distributed system is always consistent, but relationships between [Servant nodes]($servant-node) depends on number of [Servant nodes]($servant-node) in the system. Every [Servant node]($servant-node) that connects to the distributed network gets is unique ID that represents his position in distributed network.

Every [Servant node]($servant-node) has the list of his successive nodes and [Servant node]($servant-node) can directly communicate with his successive [Servant nodes]($servant-node). Every [Servant node]($servant-node) has direct link with his first successive node (with node that has id=currentNodeID+1) and additional direct links, that are used as shortcut links, that are further in the distributed system. Number of shortcut links depends on current number of [Servant nodes]($servant-node) that are in distributed system, every [Servant node]($servant-node) has shortcut links with **(N+2)^l mod n** Servant nodes, where **n** is total number of [Servant nodes]($servant-node) in distributed network. Number **l** is from interval of **[0,…,N]**.

### Connecting Servant nodes in distributed network
---

Messages that are used to connect [Servant node]($servant-node) in to distributed network:

- [HAIL](#hail-message)
- [NEW](#newmessage)
- [QuitMessage](#quitmessage)
- [NewNodeMessage](#newnodemessage)
- [WelcomeMessage](#welcomemessage)
- [UpdateMessage](#updatemessage)

More about system messages can be found in [Messages in Distributed Network](#messages-in-distributed-network) section.

#### HAIL Message
---
If [Servant node]($servant-node) wants to connect to distributed network, node first need to send [HAIL message](#hail-message) to [Bootstrap node]($bootstrap-node). Format of HAIL message is:

    HAIL\n
    newServantIP\n
    newServantPort\n


Example of HAIL message:

    HAIL\n
    127.0.0.1\n
    1200\n

When [Bootsrap node]($bootstrap-node) receives HAIL message, then [Bootstrap node]($bootstrap-node) has to return *IP address* and *port number* of first and last [Servant node]($servant-node) in distributed network. Depending on number of [Servant nodes]($servant-node) in distributed network HAIL response should look:

    lastServantIpAddress:lastServantPort\n
    firstServantIpAddress:first ServantPort\n

If [Servant node]($servant-node) that wants to connect to distributed network is first [Servant node]($servant-node) in distributed network, then [Bootstrap node]($bootstrap-node) respond with HAIL response like:

    -1\n
    -1\n


If [Servant node]($servant-node) that wants to connect to distributed network is not first [Servant node]($servant-node) in distributed network, then Bootsrap node respond HAIL response like:

    127.0.0.1:1201
    127.0.0.1:1210

#### NewNodeMessage
---
When [Servant node]($servant-node) that wants to join distributed network receives HAIL response from [Bootstrap node]($bootstrap-node), then [Servant node]($servant-node) has to send NewNodeMessage to the last [Servant node]($servant-node) that was connected to distributed network. Information of last connected [Servant node]($servant-node) is provided in [HAIL Message]($hail-message) response. [Servant node]($servant-node) should put lastServantIpAddress:lastServantPort properties in NewNodeMessage and send to last connected [Servant node]($servant-node). When last connected [Servant node]($servant-node) receives NewNodeMessage, then that [Servant node]($servant-node) has to assign new servantID for [Servant node]($servant-node) that wants to connect to distributed network. Servant ID of newly connected [Servant node]($servant-node) should be (newServantNodeID = lastServantNodeID + 1). [Servant node]($servant-node) that received NewNodeMessage sends back NewNodeMessage response that contains newServantID. And now newly connected [Servant node]($servant-node) becomes last connected [Servant node]($servant-node).

#### WelcomeMessage
---
As response to [NewNodeMessage]($newnodemessage) new servantID is assigned for [Servant node]($servant-node) that wants to join to distributed network. WelcomeMessage is sent as response to [NewNodeMessage]($newnodemessage) to [Servant node]($servant-node) that wants to join to distributed network as approval that he is able to join to distributed network. In WelcomeMessage new [Servant node]($servant-node) gets his servantID. New [Servant node]($servant-node) is not part of distributed network yet as he is not aware of other [Servant nodes]($servant-node) in distributed network.

When new [Servant nodes]($servant-node) receives WelcomeMessage then he sets the last [Servant node]($servant-node) in the distributed network as his predecessor. And for his first successor, node sets the first [Servant node]($servant-node) in distributed network.

Then new [Servant node]($servant-node) sends [NewMessage]($newmessage) to [Bootstrap node]($bootstrap-node), to inform him that new Servant node has approval to join distributed network. When [Bootstrap node]($bootstrap-node) receives [NewMessage]($newmessage), he adds new [Servant node]($servant-node) in active [Servant nodes]($servant-node) list.

Then new [Servant node]($servant-node) sends [UpdateMessage]($updatemessage) to his first successor in order to get information about other [Servant nodes]($servant-node) in distributed network and information about active jobs in network.


#### NewMessage
---
NewMessage is sent by [Servant node]($servant-node) that wants to join distributed network in order to inform [Bootstrap node]($bootstrap-node) that he has approval to join distributed network. Then [Bootstrap]($bootstrap-node) adds new [Servant]($servant-node) in to the list of active [Servant nodes]($servant-node) in network, and [Bootstrap]($bootstrap-node) uses now new [Servant node]($servant-node) as last connected [Servant node]($servant-node) in distributed network. Format of NewMessage:

    New\n
    newServantIpAddress\n
    newServantPort\n


Example of NewMessage:

    New\n
    127.0.0.1\n
    1209\n


#### UpdateMessage
---
UpdateMessage is sent by new [Servant node]($servant-node) in distributed network, to his first successor [Servant node]($servant-node), in order to get information about other active [Servant nodes]($servant-node) in distributed network. Then list of shortcut links is formed for newly added [Servant node]($servant-node). This list represents the list of newly added Servant nodes successors, and he can communicate with his sucesors directly.

Then updateMessage travels through every [Servant node]($servant-node) in network. When UpdateMessage returns to newly added [Servant node]($servant-node), all information required for final joining new [Servant node]($servant-node) in to the network is present.

If distributed system has active jobs, then it is required to perform [Job Scheduling](#job-scheduling). Because new [Servant node]($servant-node) is joined in distributed network. [Job Scheduling]($job-scheduling) is triggered by newly added [Servant node]($servant-node) by sending [JobScheduleMessage]($jobschedulemessage) to his successor. That is how [job scheduling process]($job-scheduling-process) in distributed network begins. More about job scheduling process could be found in [Job Scheduling Process Section]($job-scheduling-process).


## Job Scheduling process
In distributed system job is used to describe information about job in distributed system. Job can be active or inactive job.  Information about jobs are defined in configuration file of [Servant nodes]($servant-node). Inactive Information of inactive jobs are stored only in [Servant nodes]($servant-node) in which configuration files job is defined. Other [Servant node]($servant-node) in distributed network are not aware of job existent until job become active, and cannot access inactive jobs information. More about defining jobs could be found in [Job Scheduling Section](#job- scheduling).

### Job Scheduling
Before [job scheduling]($job-scheduling-process), distributed system gives to job [Servant nodes]($servant-node) that are available to participate in job execution.

Number of [Servant nodes]($servant-node) that executes certain job depends on number of [Servants nodes]($servant-node) and number of active jobs in distributed network.

If number of assigned [Servant nodes]($servant-node) to certain job is like that job cannot be splitted on parts that each [Servant node]($servant-node) gets his own jobs fractal region, then [Servant node]($servant-node) becomes idle. [Servant nodes]($servant-node) that are not idle executes calculation of points for his own fractal region. After [Job Scheduling process](#job-scheduling-process) finishes, distributed system is taking care that previously calculated points still belongs to appropriate jobs and fractal ID’s. This is established by sharing data between [Servant nodes] in distributed system after [Job Scheduling process](#job-scheduling-process) of newly added Jobs in distributed system.

#### Messages that are used during Job Scheduling process

- [JobExecutionMessage]($jobexecutionmessage)
- [AckJobExecutionMessage]($ackjobexecutionmessage)
- [IdleMessage]($idlemessage)
- [AckIdleMessage]($ackidlemessage)
- [ComputedPointsMessage]($computedpointsmessage)

#### Message exchange protocol of Job Scheduling process
[Servant node]($servant-node) that is calculating job scheduling, sends [JobExecutionMessage](#jobexecutionmessage) to [Servant node]($servant-node) which is responsible to calculate fractal ID for certain job. If there is no need to split fractal ID further, then [Servant node]($servant-node) that received [JobExecutionMessage](#jobexecutionmessage) sends AckJobExecutionMessage to [Servant node]($servant-node) that started [Job Scheduling process](#job-scheduling-process), in order to inform him that he is capable to start executing point calculations for job fractal ID, that is assigned to him. If [Servant node]($servant-node) that received [JobExecutionMessage](#jobexecutionmessage) already executes some job, then he needs to send [ComputedPointsMessage]($computedpointsmessage) to his sucesor. In [ComputedPointsMessage]($computedpointsmessage) [Servant node]($servant-node) sends his old fractal ID and job, as information of calculated points that he has calculated for that previous job. This [ComputedPointsMessage]($computedpointsmessage) travels through entire distributed network, and once data of all calculated points in distributed system is synchronized for certain job, then [Servant node]($servant-node) can receive information about new jobs that are scheduled to him, in order to continue executing point calculations for that job, and to prevent data loss of previous point calculations in older jobs, jobs that was executing before [Job Scheduling process](#job-scheduling-process).

If during [Job Scheduling process]($job-scheduling-process) some [Servant node]($servant-node) is determined to be Idle, then that [Servant node]($servant-node) receives [IdleMessage]($idlemessage). If that [Servant nodes]($servant-node) was already executing some job, then he needs to send [ComputedPointsMessage]($computedpointsmessage) to his sucesor, in order to find [Servant node]($servant-node) that will continue to execute calculations for job that he was executing before [Job Scheduling process](#job-scheduling-process). And in the end Idle [Servant node]($servant-node) sends [AckIdleMessage]($ackidlemessage) to [Servant node]($servant-node) that is performing [Job Scheduling]($job-scheduling) during process, to inform him that process of synchronization with Idle [Servant node]($servant-node) is done.

This protocol must be satisfied, if some [Servant node]($servant-node) wants to be part of [Job Scheduling process](#job-scheduling-process).

#### Stopping Active Job
Stopping active job in distributed system cause similar events as starting new job. It start [Job Scheduling process](#job-scheduling-process) and sharing calculated data with other [Servant nodes]($servant-node). Command **stop[x]** where x is name of the job, triggers [Job Scheduling]($job-scheduling) and temporally suspension of execution Jobs in the system.

On [Servant node]($servant-node) that **stop[x]** command is executed starts [Job Scheduling process](#job-scheduling-process). When stop command is executed then all Servant nodes that contains parts of executed point data for stopped job has to delete all job data.

#### Leaving Distributed Network
Executing command **quit** on [Servant node]($servant-node) will start process of leaving distributed network. Then [Servant node]($servant-node) sends circular [QuitMessage](#quitmessage) informing other [Servant nodes]($servant-node) that he is leaving distributed network.  [QuitMessage](#quitmessage) start process of updating distributed network global state and lists of each [Servant node]($servant-node) sucesors. After [Job Scheduling process](#job-scheduling-process) is executed. More about Job Scheduling process can be found in [Job Scheduling Section](#job-scheduling).


Messages that are used when [Servant node]($servant-node) wants to leave distributed newtork:

- [QuitMessage](#quitmessage)
- [ComputedPointsMessage]($computedpointsmessage)


## Current job status report
If we want to get status information of certain active job we can execute **status** command on some of the [Server nodes]($servant-node). If we do not specify any argument with status command, then data of all executing jobs and fractal ID will be presented.

    status

If we specify the name of the job as argument along with **status** command, then all data of specified job and all data of his fractal IDs will be presented.

    status[x]	x – name of the job

If we specify name of the job and fractal ID in **status** command, then data about specific job and his specific fractalID will be presented.

    status[x[id]] 	x – name of the job
                            id – fractal ID



Messages that are used durning job status report generation:

- [AskStatusMessage]($askstatusmessage)
- [TellStatusMessage]($tellstatusmessage)


Example – status command output:
Status:

    jobName=job2
    fractalId=0, pointsCount=388
    totalPointsCount=388, totalServantCount=1
    jobName=job1
    fractalId=0, pointsCount=1132
    fractalId=1, pointsCount=1093
    fractalId=2, pointsCount=607
    totalPointsCount=2832, totalServantCount=3




## View Job Result
If we want to get result of executing certain job we can use **result** command in any of the [Servant nodes]($servant-node). **Result** command starts the process of gathering job execution results.

If we specify only job name, then data of all jobs fractal IDs will be presented.

    result jobName


If we specify job name and fractal ID in command result, then data of specific fractal ID of certain job will be presented.

    result jobName[1]








## Communication between Servant Nodes
Communication between [Servant nodes]($servant-node) in distributed network is handled using Socket that is not [FIFO (First-in-First-Out)](https://www.geeksforgeeks.org/socket-in-computer-network/ "FIFO (First-in-First-Out)"). Every [Servant node]($servant-node) contains its unique IP address, port number and servantID.

    ipAddress|portNumber|servantID

Servant nodes could be physically separated on different networks.
Messages that are used in distributed network must extends **BasicMessage** class that represents set of common parameter for all the messages.

### Routing Messages through Distributed Network
---
Any [Servant node]($servant-node) can send message directly to other [Servant node]($servant-node), only if other [Servant node]($servant-node) is his direct sucesor or if first [Servant node]($servant-node) has that [Servant node]($servant-node) in his **shortcut list**.

Distributed System has two types of messages:

- [Circular messages](#circular-messages)
- [Messages referred to specific node](#messages-referred-to-specific-node)

#### Circular Messages
Some messages are required to be sent circularly through distributed network from one [Servant node]($servant-node) to another. Circular Message ends its network traversal when it arrives to the [Servant node]($servant-node) that firstly sent that message.

#### Messages referred to specific node
Some messages are required to be sent to specific node. Those messages could be sent directly to [Servant node]($servant-node) if [Servant nodes]($servant-node) are direct sucesors or has that node in shortcut list, or indirectly if two nodes are distant in distributed network.

**Example:** *if Servant node A is not direct sucesor of Servant node B, or Servant A don’t have defined Servant node B in his shortcut list, then we will send message to Servant node B using mediator Servant nodes, that are closest to Servant node B and has direct link with Servant node A.*

---
## Messages in Distributed Network
All messages in distributed network in order to support communication between actors in the system must extend **BasicMessage** class.

**BasicMessage** class contains all parameters that are common for all message in distributed network. All messages are serialized using [Java Serialization](https://www.simplilearn.com/tutorials/java-tutorial/serialization-in-java#:~:text=Serialization%20in%20Java%20is%20the,then%20de%2Dserialize%20it%20there. "Java Serialization").

BasicMessage class parameters:

- senderIpAddress – ip address of message sender.
- senderPort – port number of message sender.
- receiverIpAddress – ip addres of message receiver.
- receiverPort – port number of message receiver.
- messageID – message identifier.
- clock – logical clock that is used for [Lamport Mutex](https://en.wikipedia.org/wiki/Lamport%27s_distributed_mutual_exclusion_algorithm "Lamport Mutex").

### NewNodeMessage
[Servant node]($servant-node) sends NewNodeMessage to other [Servant nodes]($servant-node) after communication with [Bootstrap node]($bootstrap-node). [Servant node]($servant-node) who wants to join to distributed network send this message to last Servant node in network to inform the nodes that he wants to join. 

[Servant node]($servant-node) who wants to join network receives [WelcomeMessage](#welcomemessage) with his servantID.


NewNode message parameters are:

    firstServantIpAddressAndPort – ip address and port number of first Servant node in distributed network. (String, format: ipAddress:portNumber).



### WelcomeMessage
[Servant node]($servant-node) who received [NewNodeMessage](#newnodemessage) sends WelcomeMessage to [Servant node]($servant-node) who wants to join distributed network.

WelcomeMessage parameters are:

    firstServantIpAddressAndPort – ip address and port number of first Servant node in distributed network (String, format: ipAddress:portNumber)
    servantId – represents generated servantID for Servant node that wants to join distributed network.


### UpadateMessage
This is circular message that sends [Servant node]($servant-node) that just joined in to the distributed network. When UpdateMessage returns to [Servant node]($servant-node) that sent it first, then he can update information about other [Servant nodes]($servant-node) in distributed network. This message triggers [Job Scheduling Process](#job-scheduling-process).

UpdateMessage parameters are:

    servantsMap – data about all Servant nodes in distributed network. (Map<Integer, ServantInfo> - Key: servantID, Value: servantInfo)
    servantJobsMap – data about all active jobs, information about all Servant nodes that are executing specific job and their given Fractal IDs. (Map<Integer, FractalJob> - Key: servantId, Value: fractalJob).
    activeJobs – list of currently active jobs in distributed system (List<Job>) 


### QuitMessage
This is circular message that is sent by [Servant node]($servant-node) that wants to leave distributed network. [Servant node]($servant-node) who receives QuitMessage have to update his successor map. This message trigers [Job Scheduling process](#job-scheduling-process).

QuitMessage parameters are:

    quitServantId – represents ID of Servant node that wants to leave distributed network.
    jobName – represents the name of the job that Servant who wants to leave distributed network was executing.
    fractalId – represent fractal ID for job which Servant node, that wants to leave distributed network, was executing.
    quitComputedPoints – contains all points that were calculeted by current Servant node.


### PoisonMessage
This message is used for shuting down **FifoSendWorker** threads.




### JobScheduleMessage
This message is used as a request to begin [Job Scheduling process](#job-scheduling-process) in distributed system. This message was sent after some Servant node joined or left distributed network, durning adding or removing jobs from distributed system. Reception of this message trigers [Job Scheduling process](#job-scheduling-process) in distributed network.


JobScheduleMessage parameters are:

    finalReceiverId – id of last Servant node that should receive this message (int).

    scheduleType – represents the type of job scheduling that should be performed. Scheduling type depends of content in which this message was sent. ScheduleType: ADD_SERVET, REMOVE_SERVANT, ADD_JOB, REMOVE_JOB



### JobExecutionMessage
This message is sent by [Servant node]($servant-node) that trigered [Job Scheduling process](#job-scheduling-process) or [Servant node]($servant-node) that has done specific part of [Job Scheduling](#job-scheduling). Receiver of this message has to update information about new Job Schedule, as he will send information of previously calculated points to certain Servant Job, if he was working on any job. If reciever of this message got more than one fractal ID, then he has to make additional Scheduling based on fractal IDs list and points that he received, and then he could start calculating new points.

Also receiver sends AckJobExecutionMessage to Servant node that started [Job Scheduling process](#job-scheduling-process) as response to JobExecutionMessage.

JobExecutionMessage parameters are:

    Job – represents Servant node’s job that he is executing. (Job)

    jobFractalIds – list of fractal IDs for specific job. (List<String>)

    startPoints – starting fractal points from which fractal should be calculated. (List<Point>)

    servantJobs – represents list of all currently active jobs in distributed system and all Servant nodes that are exexuting that specific job as their fractal IDs. (Map<Integer, FractalJob> - Key: servantID, Value: fractalId).

    level – represents the level of details of fractal IDs. Division Depth of specific job. (int)

    finalReceiverId – servantId of Servant node that should last receive this message. (int)

    mappedFractalJobs – structure that represents mappings between certain new and old fractal IDs and their jobs. (Map<FractalJob, FractalJob> - Key: oldFractalJob, Value: newFractalJob)

    scheduleType – represents the schedule type.
            ScheduleType – ADD_SERVLET, REMOVE_SERVLET, ADD_JOB, REMOVE_JOB

    jobSchedulerId – represents servantId of Servant node that initialized Job Scheduling process. (int)

    finalReceiverId – represents id of Servant node that should recevie certain message. (int)

###AckIdleMessage
This message is sent by [Servant node]($servant-node) that previously received [IdleMessage]($idlemessage), node that does not execute any job after [Job Scheduling process]($job-scheduling-process). AckIdleMessage is sent to [Servant node]($servant-node) that was executing [Job Scheduling process]($job-scheduling-process) as a response to [IdleMessage]($idlemessage). Receiver of AckIdleMessage updates its information about [Servant nodes]($servant-node) that should respond to its [jobExecutionMessage]($jobexecutionmessage) or [IdleMessage]($idlemessage) by incrementing the number of ACK message responses counter.

AckIdleMessage parameters are:

    finalReceiverId – id Servant node that should receive the message.


###ComputedPointsMessage
This message is sent by [Servant node]($servant-node) that was calculating the points for specific job and fractal ID, but [Job Scheduling]($[job-scheduling]) was triggered in distributed system, so he needs to hands over his data of calculated points to the new [Servant node]($servant-node) that should continue executing his job, after new Job Scheduling process finished.

ComputedPointsMessage parameters are:

    jobName – the name of the job that is related to the Servant nodes calculated data. (String)
    fractalId – fractral ID of the job that Servant nodes calculated data reffers to. (String)
    computedPoints – calculated points that are related to specific job. (List<Points>)




###AskStatusMessage
This message is sent by [Servant node]($servant-node) on which was inicialized execution of **status** command. If it was defined retrieve data for specific job and fractal ID, then AskStatusMessage is sent only to the [Servant node]($servant-node) that was executing calculations for specific job. If we define **status** command only the name of the job, then AskStatusMessage is sent to first [Servant node]($servant-node) in the distributed system who was executing that job. If it was defined to get status of all jobs in distributed system, then AskStatusMessage is sent to nodes first successor.

If it was required to get data for specific job and fractal ID, then receiver of AskStatusMessage returns his points calculations using [TellStatusMessage]($tellstatusmessage). If it was required to get status of specific job, then receiver of the AskStatusMessage adds his point calculation for fractal ID for that job in [AskTellMessage]($asktellmessage), and then sends [AskTellMessage]($asktellmessage) to next [Servant node]($servant-node) that is executing other fractal ID for that job. When [AskTellMessage]($asktellmessage) reaches the last [Servant node]($servant-node) in the distributed system that is executing any fractal ID for specific job, at that time all data for all fractals calculations are present already in [AskTellMessage]($asktellmessage), so last [Servant node]($servant-node) sends all fractals ID point calculation for specific job to [Servant node]($servant-node) that initialized **status** command execution using [TellStatusMessage]($tellstatusmessage).

AskStatusMessage parameters are:

    jobName – name of the job for which status was asked for. (String)

    fractalId – fractal ID related to job name which calculation status was asked for. (String)

    resultMap – represents result map that contains data about all active jobs in the system, jobs fractals and data about calculated dos for specific fractral ID. (Map<String, Map<String, Integer>> - <Key: jobName, <Key: fractalId, Value: pointsCountForFractalId>>)

    version – represents the version of specific get status command. Version can be:
            0 – status jobName fractailId
            1 – status jobName
            2 – status

    finalReceiverId – id of Servant node that this message was intended.


###TellStatusMessage
This message is sent by [Servant node]($servant-node) who lastly added data about fractal ID calculated points for required job name. Receiver of this message is [Servant node]($servant-node) on which was initialized **get status** command. When [Servant node]($servant-node) receives this message, critical section is released and results of get status command are printed.

TellStatusMessage contains following parameters:


    resultMap – represents the results of every active job in distributed system, results of Fractal ID calculations and calculated points for that specific fractal ID. (Map<String, Map<String, Integer>> - <Key: jobName, <Key: fractalid, Value: computedPointsForFractalID>>)

    finalReceiverId – represents the id of Servant node that should receive this message.

    Version – represents the version of executing status command: Version could be:
            0 – status jobName fractalId
            1 – status jobName
            2 – status




###AskJobFractalIdResultMessage
This message is sent by [Servant node]($servant-node) that initialized **get result** command with name of the job and fractal ID. [AskJobFractalIdResultMessage]($askjobfractalidresultmessage) is sent to [Servant node]($servant-node) who is executing fractal ID for specified job. When [Servant node]($servant-node) receive this message, then as response he sends [TellJobFractalIdResultMessage]($telljobfractalidresultmessage) to [Servant node]($servant-node) that initialize **get result** command.

AskJobFractalIdResultMessage parameters are:

    jobName – name of the job for which results are asked for. (String)
    finalReceiverId – id of Servant node that should receive this message. (Integer)




###AskJobResultMessage
This message is sent by [Servant node]($servant-node) who initialized **get result** command. Message is sent to the first [Servant node]($servant-node) that is calculating points for specified fractal ID for that job that is specified in get result command. Then AskJobResultMessage is forwarding all the way to the last [Servant node]($servant-node) that is calculating points for specific job.

Receiver of AskJobResultMessage adds his calculation data to the message, and if there is any [Servant node]($servant-node) who is executing calculations for specific job left, then the AskJobResultMessage is forwarded to the next [Servant node]($servant-node) that is executing calculations for that job. If receiver of AskJobResultMessage is last [Servant node]($servant-node) who executes that job, then he sends TellJobResultMessage to [Servant node]($servant-node) who initialized get result command. 

AskJobResultMessage parameters are:

    jobName – the name of the job for which results are asked for. (String)
    lastServantId – id of the last Servant node that executes specific job. (int)
    computedPoints – represents computed points for specific job. (int)
    finalReceiverId – id of Servant node who should receive this message. (int)




###TellJobResultMessage
This message is sent by last [Servant node]($servant-node) that is executing calculations for specific job. Receiver of this message is [Servant node]($servant-node) that initialized **get result** command. Receiver of this message releases critical section and presents the results of specified jobs calculations in image (.png) format. Result image is stored on following path *- “/results/jobName_proprotion.png”*.


Parameters of TellJobResultMessage are:


    jobName – the name of the job for which results are asked for. (Stirng)
    computedPoints – represents computed point points for specific job. (List<Point>)
    width – width of calculated surface area. (int)
    height – height of calculated surface area. (int)
    proportion – job proportion. (double)



###StopJobMessage
This is circular message that is sent by [Servant node]($servant-node) that started **stop** command for specific job. This message is received by every [Servant node]($servant-node) in distributed system. When [Servant node]($servant-node) receives this message, it is required to delete specific job and its calculation data. If receiver of this message was executing specific job, then he has to stop executing that job, and then forwards the StopJobMessage to the next [Servant node]($servant-node). If all Servant nodes are notified about stopping certain job, then [Job Scheduling process]($job-scheduling-process) is triggered. 

Parameters of StopJobMessage are:

    jobName – the name of the job for which results are asked for. (Stirng)


