package io.coti.zerospend;

import io.coti.zerospend.filters.AdminFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.Arrays;
import java.util.HashSet;

@Configuration
@ComponentScan(
        basePackages = {"io.coti.zerospend", "io.coti.basenode"}
)
@EnableScheduling
@PropertySource("classpath:application.properties")
public class AppConfig {
    @Value("${whitelist.ips}")
    private String whitelistIps;

    @Bean
    public FilterRegistrationBean<AdminFilter> filterRegistrationBean() {
        FilterRegistrationBean<AdminFilter> registrationBean = new FilterRegistrationBean();
        AdminFilter adminFilter = new AdminFilter();
        adminFilter.setWhiteListIps(new HashSet<>(Arrays.asList(whitelistIps.split(","))));
        registrationBean.setFilter(adminFilter);
        registrationBean.addUrlPatterns("/admin/*");
        registrationBean.setOrder(1);
        return registrationBean;
    }
}