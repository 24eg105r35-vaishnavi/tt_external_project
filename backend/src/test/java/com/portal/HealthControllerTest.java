package com.portal;

import com.portal.controller.HealthController;
import org.junit.jupiter.api.Test;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class HealthControllerTest {

    @Test
    public void testHealthEndpointReturnsBackendConnected() {
        HealthController controller = new HealthController();
        Map<String, String> response = controller.healthCheck().getBody();
        assertEquals("UP", response.get("status"));
        assertEquals("Backend connected", response.get("message"));
    }
}
