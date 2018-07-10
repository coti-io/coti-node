package io.coti.fullnode.service;

import io.coti.common.data.Hash;
import io.coti.common.data.TransactionData;
import io.coti.fullnode.service.interfaces.IPropagationService;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.*;
import java.util.Comparator;
import java.util.List;
import java.util.Scanner;

@Data
@Slf4j
@Service
public class PropagationService implements IPropagationService {

    @Value("${last.transaction.file}")
    private String lastTransactionFile;

    @Autowired
    private IPropagationSender propagationSender;

    private int lastIndex;
    private Hash lastTransactionHash;


    @PostConstruct
    private void init() {
        log.info("full node Propagation service Started");
        loadLastTransactionFromFile();
    }

    private void loadLastTransactionFromFile() {

        try {
            Scanner scanner = new Scanner(new File(lastTransactionFile));
            if (scanner.hasNextLine()) {
                lastIndex = Integer.parseInt(scanner.nextLine());
            } else {
                lastIndex = -1;
            }
            if (scanner.hasNextLine()) {
                lastTransactionHash = new Hash(scanner.nextLine());
            }
            scanner.close();
        } catch (FileNotFoundException e) {
            log.error("Error loading from lastTransaction file", e);
        }
    }

    @Override
    public void updateLastTransactionFromFile() {
        PrintWriter pw = null;
        try {
            pw = new PrintWriter(new FileWriter("out.txt"));
        } catch (IOException e) {
            log.error("Error saving to lastTransaction file", e);
        }

        pw.write(lastIndex);
        pw.write(lastTransactionHash.toString());

        pw.close();
    }


    @Override
    public void propagateTransactionToDspNode(TransactionData transactionData) {
        propagationSender.propagateTransactionToDspNode(transactionData);
    }

    @Override
    public TransactionData propagateTransactionFromDspByHash(Hash transactionHash) {
        return propagationSender.propagateTransactionFromDspByHash(transactionHash);
    }

    @Override
    public TransactionData propagateTransactionFromDspByIndex(int index) {
        return propagationSender.propagateTransactionFromDspByIndex(index);
    }

    @Override
    public List<TransactionData> propagateMultiTransactionFromDsp(int index) {
        List<TransactionData> transactions = propagationSender.propagateMultiTransactionFromDsp(index);

        transactions.sort(new Comparator<TransactionData>() {
            @Override
            public int compare(TransactionData t1, TransactionData t2) {
                if (t1.getIndex() > t2.getIndex()) {
                    return 1;
                }
                return -1;
            }
        });

        if (transactions.get(transactions.size() - 1).getHash() == propagationSender.getLastTransactionHashFromOtherDsp(index))
            return transactions;
        // TODO: Response if they are not equal
        return null;
    }

    @Override
    public void updateLastIndex(TransactionData transactionData) {
        synchronized (this) {
            if (transactionData.getIndex() > lastIndex) {
                lastIndex = transactionData.getIndex();
                lastTransactionHash = transactionData.getHash();
                updateLastTransactionFromFile();
            }
        }
    }


}
