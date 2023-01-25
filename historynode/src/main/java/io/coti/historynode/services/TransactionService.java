package io.coti.historynode.services;

import io.coti.basenode.crypto.CryptoHelper;
import io.coti.basenode.data.Hash;
import io.coti.basenode.data.TransactionData;
import io.coti.basenode.http.*;
import io.coti.basenode.services.BaseNodeTransactionService;
import io.coti.historynode.data.AddressTransactionsByAddress;
import io.coti.historynode.data.AddressTransactionsByDate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;

import static io.coti.basenode.http.BaseNodeHttpStringConstants.INVALID_SIGNATURE;
import static io.coti.basenode.http.BaseNodeHttpStringConstants.STATUS_ERROR;
import static io.coti.historynode.services.NodeServiceManager.*;

@Slf4j
@Service
@Primary
public class TransactionService extends BaseNodeTransactionService {

    private static final String END_POINT_RETRIEVE = "/transactions/reactive";
    @Value("${storage.server.address}")
    protected String storageServerAddress;

    @Override
    protected void continueHandlePropagatedTransaction(TransactionData transactionData) {
        log.debug("Continue to handle propagated transaction {} by history node", transactionData.getHash());
        addToHistoryTransactionIndexes(transactionData);
    }

    @Override
    public void getTransactionsByAddress(GetTransactionsByAddressRequest getTransactionsByAddressRequest, HttpServletResponse response) {
        try {
            CustomHttpServletResponse customResponse = new CustomHttpServletResponse(response);
            if (!getTransactionsByAddressRequestCrypto.verifySignature(getTransactionsByAddressRequest)) {
                customResponse.printResponse(new Response(INVALID_SIGNATURE, STATUS_ERROR), HttpStatus.UNAUTHORIZED.value());
                return;
            }
            List<Hash> transactionHashes = getTransactionHashesToRetrieve(getTransactionsByAddressRequest);
            getTransactions(transactionHashes, response);
        } catch (Exception e) {
            log.error("{}: {}", e.getClass().getName(), e.getMessage());
        }
    }

    @Override
    public void getTransactionsByDate(GetTransactionsByDateRequest getTransactionsByDateRequest, HttpServletResponse response) {

        List<Hash> transactionHashes = getTransactionHashesByDate(getTransactionsByDateRequest.getDate());
        getTransactions(transactionHashes, response);
    }

    private void getTransactions(List<Hash> transactionHashes, HttpServletResponse response) {
        try {
            CustomHttpServletResponse customResponse = new CustomHttpServletResponse(response);
            PrintWriter output = response.getWriter();
            if (transactionHashes.isEmpty()) {
                customResponse.printResponse("[]", HttpStatus.OK.value());
                return;
            }
            chunkService.startOfChunk(output);

            retrieveTransactions(transactionHashes, output);

            chunkService.endOfChunk(output);

        } catch (Exception e) {
            log.error("{}: {}", e.getClass().getName(), e.getMessage());
        }
    }

    private void retrieveTransactions(List<Hash> transactionHashes, PrintWriter output) {
        List<Hash> transactionHashesToRetrieveFromElasticSearch = new ArrayList<>();
        getTransactionsFromLocal(transactionHashes, transactionHashesToRetrieveFromElasticSearch, output);
        if (!transactionHashesToRetrieveFromElasticSearch.isEmpty()) {
            getTransactionFromElasticSearch(transactionHashesToRetrieveFromElasticSearch, output);
        }
    }

    private void getTransactionsFromLocal(List<Hash> transactionHashes, List<Hash> transactionsHashesToRetrieveFromElasticSearch, PrintWriter output) {

        transactionHashes.forEach(transactionHash -> {
            TransactionData transactionData = transactions.getByHash(transactionHash);
            if (transactionData != null) {
                ((ChunkService) chunkService).transactionHandler(transactionData, output);
            } else {
                transactionsHashesToRetrieveFromElasticSearch.add(transactionHash);
            }
        });
    }

    private void getTransactionFromElasticSearch(List<Hash> transactionsHashes, PrintWriter output) {
        RestTemplate restTemplate = new RestTemplate();
        CustomRequestCallBack requestCallBack = new CustomRequestCallBack(httpJacksonSerializer, new GetHistoryTransactionsRequest(transactionsHashes));
        ((ChunkService) chunkService).transactionHandler(responseExtractor ->
                        restTemplate.execute(storageServerAddress + END_POINT_RETRIEVE, HttpMethod.POST, requestCallBack, responseExtractor)
                , output);

    }

    public List<Hash> getTransactionHashesToRetrieve(GetTransactionsByAddressRequest getTransactionsByAddressRequest) {
        Hash addressHash = getTransactionsByAddressRequest.getAddress();
        if (addressHash == null) {
            return new ArrayList<>();
        }
        return getTransactionHashesByAddressAndDates(addressHash, getTransactionsByAddressRequest.getStartDate(), getTransactionsByAddressRequest.getEndDate());
    }


    public List<Hash> getTransactionHashesByDate(LocalDate date) {
        if (date == null) {
            return new ArrayList<>();
        }
        AddressTransactionsByDate addressTransactionsByDate = addressTransactionsByDates.getByHash(calculateHashByLocalDate(date));
        if (addressTransactionsByDate == null || addressTransactionsByDate.getTransactionHashes() == null || addressTransactionsByDate.getTransactionHashes().isEmpty()) {
            return new ArrayList<>();
        }
        return new ArrayList<>(addressTransactionsByDate.getTransactionHashes());
    }

    private List<Hash> getTransactionHashesByAddressAndDates(Hash address, LocalDate startDate, LocalDate endDate) {
        List<Hash> transactionHashes = new ArrayList<>();
        AddressTransactionsByAddress addressTransactionsByAddress = addressTransactionsByAddresses.getByHash(address);
        if (addressTransactionsByAddress == null) {
            return transactionHashes;
        }

        startDate = (startDate != null) ? startDate : addressTransactionsByAddress.getStartDate();
        endDate = (endDate != null) ? endDate : calculateInstantLocalDate(Instant.now());

        Map<LocalDate, HashSet<Hash>> transactionHashesByDates = addressTransactionsByAddress.getTransactionHashesByDates();
        while (!startDate.isAfter(endDate)) {
            HashSet<Hash> hashesOfDate = transactionHashesByDates.get(startDate);
            if (hashesOfDate != null) {
                transactionHashes.addAll(hashesOfDate);
            }
            startDate = startDate.plusDays(1);
        }
        return transactionHashes;
    }


    public void addToHistoryTransactionIndexes(TransactionData transactionData) {
        Instant attachmentTime = transactionData.getAttachmentTime();
        LocalDate attachmentLocalDate = calculateInstantLocalDate(attachmentTime);
        Hash hashByDate = calculateHashByLocalDate(attachmentLocalDate);
        HashSet<Hash> relatedAddressHashes = getRelatedAddresses(transactionData);

        AddressTransactionsByDate transactionsByDateHash = addressTransactionsByDates.getByHash(hashByDate);
        if (transactionsByDateHash == null) {
            transactionsByDateHash = new AddressTransactionsByDate(attachmentTime, new HashSet<>());
        }
        transactionsByDateHash.getTransactionHashes().add(transactionData.getHash());
        addressTransactionsByDates.put(transactionsByDateHash);

        relatedAddressHashes.forEach(transactionAddressHash -> {
            AddressTransactionsByAddress transactionsByAddress = addressTransactionsByAddresses.getByHash(transactionAddressHash);
            if (transactionsByAddress == null) {
                transactionsByAddress = new AddressTransactionsByAddress(transactionAddressHash, new HashMap<>(), attachmentLocalDate);
            }
            if (transactionsByAddress.getStartDate().isAfter(attachmentLocalDate)) {
                transactionsByAddress.setStartDate(attachmentLocalDate);
            }

            HashSet<Hash> transactionsHashesByDate = transactionsByAddress.getTransactionHashesByDates().computeIfAbsent(attachmentLocalDate, k -> new HashSet<>());
            transactionsHashesByDate.add(transactionData.getHash());
            addressTransactionsByAddresses.put(transactionsByAddress);
        });
    }

    private HashSet<Hash> getRelatedAddresses(TransactionData transactionData) {
        HashSet<Hash> hashes = new HashSet<>();
        transactionData.getBaseTransactions().forEach(baseTransactionData -> hashes.add(baseTransactionData.getAddressHash()));
        return hashes;
    }

    protected Hash calculateHashByTime(Instant date) {
        LocalDate localDate = calculateInstantLocalDate(date);
        return calculateHashByLocalDate(localDate);
    }

    public LocalDate calculateInstantLocalDate(Instant date) {
        LocalDateTime ldt = LocalDateTime.ofInstant(date, ZoneOffset.UTC);
        return LocalDate.of(ldt.getYear(), ldt.getMonth(), ldt.getDayOfMonth());
    }

    private Hash calculateHashByLocalDate(LocalDate localDate) {
        return CryptoHelper.cryptoHash(localDate.toString().getBytes());
    }

    protected ResponseEntity<AddHistoryEntitiesResponse> storeEntitiesByType(String url, AddEntitiesBulkRequest addEntitiesBulkRequest) {
        return entitiesStorageConnector.storeInStorage(url, addEntitiesBulkRequest, AddHistoryEntitiesResponse.class);
    }

}
