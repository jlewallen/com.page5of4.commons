package ${package}.listeners;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.ExceptionListener;
import javax.jms.JMSException;

public class SimpleExceptionListener implements ExceptionListener {

   private static final Logger logger = LoggerFactory.getLogger(SimpleExceptionListener.class);

   public void onException(final JMSException e) {
      logger.error("JMS", e);
   }

}
