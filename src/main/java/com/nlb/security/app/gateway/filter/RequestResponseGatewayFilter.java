package com.nlb.security.app.gateway.filter;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.reactivestreams.Publisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
public class RequestResponseGatewayFilter implements GlobalFilter, Ordered {
    @Autowired
    StringRedisTemplate stringRedisTemplate;
    @Autowired
    RedisTemplate redisTemplate;

    private static int status = 0;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpResponse originalResponse = exchange.getResponse();
        DataBufferFactory bufferFactory = originalResponse.bufferFactory();
        ServerHttpRequest request = exchange.getRequest();
        URI uri = request.getURI();
        HttpHeaders authorization = request.getHeaders();
        if(uri.getPath().contains("resource")) {
            List<String> authorization1 = authorization.get("Authorization");
            String key = authorization1.get(0);
            key=key.substring(7,key.length());
            System.out.println(key);
            if(!stringRedisTemplate.hasKey("\""+key+"\"")){
                    status=1;
            }
        }

        ServerHttpResponseDecorator decoratedResponse = new ServerHttpResponseDecorator(originalResponse);

//处理响应
        if(status==0) {
            decoratedResponse = new ServerHttpResponseDecorator(originalResponse) {
                @Override
                public Mono<Void> writeWith(Publisher<? extends DataBuffer> body) {
                    if (body instanceof Flux) {
                        Flux<? extends DataBuffer> fluxBody = (Flux<? extends DataBuffer>) body;
                        return super.writeWith(fluxBody.map(dataBuffer -> {
                            // probably should reuse buffers
                            byte[] content = new byte[dataBuffer.readableByteCount()];
                            dataBuffer.read(content);
                            //释放掉内存
                            DataBufferUtils.release(dataBuffer);
                            String s = new String(content, Charset.forName("UTF-8"));
                            //TODO，s就是response的值，想修改、查看就随意而为了
                            StringBuilder ss = new StringBuilder(s);
                            if (uri.getPath().equals("/app/oauth/token")) {
                                ss.insert(s.length(), "}");
                                int token_type = s.indexOf("expires_in");
                                ss.insert(token_type + 12, "\"");
                                int scope = s.indexOf("scope");
                                ss.insert(scope - 1, "\"");
                                JSONObject jsonObject = JSONObject.parseObject(ss.toString());
                                Object access_token = jsonObject.get("access_token");
                                ServerHttpRequestDecorator serverHttpRequestDecorator = new ServerHttpRequestDecorator(request);
                                MultiValueMap<String, String> queryParams = serverHttpRequestDecorator.getQueryParams();
                                List<String> stringList = queryParams.get("username");
                                String name = stringList.get(0);
                                if (!stringRedisTemplate.hasKey("\"" + name + "\"")) {
                                    stringRedisTemplate.opsForValue().set(JSON.toJSONString(access_token), JSON.toJSONString(access_token), 600, TimeUnit.SECONDS);
                                    stringRedisTemplate.opsForValue().set("\"" + name + "\"", JSON.toJSONString(access_token), 600, TimeUnit.SECONDS);
                                } else {
                                    String s1 = stringRedisTemplate.opsForValue().get("\"" + name + "\"");
                                    stringRedisTemplate.delete(s1);
                                    stringRedisTemplate.delete("\"" + name + "\"");
                                    stringRedisTemplate.opsForValue().set(JSON.toJSONString(access_token), JSON.toJSONString(access_token), 600, TimeUnit.SECONDS);
                                    stringRedisTemplate.opsForValue().set("\"" + name + "\"", JSON.toJSONString(access_token), 600, TimeUnit.SECONDS);
                                }
                            }
                            byte[] uppedContent = null;
                            if (uri.getPath().contains("resource")) {
                                uppedContent = new String(content, Charset.forName("UTF-8")).getBytes();
                            } else {
                                uppedContent = new String(content, Charset.forName("UTF-8")).getBytes();
                            }
                            return bufferFactory.wrap(uppedContent);
                        }));
                    }

                    // if body is not a flux. never got there.
                    return super.writeWith(body);
                }
            };
        } else{
            decoratedResponse = new ServerHttpResponseDecorator(originalResponse) {
                @Override
                public Mono<Void> writeWith(Publisher<? extends DataBuffer> body) {
                    if (body instanceof Flux) {
                        Flux<? extends DataBuffer> fluxBody = (Flux<? extends DataBuffer>) body;
                        return super.writeWith(fluxBody.map(dataBuffer -> {
                            // probably should reuse buffers
                            byte[] content = new byte[dataBuffer.readableByteCount()];
                            dataBuffer.read(content);
                            //释放掉内存
                            DataBufferUtils.release(dataBuffer);
                            String s = new String(content, Charset.forName("UTF-8"));
                            dataBuffer=null;
                            //TODO，s就是response的值，想修改、查看就随意而为了
                            byte[] uppedContent = new String("token无效".getBytes(), Charset.forName("UTF-8")).getBytes();
                            return bufferFactory.wrap(uppedContent);
                        }));
                    }
                    // if body is not a flux. never got there.
                    return super.writeWith(body);
                }
            };
            decoratedResponse.setStatusCode(HttpStatus.BAD_GATEWAY);
            status=0;

        }

            // replace response with decorator
            return chain.filter(exchange.mutate().response(decoratedResponse).build());
        }


        @Override
        public int getOrder() {
            return -1;
        }
}
