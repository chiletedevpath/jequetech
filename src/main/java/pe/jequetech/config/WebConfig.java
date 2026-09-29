package pe.jequetech.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.*;

@Configuration
public class WebConfig implements WebMvcConfigurer {
  private final SesionInterceptor sesion;

  public WebConfig(SesionInterceptor sesion) {
    this.sesion = sesion;
  }

  public void addInterceptors(InterceptorRegistry registry) {
    registry
        .addInterceptor(sesion)
        .excludePathPatterns(
            "/css/**", "/js/**", "/img/**", "/vendor/**", "/error", "/favicon.ico");
  }
}
