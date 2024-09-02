package io.github.seal90.seal_kiss_proxy_gateway_feign_proxy.feignClient;

import io.github.seal90.seal_kiss_proxy_gateway_feign_proxy.dto.EchoDTO;
import io.github.seal90.seal_kiss_proxy_gateway_feign_proxy.feign.FeignProxyClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

//@FeignProxyClient(proxyUrl = "http://127.0.0.1:8081", name = "hello", path = "/pathTest", url = "http://127.0.0.1:8082")
@FeignProxyClient(proxyUrl = "http://127.0.0.1:8081", name = "hello", url = "http://127.0.0.1:8082")
public interface SelfFeign {

//    @PostMapping("/hello")
//    String hello(@RequestBody String hello);


    @PostMapping("/hello")
    String hello();

    @PostMapping("/callFeignWithEmptyData")
    void callFeignWithEmptyData();

    @PostMapping("/callFeignWithData/{pathVar}?first=first&second={secondVar}")
    EchoDTO callFeignWithData(@PathVariable String pathVar, @RequestParam String secondVar,  @RequestBody EchoDTO echoDTO);


}
