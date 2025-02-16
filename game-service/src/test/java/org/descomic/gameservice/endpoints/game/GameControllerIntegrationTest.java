package org.descomic.gameservice.endpoints.game;

import org.awaitility.Awaitility;
import org.descomic.gameservice.WebSocketConfiguration;
import org.descomic.gameservice.services.GuessStorageService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.lang.NonNull;
import org.springframework.messaging.converter.CompositeMessageConverter;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.converter.StringMessageConverter;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import org.springframework.web.socket.sockjs.client.WebSocketTransport;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = WebSocketConfiguration.class)
public class GameControllerIntegrationTest {

    public static final String TOPIC_GUESSES_ENDPOINT = "/topic/guesses";
    public static final String APP_SUBMIT_ENDPOINT = "/app/submit";
    public static final String TEST_GUESS = "test";
    public static final String OTHER_GUESS = "other";
    public static final String GIGGLE_GUESS = "giggle";
    public static final String POTATO_GUESS = "potato";

    @Autowired
    private GuessStorageService guessStorageService;

    @LocalServerPort
    private Integer port;

    private String host;

    private WebSocketStompClient webSocketStompClient;

    @BeforeEach
    void setup() {
        this.webSocketStompClient = new WebSocketStompClient(new SockJsClient(List.of(new WebSocketTransport(new StandardWebSocketClient()))));
        webSocketStompClient.setMessageConverter(new CompositeMessageConverter(List.of(new StringMessageConverter(), new MappingJackson2MessageConverter())));

        this.host = String.format("http://localhost:%d/ws-endpoint", port);
    }

    @AfterEach
    void tearDown() {
        this.webSocketStompClient.stop();
        try {
            Field storeField = GuessStorageService.class.getDeclaredField("store");
            storeField.setAccessible(true);
            ((Set<?>) storeField.get(guessStorageService)).clear();
            storeField.setAccessible(false);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void givenValidHost_ExpectConnection() throws ExecutionException, InterruptedException, TimeoutException {
        StompSession session = webSocketStompClient.connectAsync(host, new StompSessionHandlerStub()).get(1, TimeUnit.SECONDS);
        assertThat(session.isConnected()).isTrue();
    }

    @Test
    public void givenAClient_WhenSubmitGuess_ThenTopicIsUpdated() throws ExecutionException, InterruptedException, TimeoutException {
        StompSession session = webSocketStompClient.connectAsync(host, new StompSessionHandlerStub()).get(1, TimeUnit.SECONDS);

        final var frameHandlerStub = new StompFrameHandlerStub();
        session.subscribe(TOPIC_GUESSES_ENDPOINT, frameHandlerStub);

        session.send(APP_SUBMIT_ENDPOINT, TEST_GUESS);

        Awaitility.waitAtMost(1, TimeUnit.SECONDS).untilAsserted(() -> {
            Map<String, Double> gameResponse = frameHandlerStub.pollElement();
            assertThat(gameResponse).isNotNull();
            assertThat(gameResponse).hasSize(1);
            assertThat(gameResponse).containsKey(TEST_GUESS);
        });
    }

    @Test
    public void givenTwoClients_WhenSubmit_ThenBothClientsReceiveUpdate() throws ExecutionException, InterruptedException, TimeoutException {
        StompSession session1 = webSocketStompClient.connectAsync(host, new StompSessionHandlerStub()).get(1, TimeUnit.SECONDS);
        StompSession session2 = webSocketStompClient.connectAsync(host, new StompSessionHandlerStub()).get(1, TimeUnit.SECONDS);

        final var frameHandlerStub1 = new StompFrameHandlerStub();
        final var frameHandlerStub2 = new StompFrameHandlerStub();
        session1.subscribe(TOPIC_GUESSES_ENDPOINT, frameHandlerStub1);
        session2.subscribe(TOPIC_GUESSES_ENDPOINT, frameHandlerStub2);

        session1.send(APP_SUBMIT_ENDPOINT, TEST_GUESS);

        Awaitility.waitAtMost(1, TimeUnit.SECONDS).untilAsserted(() -> {
            Map<String, Double> gameResponse1 = frameHandlerStub1.pollElement();
            assertThat(gameResponse1).isNotNull();
            assertThat(gameResponse1).hasSize(1);
            assertThat(gameResponse1).containsKey(TEST_GUESS);
        });

        Awaitility.waitAtMost(1, TimeUnit.SECONDS).untilAsserted(() -> {
            Map<String, Double> gameResponse2 = frameHandlerStub2.pollElement();
            assertThat(gameResponse2).isNotNull();
            assertThat(gameResponse2).hasSize(1);
            assertThat(gameResponse2).containsKey(TEST_GUESS);
        });

        session2.send(APP_SUBMIT_ENDPOINT, OTHER_GUESS);

        Awaitility.waitAtMost(1, TimeUnit.SECONDS).untilAsserted(() -> {
            Map<String, Double> gameResponse1 = frameHandlerStub1.pollElement();
            assertThat(gameResponse1).isNotNull();
            assertThat(gameResponse1).hasSize(2);
            assertThat(gameResponse1).containsKey(TEST_GUESS);
            assertThat(gameResponse1).containsKey(OTHER_GUESS);
        });

        Awaitility.waitAtMost(1, TimeUnit.SECONDS).untilAsserted(() -> {
            Map<String, Double> gameResponse2 = frameHandlerStub2.pollElement();
            assertThat(gameResponse2).isNotNull();
            assertThat(gameResponse2).hasSize(2);
            assertThat(gameResponse2).containsKey(TEST_GUESS);
            assertThat(gameResponse2).containsKey(OTHER_GUESS);
        });
    }

    @Test
    public void givenInvalidInput_WhenSubmit_ThenErrorResponse() throws ExecutionException, InterruptedException, TimeoutException {
        StompSession session = webSocketStompClient.connectAsync(host, new StompSessionHandlerStub()).get(1, TimeUnit.SECONDS);

        final var frameHandlerStub = new StompFrameHandlerStub();
        session.subscribe("/topic/errors", frameHandlerStub);

        session.send(APP_SUBMIT_ENDPOINT, "");

        Awaitility.await().atMost(2, TimeUnit.SECONDS).untilAsserted(() -> {
            Map<String, Double> gameResponse = frameHandlerStub.pollElement();
            assertThat(gameResponse).isNull();
        });
    }

    @Test
    public void givenMultipleMessages_WhenSubmit_ThenAllMessagesAreProcessed() throws ExecutionException, InterruptedException, TimeoutException {
        StompSession session = webSocketStompClient.connectAsync(host, new StompSessionHandlerStub()).get(1, TimeUnit.SECONDS);

        final var frameHandlerStub = new StompFrameHandlerStub();
        session.subscribe(TOPIC_GUESSES_ENDPOINT, frameHandlerStub);

        session.send(APP_SUBMIT_ENDPOINT, GIGGLE_GUESS);

        Awaitility.await().atMost(1, TimeUnit.SECONDS).untilAsserted(() -> {
            Map<String, Double> gameResponse = frameHandlerStub.pollElement();
            assertThat(gameResponse).isNotNull();
            assertThat(gameResponse).containsKey(GIGGLE_GUESS);
        });

        session.send(APP_SUBMIT_ENDPOINT, POTATO_GUESS);

        Awaitility.await().atMost(1, TimeUnit.SECONDS).untilAsserted(() -> {
            Map<String, Double> gameResponse = frameHandlerStub.pollElement();
            assertThat(gameResponse).isNotNull();
            assertThat(gameResponse).containsKey(GIGGLE_GUESS);
            assertThat(gameResponse).containsKey(POTATO_GUESS);
        });
    }

    @Test
    public void givenUnsubscribedClient_WhenSubmit_ThenNoUpdateReceived() throws ExecutionException, InterruptedException, TimeoutException {
        StompSession session = webSocketStompClient.connectAsync(host, new StompSessionHandlerStub()).get(1, TimeUnit.SECONDS);

        final var frameHandlerStub = new StompFrameHandlerStub();
        StompSession.Subscription subscription = session.subscribe(TOPIC_GUESSES_ENDPOINT, frameHandlerStub);
        subscription.unsubscribe();

        session.send(APP_SUBMIT_ENDPOINT, TEST_GUESS);

        Awaitility.await().atMost(2, TimeUnit.SECONDS).untilAsserted(() -> {
            Map<String, Double> gameResponse = frameHandlerStub.pollElement();
            assertThat(gameResponse).isNull();
        });
    }

    private static class StompSessionHandlerStub extends StompSessionHandlerAdapter {
    }

    private static class StompFrameHandlerStub implements StompFrameHandler {

        final BlockingQueue<Map<String, Double>> blockingQueue = new LinkedBlockingQueue<>(1);

        @Override
        public Type getPayloadType(@NonNull StompHeaders headers) {
            return Map.class;
        }

        @Override
        public synchronized void handleFrame(@NonNull StompHeaders headers, Object payload) {
            System.out.println("Received payload: " + payload);
            blockingQueue.add((Map<String, Double>) payload);
        }

        public synchronized Map<String, Double> pollElement() {
            return blockingQueue.poll();
        }
    }
}