package org.descomic.gameservice.endpoints.game;

import org.descomic.gameservice.model.Guess;
import org.descomic.gameservice.services.GuessService;
import org.descomic.gameservice.services.GuessStorageService;
import org.descomic.gameservice.services.WordOfDayService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;

class GameControllerTest {

    public static final String WORD_OF_DAY = "giggle";
    public static final String GUESS_1 = "test";
    public static final Double SCORE_1 = 0.5;
    public static final String GUESS_2 = "maracas";
    public static final Double SCORE_2 = 0.3;

    @Mock
    private WordOfDayService wordOfDayService;

    @Mock
    private GuessService guessService;

    @Mock
    private GuessStorageService guessStorageService;

    @InjectMocks
    private GameController gameController;

    @BeforeEach
    void setUp() {
        try (var _ = MockitoAnnotations.openMocks(this)) {
            when(wordOfDayService.getWordOfDay()).thenReturn(WORD_OF_DAY);
            when(guessService.submitGuess(WORD_OF_DAY, GUESS_1)).thenReturn(SCORE_1);
            when(guessService.submitGuess(WORD_OF_DAY, GUESS_2)).thenReturn(SCORE_2);
            when(guessStorageService.getStore()).thenReturn(List.of(new Guess(GUESS_1, SCORE_1), new Guess(GUESS_2, SCORE_2)));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void givenValidRequest_whenSubmit_thenReturnsResponse() {
        Map<String, Double> response1 = gameController.submit(GUESS_1);
        assertNotNull(response1);
        assertEquals(SCORE_1, response1.get(GUESS_1));

        Map<String, Double> response2 = gameController.submit(GUESS_2);
        assertNotNull(response2);
        assertEquals(SCORE_2, response2.get(GUESS_2));
    }

    @Test
    void givenNullRequest_whenSubmit_thenReturnsNull() {
        Map<String, Double> response = gameController.submit(null);
        assertNull(response);
    }

    @Test
    void givenEmptyRequest_whenSubmit_thenReturnsNull() {
        String request = "";
        Map<String, Double> response = gameController.submit(request);
        assertNull(response);
    }
}