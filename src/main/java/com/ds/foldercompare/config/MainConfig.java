/* 
 * Copyright (C) 2020 Dejan Stojanovic <dejanstojanovich@gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.ds.foldercompare.config;

import org.springframework.context.annotation.Configuration;

/**
 * Main configuration class. Thanks to Spring autoconfiguration, it is empty and only used 
 * to check the inserted webjars libraries if needed
 * @author Dejan Stojanovic
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

}
