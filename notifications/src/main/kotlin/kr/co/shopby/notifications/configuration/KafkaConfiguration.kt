package kr.co.shopby.notifications.configuration

import org.springframework.boot.autoconfigure.kafka.KafkaProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.annotation.EnableKafka
import org.springframework.kafka.core.reactive.ReactiveKafkaConsumerTemplate
import org.springframework.kafka.core.reactive.ReactiveKafkaProducerTemplate
import reactor.core.publisher.Sinks
import reactor.kafka.receiver.ReceiverOptions
import reactor.kafka.receiver.ReceiverRecord
import reactor.kafka.sender.SenderOptions

@Configuration
@EnableKafka
class KafkaConfiguration(private val kafkaProperties: KafkaProperties) {

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

  @Bean
  fun produce(): ReactiveKafkaProducerTemplate<String, String> {
    return ReactiveKafkaProducerTemplate(
      SenderOptions.create(
        kafkaProperties.buildProducerProperties()
      )
    )
  }

  private fun consume(multicaster: Sinks.Many<String>) {
    ReactiveKafkaConsumerTemplate(
      ReceiverOptions
        .create<String, String>(kafkaProperties.buildConsumerProperties())
        .subscription(listOf(Topic.NOTIFICATIONS))
    )
      .receive()
      .doOnNext { it.receiverOffset().acknowledge() }
      .subscribe { multicaster.tryEmitNext(extractMessage(it)) }
  }

  private fun extractMessage(it: ReceiverRecord<String, String>) =
    if (it.value().contains(":")) {
      it.value()
    } else {
      "all:${it.value()}"
    }
}

object Topic {
  const val NOTIFICATIONS = "BACKOFFICE-NOTIFICATIONS"
}