package com.app.zware.Filters;

import com.app.zware.Service.UserService;
import com.app.zware.Util.JwtUtil;
import jakarta.annotation.Nonnull;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.web.filter.OncePerRequestFilter;

public class JwtFilter extends OncePerRequestFilter {


  private final UserService userService;

  public JwtFilter(UserService userService) {
    this.userService = userService;
  }

  @Override
  protected void doFilterInternal(
      HttpServletRequest request,
      @Nonnull HttpServletResponse response,
      @Nonnull FilterChain filterChain) throws ServletException, IOException {

    // Bypass JWT authentication for /login
    final String requestURI = request.getRequestURI();
    if (requestURI.equals("/api/auth/login") ||
        requestURI.startsWith("/imageproducts") ||
        requestURI.startsWith("/avatars")) {
      filterChain.doFilter(request, response);
      return;
    }

    //GET Jwt token
    String jwt = JwtUtil.getJwtToken(request);
    if (jwt == null) {
      response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
      return;
    }

    //CHECK Email in database
    String jwtEmail = JwtUtil.extractEmail(jwt);
    if (userService.getByEmail(jwtEmail) == null) {
      response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
      return;
    }

    //validate Token
    if (!JwtUtil.validateToken(jwt)) {
      response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
      return;
    }

    filterChain.doFilter(request, response);
  }
}
