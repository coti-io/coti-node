package io.coti.historynode.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.coti.basenode.crypto.GetHistoryAddressesRequestCrypto;
import io.coti.historynode.services.AddressService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockServletContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import javax.servlet.ServletContext;

@Slf4j
@ContextConfiguration(classes = {AddressController.class})
@TestPropertySource(locations = "classpath:test.properties")
@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureMockMvc
@WebAppConfiguration
class AddressControllerTest {

    private MockMvc mockMvc;

    @MockBean
    private GetHistoryAddressesRequestCrypto getHistoryAddressesRequestCrypto;

    @MockBean
    private AddressService addressService;

    @Autowired
    private WebApplicationContext appContext;

    private ObjectMapper mapper;

    @BeforeEach
    void setup() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.appContext).build();
        mapper = new ObjectMapper();
    }

    @Test
    void givenWac_whenServletContext_thenItProvidesAddressController() {
        ServletContext servletContext = appContext.getServletContext();
        Assertions.assertNotNull(servletContext);
        Assertions.assertTrue(servletContext instanceof MockServletContext);
        Assertions.assertNotNull(appContext.getBean("addressController"));
    }

}
