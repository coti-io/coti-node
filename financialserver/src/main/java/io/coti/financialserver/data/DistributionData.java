package io.coti.financialserver.data;

import io.coti.basenode.data.Hash;
import io.coti.basenode.data.SignatureData;
import io.coti.basenode.data.interfaces.IEntity;
import io.coti.basenode.data.interfaces.ISignValidatable;
import lombok.Data;
import org.json.JSONObject;

import java.nio.ByteBuffer;
import java.time.Instant;

@Data
public class DistributionData implements ISignValidatable, IEntity {

    private Hash hash;
    private int fundId;
    private Hash receiverAddressHash;
    private boolean isOnHold;
    private int onHoldDays;
    private int amount;
    private Instant creationTime;
    private Hash kycHash;
    private SignatureData kycSignature;

    public DistributionData(JSONObject distributionJsonData) throws Exception {
        fundId = distributionJsonData.getInt("fundId");
        receiverAddressHash = new Hash(distributionJsonData.getString("receiverAddressHash"));
        onHoldDays = distributionJsonData.getInt("onHoldDays");
        amount = distributionJsonData.getInt("amount");
        isOnHold = distributionJsonData.getBoolean("onHold");
        creationTime = Instant.now();

        JSONObject kycSignatureJson = distributionJsonData.getJSONObject("kycSignature");
        kycSignature = new SignatureData(kycSignatureJson.getString("r"), kycSignatureJson.getString("s"));

        setHash();
    }

    private void setHash() {
        int byteBufferLength = receiverAddressHash.getBytes().length + creationTime.toString().getBytes().length;
        ByteBuffer receiverAddressHashCreationTimeBuffer = ByteBuffer.allocate(byteBufferLength)
                .put(receiverAddressHash.getBytes())
                .put(creationTime.toString().getBytes());
        byte[] receiverAddressHashCreationTimeInBytes = receiverAddressHashCreationTimeBuffer.array();

        hash = new Hash(receiverAddressHashCreationTimeInBytes);
    }

    @Override
    public SignatureData getSignature() {
        return kycSignature;
    }

    @Override
    public Hash getSignerHash() {
        return kycHash;
    }

    @Override
    public Hash getHash() {
        return hash;
    }

    @Override
    public void setHash(Hash hash) {
        this.hash = hash;
    }
}
