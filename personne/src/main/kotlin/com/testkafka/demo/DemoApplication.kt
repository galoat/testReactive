package com.testkafka.demo

import org.reactivestreams.Publisher
import org.reactivestreams.Subscriber
import org.reactivestreams.Subscription
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cloud.stream.annotation.EnableBinding
import org.springframework.cloud.stream.annotation.Input
import org.springframework.cloud.stream.annotation.StreamListener
import org.springframework.cloud.stream.messaging.Sink
import org.springframework.context.annotation.Bean
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import org.springframework.stereotype.Component
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.reactive.function.server.*
import reactor.core.publisher.Flux

@EnableBinding(Sink::class)
@SpringBootApplication
class DemoApplication{
    @Autowired
    lateinit var reservationRepository : ReservationRepository
    @Bean
    fun routes(@Autowired reservationRepository: ReservationRepository) :RouterFunction<ServerResponse>{
        return RouterFunctions.route(RequestPredicates.GET("/reservations"), HandlerFunction<ServerResponse> { ServerResponse.ok().body(reservationRepository.findAll()) })
    }
    @StreamListener
    fun process(@Input(Sink.INPUT)incomingdata: Flux<String>){
        incomingdata.map{ Reservation(null, it)}.flatMap { this.reservationRepository.save(it) }.subscribe { System.out.println("saved " +it.reservationName) }
    }


}
    fun main(args: Array<String>) {
        runApplication<DemoApplication>(*args)

    }



/*
@RestController
class ReservationRestController(@Autowired val reservationRepository: ReservationRepository){

    @GetMapping("/reservations")
     fun reservationFlux():Flux<Reservation>{
        return this.reservationRepository.findAll();

    }

}*/


@Component
class DataWriteddezdezdzer(@Autowired val reservationRepository: ReservationRepository) : ApplicationRunner {

    override fun run(args: ApplicationArguments?) {
        reservationRepository.deleteAll().thenMany(Flux.just("test1", "test2"))
                .map { name -> Reservation(null, name) }
                .flatMap { this.reservationRepository.save(it) }
                .thenMany(this.reservationRepository.findAll())
                .subscribe(System.out::println)
    }
}

    interface ReservationRepository : ReactiveMongoRepository<Reservation, String>


@Document
    data class Reservation(@Id var id: String?, val reservationName: String)