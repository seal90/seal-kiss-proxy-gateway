package io.github.seal90.kiss.proxy.gateway.client.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.rsocket.RSocketRequester;
import reactor.core.publisher.Mono;

@Slf4j
//@Controller
public class ClientRSocketController {

    private RSocketRequester rsocketRequester;

    @MessageMapping("callClient")
    public Mono<Object> callClient() {
        // proxy http
        log.info("client response");
        return Mono.empty();
    }
}
