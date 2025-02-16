package org.descomic.gameservice.services;

import org.descomic.gameservice.model.Guess;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class GuessStorageServiceTest {

    @Test
    public void givenGuess_ExpectGuessToBeStored() {
        GuessStorageService guessStorageService = new GuessStorageService();
        Guess guess = new Guess("testWord", 0.5);

        guessStorageService.storeGuess(guess);

        assertThat(guessStorageService.getStore()).contains(guess);
    }
}