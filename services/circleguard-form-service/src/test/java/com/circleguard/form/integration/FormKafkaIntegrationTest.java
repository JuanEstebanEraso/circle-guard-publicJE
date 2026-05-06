package com.circleguard.form.integration;

import com.circleguard.form.model.HealthSurvey;
import com.circleguard.form.repository.HealthSurveyRepository;
import com.circleguard.form.service.HealthSurveyService;
import com.circleguard.form.service.QuestionnaireService;
import com.circleguard.form.service.SymptomMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.Optional;
import java.util.UUID;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@SpringBootTest
public class FormKafkaIntegrationTest {

    @Autowired
    private HealthSurveyService healthSurveyService;

    @MockBean
    private HealthSurveyRepository repository;

    @MockBean
    private QuestionnaireService questionnaireService;

    @MockBean
    private SymptomMapper symptomMapper;

    @MockBean
    private KafkaTemplate<String, Object> kafkaTemplate;

    // INTEGRATION TEST 2: Validates Form Service sends survey.submitted to Kafka
    @Test
    void submittingSurveyShouldTriggerKafkaEvent() {
        HealthSurvey survey = new HealthSurvey();
        survey.setAnonymousId(UUID.randomUUID());
        
        Mockito.when(questionnaireService.getActiveQuestionnaire()).thenReturn(Optional.empty());
        Mockito.when(repository.save(any(HealthSurvey.class))).thenReturn(survey);

        healthSurveyService.submitSurvey(survey);

        verify(kafkaTemplate).send(
            eq("survey.submitted"),
            eq(survey.getAnonymousId().toString()),
            any(Map.class)
        );
    }
}
