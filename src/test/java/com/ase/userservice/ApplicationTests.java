package com.ase.userservice;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.keycloak.admin.client.resource.RealmResource;

@SpringBootTest
class ApplicationTests {

    @SuppressWarnings("removal")
    @MockBean
    private RealmResource realmResource;

    @Test
    void contextLoads() {
    }
}

