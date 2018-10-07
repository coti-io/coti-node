
########### Docker execution info ###########
Steps:
	0. Navigate with gitbash (or other shell app) to coti-node/bashFiles folder and run Dos2Unix on each bash file. This is will change the lines ending, linux command will not work without it and the GIT-push reset it to windows style (CRLF).
	1. Install Docker for windows. We developed with: Version 18.06.1-ce-win73 (19507)
	2. Navigate to coti-node project folder (i.e C:\Projects\github\coti-node) where the Dockerfile(s) and docker-compose file are located.
	3. run:
		a. docker-compose build
				This step will take few minutes in its first run. This step builds each container but it's not up yet.
		b. docker-compose up -d
				You can run "docker-compose up" without the '-d' if you want to see the logs from the several java applications.
				This step executes the containers.
		c. To stop the servers just click Ctrl+c. If you want to change the docker-compose/Dockerfile(s) run "docker-compose down", change the desired lines and go back to steps a+b

Technical information:
	Each dockerfile is separated to two parts:
		1. Building the jar using maven commands. Pay attention to the 'must have' components of each docker file (node) . i.e 'base node','libs','pot' ...
		2. Copying the jar from #1 container and copping it (+ properties, sh file and snapshot.csv) to a new (slim) container, which will be the container that will actually run the jar.
	We created 6 containers that are running from 4 images (node types).1 full node + 3 dsps + 1 zerospend+ 1 trustscore.
	So basically we added 4 type of files:
		1. Properties folder. which have one property file for each type of node.
		2. Docker files which holds each container's commands.
		3. docker-compose file which is a yaml file that define services. There we define the pc (this) and the containers ports, dockerfile, execution commands...
		4. bashFiles
				We control the order with the bash execution files which located in the "bashFiles" folder. In the files there is kind of a listener that check every 3 seconds if the dependent
				server is up and only after it is up, the service will run.
				The execution order is:
					zerospend
					dsps
					trustscore
					fullnode



	* 	You can enter the properties folder and see the servers properties.
		The properties that are different in the dsps and full node are entered to the bash script in the docker-compose level as part of the execution command.

	* 	If you want to see the logs of each server type :
		docker ps
			This is how you will see the containers that are running.

		winpty docker exec -it dsp1 bash
			If you want to enter the dsp1 container type:

		now you can navigate to the logs folder and read the logs.



An indication that the services are running smoodly are the following logs when running without the '-d' option in the "docker-compose up" command:

	zerospend1            | 2018-10-04 16:14:07.190  INFO 1 --- [MessageBroker-2] i.c.b.services.BaseNodeMonitorService    : Transactions = 11, TccConfirmed = 0, DspConfirmed = 11, Confirmed = 0, LastIndex = 10, Sources = 11, PostponedTransactions = 0
	zerospend1            | 2018-10-04 16:14:07.541  INFO 1 --- [MessageBroker-1] i.c.zerospend.services.DspVoteService    : Updated live dsp nodes list. Count: 3
	dsp1                  | 2018-10-04 16:14:07.925  INFO 43 --- [sk-scheduler-10] i.c.b.services.BaseNodeMonitorService    : Transactions = 11, TccConfirmed = 0, DspConfirmed = 11, Confirmed = 0, LastIndex = 10, Sources = 11, PostponedTransactions = 0
	dsp2                  | 2018-10-04 16:14:09.197  INFO 37 --- [ask-scheduler-8] i.c.b.services.BaseNodeMonitorService    : Transactions = 11, TccConfirmed = 0, DspConfirmed = 11, Confirmed = 0, LastIndex = 10, Sources = 11, PostponedTransactions = 0
	trustscore1           | 2018-10-04 16:14:09.377  INFO 55 --- [MessageBroker-2] i.c.b.services.BaseNodeMonitorService    : Transactions = 11, TccConfirmed = 0, DspConfirmed = 11, Confirmed = 0, LastIndex = 10, Sources = 11, PostponedTransactions = 0
	fullnode1             | 2018-10-04 16:14:10.973  INFO 67 --- [MessageBroker-1] i.c.b.services.BaseNodeMonitorService    : Transactions = 11, TccConfirmed = 0, DspConfirmed = 11, Confirmed = 0, LastIndex = 10, Sources = 11, PostponedTransactions = 0
	dsp3                  | 2018-10-04 16:14:12.127  INFO 28 --- [ask-scheduler-5] i.c.b.services.BaseNodeMonitorService    : Transactions = 11, TccConfirmed = 0, DspConfirmed = 11, Confirmed = 0, LastIndex = 10, Sources = 11, PostponedTransactions = 0

Now you can use the  wallet-encryption-library project that can send multiple transactions to the containers.
Steps:
	1. Pull latest dev branch of wallet-encryption-library.
	2. Navigate to wallet-encryption-library\TestRunFromNode and run :
		node SystemTest.js http://localhost:7070 1000 500 http://localhost:7030
		Which run 2 transction per second , 1000 transactions in total. Full node is http://localhost:7070 , trustscore node is http://localhost:7030

This will make the motors running, you can see the logs.
Now you can use coti-live (after changing the hard coded severs address)



Enjoy




