package io.coti.financialserver.crypto;


import io.coti.basenode.crypto.CryptoHelper;
import io.coti.basenode.crypto.SignatureCrypto;
import io.coti.financialserver.data.TokenSaleDistributionData;
import io.coti.financialserver.data.TokenSaleDistributionEntryData;
import org.springframework.stereotype.Service;

import java.nio.ByteBuffer;

@Service
public class TokenSaleDistributionCrypto extends SignatureCrypto<TokenSaleDistributionData> {

    @Override
    public byte[] getSignatureMessage(TokenSaleDistributionData tokenSaleDistributionData) {
        // Need to iterate on every entry and extract from each entry the main fields' values that were sent
        int byteBufferLength = 0;

        for( TokenSaleDistributionEntryData entry : tokenSaleDistributionData.getTokenDistributionDataEntries() ) {
            byte[] entryDistributionFundNameBytes = entry.getFundName().getBytes();
            byte[] entryAmountBytes = entry.getAmount().toString().getBytes();
            byte[] entryIdentifyingDescriptionBytes = entry.getIdentifyingDescription().getBytes();
            byteBufferLength += (entryDistributionFundNameBytes.length + entryAmountBytes.length + entryIdentifyingDescriptionBytes.length);
        }
        ByteBuffer tokenSaleDistributionDataBuffer = ByteBuffer.allocate(byteBufferLength);
        for( TokenSaleDistributionEntryData entry : tokenSaleDistributionData.getTokenDistributionDataEntries() ) {
            byte[] entryDistributionFundNameBytes = entry.getFundName().getBytes();
            byte[] entryAmountBytes = entry.getAmount().toString().getBytes();
            byte[] entryIdentifyingDescriptionBytes = entry.getIdentifyingDescription().getBytes();
            tokenSaleDistributionDataBuffer.put(entryDistributionFundNameBytes).put(entryAmountBytes).put(entryIdentifyingDescriptionBytes);
        }

        byte[] tokenSaleDistributionDataInBytes = tokenSaleDistributionDataBuffer.array();
        return CryptoHelper.cryptoHash(tokenSaleDistributionDataInBytes).getBytes();
    }

}
