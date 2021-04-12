package kr.co.shopby.notifications

import org.apache.kafka.common.protocol.Message
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.kafka.core.reactive.ReactiveKafkaProducerTemplate
import org.springframework.messaging.support.GenericMessage

@SpringBootApplication
class NotificationsApplication

fun main(args: Array<String>) {
  runApplication<NotificationsApplication>(*args).run {
    println("값을 입력하세요!")
    val producer = this.getBean(ReactiveKafkaProducerTemplate::class.java)

    while(true) {
      producer.send("BACKOFFICE-NOTIFICATIONS", GenericMessage(readLine()!!)).subscribe()
    }
  }
}
