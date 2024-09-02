package io.github.seal90.seal_kiss_proxy_gateway_feign_proxy.controller;

import io.github.seal90.seal_kiss_proxy_gateway_feign_proxy.dto.EchoDTO;
import io.github.seal90.seal_kiss_proxy_gateway_feign_proxy.feignClient.SelfFeign;
import io.github.seal90.seal_kiss_proxy_gateway_feign_proxy.feignClient.ServiceNameFeign;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
public class HelloController {

    @Autowired
    private SelfFeign selfFeign;

    @Autowired
    private ServiceNameFeign serviceNameFeign;

    @PostMapping("/hello")
    public String hello() {
        return "hello";
    }

    @PostMapping("/world")
    public String world() {
        return "world";
    }

    @PostMapping("/callFeign")
    public String callFeign() {
        return "callFeign "+selfFeign.hello();
    }

    @PostMapping("/callFeignWithEmptyData")
    public String callFeignWithEmptyData() {
        selfFeign.callFeignWithEmptyData();
        return "callFeignWithEmptyData";
    }

    @PostMapping("/callFeignWithData")
    public EchoDTO callFeignWithData() {
        EchoDTO echoDTO = new EchoDTO();
        echoDTO.setEchoData("callFeignWithData");
        return selfFeign.callFeignWithData("pathVarVal", "secondVarVar", echoDTO);
    }

    @PostMapping("/callServiceNameFeignWithEmptyData")
    public String callServiceNameFeignWithEmptyData() {
        serviceNameFeign.callFeignWithEmptyData();
        return "callServiceNameFeignWithEmptyData";
    }

    @PostMapping("/callServiceNameFeignWithData")
    public EchoDTO callServiceNameFeignWithData() {
        EchoDTO echoDTO = new EchoDTO();
        echoDTO.setEchoData("callServiceNameFeignWithEmptyData");
        return serviceNameFeign.callFeignWithData("pathVarVal", "secondVarVar", echoDTO);
    }
}
