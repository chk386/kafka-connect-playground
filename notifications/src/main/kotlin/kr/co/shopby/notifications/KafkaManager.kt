package kr.co.shopby.notifications

import org.springframework.boot.autoconfigure.kafka.KafkaProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.core.reactive.ReactiveKafkaConsumerTemplate
import org.springframework.kafka.core.reactive.ReactiveKafkaProducerTemplate
import reactor.kafka.receiver.ReceiverOptions
import reactor.kafka.sender.SenderOptions

@Configuration
class KafkaManager(private val kafkaProperties: KafkaProperties) {

  @Bean
  fun produce(): ReactiveKafkaProducerTemplate<String, String> {
    return ReactiveKafkaProducerTemplate(SenderOptions.create(kafkaProperties.buildProducerProperties()))
  }

  init {

    val subscription = ReceiverOptions.create<String, String>(kafkaProperties.buildConsumerProperties())
      .subscription(listOf("BACKOFFICE-NOTIFICATIONS"))

    val consumerTemplate =
      ReactiveKafkaConsumerTemplate(subscription)

    consumerTemplate.receive().subscribe {
      it.receiverOffset().acknowledge()
      println("구독중 : ${it.value()}")
    }
  }
}