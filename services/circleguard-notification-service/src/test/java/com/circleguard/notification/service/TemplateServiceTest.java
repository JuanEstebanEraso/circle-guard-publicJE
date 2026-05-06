package com.circleguard.notification.service;

import freemarker.template.Configuration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

public class TemplateServiceTest {

    private TemplateService service;
    private Configuration freemarkerConfig;

    @BeforeEach
    void setUp() {
        freemarkerConfig = Mockito.mock(Configuration.class);
        service = new TemplateService(freemarkerConfig);
        ReflectionTestUtils.setField(service, "testingUrl", "http://test");
        ReflectionTestUtils.setField(service, "isolationUrl", "http://isolate");
        ReflectionTestUtils.setField(service, "guidelinesDeepLink", "app://guide");
    }

    // TEST 5
    @Test
    void shouldGenerateCorrectPushContentBasedOnStatus() {
        String suspectMsg = service.generatePushContent("SUSPECT");
        String probableMsg = service.generatePushContent("PROBABLE");
        String confirmedMsg = service.generatePushContent("CONFIRMED");

        assertTrue(suspectMsg.contains("SUSPECT"));
        assertTrue(suspectMsg.contains("isolation steps"));
        
        assertTrue(probableMsg.contains("PROBABLE"));
        assertTrue(probableMsg.contains("area exposure"));
        
        assertTrue(confirmedMsg.contains("CONFIRMED"));
        assertFalse(confirmedMsg.contains("isolation steps"));
    }
}
