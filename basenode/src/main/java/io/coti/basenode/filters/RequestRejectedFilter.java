package io.coti.basenode.filters;

import io.coti.basenode.http.CustomHttpServletResponse;
import io.coti.basenode.http.Response;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpStatus;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.web.firewall.RequestRejectedException;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.GenericFilterBean;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static io.coti.basenode.http.BaseNodeHttpStringConstants.STATUS_ERROR;

@Component
@Slf4j
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RequestRejectedFilter extends GenericFilterBean {

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        try {
            filterChain.doFilter(servletRequest, servletResponse);
        } catch (RequestRejectedException e) {
            CustomHttpServletResponse response = new CustomHttpServletResponse((HttpServletResponse) servletResponse);
            response.printResponse(new Response(e.getMessage(), STATUS_ERROR), HttpStatus.SC_BAD_REQUEST);
        }
    }

}
