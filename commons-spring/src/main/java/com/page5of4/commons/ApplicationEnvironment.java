package com.page5of4.commons;

import java.util.regex.Pattern;

import javax.servlet.ServletContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.page5of4.commons.beans.config.AllPurposeContextLoaderListener;

/**
 * Provides access to the name of the environment that the application is running under. That name is usually set via an
 * ss.env system property (specified via -D on the command line, for example) It can also be set via a web.xml parameter
 * to allow for hard-coding the environment in, for example, release WARs. To use the web.xml feature you'll need to use
 * the {@link AllPurposeContextLoaderListener}.
 * 
 * @author jlewallen
 */
public class ApplicationEnvironment {

   public static final String[] KEYS = new String[] { "ss.env", "app.env" };
   public static final String DEFAULT_ENVIRONMENT = "dev";
   public static final String DEVELOPMENT_PATTERN = "dev.*";
   public static final String STAGING_PATTERN = "staging.*";
   public static final String TESTING_PATTERN = "testing.*";
   public static final String PRODUCTION_PATTERN = "production.*";

   private static final Logger logger = LoggerFactory.getLogger(ApplicationEnvironment.class);

   public static String getEnvironmentName() {
      for(String key : KEYS) {
         String value = System.getProperty(key);
         if(value != null) {
            return value;
         }
      }
      return DEFAULT_ENVIRONMENT;
   }

   public static boolean isProduction() {
      return getEnvironmentName().matches(PRODUCTION_PATTERN);
   }

   public static boolean isDevelopment() {
      return getEnvironmentName().matches(DEVELOPMENT_PATTERN);
   }

   public static boolean isStaging() {
      return getEnvironmentName().matches(STAGING_PATTERN);
   }

   public static boolean isTesting() {
      return getEnvironmentName().matches(TESTING_PATTERN);
   }

   public static void configure(ServletContext servletContext) {
      for(String key : KEYS) {
         String overrideKey = String.format("%s.override", key);
         String value = servletContext.getInitParameter(overrideKey);
         if(value == null) {
            continue;
         }
         if(!Pattern.matches("[a-z0-9-]+", value)) {
            logger.info("Ignoring {} = {}", overrideKey, value);
            return;
         }
         logger.info("Overriding {} = {}", key, value);
         System.setProperty(key, value);
         return;
      }
   }

}
