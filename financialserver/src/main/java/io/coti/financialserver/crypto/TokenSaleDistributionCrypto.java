package io.coti.financialserver.crypto;


import io.coti.basenode.crypto.CryptoHelper;
import io.coti.basenode.crypto.SignatureValidationCrypto;
import io.coti.financialserver.data.TokenSaleDistributionData;
import io.coti.financialserver.data.TokenSaleDistributionEntryData;
import org.springframework.stereotype.Component;

import java.nio.ByteBuffer;

@Component
public class TokenSaleDistributionCrypto extends SignatureValidationCrypto<TokenSaleDistributionData> {

    @Override
    public byte[] getSignatureMessage(TokenSaleDistributionData tokenSaleDistributionData) {
        int byteBufferLength = 0;

        for (TokenSaleDistributionEntryData entry : tokenSaleDistributionData.getTokenDistributionDataEntries()) {
            byte[] entryDistributionFundNameBytes = entry.getFundName().toString().getBytes();
            byte[] entryAmountBytes = entry.getAmount().stripTrailingZeros().toPlainString().getBytes();
            byte[] entryIdentifyingDescriptionBytes = entry.getIdentifyingDescription().getBytes();
            byteBufferLength += (entryDistributionFundNameBytes.length + entryAmountBytes.length + entryIdentifyingDescriptionBytes.length);
        }
        ByteBuffer tokenSaleDistributionDataBuffer = ByteBuffer.allocate(byteBufferLength);
        for (TokenSaleDistributionEntryData entry : tokenSaleDistributionData.getTokenDistributionDataEntries()) {
            byte[] entryDistributionFundNameBytes = entry.getFundName().toString().getBytes();
            byte[] entryAmountBytes = entry.getAmount().stripTrailingZeros().toPlainString().getBytes();
            byte[] entryIdentifyingDescriptionBytes = entry.getIdentifyingDescription().getBytes();
            tokenSaleDistributionDataBuffer.put(entryDistributionFundNameBytes).put(entryAmountBytes).put(entryIdentifyingDescriptionBytes);
        }

        byte[] tokenSaleDistributionDataInBytes = tokenSaleDistributionDataBuffer.array();
        return CryptoHelper.cryptoHash(tokenSaleDistributionDataInBytes).getBytes();
    }

}
