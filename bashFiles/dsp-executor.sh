#!/bin/bash
while ! curl -X POST http://zerospend1:7040/getTransactionBatch -H 'Content-Type:application/json' -d '{ "startingIndex":1000}' | grep transactions
do
		echo waiting for dependency
        sleep 3
done
java -jar ./dspnode.jar --spring.config.additional-location=./config/dsp.properties --propagation.server.addresses=$1