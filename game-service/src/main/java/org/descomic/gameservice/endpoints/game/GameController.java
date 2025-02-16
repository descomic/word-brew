package org.descomic.gameservice.endpoints.game;

import org.descomic.gameservice.model.Guess;
import org.descomic.gameservice.services.GuessService;
import org.descomic.gameservice.services.GuessStorageService;
import org.descomic.gameservice.services.WordOfDayService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

import java.util.HashMap;
import java.util.Map;

@Controller
public class GameController {

    private static final Logger logger = LoggerFactory.getLogger(GameController.class);

    private final WordOfDayService wordOfDayService;
    private final GuessService guessService;
    private final GuessStorageService guessStorageService;

    @Autowired
    public GameController(WordOfDayService wordOfDayService, GuessService guessService, GuessStorageService guessStorageService) {
        this.wordOfDayService = wordOfDayService;
        this.guessService = guessService;
        this.guessStorageService = guessStorageService;
    }

    @MessageMapping("/submit")
    @SendTo("/topic/guesses")
    public Map<String, Double> submit(String guess) {
        logger.info("guess received: {}", guess);
        if (guess == null || guess.isEmpty()) {
            return null;
        }

        var wordOfTheDay = wordOfDayService.getWordOfDay();
        var score = guessService.submitGuess(wordOfTheDay, guess);
        guessStorageService.storeGuess(new Guess(guess, score));

        HashMap<String, Double> store = guessStorageService.getStore().stream().collect(HashMap::new, (map, g) -> map.put(g.guess(), g.score()), HashMap::putAll);
        logger.info("Sending to /topic/guesses: {}", store);
        return store;
    }
}
