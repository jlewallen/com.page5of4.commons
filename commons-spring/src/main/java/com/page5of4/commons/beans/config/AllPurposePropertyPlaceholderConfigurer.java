package com.page5of4.commons.beans.config;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;

import com.page5of4.commons.ApplicationEnvironment;

/**
 * This property placeholder configurer allows for many additional features to Spring's standard
 * <code>PropertyPlaceholderConfigurer</code>. Some of the additional features include:
 * <ul>
 * <li>Using JNDI to specify properties file locations.</li>
 * <li>Encrypting and decrypting values that are passed from the properties file.</li>
 * <li>Specifying property file override.</li>
 * <li>Reading per-environment configuration. See {@link ApplicationEnvironment}</li>
 * </ul>
 * There are many other capabilities that can be added as needs arise surrounding the properties file management for
 * various projects.
 * 
 * @author Joshua Keith
 */
public class AllPurposePropertyPlaceholderConfigurer extends PropertyPlaceholderConfigurer {

   private ResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver();

   /**
    * 
    */
   @Override
   public void setLocations(Resource[] locations) {
      List<Resource> allResources = new ArrayList<Resource>();
      for(Resource resource : locations) {
         if(!(resource instanceof UrlWildcardResource)) {
            allResources.add(resource);
         }
      }
      try {
         String name = ApplicationEnvironment.getEnvironmentName();
         String path = "classpath*:META-INF/spring/" + name + "/*.properties";
         logger.info("Reading: " + path);
         for(Resource resource : resourcePatternResolver.getResources(path)) {
            logger.info("Found: " + resource);
            allResources.add(resource);
         }
      }
      catch(IOException error) {
         throw new RuntimeException("Error reading environment properties", error);
      }
      for(Resource resource : locations) {
         if(resource instanceof UrlWildcardResource) {
            allResources.addAll(((UrlWildcardResource)resource).extractUrlResources());
         }
      }
      super.setLocations(allResources.toArray(new Resource[allResources.size()]));
   }

   /**
    * The property can be encrypted that is being read in and the value may need to be decrypted at this phase. There
    * are several different ways that this can happen. They all revolve around convention over configuration. Either the
    * user must use the .encrypted flag in their propertyName parameter or they must include the DECRYPT() function in
    * the propertyValue parameter itself.
    * 
    * <pre>
    *    <code>property.something.encrypted=qwpoietha2345890sh380!@#(&dta99</code>
    *    
    *    or...
    *    
    *    <code>property.something=DECRYPT(qwpoietha2345890sh380!@#(&dta99)</code>
    * </pre>
    */
   @Override
   protected String convertProperty(String propertyName, String propertyValue) {
      String value = super.convertProperty(propertyName, propertyValue);
      if(propertyName.contains(".encrypted") || value != null && value.contains("DECRYPT(")) {
         Pattern pattern = Pattern.compile("DECRYPT\\((.*)\\)");
         Matcher matcher = pattern.matcher(propertyValue);
         if(matcher.matches()) {
            String encryptedValue = matcher.group();
            // value = EncryptionUtil.decrypt(encryptedValue);
         }
      }

      return value;
   }

   /**
    * Returns a {@link Properties} populated with the effective set of configuration. Those properties after all sources
    * have been read and overrides applied.
    * 
    * @return A {@link Properties} instance.
    */
   public Properties getEffectiveProperties() {
      try {
         return mergeProperties();
      }
      catch(IOException error) {
         throw new RuntimeException("Error reading environment properties", error);
      }
   }

}
