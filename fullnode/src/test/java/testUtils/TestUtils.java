package testUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.coti.basenode.data.BaseTransactionData;
import io.coti.basenode.data.Hash;
import io.coti.basenode.data.SignatureData;
import io.coti.basenode.data.TransactionData;
import io.coti.basenode.http.AddTransactionRequest;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

@Slf4j
public class TestUtils {
    public static AddTransactionRequest createTransactionRequestFromJson(String transactionJson) {
        AddTransactionRequest addTransactionRequest = null;
        ObjectMapper mapper = new ObjectMapper();
        try {
            addTransactionRequest =
                    mapper.readValue(transactionJson, AddTransactionRequest.class);
        } catch (IOException e) {
            log.error("Error when create trx from JSON" + e);
        }
        return
                addTransactionRequest;
    }

    public static TransactionData createTransactionWithSpecificHash(Hash hash) {
        ArrayList<BaseTransactionData> baseTransactions = new ArrayList<BaseTransactionData>(
                Arrays.asList(new BaseTransactionData
                        (new Hash("caba14b7fe219b3da5dee0c29389c88e4d134333a2ee104152d6e9f7b673be9e0e28ca511d1ac749f46bea7f1ab25818f335ab9111a6c5eebe2f650974e12d1b7dccd4d7"),
                                new BigDecimal(0),
                                hash,
                                new SignatureData("", ""),
                                new Date())));
        TransactionData tx = new TransactionData(baseTransactions,
                hash,
                "test",
                80.53,
                new Date());
        return tx;
    }

}
