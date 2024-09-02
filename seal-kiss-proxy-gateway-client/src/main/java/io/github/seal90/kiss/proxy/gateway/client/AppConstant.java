package io.github.seal90.kiss.proxy.gateway.client;

import org.springframework.util.MimeType;

public class AppConstant {

    public static final String X_PROXY_GATEWAY_TARGET_SERVICE = "x-target-service";

    public static final String X_PROXY_GATEWAY_TARGET_METHOD = "x-target-method";

    public static final String X_PROXY_GATEWAY_TARGET_PATH = "x-target-path";


    public static final MimeType MIME_PROXY_HTTP_HEADERS = MimeType.valueOf("message/x.proxy.http.headers");
    public static final String PROXY_HTTP_HEADERS = "proxy-http-headers";

}
