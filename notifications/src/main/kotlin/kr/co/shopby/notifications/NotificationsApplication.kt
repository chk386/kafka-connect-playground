package kr.co.shopby.notifications

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.kafka.core.reactive.ReactiveKafkaProducerTemplate
import org.springframework.messaging.support.GenericMessage

@SpringBootApplication
class NotificationsApplication

fun main(args: Array<String>) {
  runApplication<NotificationsApplication>(*args).run {
    println("보낼 메세지 입력하세요!")
    val producer = this.getBean(ReactiveKafkaProducerTemplate::class.java)

    while (true) {
      producer.send(Topic.NOTIFICATIONS, GenericMessage(readLine()!!)).subscribe()
    }
  }
}
