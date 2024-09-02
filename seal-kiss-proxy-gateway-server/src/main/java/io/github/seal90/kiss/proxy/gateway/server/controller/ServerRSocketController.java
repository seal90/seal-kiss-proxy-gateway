package io.github.seal90.kiss.proxy.gateway.server.controller;

import io.github.seal90.kiss.proxy.gateway.server.AppConstant;
import io.github.seal90.kiss.proxy.gateway.server.ProxyHttpHeader;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.loadbalancer.reactive.ReactorLoadBalancerExchangeFilterFunction;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.messaging.rsocket.annotation.support.RSocketPayloadReturnValueHandler;
import org.springframework.stereotype.Controller;
import org.springframework.util.MimeType;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

@Getter
@Slf4j
@Controller
public class ServerRSocketController {


    @Autowired
    private ReactorLoadBalancerExchangeFilterFunction lbFunction;

    @MessageMapping("callServer")
    public Mono<DataBuffer> callServer(@Header(AppConstant.PROXY_HTTP_HEADERS) ProxyHttpHeader proxyHttpHeader,
                                   @Header(RSocketPayloadReturnValueHandler.RESPONSE_HEADER) AtomicReference<ProxyHttpHeader> responseRef,
                                   @Header(RSocketPayloadReturnValueHandler.RESPONSE_HEADER_HEADER) AtomicReference<List<Tuple2<MimeType, Object>>> responseHeadersRef ,
                                   @Headers Map<String, Object> headers1,
                                   @Payload(required = false) Mono<DataBuffer> body,
                                       RSocketRequester rSocketRequester) {
        log.info("server response");
        Map<String, String> proxyHeaders = proxyHttpHeader.getBizHeaders();

        String url = proxyHttpHeader.getUrl();
        if(null != proxyHttpHeader.getQueryLine()) {
            url = url + proxyHttpHeader.getQueryLine();
        }
        // TODO on error
        if("BY_NAME".equals(proxyHttpHeader.getDiscoveryType())) {
            return WebClient.builder().filter(lbFunction).baseUrl(url).build()
                    .method(HttpMethod.valueOf(proxyHttpHeader.getMethod())).contentType(MediaType.APPLICATION_JSON)
                    .headers((headers) -> headers.setAll(proxyHeaders))
                    .body(body, DataBuffer.class)
                    .exchangeToMono(resp -> {
                        ProxyHttpHeader proxyHttpHeaderResp = new ProxyHttpHeader();
                        proxyHttpHeaderResp.setMethod("POST");
                        Map<String, String> bizReturnMap = new HashMap<>(resp.headers().asHttpHeaders().toSingleValueMap());
                        bizReturnMap.put("helloserver", "world");
                        proxyHttpHeaderResp.setBizHeaders(bizReturnMap);
                        List<Tuple2<MimeType, Object>> returnHeaders = responseHeadersRef.get();
                        returnHeaders.add(Tuples.of(MediaType.APPLICATION_JSON, proxyHttpHeaderResp));
                        return resp.bodyToMono(DataBuffer.class);
                    });
        } else {
            return WebClient.builder().baseUrl(url).build()
                    .method(HttpMethod.valueOf(proxyHttpHeader.getMethod())).contentType(MediaType.APPLICATION_JSON)
                    .headers((headers) -> headers.setAll(proxyHeaders))
                    .body(body, DataBuffer.class)
                    .exchangeToMono(resp -> {
                        ProxyHttpHeader proxyHttpHeaderResp = new ProxyHttpHeader();
                        proxyHttpHeaderResp.setMethod("POST");
                        Map<String, String> bizReturnMap = new HashMap<>(resp.headers().asHttpHeaders().toSingleValueMap());
                        bizReturnMap.put("helloserver", "world");
                        proxyHttpHeaderResp.setBizHeaders(bizReturnMap);
                        List<Tuple2<MimeType, Object>> returnHeaders = responseHeadersRef.get();
                        returnHeaders.add(Tuples.of(MediaType.APPLICATION_JSON, proxyHttpHeaderResp));
                        return resp.bodyToMono(DataBuffer.class);
                    });
        }

    }

}
