package io.coti.basenode.services.interfaces;

import javax.servlet.http.HttpServletRequest;

public interface IScraperInterface {

    void init();

    String getMetrics(HttpServletRequest request);

    void initMonitor();
}
