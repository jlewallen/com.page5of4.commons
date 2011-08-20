package com.page5of4.commons.beans.config;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.ConfigurableWebApplicationContext;
import org.springframework.web.context.ContextLoaderListener;

import com.page5of4.commons.ApplicationEnvironment;

/**
 * A {@link ContextLoaderListener} that provides two additional features:
 * <ul>
 * <li>Allows the overriding of {@link ApplicationEnvironment} via a context parameter.</li>
 * <li>Loads per-environment Spring configuration files from META-INF/spring/<ss.env>/*.xml.</li>
 * </ul>
 * 
 * @author jlewallen
 */
public class AllPurposeContextLoaderListener extends ContextLoaderListener {

   private static final Logger logger = LoggerFactory.getLogger(AllPurposeContextLoaderListener.class);

   @Override
   protected void customizeContext(ServletContext servletContext, ConfigurableWebApplicationContext applicationContext) {
      ApplicationEnvironment.configure(servletContext);
      List<String> locations = new ArrayList<String>();
      for(String location : applicationContext.getConfigLocations()) {
         locations.add(location);
      }

      String name = ApplicationEnvironment.getEnvironmentName();
      locations.add("classpath*:META-INF/spring/" + name + "/*.xml");

      for(String location : locations) {
         logger.info("Config: {}", location);
      }

      applicationContext.setConfigLocations(locations.toArray(new String[0]));
      super.customizeContext(servletContext, applicationContext);
   }
}
