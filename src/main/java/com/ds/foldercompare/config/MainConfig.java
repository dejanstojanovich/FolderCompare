/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ds.foldercompare.config;

import org.apache.catalina.filters.RemoteAddrFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 *
 * @author Dejan
 */
@Configuration
public class MainConfig {

//       @Bean
//    public ServletRegistrationBean servletRegistrationBean() {
//        final DefaultServlet servlet = new DefaultServlet();
//        final ServletRegistrationBean bean = new ServletRegistrationBean(servlet, "/webjars/*");
//        bean.addInitParameter("listings", "true");
//        bean.setLoadOnStartup(1);
//        return bean;
//    }
    @Bean
    public FilterRegistrationBean remoteAddressFilter() {

        FilterRegistrationBean filterRegistrationBean = new FilterRegistrationBean();
        RemoteAddrFilter filter = new RemoteAddrFilter();

        filter.setAllow("127.0.0.1");
        filter.setDenyStatus(403);

        filterRegistrationBean.setFilter(filter);
        filterRegistrationBean.addUrlPatterns("/*");

        return filterRegistrationBean;

    }
}
