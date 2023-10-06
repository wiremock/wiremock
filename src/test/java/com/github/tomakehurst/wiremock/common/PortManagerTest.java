package com.github.tomakehurst.wiremock.common;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.ServerSocket;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.fail;

class PortManagerTest {

    @BeforeEach
    public void setUp() {
        PortManager.clearUsedPorts();
    }

    @AfterEach
    public void tearDown() {
        PortManager.clearUsedPorts();
    }

    @Test
    public void testAllocatePort() {
        int port1 = PortManager.allocatePort();
        int port2 = PortManager.allocatePort();

        assertNotEquals(port1, port2);
    }

    @Test
    public void testReleasePort() {
        int port = PortManager.allocatePort();

        PortManager.releasePort(port);

        try {
            ServerSocket serverSocket = new ServerSocket(port);
            serverSocket.close();
        } catch (Exception e) {
            fail("Port was not released successfully");
        }
    }

    @Test
    public void testClearUsedPorts() {
        int port1 = PortManager.allocatePort();
        int port2 = PortManager.allocatePort();

        assertEquals(2, PortManager.usedPorts.size());
        PortManager.clearUsedPorts();
        assertEquals(0, PortManager.usedPorts.size());
    }
}