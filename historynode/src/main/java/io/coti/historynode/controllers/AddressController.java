package io.coti.historynode.controllers;

import io.coti.basenode.data.AddressData;
import io.coti.basenode.data.Hash;
import io.coti.basenode.http.GetHistoryAddressesRequest;
import io.coti.basenode.http.interfaces.IResponse;
import io.coti.basenode.model.Addresses;
import io.coti.basenode.model.Transactions;
import io.coti.historynode.database.RocksDBConnector;
import io.coti.historynode.services.AddressService;
import lombok.extern.slf4j.Slf4j;
import org.rocksdb.RocksDBException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

import static org.springframework.web.bind.annotation.RequestMethod.POST;

@Slf4j
@RestController
public class AddressController {

    @Autowired
    private AddressService addressService;

    @Autowired
    private Addresses addreses;
    @Autowired
    private Transactions transactions;
    @Autowired
    private RocksDBConnector rocksconnector;

    @RequestMapping(value = "/addresses", method = POST)
    public ResponseEntity<IResponse> getAddresses(@Valid @RequestBody GetHistoryAddressesRequest getHistoryAddressesRequest) {
        return addressService.getAddresses(getHistoryAddressesRequest);
    }
    @RequestMapping(value = "/rocksprint", method = RequestMethod.GET)
    public void rocksPrint() {
        log.info("printing addresses collection");
        addreses.forEach(addressData -> log.info("address: {}", addressData.toString()));
    }

    @RequestMapping(value = "/rockstest", method = RequestMethod.GET)
    public void rocksTest() {
        int j = 7;
        log.info("is empty? {} ",addreses.isEmpty());
        int i = 7;
    }



    @RequestMapping(value = "/rocksbackup", method = RequestMethod.GET)
    public boolean backUpRocks() {
        if(addreses.isEmpty()){
            log.info("Addresses collection is empty. adding some dummy addresses to collection");
        }

        try {
            prepareDatabase();
        } catch (RocksDBException e) {
            e.printStackTrace();
        }
        rocksconnector.backUpDb();
        return true;
    }

    @RequestMapping(value = "/rocksrestore", method = RequestMethod.GET)
    public boolean restoreRocks() {
        if(!addreses.isEmpty()){
            log.info("Addresses collection is not empty! Clearing collection");
            addreses.deleteAll();
            if(!addreses.isEmpty()){
                log.info("failed clearing db");
                return false;
            }
            log.info("Addresses collection is empty.");
        }
        rocksconnector.restoreUpDb();
        if(addreses.isEmpty()){
            log.info("Restore failed. Addresses collection is empty");
        }
        else{
            log.info("Restore success");
            addreses.forEach(addressData -> log.info(addressData.getHash().toString()));
        }
        return true;
    }

    private void prepareDatabase() throws RocksDBException {
        AddressData addressdata1 = new AddressData(new Hash("9aaf17d8b83748d4e7a10e7a8ae02039d6557bf1825220e45965b25d03b5958fbd727548bcb5ca80f8af39cb078d7d8970d3331d508510776a8874450a12cd6395d51884"));
        AddressData addressdata2 = new AddressData(new Hash("9aaf17d8b83748d4e7a10e7a8ae02039d6557bf1825220e45965b25d03b5958fbd727548bcb5ca80f8af39cb078d7d8970d3331d508510776a8874450a12cd6395d51881"));
        addreses.put(addressdata1);
        addreses.put(addressdata2);
        log.info("Added to new addresses to empty collection:");
        log.info("address 1: {}",addreses.getByHash(addressdata1.getHash()));
        log.info("address 2: {}",addreses.getByHash(addressdata2.getHash()));
    }





}
