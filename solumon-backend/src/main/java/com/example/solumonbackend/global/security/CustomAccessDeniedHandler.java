package com.example.solumonbackend.global.security;

import com.example.solumonbackend.global.exception.CustomSecurityException;
import com.example.solumonbackend.global.exception.ErrorCode;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class CustomAccessDeniedHandler implements
    org.springframework.security.web.access.AccessDeniedHandler {

  @Override
  public void handle(HttpServletRequest request, HttpServletResponse response,
      AccessDeniedException accessDeniedException) throws IOException, ServletException {
    log.error("[Access is Denied] 접근 불가");
    throw new CustomSecurityException(ErrorCode.INVALID_TOKEN);
  }
}
