package io.github.seal90.seal_kiss_proxy_gateway_feign_proxy;

import io.github.seal90.seal_kiss_proxy_gateway_feign_proxy.feign.EnableFeignProxyClients;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
//@EnableDiscoveryClient
//@EnableFeignClients
@EnableFeignProxyClients(proxyName = "seal-kiss-proxy-gateway-client")
public class SealKissProxyGatewayFeignProxyApplication {

	public static void main(String[] args) {
		SpringApplication.run(SealKissProxyGatewayFeignProxyApplication.class, args);
	}

}
