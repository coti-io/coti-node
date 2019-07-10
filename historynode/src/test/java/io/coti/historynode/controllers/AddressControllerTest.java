package io.coti.historynode.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.coti.basenode.http.GetAddressesBulkRequest;
import io.coti.historynode.crypto.AddressesRequestCrypto;
import io.coti.historynode.services.HistoryAddressService;
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
import utils.TestUtils;

import javax.servlet.ServletContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
    private AddressesRequestCrypto addressesRequestCrypto;

    @MockBean
    private HistoryAddressService historyAddressService;

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

    @Test
    public void getAddressesTest() throws Exception {

        String url = /*BASE_URL + */ "/addresses";
        GetAddressesBulkRequest getAddressesBulkRequest = TestUtils.generateGetAddressesRequest();
        //addressesRequestCrypto.signMessage(getAddressesRequest);
        String addressAsJson = mapper.writeValueAsString(getAddressesBulkRequest);
        log.info(addressAsJson);
        this.mockMvc.perform(put(url).content(addressAsJson)).andDo(print()).andExpect(status().isOk());
//                .andExpect(content().string(containsString("Hello World")));
    }



}
