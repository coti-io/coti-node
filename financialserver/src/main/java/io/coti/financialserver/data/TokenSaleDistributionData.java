package io.coti.financialserver.data;

import io.coti.basenode.crypto.CryptoHelper;
import io.coti.basenode.data.Hash;
import io.coti.basenode.data.SignatureData;
import io.coti.basenode.data.interfaces.IEntity;
import io.coti.basenode.data.interfaces.ISignValidatable;
import io.coti.basenode.data.interfaces.ISignable;
import lombok.Data;
import org.apache.commons.lang3.ArrayUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Data
public class TokenSaleDistributionData implements IEntity, ISignable, ISignValidatable {

    List<TokenSaleDistributionEntryData> tokenDistributionDataEntries = new ArrayList<TokenSaleDistributionEntryData>();
    private SignatureData signatureData;
    private Hash signerHash;
    private Hash hash;

    public void init() {
        byte[] hashConcatenatedBytes = ArrayUtils.addAll(signerHash.getBytes(), tokenDistributionDataEntries.toString().getBytes());
        hash = CryptoHelper.cryptoHash(hashConcatenatedBytes);
    }

    @Override
    public Hash getHash() {
        return hash;
    }

    @Override
    public void setHash(Hash hash) {
        this.hash = hash;
    }

    @Override
    public SignatureData getSignature() {
        return signatureData;
    }

    @Override
    public Hash getSignerHash() {
        return signerHash;
    }

    @Override
    public void setSignerHash(Hash signerHash) {
        this.signerHash = signerHash;
    }

    @Override
    public void setSignature(SignatureData signature) {
        this.signatureData = signature;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TokenSaleDistributionData that = (TokenSaleDistributionData) o;
        return Objects.equals(tokenDistributionDataEntries, that.tokenDistributionDataEntries) &&
                Objects.equals(signatureData, that.signatureData);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tokenDistributionDataEntries, signatureData);
    }
}
