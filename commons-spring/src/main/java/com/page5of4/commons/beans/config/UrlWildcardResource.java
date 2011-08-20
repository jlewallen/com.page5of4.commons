package com.page5of4.commons.beans.config;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.UrlResource;

/**
 * @author Joshua Keith
 */
public class UrlWildcardResource extends UrlResource {

   private static Logger logger = LoggerFactory.getLogger(UrlWildcardResource.class);

   private final URL url;

   public UrlWildcardResource(URL url) {
      super(url);
      this.url = url;
   }

   public List<UrlResource> extractUrlResources() {
      List<UrlResource> resources = new ArrayList<UrlResource>();
      if(url != null) {
         String filename = url.getFile();
         int indexOfWildcard = filename.indexOf('*');
         if(indexOfWildcard > 0) {
            String directoryName = filename.substring(0, indexOfWildcard);
            String extension = filename.substring(indexOfWildcard + 1);
            File directory = new File(directoryName);
            if(directory.exists()) {
               for(File file : directory.listFiles()) {
                  if(file.getName().contains(extension)) {
                     logger.info("Checking {}", url);
                     try {
                        resources.add(new UrlResource(file.toURI().toURL()));
                     }
                     catch(MalformedURLException e) {
                        logger.error("Error loading URL: " + url, e);
                     }
                  }
               }
            }
         }
         else {
            resources.add(new UrlResource(url));
         }
      }
      if(resources.isEmpty()) {
         logger.info("No files found for  {}", url);
      }
      for(UrlResource url : resources) {
         try {
            if(url.getFile().isFile()) {
               logger.info("Reading: {}", url);
            }
            else {
               logger.info("Ignoring: {}", url);
            }
         }
         catch(IOException e) {
            logger.error("Error checking " + url, e);
         }
      }
      return resources;
   }
}
