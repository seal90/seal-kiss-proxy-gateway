# proxy http by rsocket
http client --> (http server -> rsocket client) --> (rsocket server -> http client) --> http server

# impl

* http client [seal-kiss-proxy-gateway-feign-proxy](seal-kiss-proxy-gateway-feign-proxy) 
  * [HelloController.java](seal-kiss-proxy-gateway-feign-proxy%2Fsrc%2Fmain%2Fjava%2Fio%2Fgithub%2Fseal90%2Fseal_kiss_proxy_gateway_feign_proxy%2Fcontroller%2FHelloController.java)
  * http listen 8080
* (http server -> rsocket client) [seal-kiss-proxy-gateway-client](seal-kiss-proxy-gateway-client)
  * [ClientController.java](seal-kiss-proxy-gateway-client%2Fsrc%2Fmain%2Fjava%2Fio%2Fgithub%2Fseal90%2Fkiss%2Fproxy%2Fgateway%2Fclient%2Fcontroller%2FClientController.java)
  * http listen 8081
* (rsocket server -> http client) [seal-kiss-proxy-gateway-server](seal-kiss-proxy-gateway-server)
  * [ServerRSocketController.java](seal-kiss-proxy-gateway-server%2Fsrc%2Fmain%2Fjava%2Fio%2Fgithub%2Fseal90%2Fkiss%2Fproxy%2Fgateway%2Fserver%2Fcontroller%2FServerRSocketController.java)
  * rsocket 9898
* http server [seal-kiss-proxy-gateway-server](seal-kiss-proxy-gateway-server)
  * [ServerController.java](seal-kiss-proxy-gateway-server%2Fsrc%2Fmain%2Fjava%2Fio%2Fgithub%2Fseal90%2Fkiss%2Fproxy%2Fgateway%2Fserver%2Fcontroller%2FServerController.java)
  * http listen 8082
* call
  * POST IP:8080/callFeignWithEmptyData
  * POST IP:8080/callFeignWithData
  * POST IP:8080/callServiceNameFeignWithEmptyData
  * POST IP:8080/callServiceNameFeignWithData


# 扩充 spring
## client
[RSocketRequester.java](seal-kiss-proxy-gateway-client%2Fsrc%2Fmain%2Fjava%2Forg%2Fspringframework%2Fmessaging%2Frsocket%2FRSocketRequester.java)
增加接口方法
```java
		<T> Mono<Tuple2<T, Map<String, Object>>> retrieveMetadataMono(ParameterizedTypeReference<T> dataTypeRef);

		<T, V> Mono<Tuple2<T[Readme.md](Readme.md), V>> retrieveMetadataMono(Class<T> dataType, String metadataKey, Class<V> metadataType);

		<T, V> Mono<Tuple2<T, V>> retrieveMetadataMono(ParameterizedTypeReference<T> dataTypeRef, String metadataKey, ParameterizedTypeReference<V> metadataTypeRef);
```
[DefaultRSocketRequester.java](seal-kiss-proxy-gateway-client%2Fsrc%2Fmain%2Fjava%2Forg%2Fspringframework%2Fmessaging%2Frsocket%2FDefaultRSocketRequester.java)
实现 RSocketRequester

[MetadataEncoder.java](seal-kiss-proxy-gateway-client%2Fsrc%2Fmain%2Fjava%2Forg%2Fspringframework%2Fmessaging%2Frsocket%2FMetadataEncoder.java)


## server
* [MessagingRSocket.java](seal-kiss-proxy-gateway-server%2Fsrc%2Fmain%2Fjava%2Forg%2Fspringframework%2Fmessaging%2Frsocket%2Fannotation%2Fsupport%2FMessagingRSocket.java)
增加 返回 header 到请求中
```java
		AtomicReference<List<Tuple2<MimeType, Object>>> responseHeadersRef = new AtomicReference<>();
		responseHeadersRef.set(new ArrayList<>());
		headers.setHeader(RSocketPayloadReturnValueHandler.RESPONSE_HEADER_HEADER, responseHeadersRef);
```
* [RSocketPayloadReturnValueHandler.java](seal-kiss-proxy-gateway-server%2Fsrc%2Fmain%2Fjava%2Forg%2Fspringframework%2Fmessaging%2Frsocket%2Fannotation%2Fsupport%2FRSocketPayloadReturnValueHandler.java)
处理上面的 header 到响应中
```java
        
		AtomicReference<Flux<Payload>> responseRef = getResponseReference(message);
		Assert.notNull(responseRef, "Missing '" + RESPONSE_HEADER + "'");

		MessageHeaders messageHeaders = message.getHeaders();
		Object headerValue = messageHeaders.get(RSocketRequesterMethodArgumentResolver.RSOCKET_REQUESTER_HEADER);

		Assert.isInstanceOf(RSocketRequester.class, headerValue, "Expected header value of type RSocketRequester");
		RSocketRequester requester = (RSocketRequester) headerValue;

		// MimeType metadataMimeType, RSocketStrategies strategies
		AtomicReference<List<Tuple2<MimeType, Object>>> val = (AtomicReference<List<Tuple2<MimeType, Object>>>)messageHeaders.get(RESPONSE_HEADER_HEADER);
		List<Tuple2<MimeType, Object>> returnHeaders = val.get();

		MetadataEncoder encoder = new MetadataEncoder(requester.metadataMimeType(),requester.strategies());
		for(Tuple2<MimeType, Object> header : returnHeaders) {
			encoder.metadata(header.getT2(), header.getT1());
		}

		Mono<DataBuffer> headerDataBuffer =  encoder.encode();

		responseRef.set(headerDataBuffer.flatMapMany(metadata -> {
			return encodedContent.map(data -> PayloadUtils.createPayload(data, metadata));
		}));
```
* [MetadataEncoder.java](seal-kiss-proxy-gateway-server%2Fsrc%2Fmain%2Fjava%2Forg%2Fspringframework%2Fmessaging%2Frsocket%2FMetadataEncoder.java)
声明为 public，在 RSocketPayloadReturnValueHandler 中使用


# TODO 功能
* 开放接口白名单
* 安全认证
* 服务端断开重连
* 高可用思路： 
  * 服务端部署多个节点，客户端注册到服务端是，服务端将注册节点的信息放到注册中心里面去（或者其他地方），当服务端要调用客户端时有两个方式
    * 客户端增强服务发现，在选择服务实例时，根据注册中心的扩展信息进行判断选择
    * 客户端调用任意一个服务端，服务端寻找实例并调用


# 调用下游
* 结合网关 ReactorHttpHandlerAdapter
* 泛化调用 
  * webclient 增加 LoadBalance LoadBalancerExchangeFilterFunction
  * RestTemplate 注解 @LoadBalanced

# Question
* 如何结合 spring cloud gateway 实现

```java
// feign.Target<T>

    @Override
    public Request apply(RequestTemplate input) {
      if (input.url().indexOf("http") != 0) {
          // input.target(input.feignTarget().url());
        input.target(url());
      }
      return input.request();
    }
```

# 相关文档
* rsocket:
  * https://docs.spring.io/spring-boot/reference/messaging/rsocket.html
  * https://docs.spring.io/spring-framework/reference/rsocket.html#rsocket-spring
  * https://spring.io/blog/2020/05/12/getting-started-with-rsocket-servers-calling-clients
  * https://docs.spring.io/spring-framework/docs/5.3.36-SNAPSHOT/reference/pdf/rsocket.pdf
  * https://docs.spring.vmware.com/spring-integration/docs/6.0.11/reference/html/rsocket.html#rsocket-inbound
* reactive web: https://docs.spring.io/spring-boot/reference/web/reactive.html
