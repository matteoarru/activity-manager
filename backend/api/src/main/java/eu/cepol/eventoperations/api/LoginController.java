package eu.cepol.eventoperations.api;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
class LoginController {
  private final AuthenticationManager authenticationManager;
  private final SecurityContextRepository contexts = new HttpSessionSecurityContextRepository();

  LoginController(AuthenticationManager authenticationManager) {
    this.authenticationManager = authenticationManager;
  }

  @PostMapping("/api/auth/login")
  Map<String, Object> login(
      @Valid @RequestBody LoginRequest request,
      HttpServletRequest servletRequest,
      HttpServletResponse servletResponse) {
    Authentication auth = authenticate(request);
    var context = SecurityContextHolder.createEmptyContext();
    context.setAuthentication(auth);
    contexts.saveContext(context, servletRequest, servletResponse);
    return profile(auth);
  }

  @GetMapping("/api/me")
  Map<String, Object> me(Authentication auth) {
    return profile(auth);
  }

  @PostMapping("/api/auth/logout")
  Map<String, String> logout(
      HttpServletRequest request,
      HttpServletResponse response,
      Authentication authentication) {
    new SecurityContextLogoutHandler().logout(request, response, authentication);
    return Map.of("status", "SIGNED_OUT");
  }

  private Authentication authenticate(LoginRequest request) {
    return authenticationManager.authenticate(
        UsernamePasswordAuthenticationToken.unauthenticated(
            request.username(), request.password()));
  }

  private Map<String, Object> profile(Authentication auth) {
    return Map.of(
        "username", auth.getName(),
        "roles",
            auth.getAuthorities().stream()
                .map(a -> a.getAuthority())
                .filter(a -> a.startsWith("ROLE_"))
                .toList());
  }

  @ExceptionHandler(org.springframework.security.core.AuthenticationException.class)
  @ResponseStatus(HttpStatus.UNAUTHORIZED)
  Map<String, String> invalidCredentials() {
    return Map.of("error", "Invalid username or password");
  }

  record LoginRequest(@NotBlank String username, @NotBlank String password) {}
}
