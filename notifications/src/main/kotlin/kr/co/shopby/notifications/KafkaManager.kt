package kr.co.shopby.notifications

import org.springframework.boot.autoconfigure.kafka.KafkaProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.core.reactive.ReactiveKafkaConsumerTemplate
import org.springframework.kafka.core.reactive.ReactiveKafkaProducerTemplate
import reactor.core.publisher.Sinks
import reactor.kafka.receiver.ReceiverOptions
import reactor.kafka.sender.SenderOptions

@Configuration
class KafkaManager(private val kafkaProperties: KafkaProperties) {

  @Bean
  fun multicaster(): Sinks.Many<String> {
    val multicaster = Sinks.many()
      .multicast()
      .onBackpressureBuffer<String>()

    multicaster.asFlux()
      .subscribe { println("consumer -> Sinks.many().multicast() => $it") }

    consume(multicaster)

    return multicaster
  }

  fun consume(multicaster: Sinks.Many<String>) {
    val subscription = ReceiverOptions
      .create<String, String>(kafkaProperties.buildConsumerProperties())
      .subscription(listOf(Topic.NOTIFICATIONS))

    ReactiveKafkaConsumerTemplate(subscription)
      .receive()
      .subscribe {
        it.receiverOffset().acknowledge()
        val msg = if (it.value().contains(":")) {
          it.value()
        } else {
          "all:${it.value()}"
        }

        multicaster.tryEmitNext(msg)
      }
  }

  @Bean
  fun produce(): ReactiveKafkaProducerTemplate<String, String> {
    return ReactiveKafkaProducerTemplate(
      SenderOptions.create(
        kafkaProperties.buildProducerProperties()
      )
    )
  }
}