package io.coti.basenode.config;

import io.coti.basenode.filters.AdminFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.HashSet;

@Configuration
public class FilterConfig {
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
