package urlshortener2015.heatwave.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.social.security.SocialUserDetailsService;
import org.springframework.social.security.SpringSocialConfigurer;

import urlshortener2015.heatwave.config.SimpleSocialUsersDetailService;

import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
            .authorizeRequests()
                	.antMatchers("/**,/**/**").permitAll()
            .and()
            	.formLogin()
                .loginPage("/login")
                .permitAll()
            .and()
                .logout()
                	.permitAll()
            .and()
            	.apply(new SpringSocialConfigurer())
            .and()
                .csrf()
                	.disable();
    }
    
	@Bean
	public SocialUserDetailsService socialUsersDetailService() {
		return new SimpleSocialUsersDetailService(userDetailsService());
	}
}