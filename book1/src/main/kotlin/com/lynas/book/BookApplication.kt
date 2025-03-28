package com.lynas.book

import org.springframework.amqp.core.BindingBuilder
import org.springframework.amqp.core.Queue
import org.springframework.amqp.core.TopicExchange
import org.springframework.amqp.rabbit.connection.ConnectionFactory
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.stereotype.Service
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@SpringBootApplication
class BookApplication

fun main(args: Array<String>) {
	runApplication<BookApplication>(*args)
}

const val QUEUE_NAME = "QUEUE_BOOK"
const val TOPIC_EXCHANGE_NAME = "TOPIC_EXCHANGE_BOOK"
const val ROUTE_KEY_NAME = "ROUTE_KEY_BOOK"

@Configuration
class RabbitMqConfig {
	@Bean
	fun queue(): Queue = Queue(QUEUE_NAME)
	@Bean
	fun exchange(): TopicExchange = TopicExchange(TOPIC_EXCHANGE_NAME)
	@Bean
	fun binding() = BindingBuilder.bind(queue()).to(exchange()).with(ROUTE_KEY_NAME)
	@Bean
	fun template(connectionFactory: ConnectionFactory) = RabbitTemplate(connectionFactory).
			apply {
				messageConverter = Jackson2JsonMessageConverter()
			}
}

data class Book(val title: String)

@Service
class Producer(val template: RabbitTemplate){
	fun sendMessage(book: Book){
		template.convertAndSend(TOPIC_EXCHANGE_NAME,ROUTE_KEY_NAME, book)
	}
}

@RestController
class BookController(val producer: Producer) {

	@PostMapping("/send")
	fun sendBook(@RequestBody book: Book){
		producer.sendMessage(book)
	}
}