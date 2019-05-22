package io.coti.financialserver.data;

import io.coti.basenode.crypto.CryptoHelper;
import io.coti.basenode.data.Hash;
import io.coti.basenode.data.SignatureData;
import io.coti.basenode.data.interfaces.IEntity;
import io.coti.basenode.data.interfaces.ISignValidatable;
import io.coti.basenode.data.interfaces.ISignable;
import lombok.Data;
import org.apache.commons.lang3.ArrayUtils;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Data
public class FundDistributionData implements IEntity, ISignable, ISignValidatable {

    private Hash hash;
    private Hash hashByDate;
    private String fileName; // File name according to which this entry was created
    private DistributionEntryStatus status;

    private Hash receiverAddress;
    private Fund distributionPoolFund; // Expected range from ReservedAddress.isSecondaryFundDistribution()
    private BigDecimal amount;
    private Instant createTime;
    private Instant transactionTime;
    private String source; // "Source" secondary key

    public FundDistributionData(Hash receiverAddress, Fund distributionPoolFund, BigDecimal amount, Instant createTime,
                                Instant transactionTime, String source) {
        this.receiverAddress = receiverAddress;
        this.distributionPoolFund = distributionPoolFund;
        this.amount = amount;
        this.createTime = createTime;
        this.transactionTime = transactionTime;
        this.source = source;
        initHashes();
    }

    @Override
    public Hash getHash() {
        return hash;
    }

    @Override
    public void setHash(Hash hash) {
        this.hash = hash;
    }

    public void initHashes() {
        byte[] concatDataFields = ArrayUtils.addAll((distributionPoolFund.getText()+ source).getBytes(),receiverAddress.getBytes());
        this.hash = CryptoHelper.cryptoHash(concatDataFields);

        this.status = DistributionEntryStatus.ONHOLD;
        Instant date = (transactionTime != null) ? transactionTime : Instant.now();
        if( transactionTime != null )
        {
            date = (Instant.now().isAfter(transactionTime)) ? Instant.now() : transactionTime;
        }

        LocalDateTime ldt = LocalDateTime.ofInstant(date, ZoneId.systemDefault());
        this.hashByDate = CryptoHelper.cryptoHash( (ldt.getYear() + ldt.getMonth().toString() +
                ldt.getDayOfMonth()).getBytes() );
    }

    public boolean isReadyToInitiate() {
        return this.status == DistributionEntryStatus.ACCEPTED || this.status == DistributionEntryStatus.ONHOLD;
    }


    //TODO: ISignable, ISignValidatable due to FundDistributionFileEntryDataCrypto, check for alternative
    @Override
    public void setSignerHash(Hash signerHash) {

    }

    @Override
    public void setSignature(SignatureData signature) {

    }

    @Override
    public SignatureData getSignature() {
        return null;
    }

    @Override
    public Hash getSignerHash() {
        return null;
    }
}
