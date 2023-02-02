package io.coti.basenode.crypto;

import io.coti.basenode.data.Hash;
import io.coti.basenode.services.BaseNodeIdentityService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ContextConfiguration(classes = {BaseNodeIdentityService.class})

@TestPropertySource(locations = "classpath:test.properties")
@SpringBootTest
@ExtendWith(SpringExtension.class)
@Slf4j
class NodeCryptoHelperTest {

    private static final String SEED = "6c233873925b0cbf352bd5cb4f5548cb4f5c5e2e33664043f6f31c883304b6ce";
    private static final Integer INDEX = 0;
    private static final Hash ADDRESS = new Hash("be3e0406a085842bd1b388fff69691ae3d7f533d8be76a3c8e166b21b0b35e9104f8aa3768fa6380105582874e371bd5e67a2262f83d48ecefabacf7a8bdf453cb45beed");

    @Autowired
    BaseNodeIdentityService nodeIdentityService;

    @Test
    void generate_address() {
        Hash address = nodeIdentityService.generateAddress(SEED, INDEX);
        Assertions.assertEquals(ADDRESS, address);
    }
}