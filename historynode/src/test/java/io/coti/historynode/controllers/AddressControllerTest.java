package io.coti.historynode.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.coti.basenode.crypto.GetHistoryAddressesRequestCrypto;
import io.coti.historynode.services.AddressService;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockServletContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import javax.servlet.ServletContext;

@Slf4j
@ContextConfiguration(classes = {AddressController.class})
@TestPropertySource(locations = "classpath:test.properties")
@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@WebAppConfiguration
public class AddressControllerTest {

    private MockMvc mockMvc;

    @MockBean
    private GetHistoryAddressesRequestCrypto getHistoryAddressesRequestCrypto;

    @MockBean
    private AddressService addressService;

    @Autowired
    private WebApplicationContext appContext;

    private ObjectMapper mapper;

    @Before
    public void setup() throws Exception {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.appContext).build();
        mapper = new ObjectMapper();
    }

    @Test
    public void givenWac_whenServletContext_thenItProvidesAddressController() {
        ServletContext servletContext = appContext.getServletContext();
        Assert.assertNotNull(servletContext);
        Assert.assertTrue(servletContext instanceof MockServletContext);
        Assert.assertNotNull(appContext.getBean("addressController"));
    }

}
