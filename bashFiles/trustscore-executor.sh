#!/bin/bash
while ! curl -X POST http://dsp3:8040/getTransactionBatch  -H 'Content-Type: application/json' -d '{ "startingIndex":1000}' | grep transactions
do
		echo waitingForDependency
        sleep 3
done
java -jar ./trustscore.jar --spring.config.additional-location=./config/trustscore.properties
