package io.coti.basenode.crypto;

import io.coti.basenode.crypto.interfaces.IBaseTransactionCrypto;
import io.coti.basenode.data.*;
import io.coti.basenode.data.interfaces.ITrustScoreNodeValidatable;
import lombok.extern.slf4j.Slf4j;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

@Slf4j
public enum BaseTransactionCrypto implements IBaseTransactionCrypto {
    INPUT_BASE_TRANSACTION_DATA(InputBaseTransactionData.class) {
        @Override
        public byte[] getMessageInBytes(BaseTransactionData inputBaseTransactionData) {
            if (!(inputBaseTransactionData instanceof InputBaseTransactionData)) {
                throw new IllegalArgumentException("");
            }
            return getBaseMessageInBytes(inputBaseTransactionData);
        }

        @Override
        public byte[] getSignatureMessage(TransactionData transactionData) {
            return transactionData.getHash().getBytes();
        }
    },
    PAYMENT_INPUT_BASE_TRANSACTION_DATA(PaymentInputBaseTransactionData.class) {
        @Override
        public byte[] getMessageInBytes(BaseTransactionData baseTransactionData) {
            if (!(baseTransactionData instanceof PaymentInputBaseTransactionData)) {
                throw new IllegalArgumentException("");
            }
            PaymentInputBaseTransactionData paymentInputBaseTransactionData = (PaymentInputBaseTransactionData) baseTransactionData;
            byte[] inputMessageInBytes = BaseTransactionCrypto.INPUT_BASE_TRANSACTION_DATA.getMessageInBytes(baseTransactionData);

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
    FULL_NODE_FEE_DATA(FullNodeFeeData.class) {
        @Override
        public byte[] getMessageInBytes(BaseTransactionData baseTransactionData) {
            if (!(baseTransactionData instanceof FullNodeFeeData)) {
                throw new IllegalArgumentException("");
            }

            try {
                return getOutputMessageInBytes((FullNodeFeeData) baseTransactionData);
            } catch (Exception e) {
                log.error(GET_MESSAGE_IN_BYTE_ERROR, e);
                return new byte[0];
            }
        }

        @Override
        public void signMessage(TransactionData transactionData, BaseTransactionData baseTransactionData, int index) {
            baseTransactionData.setSignature(NodeCryptoHelper.signMessage(this.getSignatureMessage(transactionData), index));

        }
    },
    NETWORK_FEE_DATA(NetworkFeeData.class) {
        @Override
        public byte[] getMessageInBytes(BaseTransactionData baseTransactionData) {
            if (!(baseTransactionData instanceof NetworkFeeData)) {
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
            } catch (Exception e) {
                log.error(GET_MESSAGE_IN_BYTE_ERROR, e);
                return new byte[0];
            }

        }

    },
    ROLLING_RESERVE_DATA(RollingReserveData.class) {
        @Override
        public byte[] getMessageInBytes(BaseTransactionData baseTransactionData) {
            if (!(baseTransactionData instanceof RollingReserveData)) {
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
            } catch (Exception e) {
                log.error(GET_MESSAGE_IN_BYTE_ERROR, e);
                return new byte[0];
            }
        }

    },
    RECEIVER_BASE_TRANSACTION_DATA(ReceiverBaseTransactionData.class) {
        @Override
        public byte[] getMessageInBytes(BaseTransactionData baseTransactionData) {
            if (!(baseTransactionData instanceof ReceiverBaseTransactionData)) {
                throw new IllegalArgumentException("");
            }

            try {
                ReceiverBaseTransactionData receiverBaseTransactionData = (ReceiverBaseTransactionData) baseTransactionData;
                byte[] outputMessageInBytes = getOutputMessageInBytes(receiverBaseTransactionData);
                byte[] receiverDescriptionInBytes = receiverBaseTransactionData.getReceiverDescription() != null ? receiverBaseTransactionData.getReceiverDescription().toString().getBytes() : new byte[0];
                ByteBuffer receiverBaseTransactionBuffer = ByteBuffer.allocate(outputMessageInBytes.length + receiverDescriptionInBytes.length).
                        put(outputMessageInBytes).put(receiverDescriptionInBytes);

                return receiverBaseTransactionBuffer.array();
            } catch (Exception e) {
                log.error(GET_MESSAGE_IN_BYTE_ERROR, e);
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
            } catch (Exception e) {
                log.error("Error at verifying signature", e);
                return false;
            }
        }

        @Override
        public byte[] getSignatureMessage(TransactionData transactionData) {
            ByteBuffer baseTransactionHashBuffer = ByteBuffer.allocate(3 * BASE_TRANSACTION_HASH_SIZE);
            for (BaseTransactionData baseTransactionData : transactionData.getBaseTransactions()) {
                if (baseTransactionData instanceof NetworkFeeData
                        || baseTransactionData instanceof RollingReserveData
                        || baseTransactionData instanceof ReceiverBaseTransactionData) {
                    baseTransactionHashBuffer.put(baseTransactionData.getHash().getBytes());
                }
            }
            return CryptoHelper.cryptoHash(baseTransactionHashBuffer.array()).getBytes();
        }
    };

    protected static final int BASE_TRANSACTION_HASH_SIZE = 32;
    protected static final String GET_MESSAGE_IN_BYTE_ERROR = "Error at getting message in byte";
    private final Class<? extends BaseTransactionData> baseTransactionClass;

    @SuppressWarnings("unused")
    <T extends BaseTransactionData> BaseTransactionCrypto(Class<T> baseTransactionClass) {
        this.baseTransactionClass = baseTransactionClass;
    }

    @Override
    public Class<? extends BaseTransactionData> getBaseTransactionClass() {
        return this.baseTransactionClass;
    }

    public static BaseTransactionCrypto getByBaseTransactionClass(Class<? extends BaseTransactionData> baseTransactionClass) {
        for (BaseTransactionCrypto baseTransactionCrypto : values()) {
            if (baseTransactionCrypto.baseTransactionClass.equals(baseTransactionClass)) {
                return baseTransactionCrypto;
            }
        }
        throw new IllegalArgumentException("Invalid base transaction class");
    }

    @Override
    public void createAndSetBaseTransactionHash(BaseTransactionData baseTransactionData) {
        if (!this.baseTransactionClass.isInstance(baseTransactionData)) {
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
            return this.baseTransactionClass.isInstance(baseTransactionData) && this.createBaseTransactionHashFromData(baseTransactionData).equals(baseTransactionData.getHash())
                    && CryptoHelper.isAddressValid(baseTransactionData.getAddressHash()) && verifySignature(transactionData, baseTransactionData);

        } catch (Exception e) {
            log.error("Error at checking the validity of base transaction", e);
            return false;

        }
    }

    @Override
    public void signMessage(TransactionData transactionData, BaseTransactionData baseTransactionData, int index) {
        baseTransactionData.setSignature(NodeCryptoHelper.signMessage(this.getSignatureMessage(transactionData), index));

    }

    @Override
    public <T extends BaseTransactionData & ITrustScoreNodeValidatable> void signMessage(TransactionData transactionData, T baseTransactionData, TrustScoreNodeResultData trustScoreNodeResultData) {

        List<TrustScoreNodeResultData> trustScoreNodeResult = baseTransactionData.getTrustScoreNodeResult() != null ? baseTransactionData.getTrustScoreNodeResult() : new ArrayList<>();
        trustScoreNodeResultData.setSignature(NodeCryptoHelper.signMessage(this.getSignatureMessage(transactionData, trustScoreNodeResultData)));
        trustScoreNodeResult.add(trustScoreNodeResultData);
        baseTransactionData.setTrustScoreNodeResult(trustScoreNodeResult);

    }

    @Override
    public boolean verifySignature(TransactionData transactionData, BaseTransactionData baseTransactionData) {

        try {
            if (ITrustScoreNodeValidatable.class.isAssignableFrom(this.baseTransactionClass)) {
                ITrustScoreNodeValidatable trustScoreNodeValidatable = (ITrustScoreNodeValidatable) baseTransactionData;
                for (TrustScoreNodeResultData trustScoreNodeResultData : trustScoreNodeValidatable.getTrustScoreNodeResult()) {
                    if (!CryptoHelper.verifyByPublicKey(getSignatureMessage(transactionData, trustScoreNodeResultData), trustScoreNodeResultData.getSignature().getR(), trustScoreNodeResultData.getSignature().getS(), getPublicKey(trustScoreNodeResultData))) {
                        return false;
                    }
                }
                return true;
            }
            return CryptoHelper.verifyByPublicKey(getSignatureMessage(transactionData), baseTransactionData.getSignatureData().getR(), baseTransactionData.getSignatureData().getS(), getPublicKey(baseTransactionData));
        } catch (Exception e) {
            log.error("{}: {}", e.getClass().getName(), e.getMessage());
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
    public byte[] getSignatureMessage(TransactionData transactionData) {
        for (BaseTransactionData baseTransactionData : transactionData.getBaseTransactions()) {
            if (this.baseTransactionClass.isInstance(baseTransactionData)) {
                return baseTransactionData.getHash().getBytes();
            }
        }
        return new byte[0];
    }

    @Override
    public byte[] getSignatureMessage(TransactionData transactionData, TrustScoreNodeResultData trustScoreNodeResultData) {
        for (BaseTransactionData baseTransactionData : transactionData.getBaseTransactions()) {
            if (this.baseTransactionClass.isInstance(baseTransactionData)) {
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


    protected <T extends OutputBaseTransactionData> byte[] getOutputMessageInBytes(T outputBaseTransactionData) {
        if (!OutputBaseTransactionData.class.isAssignableFrom(this.baseTransactionClass)) {
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
