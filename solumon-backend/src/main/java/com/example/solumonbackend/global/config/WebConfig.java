package com.example.solumonbackend.global.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

  @Override
  public void addCorsMappings(CorsRegistry registry) {
    registry.addMapping("/**")  // 모든 경로에 대해 해당 설정 적용
        .allowedOrigins("*")  // 허용할 프론트엔드 도메인
        .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "HEAD")  // 허용할 HTTP 메서드
        .allowCredentials(true) // 모든 쿠키나 인증 토큰을 담는것을 허용함
        .allowedHeaders("*")  // 모든 헤더 허용
        .maxAge(3600);
  }

/* 위의 방법은 스프링 MVC에서 적합하고 아래의 방법은 스프링 부트에서 적합
근데 스프링 부트에서 다들 쓴다......
안되면 이 방법을 시도해 주세요...

  @Bean
  public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration config= new CorsConfiguration();

    config.setAllowCredentials(true);
    config.setAllowedOrigins(Arrays.asList(
        "http://허용할 ip",
    ));

    config.setAllowedMethods(Arrays.asList(
        HttpMethod.GET.name(),
        HttpMethod.POST.name(),
        HttpMethod.DELETE.name(),
        HttpMethod.PUT.name(),
        HttpMethod.HEAD.name(),
        HttpMethod.OPTIONS.name()
    ));

    config.setAllowedHeaders(Arrays.asList("*"));
    config.setExposedHeaders(Arrays.asList("*"));

    UrlBasedCorsConfigurationSource source= new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**",config);
    return source;
  }

 */

}
