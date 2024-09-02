package io.github.seal90.kiss.proxy.gateway.client.controller;

import io.github.seal90.kiss.proxy.gateway.client.AppConstant;
import io.github.seal90.kiss.proxy.gateway.client.ProxyHttpHeader;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.messaging.rsocket.RSocketStrategies;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import static io.github.seal90.kiss.proxy.gateway.client.AppConstant.PROXY_HTTP_HEADERS;

@Slf4j
@RestController
public class ClientController {

    @Autowired
    private RSocketRequester rSocketRequester;

    @Autowired
    private RSocketStrategies strategies;

    @PostMapping("/proxy")
    public Mono<DataBuffer> proxy(ServerHttpRequest request, ServerHttpResponse response) {
        HttpHeaders headers = request.getHeaders();
        Flux<DataBuffer> bodyFlux = request.getBody();

        Map<String, String> rsocketProxyHttpHeaders = new HashMap<>();
        rsocketProxyHttpHeaders.put("hello", "world");
        ProxyHttpHeader proxyHttpHeader = new ProxyHttpHeader();
        proxyHttpHeader.setServiceName(headers.getFirst("x-proxy-target-service"));
        proxyHttpHeader.setMethod(headers.getFirst("x-proxy-target-method"));
        proxyHttpHeader.setUrl(headers.getFirst("x-proxy-target-url"));
        proxyHttpHeader.setQueryLine(headers.getFirst("x-proxy-target-queryLine"));
        proxyHttpHeader.setDiscoveryType(headers.getFirst("x-proxy-target-discoveryType"));
        proxyHttpHeader.setBizHeaders(rsocketProxyHttpHeaders);

        // which api can send http body and rsocketProxyHttpHeaders ?
        // which api can parse rsocket response to http response ?
        Mono<DataBuffer> emptMono = Mono.just(this.strategies.dataBufferFactory().wrap(new byte[0]));
        return DataBufferUtils.join(bodyFlux).switchIfEmpty(Mono.defer(() -> {
            return emptMono;
        })).flatMap(dataBuffer -> {
            return rSocketRequester.route("callServer").metadata(metadataSpec -> {
                        metadataSpec.metadata(proxyHttpHeader, MimeTypeUtils.APPLICATION_JSON);
                    }).data(dataBuffer).retrieveMetadataMono(DataBuffer.class, PROXY_HTTP_HEADERS, ProxyHttpHeader.class)
                    .flatMap((dataAndMetadata) -> {
                        DataBuffer data = dataAndMetadata.getT1();
                        ProxyHttpHeader proxyHeader = dataAndMetadata.getT2();
                        Map<String, String> bizHeaders = proxyHeader.getBizHeaders();
                        // TODO filter headers
                        for(Map.Entry<String, String> bizHeader : bizHeaders.entrySet()) {
                            response.getHeaders().add(bizHeader.getKey(), bizHeader.getValue());
                        }
                        response.getHeaders().setContentType(MediaType.parseMediaType(bizHeaders.get("Content-Type")));
                        return Mono.just(data);
                    });
        }).timeout(Duration.ofSeconds(3));
    }

    @PostMapping("/callServer")
    public Mono<DataBuffer> callServer(ServerHttpRequest request, ServerHttpResponse response) {
        HttpHeaders headers = request.getHeaders();
        Flux<DataBuffer> bodyFlux = request.getBody();

        Map<String, String> rsocketProxyHttpHeaders = new HashMap<>();
        rsocketProxyHttpHeaders.put(AppConstant.X_PROXY_GATEWAY_TARGET_SERVICE, headers.getFirst(AppConstant.X_PROXY_GATEWAY_TARGET_SERVICE));
        rsocketProxyHttpHeaders.put(AppConstant.X_PROXY_GATEWAY_TARGET_METHOD, headers.getFirst(AppConstant.X_PROXY_GATEWAY_TARGET_METHOD));
        rsocketProxyHttpHeaders.put(AppConstant.X_PROXY_GATEWAY_TARGET_PATH, headers.getFirst(AppConstant.X_PROXY_GATEWAY_TARGET_PATH));
        rsocketProxyHttpHeaders.put("hello", "world");
        ProxyHttpHeader proxyHttpHeader = new ProxyHttpHeader();
        proxyHttpHeader.setBizHeaders(rsocketProxyHttpHeaders);
        proxyHttpHeader.setMethod("method");
        // TODO and other headers by config
        // http needs: content-type
        // biz needs: like auth

        // which api can send http body and rsocketProxyHttpHeaders ?
        // which api can parse rsocket response to http response ?

        return DataBufferUtils.join(bodyFlux).flatMap(dataBuffer -> {
            return rSocketRequester.route("callServer").metadata(metadataSpec -> {
                        metadataSpec.metadata(proxyHttpHeader, MimeTypeUtils.APPLICATION_JSON);
                    }).data(dataBuffer).retrieveMetadataMono(DataBuffer.class, PROXY_HTTP_HEADERS, ProxyHttpHeader.class)
                    .flatMap((dataAndMetadata) -> {
                        DataBuffer data = dataAndMetadata.getT1();
                        ProxyHttpHeader proxyHeader = dataAndMetadata.getT2();
                        Map<String, String> bizHeaders = proxyHeader.getBizHeaders();
                        // TODO filter headers
                        for(Map.Entry<String, String> bizHeader : bizHeaders.entrySet()) {
                            response.getHeaders().add(bizHeader.getKey(), bizHeader.getValue());
                        }
                        response.getHeaders().setContentType(MediaType.parseMediaType(bizHeaders.get("Content-Type")));
                        return Mono.just(data);
                    });
        });
    }
}
