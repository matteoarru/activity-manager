package eu.cepol.eventoperations.api;

import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/** Test fixture authentication. This configuration is absent outside the synthetic profile. */
@Configuration
@Profile("synthetic")
class SyntheticSecurityConfiguration {
  @Bean
  PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  UserDetailsService fixtureUsers(PasswordEncoder encoder) {
    var password = "demo-password";
    return new InMemoryUserDetailsManager(
        fixture("am.alex", "Alex Morgan", "AM", password, encoder),
        fixture("po.petra", "Petra Olsen", "PO", password, encoder),
        fixture("ia.ines", "Ines Andrade", "IA", password, encoder),
        fixture("ao.aaron", "Aaron Okafor", "AO", password, encoder),
        fixture("finance.fran", "Fran Novak", "FINANCE", password, encoder),
        fixture("provider.pavel", "Pavel Rossi", "PROVIDER", password, encoder),
        fixture("cnu.clara", "Clara Dubois", "CNU", password, encoder),
        fixture("cnu.niko", "Niko Horvat", "CNU", password, encoder),
        fixture("attendee.aria", "Aria Khan", "ATTENDEE", password, encoder),
        fixture("admin.taylor", "Taylor Reed", "TECHNICAL_ADMIN", password, encoder));
  }
  @Bean
  AuthenticationManager authenticationManager(UserDetailsService users, PasswordEncoder encoder) {
    var provider = new DaoAuthenticationProvider(users);
    provider.setPasswordEncoder(encoder);
    return new ProviderManager(List.of(provider));
  }

  @Bean
  SecurityFilterChain syntheticSecurity(HttpSecurity http) throws Exception {
    return http
        .csrf(csrf -> csrf.ignoringRequestMatchers(
            "/api/auth/login", "/api/auth/logout", "/api/v1/**"))
        .authorizeHttpRequests(auth -> auth
            .requestMatchers("/api/health", "/api/auth/login").permitAll()
            .anyRequest().authenticated())
        .formLogin(form -> form.disable())
        .httpBasic(basic -> basic.disable())
        .build();
  }

  private static org.springframework.security.core.userdetails.UserDetails fixture(
      String username,
      String displayName,
      String role,
      String password,
      PasswordEncoder encoder) {
    return User.withUsername(username)
        .password(encoder.encode(password))
        .roles(role)
        .authorities(
            "ROLE_" + role,
            "DISPLAY_NAME_" + displayName.replace(' ', '_'))
        .build();
  }
}
