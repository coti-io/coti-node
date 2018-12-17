package testUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.coti.basenode.data.*;
import io.coti.basenode.http.Request;
import lombok.extern.slf4j.Slf4j;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
@Slf4j
public class TestUtils {

    private static final String[] hexaOptions = {"0","1","2","3","4","5","6","7","8","9","A","B","C","D","E","F"};

    public static String getRandomHexa(){
        String hexa = "";
        for(int i =0 ; i < 20 ; i++){
            int randomNum = ThreadLocalRandom.current().nextInt(0, 15 + 1);
            hexa += hexaOptions[randomNum];
        }
        return hexa;
    }

    public static Double getRandomDouble() {
        Random r = new Random();
        return 1 + (100 - 1) * r.nextDouble();
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

    public static TransactionData createTransactionFromJson(String transactionJson)  {
        AddTransactionRequest addTransactionRequest = null;
        ObjectMapper mapper = new ObjectMapper();
        try {
            addTransactionRequest =
                    mapper.readValue(transactionJson, AddTransactionRequest.class);
        } catch (IOException e) {
            log.error("Error when create trx from JSON" +e);
        }
        return
                new TransactionData(
                        addTransactionRequest.baseTransactions,
                        addTransactionRequest.hash,
                        addTransactionRequest.transactionDescription,
                        addTransactionRequest.trustScoreResults,
                        addTransactionRequest.createTime,
                        addTransactionRequest.senderHash);
    }

    public static BaseTransactionData createBaseTransactionDataWithSpecificHash(Hash hash){
        return new BaseTransactionData
                (hash,
                        //new Hash("caba14b7fe219b3da5dee0c29389c88e4d134333a2ee104152d6e9f7b673be9e0e28ca511d1ac749f46bea7f1ab25818f335ab9111a6c5eebe2f650974e12d1b7dccd4d7"),
                        new BigDecimal(0),
                        hash,
                        new SignatureData("", ""),
                        new Date());
    }

}

class AddTransactionRequest extends Request {
    @NotEmpty
    public List<@Valid BaseTransactionData> baseTransactions;
    @NotNull
    public Hash hash;
    @NotEmpty
    public String transactionDescription;
    @NotNull
    public Date createTime;
    @NotEmpty
    public List<@Valid TransactionTrustScoreData> trustScoreResults;
    @NotNull
    public Hash senderHash;
    @NotNull
    public TransactionType type;
}

