package kr.co.shopby.notifications.handler

import kr.co.shopby.notifications.configuration.Topic
import org.springframework.kafka.core.reactive.ReactiveKafkaProducerTemplate
import org.springframework.stereotype.Component
import org.springframework.web.reactive.socket.WebSocketHandler
import org.springframework.web.reactive.socket.WebSocketSession
import org.springframework.web.util.UriComponentsBuilder
import reactor.core.publisher.Mono
import reactor.core.publisher.Sinks
import java.net.URI

@Component
class WebsocketHandler(
  private val producer: ReactiveKafkaProducerTemplate<String, String>,
  private val multicaster: Sinks.Many<String>
) : WebSocketHandler {
  override fun handle(session: WebSocketSession): Mono<Void> {
    val input = session
      .receive()
      .doOnNext {
        producer.send(Topic.NOTIFICATIONS, it.payloadAsText).subscribe()
      }.then()

    val output = session
      .send(multicaster
        .asFlux()
        .filter { it.contains("all:") || it.startsWith(getId(session.handshakeInfo.uri)) }
        .map(session::textMessage)
      )

    return Mono.zip(input, output).then()
  }

  private fun getId(uri: URI): String {
    return UriComponentsBuilder
      .fromUri(uri)
      .build()
      .queryParams["id"].orEmpty()[0]
  }
}