package com.lynas.order

import org.springframework.amqp.core.BindingBuilder
import org.springframework.amqp.core.Queue
import org.springframework.amqp.core.TopicExchange
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory
import org.springframework.amqp.rabbit.connection.ConnectionFactory
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.stereotype.Service

@SpringBootApplication
class OrderApplication

fun main(args: Array<String>) {
	runApplication<OrderApplication>(*args)
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
//	@Bean
//	fun template(connectionFactory: ConnectionFactory) = RabbitTemplate(connectionFactory).
//	apply {
//		messageConverter = Jackson2JsonMessageConverter()
//	}

	@Bean
	fun listenerContainerFactory (connectionFactory: ConnectionFactory) =
		SimpleRabbitListenerContainerFactory().apply {
			this.setConnectionFactory(connectionFactory)
			this.setMessageConverter(Jackson2JsonMessageConverter())
		}
}

@Service
class Consumer {
	@RabbitListener(queues = [QUEUE_NAME], containerFactory ="listenerContainerFactory" )
	fun consume(book: Book) {
		println("Consume book: ${book.title}")
	}
}

data class Book(val title: String)