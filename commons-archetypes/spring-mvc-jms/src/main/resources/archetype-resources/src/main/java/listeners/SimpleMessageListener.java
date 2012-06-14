package ${package}.listeners;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

public class SimpleMessageListener implements MessageListener {

   private static final Logger logger = LoggerFactory.getLogger(SimpleMessageListener.class);

   public void onMessage(final Message message) {
      if(message instanceof TextMessage) {
         final TextMessage textMessage = (TextMessage)message;
         try {
            logger.info(textMessage.getText());
         }
         catch(final JMSException e) {
            logger.error("Error handling", e);
         }
      }
      else {
         logger.warn("Unexpected message {}", message);
      }
   }

}
