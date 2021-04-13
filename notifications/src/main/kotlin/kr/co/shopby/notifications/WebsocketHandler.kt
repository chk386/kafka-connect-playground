package kr.co.shopby.notifications

import org.springframework.kafka.core.reactive.ReactiveKafkaProducerTemplate
import org.springframework.stereotype.Component
import org.springframework.web.reactive.socket.WebSocketHandler
import org.springframework.web.reactive.socket.WebSocketSession
import org.springframework.web.util.UriComponentsBuilder
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.publisher.Sinks

@Component
class WebsocketHandler(
  private val producer: ReactiveKafkaProducerTemplate<String, String>,
  private val multicaster: Sinks.Many<String>
) : WebSocketHandler {
  override fun handle(session: WebSocketSession): Mono<Void> {
    val input = session.receive()
      .doOnNext {
        producer.send(Topic.NOTIFICATIONS, it.payloadAsText).subscribe()
      }.then()

    val id = UriComponentsBuilder
      .fromUri(session.handshakeInfo.uri)
      .build()
      .queryParams["id"].orEmpty()[0]

    val source: Flux<String> = multicaster
      .asFlux()
      .filter { it.contains("all:") || it.startsWith(id) }
    val output = session.send(source.map(session::textMessage))

    return Mono.zip(input, output).then()
  }
}