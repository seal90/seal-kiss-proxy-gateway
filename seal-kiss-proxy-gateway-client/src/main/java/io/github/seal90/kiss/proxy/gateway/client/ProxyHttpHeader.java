package io.github.seal90.kiss.proxy.gateway.client;

import lombok.Data;

import java.util.Map;

@Data
public class ProxyHttpHeader {

    private String serviceName;

    private String method;

    private String url;

    private String queryLine;

    private String discoveryType;

    private Map<String, String> bizHeaders;
}
