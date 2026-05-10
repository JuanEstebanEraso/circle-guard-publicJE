package com.circleguard.notification.integration;

import com.circleguard.notification.service.ExposureNotificationListener;
import com.circleguard.notification.service.NotificationDispatcher;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@SpringBootTest
public class NotificationKafkaIntegrationTest {

    @Autowired
    private ExposureNotificationListener listener;

    @MockBean
    private NotificationDispatcher dispatcher;

    @MockBean
    private LmsService lmsService;

    // INTEGRATION TEST 5: Validates Notification Service processes Kafka messages and triggers Dispatcher
    @Test
    void whenKafkaMessageReceived_thenDispatcherIsCalled() {
        String anonymousId = UUID.randomUUID().toString();
        String eventJson = String.format("{\"anonymousId\":\"%s\", \"status\":\"PROBABLE\"}", anonymousId);

        // Simulate Kafka delivering the message to the listener
        listener.handleStatusChange(eventJson);

        // Verify Integration: Dispatcher should be called
        verify(dispatcher).dispatch(
            eq(anonymousId),
            eq("PROBABLE")
        );
    }
}
