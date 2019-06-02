package io.coti.financialserver.data;

import io.coti.basenode.crypto.CryptoHelper;
import io.coti.basenode.data.Hash;
import io.coti.basenode.data.interfaces.IEntity;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Data
public class FundDistributionData implements IEntity {

    private Hash hash;
    private Hash hashByDate;
    private String fileName; // File name according to which this entry was created
    private DistributionEntryStatus status;
    private long id;
    private Hash receiverAddress;
    private Fund distributionPoolFund; // Expected range from ReservedAddress.isSecondaryFundDistribution()
    private BigDecimal amount;
    private Instant createTime;
    private Instant releaseTime;
    private String source; // "Source" secondary key

    public FundDistributionData(@NotNull long id, @NotEmpty Hash receiverAddress, @NotNull Fund distributionPoolFund, @NotNull BigDecimal amount, @NotNull Instant createTime,
                                @NotNull Instant releaseTime, @NotEmpty String source) {
        this.id = id;
        this.receiverAddress = receiverAddress;
        this.distributionPoolFund = distributionPoolFund;
        this.amount = amount;
        this.createTime = createTime;
        this.releaseTime = releaseTime;
        this.source = source;
        init();
    }

    @Override
    public Hash getHash() {
        return hash;
    }

    @Override
    public void setHash(Hash hash) {
        this.hash = hash;
    }

    public void init() {
        byte[] distributionPoolInBytes = distributionPoolFund.getText().getBytes();
        byte[] sourceInBytes = source.getBytes();
        byte[] receiverAddressInBytes = receiverAddress.getBytes();
        byte[] concatDataFields = ByteBuffer.allocate(distributionPoolInBytes.length + sourceInBytes.length + receiverAddressInBytes.length).
                put(distributionPoolInBytes).put(sourceInBytes).put(receiverAddressInBytes).array();

        this.hash = CryptoHelper.cryptoHash(concatDataFields);
        this.status = DistributionEntryStatus.ONHOLD;

        LocalDateTime ldt = LocalDateTime.ofInstant(releaseTime, ZoneOffset.UTC);
        this.hashByDate = CryptoHelper.cryptoHash((ldt.getYear() + ldt.getMonth().toString() +
                ldt.getDayOfMonth()).getBytes());
    }

    public boolean isReadyToInitiate() {
        return this.status == DistributionEntryStatus.ACCEPTED || this.status == DistributionEntryStatus.ONHOLD;
    }

    public boolean isLockingAmount() {
        return this.status == DistributionEntryStatus.ONHOLD || this.status == DistributionEntryStatus.ACCEPTED ||
                this.status == DistributionEntryStatus.FAILED;
    }

}
