package io.github.seal90.kiss.proxy.gateway.server.controller;

import io.github.seal90.kiss.proxy.gateway.server.AppConstant;
import io.github.seal90.kiss.proxy.gateway.server.discovery.InstanceDiscovery;
import io.github.seal90.kiss.proxy.gateway.server.dto.EchoDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
public class ServerController {

    @Autowired
    private InstanceDiscovery instanceDiscovery;

    @PostMapping("/callClient")
    public Mono<Object> callClient(ServerHttpRequest request) {
        HttpHeaders headers = request.getHeaders();

        Flux<DataBuffer> bodyFlux = request.getBody();


        Flux<RSocketRequester> rSocketRequesterFlux = instanceDiscovery.findRequester();
        return rSocketRequesterFlux.next().flatMap(rSocketRequester -> rSocketRequester
                .route("callClient")
                .data(bodyFlux)
                .retrieveMono(Object.class)).defaultIfEmpty(Mono.empty());
    }

    @PostMapping("/show")
    public Mono<String> show() {

        return Mono.just("hello");
    }

    @PostMapping("/callFeignWithEmptyData")
    public void callFeignWithEmptyData() {
        log.info("callFeignWithEmptyData call");
    }

    @PostMapping("/callFeignWithData/{pathVar}")
    public EchoDTO callFeignWithData(@PathVariable String pathVar, @RequestParam String secondVar, @RequestBody EchoDTO echoDTO) {
        log.info("pathVar: {}, secondVar: {}, {}",pathVar, secondVar, echoDTO.getEchoData());
        echoDTO.setEchoData("response " + echoDTO.getEchoData());
        return echoDTO;
    }

}
