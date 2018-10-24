package io.coti.zerospend;

import io.coti.basenode.communication.ZeroMQPropagationPublisher;
import io.coti.basenode.data.Network;
import io.coti.basenode.data.NodeType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

import java.util.Arrays;

@Slf4j
@SpringBootApplication
@EnableAsync
public class ZeroSpendServerApplication {

    @Autowired
    private static ZeroMQPropagationPublisher zeroMQPropagationPublisher;

    public static void main(String[] args) {

        SpringApplication.run(ZeroSpendServerApplication.class, args);
        log.info("##################################################################");
        log.info("################    ZERO SPEND NODE IS UP       ##################");
        log.info("##################################################################");
    }
}