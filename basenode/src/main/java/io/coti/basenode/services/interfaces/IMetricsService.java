package io.coti.basenode.services.interfaces;

import javax.servlet.http.HttpServletRequest;

public interface IMetricsService {

    void init();

    String getMetrics(HttpServletRequest request);

}
