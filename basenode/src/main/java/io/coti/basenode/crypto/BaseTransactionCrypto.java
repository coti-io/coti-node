package io.coti.basenode.crypto;

import io.coti.basenode.crypto.interfaces.IBaseTransactionCrypto;
import io.coti.basenode.data.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Date;
import java.util.EnumSet;

@Slf4j
public enum BaseTransactionCrypto implements IBaseTransactionCrypto {
    InputBaseTransactionData {
        @Override
        public byte[] getMessageInBytes(BaseTransactionData inputBaseTransactionData) {
            if (!InputBaseTransactionData.class.isInstance(inputBaseTransactionData)) {
                throw new IllegalArgumentException("");
            }
            return getBaseMessageInBytes(inputBaseTransactionData);
        }

        @Override
        public byte[] getSignatureMessage(TransactionData transactionData) {
            return transactionData.getHash().getBytes();
        }
    },
    FullNodeFeeData {
        @Override
        public byte[] getMessageInBytes(BaseTransactionData fullNodeFeeData) {
            if (!FullNodeFeeData.class.isInstance(fullNodeFeeData)) {
                throw new IllegalArgumentException("");
            }

            try {
                return getOutputMessageInBytes(fullNodeFeeData);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
                return new byte[0];
            }
        }
    },
    NetworkFeeData {
        @Override
        public byte[] getMessageInBytes(BaseTransactionData networkFeeData) {
            if (!NetworkFeeData.class.isInstance(networkFeeData)) {
                throw new IllegalArgumentException("");
            }

            try {
                return getOutputMessageInBytes(networkFeeData);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
                return new byte[0];
            }

        }

        @Override
        public boolean verifySignature(TransactionData transactionData, BaseTransactionData baseTransactionData) {
            try {
                NetworkFeeData networkFeeData = (NetworkFeeData) baseTransactionData;
                for (TrustScoreNodeResultData trustScoreNodeResultData : networkFeeData.getNetworkFeeTrustScoreNodeResult()) {
                    if (!CryptoHelper.VerifyByPublicKey(getSignatureMessage(transactionData), trustScoreNodeResultData.getTrustScoreNodeSignature().getR(), trustScoreNodeResultData.getTrustScoreNodeSignature().getS(), getPublicKey(trustScoreNodeResultData))) {
                        return false;
                    }
                }
                return true;

            } catch (ClassNotFoundException | NoSuchAlgorithmException | InvalidKeySpecException e) {
                e.printStackTrace();
                return false;
            }
        }

    },
    RollingReserveData {
        @Override
        public byte[] getMessageInBytes(BaseTransactionData rollingReserveData) {
            if (!RollingReserveData.class.isInstance(rollingReserveData)) {
                throw new IllegalArgumentException("");
            }

            try {
                return getOutputMessageInBytes(rollingReserveData);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
                return new byte[0];
            }
        }

        @Override
        public boolean verifySignature(TransactionData transactionData, BaseTransactionData baseTransactionData) {
            try {
                RollingReserveData rollingReserveData = (RollingReserveData) baseTransactionData;
                for (TrustScoreNodeResultData trustScoreNodeResultData : rollingReserveData.getRollingReserveTrustScoreNodeResult()) {
                    if (!CryptoHelper.VerifyByPublicKey(getSignatureMessage(transactionData), trustScoreNodeResultData.getTrustScoreNodeSignature().getR(), trustScoreNodeResultData.getTrustScoreNodeSignature().getS(), getPublicKey(trustScoreNodeResultData))) {
                        return false;
                    }
                }
                return true;

            } catch (ClassNotFoundException | NoSuchAlgorithmException | InvalidKeySpecException e) {
                e.printStackTrace();
                return false;
            }
        }
    },
    ReceiverBaseTransactionData {
        @Override
        public byte[] getMessageInBytes(BaseTransactionData receiverBaseTransactionData) {
            return new byte[0];
        }

        @Override
        public boolean verifySignature(TransactionData transactionData, BaseTransactionData baseTransactionData) {
            if (TransactionType.Transfer.equals(transactionData.getType())) {
                return true;
            }
            try {
                return CryptoHelper.VerifyByPublicKey(getSignatureMessage(transactionData), baseTransactionData.getSignatureData().getR(), baseTransactionData.getSignatureData().getS(), getPublicKey(baseTransactionData));
            } catch (ClassNotFoundException | NoSuchAlgorithmException | InvalidKeySpecException e) {
                e.printStackTrace();
                return false;
            }
        }

        @Override
        public byte[] getSignatureMessage(TransactionData transactionData) throws ClassNotFoundException {
            ByteBuffer baseTransactionHashBuffer = ByteBuffer.allocate(3 * baseTransactionHashSize);
            for (BaseTransactionData baseTransactionData : transactionData.getBaseTransactions()) {
                if (Class.forName(packagePath + "NetworkFeeData").isInstance(baseTransactionData)
                        || Class.forName(packagePath + "RollingReserveData").isInstance(baseTransactionData)
                        || Class.forName(packagePath + "ReceiverBaseTransactionData").isInstance(baseTransactionData)) {
                    baseTransactionHashBuffer.put(baseTransactionData.getHash().getBytes());
                }
            }
            return CryptoHelper.cryptoHash(baseTransactionHashBuffer.array()).getBytes();
        }
    };

    final static int baseTransactionHashSize = 32;
    protected NodeCryptoHelper nodeCryptoHelper;
    protected final String packagePath = "io.coti.basenode.data.";

    @Component
    public static class BaseTransactionCryptoInjector {
        @Autowired
        private NodeCryptoHelper nodeCryptoHelper;

        @PostConstruct
        public void postConstruct() {
            for (BaseTransactionCrypto baseTransactionCrypto : EnumSet.allOf(BaseTransactionCrypto.class))
                baseTransactionCrypto.nodeCryptoHelper = nodeCryptoHelper;
        }
    }

    @Override
    public void setBaseTransactionHash(BaseTransactionData baseTransactionData) {
        baseTransactionData.setHash(createBaseTransactionHashFromData(baseTransactionData));
    }

    @Override
    public Hash createBaseTransactionHashFromData(BaseTransactionData baseTransactionData) {
        byte[] bytesToHash = getMessageInBytes(baseTransactionData);
        return CryptoHelper.cryptoHash(bytesToHash);
    }

    @Override
    public boolean isBaseTransactionValid(TransactionData transactionData, BaseTransactionData baseTransactionData) {
        try {
            if (!this.createBaseTransactionHashFromData(baseTransactionData).equals(baseTransactionData.getHash()))
                return false;

            if (!CryptoHelper.IsAddressValid(baseTransactionData.getAddressHash()))
                return false;

            return verifySignature(transactionData, baseTransactionData);

        } catch (Exception e) {
            log.error("error", e);
            return false;

        }
    }

    @Override
    public void signMessage(BaseTransactionData baseTransactionData) {
        baseTransactionData.setSignature(nodeCryptoHelper.signMessage(this.getMessageInBytes(baseTransactionData)));
    }

    @Override
    public boolean verifySignature(TransactionData transactionData, BaseTransactionData baseTransactionData) {
        try {
            return CryptoHelper.VerifyByPublicKey(getSignatureMessage(transactionData), baseTransactionData.getSignatureData().getR(), baseTransactionData.getSignatureData().getS(), getPublicKey(baseTransactionData));
        } catch (ClassNotFoundException | NoSuchAlgorithmException | InvalidKeySpecException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public String getPublicKey(BaseTransactionData baseTransactionData) {
        return baseTransactionData.getAddressHash().toString().substring(0, 128); //addressWithoutCRC
    }

    @Override
    public String getPublicKey(TrustScoreNodeResultData trustScoreNodeResultData) {
        return trustScoreNodeResultData.getTrustScoreNodeHash().toString();
    }

    @Override
    public byte[] getSignatureMessage(TransactionData transactionData) throws ClassNotFoundException {
        for (BaseTransactionData baseTransactionData : transactionData.getBaseTransactions()) {
            if (Class.forName(packagePath + name()).isInstance(baseTransactionData)) {
                return baseTransactionData.getHash().getBytes();
            }
        }
        return new byte[0];
    }

    public byte[] getBaseMessageInBytes(BaseTransactionData baseTransactionData) {
        byte[] addressBytes = baseTransactionData.getAddressHash().getBytes();
        String decimalStringRepresentation = baseTransactionData.getAmount().toString();
        byte[] bytesOfAmount = decimalStringRepresentation.getBytes(StandardCharsets.UTF_8);

        Date baseTransactionDate = baseTransactionData.getCreateTime();
        int timestamp = (int) (baseTransactionDate.getTime());

        ByteBuffer dateBuffer = ByteBuffer.allocate(4);
        dateBuffer.putInt(timestamp);

        ByteBuffer baseTransactionArray = ByteBuffer.allocate(addressBytes.length + bytesOfAmount.length + dateBuffer.array().length).
                put(addressBytes).put(bytesOfAmount).put(dateBuffer.array());

        byte[] arrToReturn = baseTransactionArray.array();
        return arrToReturn;
    }

    public byte[] getOutputMessageInBytes(BaseTransactionData baseTransactionData) throws ClassNotFoundException {
        if (!OutputBaseTransactionData.class.isAssignableFrom(Class.forName(packagePath + name()))) {
            throw new IllegalArgumentException("");
        }
        OutputBaseTransactionData outputBaseTransactionData = (OutputBaseTransactionData) baseTransactionData;
        byte[] baseMessageInBytes = getBaseMessageInBytes(outputBaseTransactionData);

        String decimalOriginalAmountRepresentation = outputBaseTransactionData.getOriginalAmount().toString();
        byte[] bytesOfOriginalAmount = decimalOriginalAmountRepresentation.getBytes(StandardCharsets.UTF_8);

        ByteBuffer baseTransactionArray = ByteBuffer.allocate(baseMessageInBytes.length + bytesOfOriginalAmount.length).
                put(baseMessageInBytes).put(bytesOfOriginalAmount);

        byte[] arrToReturn = baseTransactionArray.array();
        return arrToReturn;
    }
}
