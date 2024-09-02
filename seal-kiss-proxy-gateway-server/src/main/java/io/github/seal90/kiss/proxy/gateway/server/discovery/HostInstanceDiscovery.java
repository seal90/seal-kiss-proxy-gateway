package io.github.seal90.kiss.proxy.gateway.server.discovery;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.messaging.rsocket.annotation.ConnectMapping;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@Controller
public class HostInstanceDiscovery implements InstanceDiscovery, DisposableBean {

    private final List<RSocketRequester> CLIENTS = new ArrayList<>();

    @ConnectMapping("register")
    public void register(RSocketRequester requester, @Payload String client) {
        requester.rsocketClient()
                .onClose() // (1)
                .doFirst(() -> {
                    log.info("Client: {} CONNECTED.", client);
                    CLIENTS.add(requester); // (2)
                })
                .doOnError(error -> {
                    log.warn("Channel to client {} CLOSED", client); // (3)
                })
                .doFinally(consumer -> {
                    CLIENTS.remove(requester);
                    log.info("Client {} DISCONNECTED", client); // (4)
                })
                .subscribe();
    }

    @Override
    public Flux<RSocketRequester> findRequester() {
        return Flux.fromIterable(CLIENTS);
    }

    @Override
    public void destroy() throws Exception {
        log.info("Detaching all remaining clients...");
        CLIENTS.stream().forEach(requester -> requester.rsocket().dispose());
        log.info("Shutting down.");
    }
}
