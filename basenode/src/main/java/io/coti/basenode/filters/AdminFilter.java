package io.coti.basenode.filters;

import io.coti.basenode.http.CustomHttpServletResponse;
import io.coti.basenode.http.Response;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpStatus;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashSet;

import static io.coti.basenode.http.BaseNodeHttpStringConstants.STATUS_ERROR;
import static io.coti.basenode.http.BaseNodeHttpStringConstants.UNAUTHORIZED_IP_ACCESS;

@Slf4j
public class AdminFilter implements Filter {

    private static HashSet<String> whiteListIps;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        log.info("{} is initialized", getClass().getSimpleName());
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) servletRequest;
        CustomHttpServletResponse response = new CustomHttpServletResponse((HttpServletResponse) servletResponse);

        if (!whiteListIps.contains(getClientIp(request))) {
            response.printResponse(new Response(UNAUTHORIZED_IP_ACCESS, STATUS_ERROR), HttpStatus.SC_UNAUTHORIZED);
        } else {
            filterChain.doFilter(request, response);
        }

    }

    private static String getClientIp(HttpServletRequest request) {

        String remoteAddr = "";

        if (request != null) {
            remoteAddr = request.getHeader("X-FORWARDED-FOR");
            if (remoteAddr == null || remoteAddr.equals("")) {
                remoteAddr = request.getRemoteAddr();
            }
        }

        return remoteAddr;
    }

    public void setWhiteListIps(HashSet<String> whiteListIps) {
        this.whiteListIps = whiteListIps;
    }


    @Override
    public void destroy() {
        log.info("Destroying admin filter");
    }
}
