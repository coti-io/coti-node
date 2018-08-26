package io.coti.fullnode.services;
import io.coti.common.Infrastructure.ComparableFutureTask;
import io.coti.common.Infrastructure.PriorityExecutor;
import io.coti.common.Pot.PotRunnableTask;
import io.coti.common.data.TransactionData;
import io.coti.common.services.PotService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.concurrent.*;

@Slf4j
@Service
public class PotWorkerService extends PotService {
    private static HashMap<Integer, ExecutorService> queuesPot = new HashMap<>();

    @PostConstruct
    public void init() {
        queuesPot.put(4,  PriorityExecutor.newFixedThreadPool(8));
        queuesPot.put(3,  PriorityExecutor.newFixedThreadPool(3));
        queuesPot.put(2,  PriorityExecutor.newFixedThreadPool(2));
        queuesPot.put(1,  PriorityExecutor.newFixedThreadPool(2));

    }

    public void potAction(TransactionData transactionData) throws InterruptedException {

        int bucketChoice=0;
        int trustScore = transactionData.getRoundedSenderTrustScore();
        if (trustScore>=94)
            bucketChoice =4;
        else if (trustScore>=70)
            bucketChoice =3;
        else if (trustScore>=40)
            bucketChoice =2;
        else
            bucketChoice =1;


        queuesPot.get(bucketChoice).submit(new ComparableFutureTask(new PotRunnableTask(transactionData,targetDifficulty )));
        synchronized (transactionData) {
            transactionData.wait();
        }
    }



}
