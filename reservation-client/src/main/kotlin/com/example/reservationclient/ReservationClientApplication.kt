package com.example.reservationclient

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

import org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder
import org.springframework.cloud.gateway.route.builder.filters
import org.springframework.cloud.gateway.route.builder.routes
import org.springframework.context.annotation.Bean
import org.springframework.context.support.beans
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.core.userdetails.MapReactiveUserDetailsService
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetails

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
