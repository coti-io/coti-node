package io.coti.financialserver.services;

import io.coti.basenode.crypto.GetMerchantRollingReserveAddressCrypto;
import io.coti.basenode.services.BaseNodeServiceManager;
import io.coti.financialserver.crypto.*;
import io.coti.financialserver.model.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@Primary
@SuppressWarnings({"java:S1104", "java:S1444"})
public class NodeServiceManager extends BaseNodeServiceManager {

    public static FeeService feeService;
    public static FundDistributionService fundDistributionService;
    public static CommentService commentService;
    public static DisputeService disputeService;
    public static ItemService itemService;
    public static DistributeTokenService distributeTokenService;
    public static DocumentService documentService;
    public static RollingReserveService rollingReserveService;
    public static DisputeCommentCrypto disputeCommentCrypto;
    public static Disputes disputes;
    public static DisputeComments disputeComments;
    public static WebSocketService webSocketService;
    public static GetDisputeItemDetailCrypto getDisputeCommentsCrypto;
    public static ConsumerDisputes consumerDisputes;
    public static ArbitratorDisputes arbitratorDisputes;
    public static MerchantDisputes merchantDisputes;
    public static DisputeCrypto disputeCrypto;
    public static TransactionDisputes transactionDisputes;
    public static GetDisputesCrypto getDisputesCrypto;
    public static GetDisputeHistoryCrypto getDisputeHistoryCrypto;
    public static DisputeHistory disputeHistory;
    public static UnreadUserDisputeEvents unreadUserDisputeEvents;
    public static DisputeEvents disputeEvents;
    public static ReceiverBaseTransactionOwners receiverBaseTransactionOwners;
    public static TokenSaleDistributionCrypto tokenSaleDistributionCrypto;
    public static TokenSaleDistributions tokenSaleDistributions;
    public static TransactionCreationService transactionCreationService;
    public static InitialFunds initialFunds;
    public static DisputeDocumentCrypto disputeDocumentCrypto;
    public static DisputeDocuments disputeDocuments;
    public static GetDisputeItemDetailCrypto getDisputeDocumentNamesCrypto;
    public static GetDocumentFileCrypto getDocumentFileCrypto;
    public static GetUnreadEventsCrypto getUnreadEventsCrypto;
    public static DailyFundDistributions dailyFundDistributions;
    public static FundDistributionFileCrypto fundDistributionFileCrypto;
    public static DailyFundDistributionFiles dailyFundDistributionFiles;
    public static FailedFundDistributions failedFundDistributions;
    public static FundDistributionFileResultCrypto fundDistributionFileResultCrypto;
    public static DisputeUpdateItemCrypto disputeUpdateItemCrypto;
    public static DisputeItemVoteCrypto disputeItemVoteCrypto;
    public static MintingFeeQuoteCrypto mintingFeeQuoteCrypto;
    public static MerchantRollingReserves merchantRollingReserves;
    public static MerchantRollingReserveCrypto merchantRollingReserveCrypto;
    public static RollingReserveReleaseDates rollingReserveReleaseDates;
    public static RecourseClaims recourseClaims;
    public static GetMerchantRollingReserveAddressCrypto getMerchantRollingReserveAddressCrypto;
    public static TransactionCryptoCreator transactionCryptoCreator;
    public static ReceiverBaseTransactionOwnerCrypto receiverBaseTransactionOwnerCrypto;
    public static DisputeEventReadCrypto disputeEventReadCrypto;
    public static GetTokenMintingFeeQuoteRequestCrypto getTokenMintingFeeQuoteRequestCrypto;
    public static EventService eventService;
    public static DistributionService distributionService;

    @Autowired
    public DistributionService autowiredDistributionService;
    @Autowired
    public GetTokenMintingFeeQuoteRequestCrypto autowiredGetTokenMintingFeeQuoteRequestCrypto;
    @Autowired
    public DisputeEventReadCrypto autowiredDisputeEventReadCrypto;
    @Autowired
    public ReceiverBaseTransactionOwnerCrypto autowiredReceiverBaseTransactionOwnerCrypto;
    @Autowired
    public TransactionCryptoCreator autowiredTransactionCryptoCreator;
    @Autowired
    public GetMerchantRollingReserveAddressCrypto autowiredGetMerchantRollingReserveAddressCrypto;
    @Autowired
    public RecourseClaims autowiredRecourseClaims;
    @Autowired
    public RollingReserveReleaseDates autowiredRollingReserveReleaseDates;
    @Autowired
    public MerchantRollingReserveCrypto autowiredMerchantRollingReserveCrypto;
    @Autowired
    public MerchantRollingReserves autowiredMerchantRollingReserves;
    @Autowired
    public MintingFeeQuoteCrypto autowiredMintingFeeQuoteCrypto;
    @Autowired
    public DisputeItemVoteCrypto autowiredDisputeItemVoteCrypto;
    @Autowired
    public DisputeUpdateItemCrypto autowiredDisputeUpdateItemCrypto;
    @Autowired
    public FundDistributionFileResultCrypto autowiredFundDistributionFileResultCrypto;
    @Autowired
    public FailedFundDistributions autowiredFailedFundDistributions;
    @Autowired
    public FundDistributionFileCrypto autowiredFundDistributionFileCrypto;
    @Autowired
    public DailyFundDistributionFiles autowiredDailyFundDistributionFiles;
    @Autowired
    public DailyFundDistributions autowiredDailyFundDistributions;
    @Autowired
    public GetUnreadEventsCrypto autowiredGetUnreadEventsCrypto;
    @Autowired
    public GetDocumentFileCrypto autowiredGetDocumentFileCrypto;
    @Autowired
    public GetDisputeItemDetailCrypto autowiredGetDisputeDocumentNamesCrypto;
    @Autowired
    public DisputeDocuments autowiredDisputeDocuments;
    @Autowired
    public DisputeDocumentCrypto autowiredDisputeDocumentCrypto;
    @Autowired
    public InitialFunds autowiredInitialFunds;
    @Autowired
    public TransactionCreationService autowiredTransactionCreationService;
    @Autowired
    public TokenSaleDistributions autowiredTokenSaleDistributions;
    @Autowired
    public TokenSaleDistributionCrypto autowiredTokenSaleDistributionCrypto;
    @Autowired
    public ReceiverBaseTransactionOwners autowiredReceiverBaseTransactionOwners;
    @Autowired
    public DisputeEvents autowiredDisputeEvents;
    @Autowired
    public UnreadUserDisputeEvents autowiredUnreadUserDisputeEvents;
    @Autowired
    public DisputeHistory autowiredDisputeHistory;
    @Autowired
    public GetDisputeHistoryCrypto autowiredGetDisputeHistoryCrypto;
    @Autowired
    public GetDisputesCrypto autowiredGetDisputesCrypto;
    @Autowired
    public TransactionDisputes autowiredTransactionDisputes;
    @Autowired
    public DisputeCrypto autowiredDisputeCrypto;
    @Autowired
    public ArbitratorDisputes autowiredArbitratorDisputes;
    @Autowired
    public MerchantDisputes autowiredMerchantDisputes;
    @Autowired
    public ConsumerDisputes autowiredConsumerDisputes;
    @Autowired
    public GetDisputeItemDetailCrypto autowiredGetDisputeCommentsCrypto;
    @Autowired
    public WebSocketService autowiredWebSocketService;
    @Autowired
    public DisputeComments autowiredDisputeComments;
    @Autowired
    public Disputes autowiredDisputes;
    @Autowired
    public DisputeCommentCrypto autowiredDisputeCommentCrypto;
    @Autowired
    public RollingReserveService autowiredRollingReserveService;
    @Autowired
    public EventService autowiredEventService;
    @Autowired
    public DocumentService autowiredDocumentService;
    @Autowired
    public DistributeTokenService autowiredDistributeTokenService;
    @Autowired
    public DisputeService autowiredDisputeService;
    @Autowired
    public ItemService autowiredItemService;
    @Autowired
    public CommentService autowiredCommentService;
    @Autowired
    public FundDistributionService autowiredFundDistributionService;
    @Autowired
    public FeeService autowiredFeeService;

}
