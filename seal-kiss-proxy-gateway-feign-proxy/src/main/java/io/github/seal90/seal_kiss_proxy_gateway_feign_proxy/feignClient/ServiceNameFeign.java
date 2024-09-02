package io.github.seal90.seal_kiss_proxy_gateway_feign_proxy.feignClient;

import io.github.seal90.seal_kiss_proxy_gateway_feign_proxy.dto.EchoDTO;
import io.github.seal90.seal_kiss_proxy_gateway_feign_proxy.feign.FeignProxyClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@FeignProxyClient(name = "seal-kiss-proxy-gateway-server")
public interface ServiceNameFeign {

    @PostMapping("/callFeignWithEmptyData")
    void callFeignWithEmptyData();

    @PostMapping("/callFeignWithData/{pathVar}?first=first&second={secondVar}")
    EchoDTO callFeignWithData(@PathVariable String pathVar, @RequestParam String secondVar, @RequestBody EchoDTO echoDTO);

}
