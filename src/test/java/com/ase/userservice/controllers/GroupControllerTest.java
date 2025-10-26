package com.ase.userservice.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.keycloak.admin.client.resource.RealmResource;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
class GroupControllerTest {
	@SuppressWarnings("removal")
    @MockBean
    private RealmResource realmResource;

    @Autowired
    private GroupController groupController;

    @Test
    void contextLoads() {
        // Prüft, ob der Controller korrekt injiziert wurde
        assertNotNull(groupController);
    }
}

