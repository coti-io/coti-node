#!/bin/bash
while ! curl -X POST http://trustscore1:7030/getTransactionBatch  -H 'Content-Type: application/json' -d '{ "startingIndex":1000}' | grep transactions
do
		echo waitingForDependency
        sleep 3
done
java -jar ./fullnode.jar --spring.config.additional-location=./config/fullnode.properties --receiving.server.addresses=$1 --propagation.server.addresses=$2 --recovery.server.address=$3
