package com.discussion.ryu.config;

import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.springframework.stereotype.Component;
import org.springframework.ui.ModelMap;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.context.request.WebRequestInterceptor;

@Component
public class QueryCountingInterceptor implements WebRequestInterceptor {

    private final SessionFactory sessionFactory;
    private static final ThreadLocal<Long> queryCountHolder = new ThreadLocal<>();

    public QueryCountingInterceptor(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Override
    public void preHandle(WebRequest request) throws Exception {
        Statistics stats = sessionFactory.getStatistics();
        stats.clear();
        queryCountHolder.set(stats.getPrepareStatementCount());
    }

    @Override
    public void postHandle(WebRequest request, ModelMap model) throws Exception {
        // 아무것도 하지 않음
    }

    @Override
    public void afterCompletion(WebRequest request, Exception ex) throws Exception {
        Statistics stats = sessionFactory.getStatistics();
        long queryCount = stats.getPrepareStatementCount();
        long startCount = queryCountHolder.get() != null ? queryCountHolder.get() : 0;
        long actualQueryCount = queryCount - startCount;

        System.out.println("📊 API 쿼리 수: " + actualQueryCount);
        queryCountHolder.remove();
    }
}
