// package to.charlie.foodPlanner.config;
//
// import org.springframework.context.annotation.Bean;
// import org.springframework.security.config.annotation.web.builders.HttpSecurity;
// import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
// import
// org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
// import org.springframework.security.config.http.SessionCreationPolicy;
// import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
//
// @EnableWebSecurity
// public class WebSecurityConfiguration extends WebSecurityConfigurerAdapter {
//  @Bean
//  public BCryptPasswordEncoder passwordEncoder() {
//    return new BCryptPasswordEncoder();
//  }
//
//  private static final String[] AUTH_WHITELIST = {
//      "/v2/api-docs",
//      "/swagger-resources",
//      "/swagger-resources/**",
//      "/configuration/ui",
//      "/configuration/security",
//      "/swagger-ui.html",
//      "/webjars/**"
//  };
//
//  @Override
//  protected void configure(HttpSecurity httpSecurity) throws Exception {
//    httpSecurity
//        .httpBasic().disable()
//        .cors().and()
//        .csrf().disable()
//        .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
//        .and()
//        .authorizeRequests()
//        .antMatchers(AUTH_WHITELIST).permitAll()
//        .anyRequest().permitAll();
//  }
// }
