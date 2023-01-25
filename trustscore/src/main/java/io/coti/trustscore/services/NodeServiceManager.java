package io.coti.trustscore.services;

import io.coti.basenode.crypto.GetMerchantRollingReserveAddressCrypto;
import io.coti.basenode.services.BaseNodeServiceManager;
import io.coti.trustscore.crypto.GetTransactionTrustScoreRequestCrypto;
import io.coti.trustscore.crypto.TrustScoreCrypto;
import io.coti.trustscore.crypto.TrustScoreUserTypeCrypto;
import io.coti.trustscore.model.BucketEvents;
import io.coti.trustscore.model.MerchantRollingReserveAddresses;
import io.coti.trustscore.model.TrustScores;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@Primary
@SuppressWarnings({"java:S1104", "java:S1444"})
public class NodeServiceManager extends BaseNodeServiceManager {

    public static TrustScoreService trustScoreService;
    public static NetworkFeeService feeService;
    public static RollingReserveService rollingReserveService;
    public static TrustScores trustScores;
    public static MerchantRollingReserveAddresses merchantRollingReserveAddresses;
    public static GetMerchantRollingReserveAddressCrypto getMerchantRollingReserveAddressCrypto;
    public static BucketEvents bucketEvents;
    public static TrustScoreUserTypeCrypto trustScoreUserTypeCrypto;
    public static GetTransactionTrustScoreRequestCrypto getTransactionTrustScoreRequestCrypto;
    public static TrustScoreCrypto trustScoreCrypto;
    public static BucketBehaviorEventsService bucketBehaviorEventsService;
    public static BucketInitialTrustScoreEventsService bucketInitialTrustScoreEventsService;
    public static BucketChargeBackEventsService bucketChargeBackEventsService;
    public static BucketNotFulfilmentEventsService bucketNotFulfilmentEventsService;
    public static BucketTransactionService bucketTransactionService;

    @Autowired
    public TrustScoreService autowiredTrustScoreService;
    @Autowired
    public NetworkFeeService autowiredFeeService;
    @Autowired
    public RollingReserveService autowiredRollingReserveService;
    @Autowired
    public TrustScores autowiredTrustScores;
    @Autowired
    public MerchantRollingReserveAddresses autowiredMerchantRollingReserveAddresses;
    @Autowired
    public GetMerchantRollingReserveAddressCrypto autowiredGetMerchantRollingReserveAddressCrypto;
    @Autowired
    public BucketEvents autowiredBucketEvents;
    @Autowired
    public TrustScoreUserTypeCrypto autowiredTrustScoreUserTypeCrypto;
    @Autowired
    public GetTransactionTrustScoreRequestCrypto autowiredGetTransactionTrustScoreRequestCrypto;
    @Autowired
    public TrustScoreCrypto autowiredTrustScoreCrypto;
    @Autowired
    public BucketBehaviorEventsService autowiredBucketBehaviorEventsService;
    @Autowired
    public BucketInitialTrustScoreEventsService autowiredBucketInitialTrustScoreEventsService;
    @Autowired
    public BucketChargeBackEventsService autowiredBucketChargeBackEventsService;
    @Autowired
    public BucketNotFulfilmentEventsService autowiredBucketNotFulfilmentEventsService;
    @Autowired
    public BucketTransactionService autowiredBucketTransactionService;

}
