package it.sdcc.projectsonlinebackend.configurations;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfiguration {

    @Value("${client.url}")
    private String CLIENT;

    /**Questo bean disabilita la protezione contro le csrf, in quanto si accettano richieste da un'unica origine. Per di più, si impone una connessione stateless e si accetta qualunque richiesta verso qualunque controller*/
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf().disable()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and().authorizeHttpRequests()

                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                .requestMatchers("/user/**").permitAll()
                .requestMatchers("/project/**").permitAll()
                .requestMatchers("/file/**").permitAll()
                .requestMatchers("/comment/**").permitAll();

        return http.build();
    }//filterChain

    /**Con questo bean, definiamo quali sono le origini da cui si accettano richieste, gli header ammessi e le tipologie di richieste HTTP che il server può accettare*/
    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration configuration = new CorsConfiguration();

        configuration.addAllowedOrigin(CLIENT);

        configuration.addAllowedHeader("*");

        configuration.addAllowedMethod("OPTIONS");
        configuration.addAllowedMethod("GET");
        configuration.addAllowedMethod("DELETE");
        configuration.addAllowedMethod("POST");
        configuration.addAllowedMethod("PUT");

        source.registerCorsConfiguration("/**", configuration);
        return new CorsFilter(source);
    }//corsFilter
}//SecurityConfiguration

