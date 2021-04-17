package kr.co.shopby.notifications

import kr.co.shopby.notifications.configuration.Topic
import kr.co.shopby.notifications.handler.SseHandler
import kr.co.shopby.notifications.handler.WebsocketHandler
import org.springframework.boot.ApplicationRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Profile
import org.springframework.kafka.core.reactive.ReactiveKafkaProducerTemplate
import org.springframework.messaging.support.GenericMessage
import org.springframework.web.reactive.HandlerMapping
import org.springframework.web.reactive.config.CorsRegistry
import org.springframework.web.reactive.config.EnableWebFlux
import org.springframework.web.reactive.config.ResourceHandlerRegistry
import org.springframework.web.reactive.config.WebFluxConfigurer
import org.springframework.web.reactive.function.server.RouterFunction
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.coRouter
import org.springframework.web.reactive.handler.SimpleUrlHandlerMapping
import org.springframework.web.reactive.socket.server.support.WebSocketHandlerAdapter

fun main(args: Array<String>) {
  runApplication<NotificationsApplication>(*args)
}

@SpringBootApplication
@EnableWebFlux
class NotificationsApplication: WebFluxConfigurer {

  @Bean
  @Profile("default")
  fun run(producer: ReactiveKafkaProducerTemplate<String, String>): ApplicationRunner {
    return ApplicationRunner {
      while (true) {
        println("메세지를 입력해주세요.")
        producer.send(Topic.NOTIFICATIONS, GenericMessage(readLine()!!)).subscribe()
      }
    }
  }

  @Bean
  fun coRoute(sseHandler: SseHandler): RouterFunction<ServerResponse> {
    return coRouter {
      GET("/notifications", sseHandler::httpStream)
      GET("/produce", sseHandler::produce)
    }
  }

  override fun addCorsMappings(registry: CorsRegistry) {
    registry.addMapping("/**")
      .allowedOrigins("*")
      .allowedMethods("GET", "POST", "PUT", "DELETE")
  }

  override fun addResourceHandlers(registry: ResourceHandlerRegistry) {
    registry.addResourceHandler("**")
      .addResourceLocations("classpath:/static/")
  }

  /**
   * @see <a href="https://docs.spring.io/spring-framework/docs/current/reference/html/web-reactive.html#webflux-websocket-server-handler">참고</a>
   */
  @Bean
  fun handlerMapping(websocketHandler: WebsocketHandler): HandlerMapping {
    val map = mapOf("/ws" to websocketHandler)
    val order = -1 // before annotated controllers

    return SimpleUrlHandlerMapping(map, order)
  }

  @Bean
  fun handlerAdapter() = WebSocketHandlerAdapter();
}
