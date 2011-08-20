package com.page5of4.commons.beans.config;

import java.io.File;
import java.net.MalformedURLException;

import javax.naming.NamingException;

import org.springframework.jndi.JndiObjectFactoryBean;

/**
 * Class used to bypass the <code>NamingException</code> that is thrown if the JNDI name is not specified. The reason
 * that you may want to bypass this is if you are using this object factory to create an object and you are depending on
 * while autowiring it. There may be a case where you don't want to throw an exception, but rather fail silently and
 * provide a backup mechanism. This class allows you to do that if you specify
 * <code>ignoreNameNotFoundException = true</code>.
 * 
 * @author Joshua Keith
 */
public class SpringJndiObjectFactoryBean extends JndiObjectFactoryBean {

   boolean ignoreNameNotFoundException;

   /**
    * Wrap the super method and swallow the exception if necessary.
    * 
    * @see JndiObjectFactoryBean#afterPropertiesSet()
    */
   @Override
   public void afterPropertiesSet() throws IllegalArgumentException, NamingException {
      try {
         super.afterPropertiesSet();
      }
      catch(NamingException e) {
         if(ignoreNameNotFoundException) {
            logger.warn("Name not found, but you are ignoring the exception. Set property to false if you would like to break on this exception.");
         }
         else {
            throw e;
         }
      }
   }

   /**
    * If there is nothing to return from the factory, create a <code>URL</code> with an empty string.
    * 
    * @return a <code>URL</code> that cannot be null.
    */
   @Override
   public Object getObject() {
      Object object = super.getObject();
      if(object == null) {
         try {
            object = new File("/").toURI().toURL();
         }
         catch(MalformedURLException e) {
            logger.error("Problem finding the file to use in the URL...", e);
         }
      }
      return object;
   }

   /**
    * @param ignoreNameNotFoundException
    */
   public void setIgnoreNameNotFoundException(boolean ignoreNameNotFoundException) {
      this.ignoreNameNotFoundException = ignoreNameNotFoundException;
   }
}
