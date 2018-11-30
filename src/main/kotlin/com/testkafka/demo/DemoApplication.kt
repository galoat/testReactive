package com.testkafka.demo

import org.reactivestreams.Publisher
import org.reactivestreams.Subscriber
import org.reactivestreams.Subscription
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux

@SpringBootApplication
class DemoApplication

fun main(args: Array<String>) {
    runApplication<DemoApplication>(*args)
}

@Component
class dataWriter(@Autowired val reservationRepository: ReservationRepository) : ApplicationRunner {

    override fun run(args: ApplicationArguments?) {
        reservationRepository.deleteAll().thenMany(Flux.just("test1", "test2"))
                .map { name -> Reservation(null, name) }
                .flatMap { this.reservationRepository.save(it) }
                .thenMany(this.reservationRepository.findAll())
                .subscribe(System.out::println)
    }
}

    interface ReservationRepository : ReactiveMongoRepository<Reservation, String> {

    }


    @Document
    data class Reservation(@Id var id: String?, val reservationName: String)