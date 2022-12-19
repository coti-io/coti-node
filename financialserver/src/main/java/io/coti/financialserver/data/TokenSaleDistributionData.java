package io.coti.financialserver.data;

import io.coti.basenode.crypto.CryptoHelper;
import io.coti.basenode.data.Hash;
import io.coti.basenode.data.SignatureData;
import io.coti.basenode.data.interfaces.IEntity;
import io.coti.basenode.data.interfaces.ISignValidatable;
import lombok.Data;
import org.apache.commons.lang3.ArrayUtils;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.Objects;

@Data
public class TokenSaleDistributionData implements IEntity, ISignValidatable {

    private static final long serialVersionUID = 3723624752108815229L;
    @NotEmpty
    private List<@Valid TokenSaleDistributionEntryData> tokenDistributionDataEntries;
    @NotNull
    private @Valid SignatureData signatureData;
    @NotNull
    private @Valid Hash signerHash;
    @NotNull
    private Instant creationDate;
    private Hash hash;

    public void init() {
        byte[] hashConcatenatedBytes = ArrayUtils.addAll(ByteBuffer.allocate(Long.BYTES).putLong(creationDate.toEpochMilli()).array(), tokenDistributionDataEntries.toString().getBytes(StandardCharsets.UTF_8));
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
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        TokenSaleDistributionData that = (TokenSaleDistributionData) o;
        return Objects.equals(tokenDistributionDataEntries, that.tokenDistributionDataEntries) &&
                Objects.equals(signatureData, that.signatureData);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tokenDistributionDataEntries, signatureData);
    }
}
