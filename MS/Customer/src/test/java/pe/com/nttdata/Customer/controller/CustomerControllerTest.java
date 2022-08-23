package pe.com.nttdata.Customer.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import pe.com.nttdata.Customer.models.Customer;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class CustomerControllerTest {

    @Autowired
    RestTemplate restTemplate;

    @Bean
    public RestTemplate getresttemplate() {
        return new RestTemplate();
    }

    @BeforeEach
    void setUp() {
    }

    @Test
    void findAll() throws Exception {
        ResponseEntity<Customer[]> response = restTemplate.getForEntity("http://localhost:7071/api/1.0.0/customers",Customer[].class);
        Customer[] customer = response.getBody();
        List<Customer> m = Arrays.asList(customer);
        assertNotNull(m.get(0).getId());
        assertTrue(m.size()>0);
    }
}