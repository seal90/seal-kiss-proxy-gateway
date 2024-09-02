package io.github.seal90.kiss.proxy.gateway.client.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.seal90.kiss.proxy.gateway.client.ProxyHttpHeader;
import io.github.seal90.kiss.proxy.gateway.client.controller.ClientRSocketController;
import io.rsocket.RSocket;
import io.rsocket.SocketAcceptor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.rsocket.messaging.RSocketStrategiesCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.messaging.rsocket.RSocketStrategies;
import org.springframework.messaging.rsocket.annotation.support.RSocketMessageHandler;
import org.springframework.util.MimeTypeUtils;
import reactor.util.retry.Retry;

import static io.github.seal90.kiss.proxy.gateway.client.AppConstant.PROXY_HTTP_HEADERS;

@Slf4j
@Configuration
public class ClientRSocketConfig {



    @Bean
    public RSocketStrategiesCustomizer httpProxyHeader(ObjectMapper objectMapper) {
        return (strategy) -> {
            strategy.metadataExtractorRegistry(registry -> {
                registry.metadataToExtract(MimeTypeUtils.APPLICATION_JSON, ProxyHttpHeader.class, PROXY_HTTP_HEADERS);
            });
        };
    }

//    @Bean
//    public RSocketStrategiesCustomizer encoderDecoder() {
//        return (strategy) -> {
//            strategy.encoders(encodes -> {
//                encodes.addFirst(new DataBufferEncoder());
//
//            }).decoders(decoders -> {
//                decoders.addFirst(new DataBufferDecoder());
//            });
//        };
//    }

    @Bean
    public RSocketRequester rSocketRequester(RSocketRequester.Builder rsocketRequesterBuilder,
                                                   RSocketStrategies rsocketStrategies){

        log.info("register to server");
        SocketAcceptor responder = RSocketMessageHandler.responder(rsocketStrategies, new ClientRSocketController());
        RSocketRequester rsocketRequester = rsocketRequesterBuilder
                .setupRoute("register")
                .setupData("CLIENT_ID")
//                .setupMetadata(user, SIMPLE_AUTH)
//                .rsocketStrategies(builder ->
//                        builder.encoder(new SimpleAuthenticationEncoder()))
                .rsocketConnector(
                        rSocketConnector ->
                                rSocketConnector.reconnect(Retry.indefinitely())
                )
                .rsocketConnector(connector -> connector.acceptor(responder))
                .tcp("127.0.0.1", 9898);


        // The above does not connect immediately. When requests are made, a shared connection is established transparently and used.
        // 默认上面的方式不会直接发起连接，需要等到发送第一个请求时，下面的方式可以确保在启动时连接到服务端。
        rsocketRequester.rsocketClient().source().block();

        rsocketRequester.rsocketClient()
                .source().flatMap(RSocket::onClose).repeat().retryWhen(Retry.indefinitely())
                .doOnError(error -> log.warn("Connection CLOSED"))
                .doFinally(consumer -> log.info("Client DISCONNECTED"))
                .subscribe();

        return rsocketRequester;
    }
}
