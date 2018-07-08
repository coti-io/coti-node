var SockJS = require('sockjs-client');
var Stomp = require('stomp-websocket');
const readline = require('readline');
var socket = new SockJS('http://localhost:8080/websocket');
stompClient = Stomp.over(socket);

function setConnected(isConnected) {
    if (isConnected) {
        console.log('connected!');
    }
    else {
        console.log('not connected!');
    }
    console.log('set connected!');
}

function subscribe(address){
		subscriptionAddress = '/topic/' + address;
		console.log(subscriptionAddress);
        stompClient.subscribe(subscribeToAddresses, "Subscription Request", function (subscription) {
            console.log(JSON.parse(subscription.body).message);
            console.log(JSON.parse(subscription.body).addressHash);
			console.log(JSON.parse(subscription.body).balance);
		});
}

function subscribeToAddresses(){
		
	for(var i = 2; i < process.argv.length; i++){
		var element = process.argv[i];
		console.log("Subscribing to:" + element);
		subscribe(element);	
	}
}

function connect() {
    stompClient.connect({}, function (frame) {
        setConnected(true);
        console.log('Connected: ' + frame);
		subscribeToAddresses();
    });
}

connect();
console.log("after connect!");