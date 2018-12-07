package com.example.reservationclient


import org.reactivestreams.Publisher
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cloud.client.loadbalancer.reactive.LoadBalancerExchangeFilterFunction

import org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder
import org.springframework.cloud.gateway.route.builder.filters
import org.springframework.cloud.gateway.route.builder.routes
import org.springframework.cloud.netflix.hystrix.HystrixCommands
import org.springframework.cloud.stream.annotation.EnableBinding
import org.springframework.cloud.stream.annotation.Output
import org.springframework.context.annotation.Bean
import org.springframework.context.support.beans
import org.springframework.messaging.MessageChannel
import org.springframework.messaging.support.MessageBuilder
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.core.userdetails.MapReactiveUserDetailsService
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToFlux
import org.springframework.web.reactive.function.server.*
import org.springframework.web.reactive.function.server.RequestPredicates.POST
import reactor.core.publisher.Flux
import javax.naming.spi.ResolveResult


interface  ProducerChannels{

    @Output
    fun output(): MessageChannel
}
@EnableBinding(ProducerChannels::class)
@SpringBootApplication
class ReservationClientApplication {
    /*
        @Bean
        fun gateway(rlb: RouteLocatorBuilder) =                beans {
            bean {

                rlb.routes {
                    route {
                        path("/proxy").and().host("*.gw.sc")
                        uri("https://www.google.fr/?gws_rd=ssl")
                    }
                }
            }
        }
    */
    @Bean
    fun userDetailsService(): MapReactiveUserDetailsService {
        val user = User.withDefaultPasswordEncoder()
                .username("user")
                .password("pw")
                .roles("USER")
                .build()

        return MapReactiveUserDetailsService(user)
    }


}

fun main(args: Array<String>) {
    runApplication<ReservationClientApplication>(*args) {
        addInitializers(
                beans {

                    bean{
                        WebClient.builder().filter(ref<LoadBalancerExchangeFilterFunction>()).build()
                    }
                    bean{
                        val client  = ref<WebClient>()
                        val source = ref<ProducerChannels>()
                        router{

                            POST("/reservations") {request ->
                                val send : Publisher<Boolean> = request.bodyToFlux<Reservation>()
                                        .map{ MessageBuilder.withPayload(it.reservationName!!).build()}
                                        .map{source.output().send(it)}
                                ServerResponse.ok().body(send)

                            }

                            GET("/reservations/names"){
                                val body : Publisher<String> = client
                                        .get().uri("htt://reservation-service/reservations").retrieve().bodyToFlux<Reservation>().map{it.reservationName}

                                val cb = HystrixCommands.from(body)
                                        .fallback(Flux.just("iojiojo"))
                                        .commandName("names")
                                        .eager()
                                        .build()
                                ServerResponse.ok().body(cb)
                            }

                        }
                    }

                    bean {
                        //@formatter:off
                        val serverHttpSecurity = ref<ServerHttpSecurity>()
                        serverHttpSecurity.csrf().disable().httpBasic()
                                .and()
                                .authorizeExchange()
                                .pathMatchers("/proxy").authenticated()
                                .anyExchange().permitAll()
                                .and()
                                .build()
                        //@formatter:on

                    }


                    bean {
                        RedisRateLimiter(2, 4)
                    }
                    bean {
                        val rlb = ref<RouteLocatorBuilder>()
                        rlb.routes {
                            route {
                                host("*.gw.sc").and().filters {
                                    setPath("/reservations")
                                            .requestRateLimiter {
                                                it.rateLimiter = ref<RedisRateLimiter>()
                                                // it.keyResolver = KeyResolver { Mono.just("my-count") }
                                                // it.keyResolver = PrincipalNameKeyResolver()
                                            }
                                }
                                uri("lb://reservation-service")
                            }
                        }
                    }
                })
    }
}



class Reservation(val id : String?=null, val reservationName: String? = null)