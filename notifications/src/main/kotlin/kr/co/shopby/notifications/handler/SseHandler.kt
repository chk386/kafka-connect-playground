package kr.co.shopby.notifications.handler

import kotlinx.coroutines.reactive.asFlow
import kr.co.shopby.notifications.configuration.Topic
import org.springframework.http.MediaType
import org.springframework.kafka.core.reactive.ReactiveKafkaProducerTemplate
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.bodyAndAwait
import reactor.core.publisher.Sinks

@Component
class SseHandler(
  private val producer: ReactiveKafkaProducerTemplate<String, String>,
  private val multicaster: Sinks.Many<String>
) {
  suspend fun httpStream(request: ServerRequest): ServerResponse {
    return ServerResponse
      .ok()
      .contentType(MediaType.TEXT_EVENT_STREAM)
      .bodyAndAwait(multicaster
        .asFlux()
        .filter { it.contains("all:") || it.startsWith(request.queryParam("id").orElseThrow()) }
        .asFlow()
      )
  }

  suspend fun produce(request: ServerRequest): ServerResponse {
    return ServerResponse
      .ok()
      .bodyAndAwait(
        producer.send(
          Topic.NOTIFICATIONS, request.queryParam("msg").orElseThrow()
        )
        .map { it.recordMetadata().offset().toString() }
        .asFlow()
      )
  }
}