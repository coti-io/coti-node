package io.coti.fullnode.services;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Service;
import org.web3j.abi.datatypes.Address;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.RawTransaction;
import org.web3j.crypto.TransactionEncoder;
import org.web3j.crypto.WalletUtils;
import org.web3j.evm.Configuration;
import org.web3j.evm.EmbeddedWeb3jService;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.response.EthSendTransaction;
import org.web3j.utils.Numeric;

import java.math.BigInteger;

@Service
public class EvmCompatibilityService {

    private Web3j web3j;
    private Credentials credentials;

    private Log logger = LogFactory.getLog(getClass());
    public void init() {
        this.credentials =
                WalletUtils.loadBip39Credentials("Password123", "Cookies obtain true iguanas then educations spank teacups");
        logger.info("Credentials loaded");

        try {
            // Define our own address and how much ether to prefund this address with
            Configuration configuration = new Configuration(new Address(credentials.getAddress()), 10);
            logger.info("prefund 10 eth to this credentials address");

            // We use EmbeddedWeb3jService rather than the usual service implementation.
            // This will let us run an EVM and a ledger inside the running JVM..
            EmbeddedWeb3jService service = new EmbeddedWeb3jService(configuration);
            this.web3j = Web3j.build(service);

            RawTransaction rawTransaction = RawTransaction.createContractTransaction(BigInteger.ZERO, BigInteger.ZERO, BigInteger.ZERO, BigInteger.ZERO,
                    "608060405234801561001057600080fd5b50610150806100206000396000f3fe608060405234801561001057600080fd5b50600436106100365760003560e01c80632e64cec11461003b5780636057361d14610059575b600080fd5b610043610075565b60405161005091906100a1565b60405180910390f35b610073600480360381019061006e91906100ed565b61007e565b005b60008054905090565b8060008190555050565b6000819050919050565b61009b81610088565b82525050565b60006020820190506100b66000830184610092565b92915050565b600080fd5b6100ca81610088565b81146100d557600080fd5b50565b6000813590506100e7816100c1565b92915050565b600060208284031215610103576101026100bc565b5b6000610111848285016100d8565b9150509291505056fea264697066735822122005d160d7f76cf393033d59a64019e4eac4fd1bfc66036fb96874f3343f112b5364736f6c634300080f0033");
            byte[] signedMessage = TransactionEncoder.signMessage(rawTransaction, credentials);
            String hexValue = Numeric.toHexString(signedMessage);

            EthSendTransaction sendTransaction = web3j.ethSendRawTransaction(hexValue).send();
            logger.info("Transaction to send: " + sendTransaction.getJsonrpc());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
