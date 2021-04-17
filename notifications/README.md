# github - 수정필요
https://github.com/chk386/kafka-connect-playground/tree/main/notifications

# 배경
최근 엑셀다운로드 기능이 sync에서 async로 변경되었다.
이로 인해 서버는 안정을 찾았고 우리는 편해졌으나 고객은 다운로드 완료시점을 알지 못하여 불편해졌다.

# 요구사항 정리
* 엑셀 다운완료시 알람
* 주문발생, 클레임발생, 1:1문의, 상품승인, 상품문의, FDS, 어드민 모바일 푸시[FCM]등의 알람 요구사항을 고려해야한다.
* 알람의 대상은 몰별 알람, 운영자별 알람, 전체, 파트너 등으로 분리 발송을 고려해야한다.
* dooray stream와 같이 클라이언트가 접속하고 있지 않아도 영속성을 지닌 storage에 timeline으로 구성될수 있도록 한다.

# 전제조건
* EDA기반으로 구현 - 이벤트 발생시 message broker를 이용하여 실시간으로 client로 메세지를 전달할수 있어야한다.
* 각 도메인에서는 메세지를 발행하는 행위를 제외하고 notification과의 커플링이 되어서는 안된다.
* MSA 서버들과의 서로 독립적이여야 한다.
* async-nonblocking으로 동작하여야 해야 하며 reactive 프로그래밍으로 구현해야 한다.
* spring cloud gateway를 통하지 않고 별도의 도메인으로 서버를 구성한다.
* Notification서버는 메세지 전송 대상을 구분하기 위해서 admin token, admin 정보가 필요하며 local캐쉬를 적극 활용한다. (결국 서버 통신 필요?)

# 사전조사
## polling
<img src="https://www.concurrency.com/getattachment/baa462c8-ac8e-49af-91ce-bc37e4234f31/http.png.aspx?lang=en-US" width="80%"/>

n초에 한번씩 주기적으로 서버로 호출

### 장점 
- 구현이 쉽다.
  
### 단점 
- 폴링 주기가 짧으면 성능 부담 (tcp 3way handshake)
- 폴링 주기가 길면 실시간이 아님  
- 서버 응답이 변하지 않으면 리소스 낭비

물론 http 1.1에서는 keep-alive가 default이며 모든 요청이 connectionless는 아님
<img src="https://media.vlpt.us/images/yvvyoon/post/5fe0bb75-4085-40c7-a9df-b401267a0484/image.png" width="80%"/>

## long polling
<img src="https://miro.medium.com/max/1400/0*Jij7E34MBWAq28KJ" width="80%" />

polling과 통신방법은 같으며 요청을 받은 서버는 메세지를 전달할수 있을 때까지(timeout될때까지) 무한정 커넥션을 종료하지 않고 메세지를 전달할수 있을때 응답을 준다.

### 장점
- 항상 연결이 되어있어서 polling보다는 리소스 절약
- 거의 실시간
- 브라우저 호환성

### 단점
- 데이터가 수시로 바뀔경우 polling보다 많은 리소스 낭비(호출 주기가 없기 때문에 응답이 오면 다시 서버로 요청)

## Server-Sent Events (SSE)
<img src="https://miro.medium.com/max/1400/1*zG7Jyeq02JRAN6Wz6gs15g.png" width="80%" />

* 클라이언트는 메세지를 구독하고 서버는 이벤트 발생시 클라이언트로 푸시한다. (데이터는 서버 -> 클라 단방향)
* response header의 content-type: text/event-stream이 추가되어야 하며 response body의 format은 아래와 같다.

### response payload
```
# multiline data
data: first line\n
data: second line\n\n
```

```
# JSON Data
data: {\n
data: "msg": "hello world",\n
data: "id": 12345\n
data: }\n\n
```
JSON Serialize가 복잡해 보이지만 Spring의 Content Negotiation Strategies을 믿어보자.

### 장점
- 통신 리소스 절약
- 전통적인 HTTP를 이용하며 구현 심플

### 단점
- XHLHttpRequest가 아닌 EventSource web api로 구현
- 단방향 통신

https://developer.mozilla.org/ko/docs/Web/API/EventSource/EventSource

## Websocket
<img src="https://kouzie.github.io/assets/springboot/springboot_websocket3.png" width="80%" />

* 2011년 표준화되었으며 양방향 통신 
* http://가 아닌 ws://프로토콜을 사용하며 80(ws://), 443(wss://)포트 사용
* handshake는 위와 동일하게 http통신으로 이루어지며 handshake수립후에는 ws로 양방향 통신한다

### 장점
- 웹표준이며 SSE보다 브라우저 호환성이 더 좋다.
- 양방향이다.

### 단점
- 서버와 클라이언트 모두 receive와 send를 구현해야 하며 전통적인 웹개발 방식으로는 구현이 어렵다.

https://developer.mozilla.org/ko/docs/Web/API/WebSocket

## reactive streams
### sync, async
<img src="https://media.vlpt.us/images/cyongchoi/post/9cf3d12b-cb66-4efe-a53d-2eb2236e3886/1_60iugGBHMF7PPSn-fdQrHQ.png" width="80%" />

### 데이터 처리 변화
<img src="https://engineering.linecorp.com/wp-content/uploads/2020/02/reactivestreams1-1.png" width="80%" />

#### Traditional Data Processing
- 브라우저는 서버에게 http 요청을 하고 서버 응답 데이터 전체를 받아서 메모리에 적재 후 callback실행
- 서버는 다른 서버 또는 DB로 데이터를 요청하고 응답을 받아 메모리에 적재 후 처리

**순간적으로 다량의 요청 또는 한번의 요청에 대량의 데이터를 처리할 경우 OOM또는 GC가 발생할 확률이 높다.**

#### Stream Processing
* 스트리밍 처리 방식을 작용하면 적은 하드웨어 리소스로 많은 데이터를 처리할 수 있다.

Part of the answer is the need for a non-blocking web stack to handle concurrency with a small number of threads and scale with fewer hardware resources.

### reactive streams API
```java
public interface Publisher<T> {
   public void subscribe(Subscriber<? super T> s);
}
 
public interface Subscriber<T> {
   public void onSubscribe(Subscription s);
   public void onNext(T t);
   public void onError(Throwable t);
   public void onComplete();
}
 
public interface Subscription {
   public void request(long n);
   public void cancel();
}
```

<img src="https://engineering.linecorp.com/wp-content/uploads/2020/02/reactivestreams1-10.png" width="80%" />

#### async와 reactive streams 차이
subscriber는 publisher가 push해주는 데이터나 이벤트들의 흐름을 제어할 수 있도록 backpressure를 제공한다.  
<img src="https://hyungjoon6876.github.io/jlog/assets/img/20180724/backpressure.png" width="80%" />

callback을 이용한 비동기 프로그래밍(observer pattern)으로도 흐름제어는 가능하지만 multi-thread, 이벤트 처리중의 예외처리등 고려해야할 사항이 굉장히 많고 복잡하다.

#### 패러다임의 변화
sync nonblocking -> async nonblocking -> reactive

- javascript - promise, await-async(ES 2017)
- php - await, async
- java - jdk8 CompletableFuture, jdk9 flow API(= reactive streams)
- kotlin, c#, c++ 17 - coroutine
- go - goroutine
- reactiveX - rxjs, rsjava, rsAndroid, rxSwift, rxGo, rxPy, rxPHP

#### 우리는?
reactive programming 권장

- 서버개발 : reactor
- FE팀: rxJS
- 모바일개발팀 : rxSwift, rxAndroid

# 구현
## 기술스택
- language : kotlin
- reactor, coroutine, reactive kafka, webflux functional endpoint
- message broker : kafka
- framework : springboot 2.4.4
- client : ES6, vanillaJS, EventSource, Websocket
- container : docker-compose (zookeeper, kafka, kafka-ui)
- build tool : gradle kotlin DSL
- dockerizing : spring boot maven plugin (bootBuildImage)
- nhn public cloud : http://133.186.247.62:8080/sse.html

## 핵심 키워드 : hot
### cold publisher
Mono/Flux는 subscribe하지 않으면 아무일도 일어나지 않는다. 
대부분 webflux에서 subscribe를 대신 처리하고 있다.

### hot publisher
subscribe 하기전 데이터를 생성할 수 있고 N개의 subscriber가 존재할수 있다.
Notification 서버가 최초 기동할때 hot publisher를 메모리에 올려두고 SSE, Websocket 요청시 hot publisher를 구독하여 서버 이벤트를 클라이언트로 푸시할수 있다.

### Sinks
reactor 3.4.0 이전에는 FluxProcessor, MonoProcessor, UnicastProcessor등을 이용하였으나 deprecated

```
The Sinks categories are:
1. many().multicast(): a sink that will transmit only newly pushed data to its subscribers, honoring their backpressure (newly pushed as in "after the subscriber’s subscription").
2. many().unicast(): same as above, with the twist that data pushed before the first subscriber registers is buffered.
3. many().replay(): a sink that will replay a specified history size of pushed data to new subscribers then continue pushing new data live.
4. one(): a sink that will play a single element to its subscribers
5. empty(): a sink that will play a terminal signal only to its subscribers (error or complete), but can still be viewed as a Mono<T> (notice the generic type <T>).
```

<a href="https://projectreactor.io/docs/core/release/reference/#processors">Processors and Sinks</a>

### Sinks.Many<T>.multicast().onBackpressureBuffer()
<img src="https://projectreactor.io/docs/core/release/api/reactor/core/publisher/doc-files/marbles/sinkWarmup.svg" width="80%" />

[Sinks.many().multicast().onBackpressureBuffer()](https://projectreactor.io/docs/core/release/api/reactor/core/publisher/Sinks.MulticastSpec.html)

## 시스템 구성
![구성도](./diagram.png)

## 코드 설명
### local
1. git clone https://github.com/chk386/notification
1. docker-compose up
    1. localhost:8081 : kafka UI
    1. localhost:9092 : broker
    1. localhost:2181 : zookeeper
1. gradle boot run (또는 idea에서 NotificationsApplication.kt 실행

### nhn cloud
1. dockerizing
```shell
gradle bootBuildImage --imageName=shopby-notification
docker login # docker hub 계정입력
docker tag shopby-notification ${본인의 dockerhub ID}/notification
docker image push ${본인의 dockerhub ID}/notification
```

2. docker 
```shell
# ssh 서버접속
docker-compose -f docker-compose-nhncloud.yml up
docker run -d -e "SPRING_PROFILES_ACTIVE=cloud" -p 8080:8080 chk386/notification 

docker exec -it kafka /bin/bsh

# 토픽생성
/bin/kafka-topics --create --topic BACKOFFICE-NOTIFICATIONS --bootstrap-server localhost:9092
# 토픽정보
/bin/kafka-topics --describe --topic BACKOFFICE-NOTIFICATIONS --bootstrap-server localhost:9092
# procude
/bin/kafka-console-producer --topic BACKOFFICE-NOTIFICATIONS --bootstrap-server localhost:9092
# consumer
/bin/kafka-console-consumer --topic BACKOFFICE-NOTIFICATIONS --bootstrap-server localhost:9092
# 토픽 삭제
/bin/kafka-topics --delete --topic BACKOFFICE-NOTIFICATIONS --bootstrap-server localhost:9092
```

3. 데모 페이지
   1. http://133.186.247.62:8080/sse.html
   1. http://133.186.247.62:8080/websocket.html
   


   






