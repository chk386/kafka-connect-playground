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
class Router(private val hander: Handler): WebFluxConfigurer {

  @Bean
  fun coRoute(): RouterFunction<ServerResponse> {
    return coRouter {
      GET("/notifications", hander::httpStream)
    }
  }

  override fun addCorsMappings(registry: CorsRegistry) {
    registry.addMapping("/**")
      .allowedOrigins("*")
      .allowedMethods("GET", "POST", "PUT", "DELETE")
//      .allowCredentials(true).maxAge(3600)
  }

  override fun addResourceHandlers(registry: ResourceHandlerRegistry) {
    registry.addResourceHandler("**")
      .addResourceLocations("classpath:/static/")
  }
}