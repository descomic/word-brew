package org.descomic.gameservice.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class WordOfDayService {

    private static final Logger logger = LoggerFactory.getLogger(WordOfDayService.class);

    private final RestClient wordEngineRestClient;
    private String wordOfTheDay = null;

    @Autowired
    public WordOfDayService(RestClient wordEngineRestClient) {
        this.wordEngineRestClient = wordEngineRestClient;
    }

    public String getWordOfDay() {
        if (wordOfTheDay == null) {
            wordOfTheDay = getRandomWord();
            logger.info("Word of the day: {}", wordOfTheDay);
        }
        return wordOfTheDay;
    }

    private String getRandomWord() {
        if (wordEngineRestClient == null) {
            logger.error("Word engine rest client is null");
            throw new IllegalStateException("Word engine rest client is null");
        }
        RestClient.ResponseSpec retrieve = wordEngineRestClient.get().uri("/random").retrieve();

        var entity = wordEngineRestClient.get().uri("/random").retrieve().toEntity(String.class);
        if (entity.getStatusCode().isError()) {
            logger.error("Failed to retrieve word of the day: {}", entity.getStatusCode());
            throw new IllegalStateException("Failed to retrieve word of the day");
        }

        logger.info("Word of the day response: {}", retrieve);
        return entity.getBody();
    }
}
