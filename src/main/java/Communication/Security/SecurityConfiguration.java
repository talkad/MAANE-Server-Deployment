package Communication.Security;

import Communication.Filter.CustomAuthenticationFilter;
import Communication.Filter.CustomAuthorizationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;


@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {

    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final UserDetailsService userDetailsService;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userDetailsService).passwordEncoder(bCryptPasswordEncoder);
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        CustomAuthenticationFilter authenticationFilter = new CustomAuthenticationFilter(authenticationManagerBean());
        authenticationFilter.setFilterProcessesUrl("/user/login");

        http.csrf().disable();
        http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);
// todo: allow to view pages based on permission
// todo: add to response the reason for forbidden

//        http.authorizeRequests().antMatchers("/user/login/**", "/user/refreshToken/**").permitAll();
//        http.authorizeRequests().antMatchers("/survey/createSurvey", "/survey/addRule", "/survey/removeRule",
//                "/survey/detectFault", "/survey/getSurveys").hasAnyAuthority("SUPERVISOR", "SYSTEM_MANAGER");
//        http.authorizeRequests().antMatchers(GET, "/user/getUserInfo").hasAnyAuthority("SUPERVISOR",
//                "REGISTERED", "INSTRUCTOR", "COORDINATOR", "GENERAL_SUPERVISOR", "SYSTEM_MANAGER");
//        http.authorizeRequests().anyRequest().authenticated();
        http.authorizeHttpRequests().anyRequest().permitAll();

        http.exceptionHandling().authenticationEntryPoint(jwtAuthenticationEntryPoint);

        http.addFilter(authenticationFilter);
        http.addFilterBefore(new CustomAuthorizationFilter(), UsernamePasswordAuthenticationFilter.class);
    }

    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception{
        return super.authenticationManagerBean();
    }

}
