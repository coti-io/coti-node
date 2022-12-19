package io.coti.financialserver.http.data;

import io.coti.basenode.crypto.CryptoHelper;
import io.coti.basenode.data.Hash;
import io.coti.basenode.data.SignatureData;
import io.coti.basenode.data.interfaces.IEntity;
import io.coti.basenode.data.interfaces.ISignValidatable;
import io.coti.basenode.data.interfaces.ISignable;
import lombok.Data;
import org.apache.commons.lang3.ArrayUtils;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Data
public class FundDistributionFileData implements IEntity, ISignable, ISignValidatable {

    private Hash hash;
    @NotNull
    private Hash userHash;
    private @Valid SignatureData userSignature;
    private Instant creationTime;
    private String fileName;
    private List<byte[]> signatureMessage = new ArrayList<>();
    private int messageByteSize = 0;

    public FundDistributionFileData(String fileName, Hash userHash) {
        this.fileName = fileName;
        this.userHash = userHash;
        init();
    }

    public void init() {
        this.creationTime = Instant.now();
        byte[] concatDateAndUserHashBytes = ArrayUtils.addAll(fileName.getBytes(), creationTime.toString().getBytes(StandardCharsets.UTF_8));
        this.hash = CryptoHelper.cryptoHash(concatDateAndUserHashBytes);
    }

    @Override
    public SignatureData getSignature() {
        return userSignature;
    }

    @Override
    public void setSignature(SignatureData signature) {
        this.userSignature = signature;
    }

    @Override
    public Hash getSignerHash() {
        return userHash;
    }

    @Override
    public void setSignerHash(Hash signerHash) {
        this.userHash = signerHash;
    }

    public void incrementMessageByteSize(long addedMessageByteSize) {
        messageByteSize += addedMessageByteSize;
    }
}
