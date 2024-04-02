/*
 * The MIT License
 * Copyright © 2024 LastProject-ESIEE
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package fr.uge.chargepointconfiguration.security;

import fr.uge.chargepointconfiguration.user.UserRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.core.GrantedAuthorityDefaults;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Security configuration for endpoints of backend.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class WebSecurityConfig {

  @Bean
  SecurityFilterChain apiFilterChain(HttpSecurity http) throws Exception {
    http.authorizeHttpRequests(authorize -> authorize
            .requestMatchers("/api/**")
            .authenticated()
            // allow React to access its files
            .requestMatchers("/index.html", "/static/**", "/manifest.json", "/assets/**")
            .permitAll()
            .requestMatchers("/", "/logout", "/about")
            .permitAll()
            .requestMatchers("/ocpp/**")
            .permitAll()
            .anyRequest()
            .authenticated())
        .formLogin(formLogin -> formLogin
            .loginPage("/")
            .permitAll()
            .failureUrl("/?failed")
            .defaultSuccessUrl("/home", true)
            // see :
            // https://docs.spring.io/spring-security/site/docs/current/api/org/springframework/security/config/annotation/web/configurers/AbstractAuthenticationFilterConfigurer.html#defaultSuccessUrl(java.lang.String,boolean)
            .loginProcessingUrl("/authentication/login/process"))
        .csrf(AbstractHttpConfigurer::disable) // TODO csrf propre
        .httpBasic(Customizer.withDefaults());
    return http.build();
  }

  @Bean
  static GrantedAuthorityDefaults grantedAuthorityDefaults() {
    return new GrantedAuthorityDefaults(""); // none
  }

  @Bean
  static RoleHierarchy roleHierarchy() {
    var hierarchy = new RoleHierarchyImpl();
    hierarchy.setHierarchy(
        """
          ROLE_ADMINISTRATOR > ROLE_EDITOR
          ROLE_EDITOR > ROLE_VISUALIZER
          """);
    return hierarchy;
  }

  // required because using method security
  @Bean
  static MethodSecurityExpressionHandler methodSecurityExpressionHandler(
      RoleHierarchy roleHierarchy) {
    DefaultMethodSecurityExpressionHandler expressionHandler =
        new DefaultMethodSecurityExpressionHandler();
    expressionHandler.setRoleHierarchy(roleHierarchy);
    return expressionHandler;
  }

  @Bean
  UserDetailsService userDetailsService(UserRepository userRepository) {
    return new UserDetailsServiceImpl(userRepository);
  }

  /**
   * Provides the authentication service for the app to let users connects thanks to their username.
   *
   * @param userRepository The user's repository.
   * @return An authentication provider for the user.
   */
  @Bean
  public DaoAuthenticationProvider authenticationProvider(UserRepository userRepository) {
    DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
    authProvider.setUserDetailsService(userDetailsService(userRepository));
    authProvider.setPasswordEncoder(passwordEncoder());
    return authProvider;
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }
}
