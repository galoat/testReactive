package com.example.reservationclient

import org.springframework.boot.ApplicationRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder
import org.springframework.cloud.gateway.route.builder.routes
import org.springframework.context.ApplicationContextInitializer
import org.springframework.context.annotation.Bean
import org.springframework.context.support.GenericApplicationContext
import org.springframework.context.support.beans
import org.springframework.web.reactive.function.server.RequestPredicates.path
import org.springframework.web.reactive.function.server.RouterFunctions.route

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


}

fun main(args: Array<String>) {
    runApplication<ReservationClientApplication>(*args) {
        addInitializers(
                beans {
                    bean {
                        val rlb = ref<RouteLocatorBuilder>()
                        rlb.routes {
                            route {
                                path("/proxy").and().host("*.gw.sc")
                                uri("https://www.google.fr/?gws_rd=ssl")
                            }
                        }
                    }
                })
    }
}
