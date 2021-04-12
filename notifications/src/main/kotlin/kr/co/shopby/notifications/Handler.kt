package kr.co.shopby.notifications

import kotlinx.coroutines.reactive.asFlow
import org.springframework.http.MediaType
import org.springframework.kafka.core.reactive.ReactiveKafkaProducerTemplate
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.bodyAndAwait
import reactor.core.publisher.Flux
import reactor.kotlin.core.publisher.toFlux
import java.time.Duration

@Component
class Handler(private val producer: ReactiveKafkaProducerTemplate<String, String>) {
  suspend fun httpStream(request: ServerRequest): ServerResponse {

    val publisher = Flux.range(1, 3).delayElements(Duration.ofSeconds(1))
      .flatMap {
         producer.send("BACKOFFICE-NOTIFICATIONS", it.toString())
           .toFlux()
          .flatMap { _ -> Flux.just(it) }
      }

    return ServerResponse.ok().contentType(MediaType.TEXT_EVENT_STREAM)
      .bodyAndAwait(publisher.asFlow())
  }
}