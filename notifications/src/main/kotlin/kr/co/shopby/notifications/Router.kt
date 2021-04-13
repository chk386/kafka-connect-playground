package kr.co.shopby.notifications

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.config.CorsRegistry
import org.springframework.web.reactive.config.EnableWebFlux
import org.springframework.web.reactive.config.ResourceHandlerRegistry
import org.springframework.web.reactive.config.WebFluxConfigurer
import org.springframework.web.reactive.function.server.RouterFunction
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.coRouter

@Configuration
@EnableWebFlux
class Router(private val handler: Handler): WebFluxConfigurer {

  @Bean
  fun coRoute(): RouterFunction<ServerResponse> {
    return coRouter {
      GET("/notifications", handler::httpStream)
      GET("/produce", handler::produce)
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
}