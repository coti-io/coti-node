package unitTest;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.coti.basenode.data.TransactionData;
import io.coti.dspnode.AppConfig;
import io.coti.dspnode.services.TransactionService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;

@TestPropertySource(locations = "../test.properties")
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = AppConfig.class)
@SpringBootTest
public class TransactionServiceTest {
    @Autowired
    private TransactionService transactionService;

    @Test
    public void handleNewTransactionFromFullNode() {
        TransactionData transactionData =null;
        ObjectMapper mapper = new ObjectMapper();
        try {
           transactionData =
                    mapper.readValue(
                            "{\"@class\": \"io.coti.basenode.data.TransactionData\",\"baseTransactions\":[{\"hash\":\"4bae43a679a78c9ba1d3e064423b2fdc5b6585fef6185d600b0c9d7d0d9204fc\",\"addressHash\":\"f4ef2080c3840b79341949c86ba8caa9416d844cd52099a9626cb99d3c002378efacb63c626a242df657d2cc54ec5989870cee8701d622ea82e2b5c91989a2b55459a77f\",\"amount\":-7.043477924856466,\"createTime\":1536062172556,\"signatureData\":{\"r\":\"26b488d303d774dd178c1a5bd134af4f0a08d9481417d22662b64c08d0d7165b\",\"s\":\"8f208248a51fed58427f62c160dae97c9e1d6b64537a47f74c9840914b2e4885\"}},{\"hash\":\"5f33eca2bf8c7bdaab5fb5a0b5328bcbeaea10de2c216be7e793afca2cbc9c92\",\"addressHash\":\"2409f2a23ecdbee1463086ef8fab2e2b322c150325df66e727411c83abdb95975e34e7060db33d00502ea9cb8069d556bb1e6ccf24205e23aac680a63385eaf92245c69e\",\"amount\":-3.163184893915945,\"createTime\":1536062172557,\"signatureData\":{\"r\":\"dc1ddd9c8ede5b61c9f810dde10a54b83ff3dc1cb69cfa88d4eaf0675de37bcd\",\"s\":\"9200b4f3c8b622ca50e43384d2d4f2890c7fd1b8af9bc8e9793027f5ebcf89c8\"}},{\"hash\":\"9de6f6e2ac00d7e9b38f21b6c26f43f353151c049388d337f020ec0fa1009bae\",\"addressHash\":\"5481d9ece7b60af6e4338017f68dfc949f2d3f63838a1239e68acbb2db66fd04020e5aa3334dd4b7310a2993d5c9a1606275b5a5731b869bb1fcf5d6f763eda230b4b12c\",\"amount\":-2.96968153390545,\"createTime\":1536062172558,\"signatureData\":{\"r\":\"61a0fe0ffba041e65a6912065e3ca1a91557460904a1570545b97e1aea319cc\",\"s\":\"74b2cea9f2010e8b1cdbd8b9b5bd2239c4d6ed7338c867e45041524f03614ae3\"}},{\"hash\":\"82307107f7dddf8ad4e8a5633e61bb8752e6c0f2c53a5dc8235581d62024cfe0\",\"addressHash\":\"037c26888bdb283a126045110c7378035209e4f0aa9c9554ff3da99be2882db658c8d608c7623cb392f9710d42dda766a5ec8e40b4cd258e156a57ae65d0d7f717d7a8f9\",\"amount\":13.176344352677861,\"createTime\":1536062172558,\"signatureData\":null}],\"hash\":\"8284dee5ce20e7d37627bcadb8bdb24c6a6184a8626a55b7b6670bcaa45ab93b\",\"amount\":13.176344352677861,\"leftParentHash\":\"00000004\",\"rightParentHash\":\"00000004\",\"trustChainTransactionHashes\":[],\"userTrustScoreTokenHashes\":null,\"trustChainConsensus\":false,\"trustChainTrustScore\":38.65963,\"transactionConsensusUpdateTime\":null,\"createTime\":1536062172555,\"attachmentTime\":1536062180966,\"processStartTime\":1536062175682,\"powStartTime\":1536062175965,\"powEndTime\":1536062180966,\"senderTrustScore\":38.65963,\"senderHash\":\"04cbcc71ee1245293d01a7d36e1d904b828951507784c7de41238a81f9de11112fa5651ac968fd724805e977edd8a406cd0478f2c12f1eb180a29d4b381195f150\",\"nodeIpAddress\":null,\"nodeHash\":\"61072835d67e855f60eddc1e5b5826adf9afe3521d650719ce12e37aa5e7a45fd3ed65796e6a454f8f1173ed911eba29f134a26ff3bcd7184284512bab0ed198\",\"nodeSignature\":{\"r\":\"a59738f6f0b0247c37ddd784377b64a5d99aa118611253280a46d653750911e2\",\"s\":\"b1339a1e80810990010cdd33bc1bf78c2d1c6d1de234f50e414c6559ad374b2c\"},\"childrenTransactions\":[],\"valid\":null,\"validByNodes\":null,\"transactionDescription\":\"test\",\"dspConsensusResult\":null,\"trustScoreResults\":[{\"userHash\":\"04cbcc71ee1245293d01a7d36e1d904b828951507784c7de41238a81f9de11112fa5651ac968fd724805e977edd8a406cd0478f2c12f1eb180a29d4b381195f150\",\"transactionHash\":\"8284dee5ce20e7d37627bcadb8bdb24c6a6184a8626a55b7b6670bcaa45ab93b\",\"trustScore\":38.65963,\"trustScoreNodeHash\":\"bb8402f6b0f01d31ffb44799ae33d868572bbd349e17e2c9187e08abe64d1e65cd4b35a26f9f229629f2e6082abeadbb690d1147db1d52857dacb88dc313f389\",\"trustScoreNodeSignature\":{\"r\":\"30ff7b97118c0f21f19c8f1a960db306ac26f2d0255c99e90ff3c2bdf6070d31\",\"s\":\"7549722cd798cc3b4fd638254b19520656399ee2b1205c2a6d7ec9cf0216c34\"},\"signature\":{\"r\":\"30ff7b97118c0f21f19c8f1a960db306ac26f2d0255c99e90ff3c2bdf6070d31\",\"s\":\"7549722cd798cc3b4fd638254b19520656399ee2b1205c2a6d7ec9cf0216c34\"},\"signerHash\":\"04cbcc71ee1245293d01a7d36e1d904b828951507784c7de41238a81f9de11112fa5651ac968fd724805e977edd8a406cd0478f2c12f1eb180a29d4b381195f150\"}],\"signature\":{\"r\":\"a59738f6f0b0247c37ddd784377b64a5d99aa118611253280a46d653750911e2\",\"s\":\"b1339a1e80810990010cdd33bc1bf78c2d1c6d1de234f50e414c6559ad374b2c\"},\"signerHash\":\"04cbcc71ee1245293d01a7d36e1d904b828951507784c7de41238a81f9de11112fa5651ac968fd724805e977edd8a406cd0478f2c12f1eb180a29d4b381195f150\",\"zeroSpend\":false,\"visit\":true}",
                            TransactionData.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        //transactionService.handleNewTransactionFromFullNode(transactionData );
    }
}
