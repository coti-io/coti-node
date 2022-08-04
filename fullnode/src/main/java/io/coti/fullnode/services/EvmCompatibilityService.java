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
import org.web3j.model.SimpleAuction;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.response.EthSendTransaction;
import org.web3j.tx.gas.ContractGasProvider;
import org.web3j.tx.gas.DefaultGasProvider;
import org.web3j.utils.Numeric;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

@Service
public class EvmCompatibilityService {

    private Web3j web3j;
    private EmbeddedWeb3jService service;
    private Credentials credentials;
    private Map<String, Object> contractsAddressToContractMap = new HashMap<>();

    private Log logger = LogFactory.getLog(getClass());
    public void init() {
        this.credentials =
                WalletUtils.loadBip39Credentials("Password123", "Cookies obtain true iguanas then educations spank teacups");
        logger.info("Credentials loaded");

        try {
            // Define our own address and how much ether to prefund this address with
            Configuration configuration = new Configuration(new Address(credentials.getAddress()), 10000000);
            logger.info("prefund 10 eth to this credentials address");

            // We use EmbeddedWeb3jService rather than the usual service implementation.
            // This will let us run an EVM and a ledger inside the running JVM..
            this.service = new EmbeddedWeb3jService(configuration);
            this.web3j = Web3j.build(service);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public String deployContract() {
        String contractAddress = "";
        try {
            logger.info("Deploying smart contract");
            ContractGasProvider contractGasProvider = new DefaultGasProvider();
            SimpleAuction contract = SimpleAuction.deploy(
                    web3j,
                    credentials,
                    contractGasProvider,
                    new BigInteger(ByteBuffer.allocate(Long.SIZE / Byte.SIZE).putLong(1000000).array()),
                    "0x6053131a7e304d5c5422cb20a3f63c319cd476ac"
            ).send();

            contractAddress = contract.getContractAddress();
            contractsAddressToContractMap.put(contractAddress, contract);
            logger.info("Smart contract deployed to address " + contractAddress);
        } catch (Exception e) {
            logger.error("EvmCompatibilityService::deployContract - encountered an error: ", e);
            throw new RuntimeException(e);
        }

        return contractAddress;
    }

    public String setBid(String contractAddress) {
        Long bid = System.currentTimeMillis()%1000000;
        try {
            SimpleAuction contract = (SimpleAuction) contractsAddressToContractMap.get(contractAddress);
            contract.bid(new BigInteger(String.valueOf(bid))).send();
            logger.info("New bid was set to be: " + bid);
        } catch (Exception e) {
            logger.error("EvmCompatibilityService::setBid - encountered an error: ", e);
            throw new RuntimeException(e);
        }
        return "New bid was set to be: " + bid;
    }

    public String getHighestBid(String contractAddress) {
        Long highestBid;
        try {
            SimpleAuction contract = (SimpleAuction) contractsAddressToContractMap.get(contractAddress);
            highestBid = contract.highestBid().send().longValue();
            logger.info("The highest bid was set to be: " + highestBid);
        } catch (Exception e) {
            logger.error("EvmCompatibilityService::getHighestBid - encountered an error: ", e);
            throw new RuntimeException(e);
        }
        return "The highest bid was set to be: " + highestBid;
    }
}
