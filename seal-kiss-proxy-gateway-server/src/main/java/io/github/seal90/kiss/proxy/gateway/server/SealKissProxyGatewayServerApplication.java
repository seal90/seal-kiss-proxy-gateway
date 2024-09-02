package io.github.seal90.kiss.proxy.gateway.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class SealKissProxyGatewayServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(SealKissProxyGatewayServerApplication.class, args);
	}

}
