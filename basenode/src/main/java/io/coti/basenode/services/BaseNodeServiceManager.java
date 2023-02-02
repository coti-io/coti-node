package io.coti.basenode.services;

import io.coti.basenode.communication.JacksonSerializer;
import io.coti.basenode.communication.interfaces.*;
import io.coti.basenode.crypto.*;
import io.coti.basenode.database.interfaces.IDatabaseConnector;
import io.coti.basenode.exceptions.CotiRunTimeException;
import io.coti.basenode.http.HttpJacksonSerializer;
import io.coti.basenode.model.*;
import io.coti.basenode.services.interfaces.*;
import io.coti.basenode.services.liveview.LiveViewService;
import io.coti.basenode.utilities.MonitorConfigurationProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class BaseNodeServiceManager implements INodeServiceManager {
    public static RequestedAddressHashes requestedAddressHashes;
    public static IDatabaseConnector databaseConnector;
    public static IReceiver zeroMQReceiver;
    public static ISender zeroMQSender;
    public static ITransactionPropagationCheckService transactionPropagationCheckService;
    public static ICurrencyService currencyService;
    public static IMintingService mintingService;
    public static INodeFeesService nodeFeesService;
    public static ITransactionHelper nodeTransactionHelper;
    public static IBalanceService balanceService;
    public static IConfirmationService confirmationService;
    public static IClusterService clusterService;
    public static IDspVoteService dspVoteService;
    public static IPotService potService;
    public static IPropagationSubscriber propagationSubscriber;
    public static IShutDownService shutDownService;
    public static RestTemplate restTemplate;
    public static GetNodeRegistrationRequestCrypto getNodeRegistrationRequestCrypto;
    public static NodeRegistrationCrypto nodeRegistrationCrypto;
    public static NetworkNodeCrypto networkNodeCrypto;
    public static NodeRegistrations nodeRegistrations;
    public static BaseNodeClusterStampService clusterStampService;
    public static ITransactionSynchronizationService transactionSynchronizationService;
    public static IAwsService awsService;
    public static IDBRecoveryService dbRecoveryService;
    public static Transactions transactions;
    public static TransactionIndexService transactionIndexService;
    public static IScraperInterface scraperService;
    public static IMonitorService monitorService;
    public static ApplicationContext applicationContext;
    public static INetworkService networkService;
    public static MonitorConfigurationProperties monitorConfigurationProperties;
    public static TrustChainConfirmationService trustChainConfirmationService;
    public static RejectedTransactions rejectedTransactions;
    public static IWebSocketMessageService webSocketMessageService;
    public static IPropagationPublisher propagationPublisher;
    public static AddressTransactionsHistories addressTransactionsHistories;
    public static TransactionCrypto transactionCrypto;
    public static TransactionIndexes transactionIndexes;
    public static ExpandedTransactionTrustScoreCrypto expandedTransactionTrustScoreCrypto;
    public static IEventService nodeEventService;
    public static ICommunicationService communicationService;
    public static NetworkCrypto networkCrypto;
    public static ISslService sslService;
    public static NetworkLastKnownNodesCrypto networkLastKnownNodesCrypto;
    public static IClusterHelper clusterHelper;
    public static ISourceSelector sourceSelector;
    public static CurrencyNameIndexes currencyNameIndexes;
    public static Currencies currencies;
    public static UserCurrencyIndexes userCurrencyIndexes;
    public static GetUserTokensRequestCrypto getUserTokensRequestCrypto;
    public static OriginatorCurrencyCrypto originatorCurrencyCrypto;
    public static CurrencyTypeRegistrationCrypto currencyTypeRegistrationCrypto;
    public static IAddressService addressService;
    public static ISerializer serializer;
    public static ISubscriberHandler subscriberHandler;
    public static IDatabaseService databaseService;
    public static LiveViewService liveViewService;
    public static Addresses addresses;
    public static FileService fileService;
    public static IValidationService validationService;
    public static JacksonSerializer jacksonSerializer;
    public static ClusterStampCrypto clusterStampCrypto;
    public static DspConsensusCrypto dspConsensusCrypto;
    public static NodeFees nodeFees;
    public static UnconfirmedReceivedTransactionHashes unconfirmedReceivedTransactionHashes;
    public static IChunkService chunkService;
    public static RejectedTransactionCrypto rejectedTransactionCrypto;
    public static TransactionSenderCrypto transactionSenderCrypto;
    public static TokenMintingCrypto tokenMintingCrypto;
    public static SimpMessagingTemplate messagingSender;
    public static INodeInformationService nodeInformationService;
    public static GetHistoryAddressesRequestCrypto getHistoryAddressesRequestCrypto;
    public static GetHistoryAddressesResponseCrypto getHistoryAddressesResponseCrypto;
    public static HttpJacksonSerializer httpJacksonSerializer;
    public static ITransactionService transactionService;
    public static TransactionDspVoteCrypto transactionDspVoteCrypto;
    public static TransactionVotes transactionVotes;
    public static INodeIdentityService nodeIdentityService;

    @Autowired
    public INodeIdentityService autowiredNodeIdentityService;
    @Autowired
    public TransactionVotes autowiredTransactionVotes;
    @Autowired
    public TransactionDspVoteCrypto autowiredTransactionDspVoteCrypto;
    @Autowired
    public RequestedAddressHashes autowiredRequestedAddressHashes;
    @Autowired
    public INetworkService autowiredNetworkService;
    @Autowired
    public ITransactionService autowiredTransactionService;
    @Autowired
    public ITransactionPropagationCheckService autowiredTransactionPropagationCheckService;
    @Autowired
    public HttpJacksonSerializer autowiredHttpJacksonSerializer;
    @Autowired
    public GetHistoryAddressesResponseCrypto autowiredGetHistoryAddressesResponseCrypto;
    @Autowired
    public GetHistoryAddressesRequestCrypto autowiredGetHistoryAddressesRequestCrypto;
    @Autowired
    public INodeInformationService autowiredNodeInformationService;
    @Autowired
    public SimpMessagingTemplate autowiredMessagingSender;
    @Autowired
    public TokenMintingCrypto autowiredTokenMintingCrypto;
    @Autowired
    public TransactionSenderCrypto autowiredTransactionSenderCrypto;
    @Autowired
    public RejectedTransactionCrypto autowiredRejectedTransactionCrypto;
    @Autowired
    public IChunkService autowiredChunkService;
    @Autowired
    public UnconfirmedReceivedTransactionHashes autowiredUnconfirmedReceivedTransactionHashes;
    @Autowired
    public NodeFees autowiredNodeFees;
    @Autowired
    public DspConsensusCrypto autowiredDspConsensusCrypto;
    @Autowired
    public ClusterStampCrypto autowiredClusterStampCrypto;
    @Autowired
    public JacksonSerializer autowiredJacksonSerializer;
    @Autowired
    public Addresses autowiredAddresses;
    @Autowired
    public IValidationService autowiredValidationService;
    @Autowired
    public FileService autowiredFileService;
    @Autowired
    public IReceiver autowiredZeroMQReceiver;
    @Autowired
    public IDatabaseConnector autowiredDatabaseConnector;
    @Autowired
    public ISender autowiredZeroMQSender;
    @Autowired
    public ICurrencyService autowiredCurrencyService;
    @Autowired
    public IMintingService autowiredMintingService;
    @Autowired
    public INodeFeesService autowiredNodeFeesService;
    @Autowired
    public ITransactionHelper autowiredNodeTransactionHelper;
    @Autowired
    public IBalanceService autowiredBalanceService;
    @Autowired
    public IConfirmationService autowiredConfirmationService;
    @Autowired
    public IClusterService autowiredClusterService;
    @Autowired
    public IAddressService autowiredAddressService;
    @Autowired
    public IDspVoteService autowiredDspVoteService;
    @Autowired
    public IPotService autowiredPotService;
    @Autowired
    public IPropagationSubscriber autowiredPropagationSubscriber;
    @Autowired
    public IShutDownService autowiredShutDownService;
    @Autowired
    public RestTemplate autowiredRestTemplate;
    @Autowired
    public GetNodeRegistrationRequestCrypto autowiredGetNodeRegistrationRequestCrypto;
    @Autowired
    public NodeRegistrationCrypto autowiredNodeRegistrationCrypto;
    @Autowired
    public NetworkNodeCrypto autowiredNetworkNodeCrypto;
    @Autowired
    public NodeRegistrations autowiredNodeRegistrations;
    @Autowired
    public BaseNodeClusterStampService autowiredClusterStampService;
    @Autowired
    public ITransactionSynchronizationService autowiredTransactionSynchronizationService;
    @Autowired
    public IAwsService autowiredAwsService;
    @Autowired
    public IDBRecoveryService autowiredDbRecoveryService;
    @Autowired
    public Transactions autowiredTransactions;
    @Autowired
    public TransactionIndexService autowiredTransactionIndexService;
    @Autowired
    public IScraperInterface autowiredScraperService;
    @Autowired
    public IMonitorService autowiredMonitorService;
    @Autowired
    public ApplicationContext autowiredApplicationContext;
    @Autowired
    public MonitorConfigurationProperties autowiredMonitorConfigurationProperties;
    @Autowired
    public TrustChainConfirmationService autowiredTrustChainConfirmationService;
    @Autowired
    public RejectedTransactions autowiredRejectedTransactions;
    @Autowired
    public IWebSocketMessageService autowiredWebSocketMessageService;
    @Autowired
    public IPropagationPublisher autowiredPropagationPublisher;
    @Autowired
    public AddressTransactionsHistories autowiredAddressTransactionsHistories;
    @Autowired
    public TransactionCrypto autowiredTransactionCrypto;
    @Autowired
    public TransactionIndexes autowiredTransactionIndexes;
    @Autowired
    public ExpandedTransactionTrustScoreCrypto autowiredExpandedTransactionTrustScoreCrypto;
    @Autowired
    public IEventService autowiredNodeEventService;
    @Autowired
    public ICommunicationService autowiredCommunicationService;
    @Autowired
    public NetworkCrypto autowiredNetworkCrypto;
    @Autowired
    public ISslService autowiredSslService;
    @Autowired
    public NetworkLastKnownNodesCrypto autowiredNetworkLastKnownNodesCrypto;
    @Autowired
    public IClusterHelper autowiredClusterHelper;
    @Autowired
    public ISourceSelector autowiredSourceSelector;
    @Autowired
    public CurrencyNameIndexes autowiredCurrencyNameIndexes;
    @Autowired
    public Currencies autowiredCurrencies;
    @Autowired
    public UserCurrencyIndexes autowiredUserCurrencyIndexes;
    @Autowired
    public GetUserTokensRequestCrypto autowiredGetUserTokensRequestCrypto;
    @Autowired
    public OriginatorCurrencyCrypto autowiredOriginatorCurrencyCrypto;
    @Autowired
    public CurrencyTypeRegistrationCrypto autowiredCurrencyTypeRegistrationCrypto;
    @Autowired
    public ISerializer autowiredSerializer;
    @Autowired
    public LiveViewService autowiredLiveViewService;
    @Autowired
    public IDatabaseService autowiredDatabaseService;
    @Autowired
    public ISubscriberHandler autowiredSubscriberHandler;

    public void init() {
        List<Field> autowiredFields = Arrays.stream(this.getClass().getFields()).filter(p -> p.getDeclaredAnnotation(Autowired.class) != null).collect(Collectors.toList());
        for (Field autowiredField : autowiredFields) {
            List<Field> staticFields = Arrays.stream(this.getClass().getFields()).filter(p -> p.getType().equals(autowiredField.getType()) &&
                    p.getDeclaredAnnotation(Autowired.class) == null).collect(Collectors.toList());
            try {
                staticFields.get(0).set(this, autowiredField.get(this)); //NOSONAR
            } catch (Exception e) {
                throw new CotiRunTimeException(e.toString().concat(" , autowired:").concat(autowiredField.getName()));
            }

        }

        log.info("{} is up", this.getClass().getSimpleName());
    }
}
