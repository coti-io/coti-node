package io.coti.basenode.crypto;

import io.coti.basenode.crypto.interfaces.IBaseTransactionCrypto;
import io.coti.basenode.data.*;
import io.coti.basenode.data.interfaces.ITrustScoreNodeValidatable;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

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
    PaymentInputBaseTransactionData {
        @Override
        public byte[] getMessageInBytes(BaseTransactionData baseTransactionData) {
            if (!PaymentInputBaseTransactionData.class.isInstance(baseTransactionData)) {
                throw new IllegalArgumentException("");
            }
            PaymentInputBaseTransactionData paymentInputBaseTransactionData = (PaymentInputBaseTransactionData) baseTransactionData;
            byte[] inputMessageInBytes = this.InputBaseTransactionData.getMessageInBytes(baseTransactionData);

            int itemsByteSize = 0;
            List<PaymentItemData> items = ((PaymentInputBaseTransactionData) baseTransactionData).getItems();
            for (PaymentItemData paymentItemData : items) {
                itemsByteSize += Long.BYTES + paymentItemData.getItemPrice().stripTrailingZeros().toPlainString().getBytes(StandardCharsets.UTF_8).length
                        + paymentItemData.getItemName().getBytes(StandardCharsets.UTF_8).length + Integer.BYTES;
            }
            ByteBuffer itemsBuffer = ByteBuffer.allocate(itemsByteSize);
            items.forEach(paymentItemData -> itemsBuffer.putLong(paymentItemData.getItemId()).put(paymentItemData.getItemPrice().stripTrailingZeros().toPlainString().getBytes(StandardCharsets.UTF_8))
                    .put(paymentItemData.getItemName().getBytes(StandardCharsets.UTF_8)).putInt(paymentItemData.getItemQuantity()));
            byte[] itemsInBytes = itemsBuffer.array();

            byte[] merchantNameInBytes = paymentInputBaseTransactionData.getEncryptedMerchantName().getBytes(StandardCharsets.UTF_8);

            ByteBuffer baseTransactionBuffer = ByteBuffer.allocate(inputMessageInBytes.length + itemsByteSize + merchantNameInBytes.length).
                    put(inputMessageInBytes).put(itemsInBytes).put(merchantNameInBytes);

            return baseTransactionBuffer.array();
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
                return getOutputMessageInBytes((FullNodeFeeData) fullNodeFeeData);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
                return new byte[0];
            }
        }

        @Override
        public void signMessage(TransactionData transactionData, BaseTransactionData baseTransactionData, int index) throws ClassNotFoundException {
            baseTransactionData.setSignature(nodeCryptoHelper.signMessage(this.getSignatureMessage(transactionData), index));

        }
    },
    NetworkFeeData {
        @Override
        public byte[] getMessageInBytes(BaseTransactionData baseTransactionData) {
            if (!NetworkFeeData.class.isInstance(baseTransactionData)) {
                throw new IllegalArgumentException("");
            }

            try {
                NetworkFeeData networkFeeData = (NetworkFeeData) baseTransactionData;
                byte[] outputMessageInBytes = getOutputMessageInBytes(networkFeeData);
                byte[] bytesOfReducedAmount = networkFeeData.getReducedAmount() == null ? new byte[0]
                        : networkFeeData.getReducedAmount().stripTrailingZeros().toPlainString().getBytes(StandardCharsets.UTF_8);

                ByteBuffer baseTransactionBuffer = ByteBuffer.allocate(outputMessageInBytes.length + bytesOfReducedAmount.length).
                        put(outputMessageInBytes).put(bytesOfReducedAmount);
                return baseTransactionBuffer.array();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
                return new byte[0];
            }

        }

    },
    RollingReserveData {
        @Override
        public byte[] getMessageInBytes(BaseTransactionData baseTransactionData) {
            if (!RollingReserveData.class.isInstance(baseTransactionData)) {
                throw new IllegalArgumentException("");
            }

            try {
                RollingReserveData rollingReserveData = (RollingReserveData) baseTransactionData;
                byte[] outputMessageInBytes = getOutputMessageInBytes(rollingReserveData);
                String decimalReducedAmountRepresentation = rollingReserveData.getReducedAmount().stripTrailingZeros().toPlainString();
                byte[] bytesOfReducedAmount = decimalReducedAmountRepresentation.getBytes(StandardCharsets.UTF_8);

                ByteBuffer baseTransactionBuffer = ByteBuffer.allocate(outputMessageInBytes.length + bytesOfReducedAmount.length).
                        put(outputMessageInBytes).put(bytesOfReducedAmount);
                return baseTransactionBuffer.array();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
                return new byte[0];
            }
        }

    },
    ReceiverBaseTransactionData {
        @Override
        public byte[] getMessageInBytes(BaseTransactionData baseTransactionData) {
            if (!ReceiverBaseTransactionData.class.isInstance(baseTransactionData)) {
                throw new IllegalArgumentException("");
            }

            try {
                ReceiverBaseTransactionData receiverBaseTransactionData = (ReceiverBaseTransactionData) baseTransactionData;
                byte[] outputMessageInBytes = getOutputMessageInBytes(receiverBaseTransactionData);
                byte[] receiverDescriptionInBytes = receiverBaseTransactionData.getReceiverDescription() != null ? receiverBaseTransactionData.getReceiverDescription().toString().getBytes() : new byte[0];
                ByteBuffer receiverBaseTransactionBuffer = ByteBuffer.allocate(outputMessageInBytes.length + receiverDescriptionInBytes.length).
                        put(outputMessageInBytes).put(receiverDescriptionInBytes);

                return receiverBaseTransactionBuffer.array();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
                return new byte[0];
            }
        }

        @Override
        public boolean verifySignature(TransactionData transactionData, BaseTransactionData baseTransactionData) {
            if (EnumSet.of(TransactionType.Transfer, TransactionType.Initial).contains(transactionData.getType())) {
                return true;
            }
            try {
                return CryptoHelper.verifyByPublicKey(getSignatureMessage(transactionData), baseTransactionData.getSignatureData().getR(), baseTransactionData.getSignatureData().getS(), getPublicKey(baseTransactionData));
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
    public void setBaseTransactionHash(BaseTransactionData baseTransactionData) throws ClassNotFoundException {
        if (!Class.forName(packagePath + name()).isInstance(baseTransactionData)) {
            throw new IllegalArgumentException("");
        }
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
            return Class.forName(packagePath + name()).isInstance(baseTransactionData) && this.createBaseTransactionHashFromData(baseTransactionData).equals(baseTransactionData.getHash())
                    && CryptoHelper.isAddressValid(baseTransactionData.getAddressHash()) && verifySignature(transactionData, baseTransactionData);

        } catch (ClassNotFoundException | InvalidKeySpecException | NoSuchAlgorithmException e) {
            e.printStackTrace();
            return false;

        }
    }

    @Override
    public void signMessage(TransactionData transactionData, BaseTransactionData baseTransactionData, int index) throws ClassNotFoundException {
        baseTransactionData.setSignature(nodeCryptoHelper.signMessage(this.getSignatureMessage(transactionData), index));

    }

    @Override
    public <T extends BaseTransactionData & ITrustScoreNodeValidatable> void signMessage(TransactionData transactionData, T baseTransactionData, TrustScoreNodeResultData trustScoreNodeResultData) throws ClassNotFoundException {

        List<TrustScoreNodeResultData> trustScoreNodeResult = baseTransactionData.getTrustScoreNodeResult() != null ? baseTransactionData.getTrustScoreNodeResult() : new ArrayList<>();
        trustScoreNodeResultData.setSignature(nodeCryptoHelper.signMessage(this.getSignatureMessage(transactionData, trustScoreNodeResultData)));
        trustScoreNodeResult.add(trustScoreNodeResultData);
        baseTransactionData.setTrustScoreNodeResult(trustScoreNodeResult);

    }

    @Override
    public boolean verifySignature(TransactionData transactionData, BaseTransactionData baseTransactionData) throws ClassNotFoundException, InvalidKeySpecException, NoSuchAlgorithmException {

        if (ITrustScoreNodeValidatable.class.isAssignableFrom(Class.forName(packagePath + name()))) {
            ITrustScoreNodeValidatable trustScoreNodeValidatable = (ITrustScoreNodeValidatable) baseTransactionData;
            for (TrustScoreNodeResultData trustScoreNodeResultData : trustScoreNodeValidatable.getTrustScoreNodeResult()) {
                if (!CryptoHelper.verifyByPublicKey(getSignatureMessage(transactionData, trustScoreNodeResultData), trustScoreNodeResultData.getSignature().getR(), trustScoreNodeResultData.getSignature().getS(), getPublicKey(trustScoreNodeResultData))) {
                    return false;
                }
            }
            return true;
        }
        return CryptoHelper.verifyByPublicKey(getSignatureMessage(transactionData), baseTransactionData.getSignatureData().getR(), baseTransactionData.getSignatureData().getS(), getPublicKey(baseTransactionData));

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

    @Override
    public byte[] getSignatureMessage(TransactionData transactionData, TrustScoreNodeResultData trustScoreNodeResultData) throws ClassNotFoundException {
        for (BaseTransactionData baseTransactionData : transactionData.getBaseTransactions()) {
            if (Class.forName(packagePath + name()).isInstance(baseTransactionData)) {
                byte[] baseTransactionHashInBytes = baseTransactionData.getHash().getBytes();

                ByteBuffer validBaseTransactionBuffer = ByteBuffer.allocate(1);
                validBaseTransactionBuffer.put(trustScoreNodeResultData.isValid() ? (byte) 1 : (byte) 0);

                ByteBuffer signatureMessageBuffer = ByteBuffer.allocate(baseTransactionHashInBytes.length + 1).
                        put(baseTransactionHashInBytes).put(validBaseTransactionBuffer.array());

                byte[] signatureMessageInBytes = signatureMessageBuffer.array();
                return CryptoHelper.cryptoHash(signatureMessageInBytes).getBytes();
            }
        }
        return new byte[0];
    }


    protected byte[] getBaseMessageInBytes(BaseTransactionData baseTransactionData) {
        byte[] addressBytes = baseTransactionData.getAddressHash().getBytes();
        String decimalStringRepresentation = baseTransactionData.getAmount().stripTrailingZeros().toPlainString();
        byte[] bytesOfAmount = decimalStringRepresentation.getBytes(StandardCharsets.UTF_8);

        Instant createTime = baseTransactionData.getCreateTime();
        byte[] createTimeInBytes = ByteBuffer.allocate(Long.BYTES).putLong(createTime.toEpochMilli()).array();

        ByteBuffer baseTransactionBuffer = ByteBuffer.allocate(addressBytes.length + bytesOfAmount.length + createTimeInBytes.length).
                put(addressBytes).put(bytesOfAmount).put(createTimeInBytes);

        return baseTransactionBuffer.array();
    }


    protected <T extends OutputBaseTransactionData> byte[] getOutputMessageInBytes(T outputBaseTransactionData) throws ClassNotFoundException {
        if (!OutputBaseTransactionData.class.isAssignableFrom(Class.forName(packagePath + name()))) {
            throw new IllegalArgumentException("");
        }
        byte[] baseMessageInBytes = getBaseMessageInBytes(outputBaseTransactionData);

        String decimalOriginalAmountRepresentation = outputBaseTransactionData.getOriginalAmount().stripTrailingZeros().toPlainString();
        byte[] bytesOfOriginalAmount = decimalOriginalAmountRepresentation.getBytes(StandardCharsets.UTF_8);

        ByteBuffer outputBaseTransactionBuffer = ByteBuffer.allocate(baseMessageInBytes.length + bytesOfOriginalAmount.length).
                put(baseMessageInBytes).put(bytesOfOriginalAmount);

        return outputBaseTransactionBuffer.array();
    }
}
