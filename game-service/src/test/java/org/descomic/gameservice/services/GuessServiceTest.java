package org.descomic.gameservice.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.client.RestClient;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class GuessServiceTest {

    public static final String SIMILARITY_ENDPOINT = "similarity";
    public static final String WORD_OF_THE_DAY = "testWord";
    public static final String GUESS = "testGuess";
    public static final String SUBMIT_URI = String.format("/%s/%s/%s", SIMILARITY_ENDPOINT, WORD_OF_THE_DAY, GUESS);
    public static final String SCORE_STRING = "0.5";
    public static final Double SCORE = 0.5;

    @Mock
    private RestClient wordEngineRestClient;

    @Mock
    private GuessStorageService guessStorageService;

    @InjectMocks
    private GuessService guessService;

    @BeforeEach
    public void setup() {
        try (var _ = MockitoAnnotations.openMocks(this)) {
            var responseSpec = mock(RestClient.ResponseSpec.class);
            var requestHeadersUriSpec = mock(RestClient.RequestHeadersUriSpec.class);
            var requestHeadersSpec = mock(RestClient.RequestHeadersSpec.class);
            when(wordEngineRestClient.get()).thenReturn(requestHeadersUriSpec);
            when(requestHeadersUriSpec.uri(SUBMIT_URI)).thenReturn(requestHeadersSpec);
            when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
            when(responseSpec.body(String.class)).thenReturn(SCORE_STRING);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void givenGuessExpectRestClientSubmitToBeCalled() {
        guessService.submitGuess(WORD_OF_THE_DAY, GUESS);
        verify(wordEngineRestClient).get();
        verify(wordEngineRestClient.get()).uri(SUBMIT_URI);
    }

    @Test
    public void givenGuess_ExpectRetrievedScore() {
        var guessService = new GuessService(wordEngineRestClient);
        assertEquals(SCORE, guessService.submitGuess(WORD_OF_THE_DAY, GUESS));
    }

    @Test
    public void givenNullGuess_WhenSubmitGuess_ThenThrowException() {
        assertThrows(IllegalArgumentException.class, () -> {
            guessService.submitGuess(WORD_OF_THE_DAY, null);
        });
    }

    @Test
    public void givenEmptyGuess_WhenSubmitGuess_ThenThrowException() {
        assertThrows(IllegalArgumentException.class, () -> {
            guessService.submitGuess(null, GUESS);
        });
    }

    @Test
    public void givenRestClientException_WhenSubmitGuess_ThenThrowException() {
        when(wordEngineRestClient.get().uri(SUBMIT_URI).retrieve().body(String.class))
                .thenThrow(new RuntimeException("RestClient error"));

        assertThrows(RuntimeException.class, () -> {
            guessService.submitGuess(WORD_OF_THE_DAY, GUESS);
        });
    }

    @Test
    public void givenBadResponse_WhenSubmitGuess_ThenThrowException() {
        when(wordEngineRestClient.get().uri(SUBMIT_URI).retrieve().body(String.class)).thenReturn(null);
        assertThrows(RuntimeException.class, () -> {
            guessService.submitGuess(WORD_OF_THE_DAY, GUESS);
        });
    }
}
