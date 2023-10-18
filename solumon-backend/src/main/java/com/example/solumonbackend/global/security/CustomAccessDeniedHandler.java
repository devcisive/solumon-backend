package com.example.solumonbackend.global.security;

import com.example.solumonbackend.global.exception.CustomSecurityException;
import com.example.solumonbackend.global.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Slf4j
@Component
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

  @Override
  public void handle(HttpServletRequest request, HttpServletResponse response,
                     AccessDeniedException accessDeniedException) {
    log.error("[Access is Denied] 접근 불가");
    throw new CustomSecurityException(ErrorCode.INVALID_TOKEN);
  }

}
