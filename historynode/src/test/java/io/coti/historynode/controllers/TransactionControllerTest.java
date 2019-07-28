package io.coti.historynode.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import io.coti.historynode.services.TransactionService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@ContextConfiguration(classes = {TransactionController.class})
@TestPropertySource(locations = "classpath:test.properties")
@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@WebAppConfiguration
public class TransactionControllerTest {

    private MockMvc mockMvc;

    @Value("${storage.server.address}")
    protected String storageServerAddress;

    @MockBean
    private TransactionService transactionService;

    @Autowired
    private WebApplicationContext wac;

    private ObjectMapper mapper;

    @Before
    public void setup() throws Exception {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
        mapper = new ObjectMapper()
                .registerModule(new ParameterNamesModule())
                .registerModule(new Jdk8Module())
                .registerModule(new JavaTimeModule()); // new module, NOT JSR310Module
    }

    //TODO 7/7/2019 astolia: implement
    @Test
    public void getTransactionsByDateTest() throws Exception {
//        this.mockMvc.perform(post("/transactionsByDate")).andDo(print()).andExpect(status().isOk())
//                .andExpect(content().string(containsString("Hello World")));
    }

    //TODO 7/7/2019 astolia: implement
    @Test
    public void getTransactions() throws Exception {
//        this.mockMvc.perform(get("/transactions")).andDo(print()).andExpect(status().isOk())
//                .andExpect(content().string(containsString("Hello World")));
    }


}
