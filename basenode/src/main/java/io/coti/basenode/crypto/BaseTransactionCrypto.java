package io.coti.basenode.crypto;

import io.coti.basenode.crypto.interfaces.IBaseTransactionCrypto;
import io.coti.basenode.data.*;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Date;

public enum BaseTransactionCrypto implements IBaseTransactionCrypto {
    InputBaseTransactionData {
        @Override
        public byte[] getMessageInBytes(BaseTransactionData inputBaseTransactionData) {
            if (!InputBaseTransactionData.class.isInstance(inputBaseTransactionData)) {
                throw new IllegalArgumentException("");
            }
            byte[] addressBytes = inputBaseTransactionData.getAddressHash().getBytes();
            String decimalStringRepresentation = inputBaseTransactionData.getAmount().toString();
            byte[] bytesOfAmount = decimalStringRepresentation.getBytes(StandardCharsets.UTF_8);

            Date baseTransactionDate = inputBaseTransactionData.getCreateTime();
            int timestamp = (int) (baseTransactionDate.getTime());

            ByteBuffer dateBuffer = ByteBuffer.allocate(4);
            dateBuffer.putInt(timestamp);

            ByteBuffer baseTransactionArray = ByteBuffer.allocate(addressBytes.length + bytesOfAmount.length + dateBuffer.array().length).
                    put(addressBytes).put(bytesOfAmount).put(dateBuffer.array());

            byte[] arrToReturn = baseTransactionArray.array();
            return arrToReturn;
        }

        @Override
        public byte[] getSignatureMessage(TransactionData transactionData) {
            return transactionData.getHash().getBytes();
        }

        @Override
        public String getPublicKey(BaseTransactionData inputBaseTransactionData) {
            return inputBaseTransactionData.getAddressHash().toString().substring(0, 128); //addressWithoutCRC
        }
    },
    FullNodeFeeData {
        @Override
        public byte[] getMessageInBytes(BaseTransactionData fullNodeFeeData) {
            if (!FullNodeFeeData.class.isInstance(fullNodeFeeData)) {
                throw new IllegalArgumentException("");
            }
            return new byte[0];
        }

        @Override
        public String getPublicKey(BaseTransactionData fullNodeFeeData) {
            return fullNodeFeeData.getFullNodeHash();
        }

    },
    NetworkFeeData {
        @Override
        public byte[] getMessageInBytes(BaseTransactionData networkFeeData) {
            return new byte[0];
        }

        @Override
        public String getPublicKey(BaseTransactionData receiverBaseTransactionData) {
            return receiverBaseTransactionData.getAddressHash().toString().substring(0, 128); //addressWithoutCRC
        }
    },
    RollingReserveData {
        @Override
        public byte[] getMessageInBytes(BaseTransactionData rollingReserveData) {
            return new byte[0];
        }

        @Override
        public String getPublicKey(BaseTransactionData receiverBaseTransactionData) {
            return receiverBaseTransactionData.getAddressHash().toString().substring(0, 128); //addressWithoutCRC
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
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
                return false;
            }
        }

        @Override
        public String getPublicKey(BaseTransactionData receiverBaseTransactionData) {
            return receiverBaseTransactionData.getAddressHash().toString().substring(0, 128); //addressWithoutCRC
        }

        @Override
        public byte[] getSignatureMessage(TransactionData transactionData) throws ClassNotFoundException {
            ByteBuffer baseTransactionHashBuffer = ByteBuffer.allocate(3 * baseTransactionHashSize);
            for (BaseTransactionData baseTransactionData : transactionData.getBaseTransactionData()) {
                if (Class.forName("NetworkFeeData").isInstance(baseTransactionData)
                        || Class.forName("RollingReserveData").isInstance(baseTransactionData)
                        || Class.forName("ReceiverBaseTransactionData").isInstance(baseTransactionData)) {
                    baseTransactionHashBuffer.put(baseTransactionData.getHash().getBytes());
                }
            }
            return CryptoHelper.cryptoHash(baseTransactionHashBuffer.array()).getBytes();
        }
    };

    final static int baseTransactionHashSize = 32;

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

        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            log.error("error", e);
            return false;

        }
    }

    @Override
    public boolean verifySignature(TransactionData transactionData, BaseTransactionData baseTransactionData) {
        try {
            return CryptoHelper.VerifyByPublicKey(getSignatureMessage(transactionData), baseTransactionData.getSignatureData().getR(), baseTransactionData.getSignatureData().getS(), getPublicKey(baseTransactionData));
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public byte[] getSignatureMessage(TransactionData transactionData) throws ClassNotFoundException {
        for (BaseTransactionData baseTransactionData : transactionData.getBaseTransactionData()) {
            if (Class.forName(name()).isInstance(baseTransactionData)) {
                return baseTransactionData.getHash().getBytes();
            }
        }
        return new byte[0];
    }
}
