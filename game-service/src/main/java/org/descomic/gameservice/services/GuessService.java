package org.descomic.gameservice.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class GuessService {

    private static final Logger logger = LoggerFactory.getLogger(GuessService.class);

    private final RestClient wordEngineRestClient;

    @Autowired
    public GuessService(RestClient wordEngineRestClient) {
        this.wordEngineRestClient = wordEngineRestClient;
    }

    public Double submitGuess(String wordOfTheDay, String guess) {
        if (wordOfTheDay == null || guess == null) {
            String message = String.format("Bad arguments for submitGuess(%s, %s)", wordOfTheDay, guess);
            logger.error(message);
            throw new IllegalArgumentException(message);
        }

        var response = wordEngineRestClient.get()
                .uri("/similarity/" + wordOfTheDay + "/" + guess)
                .retrieve()
                .body(String.class);

        if (response == null) {
            logger.error("Response from word engine is null");
            throw new RuntimeException("Response from word engine is null");
        }
        return Double.parseDouble(response);
    }
}
