package io.github.seal90.kiss.proxy.gateway.server.discovery;

import org.springframework.messaging.rsocket.RSocketRequester;
import reactor.core.publisher.Flux;

public interface InstanceDiscovery {

    Flux<RSocketRequester> findRequester();

}
