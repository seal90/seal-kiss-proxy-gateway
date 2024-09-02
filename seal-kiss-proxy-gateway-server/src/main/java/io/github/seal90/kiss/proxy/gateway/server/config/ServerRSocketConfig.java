package io.github.seal90.kiss.proxy.gateway.server.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.seal90.kiss.proxy.gateway.server.ProxyHttpHeader;
import io.github.seal90.kiss.proxy.gateway.server.AppConstant;
import org.springframework.boot.rsocket.messaging.RSocketStrategiesCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.MimeTypeUtils;

@Configuration
public class ServerRSocketConfig {

    @Bean
    public RSocketStrategiesCustomizer httpProxyHeader(ObjectMapper objectMapper) {
        return (strategy) -> {
            strategy.metadataExtractorRegistry(registry -> {
                registry.metadataToExtract(MimeTypeUtils.APPLICATION_JSON, ProxyHttpHeader.class, AppConstant.PROXY_HTTP_HEADERS);
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
}
