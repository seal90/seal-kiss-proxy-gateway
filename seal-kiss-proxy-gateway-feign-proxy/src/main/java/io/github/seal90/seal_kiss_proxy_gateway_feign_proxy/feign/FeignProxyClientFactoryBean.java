package io.github.seal90.seal_kiss_proxy_gateway_feign_proxy.feign;

import feign.*;
import org.springframework.cloud.openfeign.FeignClientFactory;
import org.springframework.cloud.openfeign.FeignClientFactoryBean;
import org.springframework.core.Ordered;
import org.springframework.util.StringUtils;

public class FeignProxyClientFactoryBean extends FeignClientFactoryBean {

    private String proxyName;

    private String proxyUrl;

    private String proxyPath;

    @Override
    protected void configureUsingConfiguration(FeignClientFactory context, Feign.Builder builder) {
        super.configureUsingConfiguration(context, builder);
        builder.requestInterceptor(new ProxyRequestInterceptor(proxyName, proxyUrl, proxyPath));
    }

    private static class ProxyRequestInterceptor implements RequestInterceptor, Ordered {

        private String proxyName;

        private String proxyUrl;

        private String proxyPath;

        public ProxyRequestInterceptor(String proxyName, String proxyUrl, String proxyPath) {
            this.proxyName = proxyName;
            this.proxyUrl = proxyUrl;
            this.proxyPath = proxyPath;
        }

        @Override
        public void apply(RequestTemplate template) {
            Target<?> target = template.feignTarget();
            template.target(target.url());

            template.header("x-proxy-target-url", template.path());
//            template.header("x-proxy-target-uri", template.url());
//            template.header("x-proxy-target-path", template.path());
            template.header("x-proxy-target-service", target.name());
            template.header("x-proxy-target-method", template.method());
            template.header("x-proxy-target-queryLine", template.queryLine());
            template.queries(null);
            template.uri("");

            String discoveryType = "BY_URL";
            String targetProxyUrl = proxyUrl;
            if (!StringUtils.hasText(proxyUrl)) {

                if (!proxyName.startsWith("http://") && !proxyName.startsWith("https://")) {
                    targetProxyUrl = "http://" + proxyName;
                }
                else {
                    targetProxyUrl = proxyName;
                }
                discoveryType = "BY_NAME";
            }
            template.header("x-proxy-target-discoveryType", discoveryType);
            String proxyFullPath = targetProxyUrl + proxyPath;
            template.target(proxyFullPath);
            template.feignTarget(new ProxyTarget(proxyName, proxyUrl, target));
        }

        @Override
        public int getOrder() {
            return LOWEST_PRECEDENCE;
        }
    }

    public static class ProxyTarget implements Target {

        private final String name;

        private final String url;

        private final Target<?> target;

        public ProxyTarget(String name, String url, Target<?> target) {
            this.name = name;
            this.url = url;
            this.target = target;
        }

        @Override
        public Class type() {
            return target.type();
        }

        @Override
        public String name() {
            return this.name;
        }

        @Override
        public String url() {
            return this.url;
        }

        @Override
        public Request apply(RequestTemplate input) {
            return target.apply(input);
        }
    }

    public String getProxyName() {
        return proxyName;
    }

    public void setProxyName(String proxyName) {
        this.proxyName = proxyName;
    }

    public String getProxyUrl() {
        return proxyUrl;
    }

    public void setProxyUrl(String proxyUrl) {
        this.proxyUrl = proxyUrl;
    }

    public String getProxyPath() {
        return proxyPath;
    }

    public void setProxyPath(String proxyPath) {
        this.proxyPath = proxyPath;
    }
}
