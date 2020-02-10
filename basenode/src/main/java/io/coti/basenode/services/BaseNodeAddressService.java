package io.coti.basenode.services;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.coti.basenode.data.AddressData;
import io.coti.basenode.data.Hash;
import io.coti.basenode.data.RequestedAddressHashData;
import io.coti.basenode.http.AddressFileRequest;
import io.coti.basenode.http.CustomGson;
import io.coti.basenode.http.Response;
import io.coti.basenode.http.data.AddressResponseData;
import io.coti.basenode.http.interfaces.IResponse;
import io.coti.basenode.model.Addresses;
import io.coti.basenode.services.interfaces.IAddressService;
import io.coti.basenode.services.interfaces.IValidationService;
import lombok.extern.slf4j.Slf4j;
import org.rocksdb.RocksIterator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.SerializationUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

import static io.coti.basenode.http.BaseNodeHttpStringConstants.ADDRESS_BATCH_UPLOADED;
import static io.coti.basenode.http.BaseNodeHttpStringConstants.ADDRESS_BATCH_UPLOAD_ERROR;

@Slf4j
@Service
public class BaseNodeAddressService implements IAddressService {

    protected static final int TRUSTED_RESULT_MAX_DURATION_IN_MILLIS = 600_000;
    @Autowired
    private Addresses addresses;
    @Autowired
    private IValidationService validationService;

    public void init() {
        log.info("{} is up", this.getClass().getSimpleName());
    }

    public boolean addNewAddress(AddressData addressData) {
        if (!addressExists(addressData.getHash())) {
            addresses.put(addressData);
            log.info("Address {} was successfully inserted", addressData.getHash());
            return true;
        }
        log.debug("Address {} already exists", addressData.getHash());
        return false;
    }

    @Override
    public boolean addressExists(Hash addressHash) {
        return addresses.getByHash(addressHash) != null;
    }

    public void handlePropagatedAddress(AddressData addressData) {
        try {
            if (addressExists(addressData.getHash())) {
                log.debug("Address {} already exists", addressData.getHash());
                return;
            }
            if (!validateAddress(addressData.getHash())) {
                log.error("Invalid address {}", addressData.getHash());
                return;
            }
            addNewAddress(addressData);
            continueHandleGeneratedAddress(addressData);
        } catch (Exception e) {
            log.error("Error at handlePropagatedAddress", e);
        }
    }

    protected void continueHandleGeneratedAddress(AddressData addressData) {
        // implemented by sub classes
    }

    @Override
    public boolean validateAddress(Hash addressHash) {
        return validationService.validateAddress(addressHash);
    }

    @Override
    public void getAddressBatch(HttpServletResponse response) {
        try {
            PrintWriter output = response.getWriter();
            output.write("[");
            output.flush();

            RocksIterator iterator = addresses.getIterator();
            iterator.seekToFirst();
            while (iterator.isValid()) {
                AddressData addressData = (AddressData) SerializationUtils.deserialize(iterator.value());
                addressData.setHash(new Hash(iterator.key()));
                output.write(new CustomGson().getInstance().toJson(new AddressResponseData(addressData)));
                iterator.next();
                if (iterator.isValid()) {
                    output.write(",");
                }
                output.flush();
            }
            output.write("]");
            output.flush();
        } catch (Exception e) {
            log.error("Error at get address batch: " + e);
        }
    }

    @Override
    public ResponseEntity<IResponse> uploadAddressBatch(AddressFileRequest request) {
        MultipartFile multiPartFile = request.getFile();

        String fileName = "addressBatch.txt";
        File file = new File(fileName);

        try {
            if (file.createNewFile()) {
                FileOutputStream fileOutputStream = new FileOutputStream(file);
                fileOutputStream.write(multiPartFile.getBytes());
                fileOutputStream.close();
            }
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new Response(String.format(ADDRESS_BATCH_UPLOAD_ERROR, e.getMessage())));
        }

        String line;
        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(fileName))) {
            while ((line = bufferedReader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) {
                    break;
                }
                ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());
                List<AddressResponseData> addressResponseDataList = mapper.readValue(line, new TypeReference<List<AddressResponseData>>() {
                });
                addressResponseDataList.forEach(addressResponseData -> addresses.put(new AddressData(new Hash(addressResponseData.getAddress()), addressResponseData.getCreationTime())));
            }
        } catch (Exception e) {
            log.error("Address batch upload error", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new Response(String.format(ADDRESS_BATCH_UPLOAD_ERROR, e.getMessage())));
        }
        return ResponseEntity.status(HttpStatus.OK).body(new Response(ADDRESS_BATCH_UPLOADED));
    }

    @Override
    public boolean validateRequestedAddressHashExistsAndRelevant(RequestedAddressHashData requestedAddressHashData) {
        if (requestedAddressHashData != null) {
            long diffInMilliSeconds = Math.abs(Duration.between(Instant.now(), requestedAddressHashData.getLastUpdateTime()).toMillis());
            return diffInMilliSeconds <= TRUSTED_RESULT_MAX_DURATION_IN_MILLIS;
        }
        return false;
    }
}