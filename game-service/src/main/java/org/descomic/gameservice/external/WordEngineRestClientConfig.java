package org.descomic.gameservice.external;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class WordEngineRestClientConfig {

    @Bean
    public RestClient wordEngineRestClient(@Value("${word-engine.url}") String wordEngineUrl) {
        return RestClient.create(wordEngineUrl);
    }
}
