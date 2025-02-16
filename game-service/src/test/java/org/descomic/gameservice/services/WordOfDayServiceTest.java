package org.descomic.gameservice.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.client.RestClient;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class WordOfDayServiceTest {

    private static final String WORD_OF_DAY = "giggle";
    private static final String WORD_OF_DAY_1 = "clown";
    public static final String RANDOM_ENDPOINT = "/random";

    @Mock
    private RestClient wordEngineRestClient;

    @InjectMocks
    private WordOfDayService wordOfDayService;

    @BeforeEach
    public void setup() {
        try (var _ = MockitoAnnotations.openMocks(this)) {
            var responseSpec = mock(RestClient.ResponseSpec.class);
            var requestHeadersUriSpec = mock(RestClient.RequestHeadersUriSpec.class);
            var requestHeadersSpec = mock(RestClient.RequestHeadersSpec.class);
            when(wordEngineRestClient.get()).thenReturn(requestHeadersUriSpec);
            when(requestHeadersUriSpec.uri(RANDOM_ENDPOINT)).thenReturn(requestHeadersSpec);
            when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
            when(responseSpec.body(String.class)).thenReturn(WORD_OF_DAY).thenReturn(WORD_OF_DAY_1);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void givenService_WhenGetWordOfDay_ReturnWord() {
        assertEquals(WORD_OF_DAY, wordOfDayService.getWordOfDay());
        assertEquals(WORD_OF_DAY, wordOfDayService.getWordOfDay());
    }
}