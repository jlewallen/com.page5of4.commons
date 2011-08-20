package ${package}.web;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.stereotype.Controller;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.jms.core.JmsTemplate;

@Controller
public class HomeController {

   private static final Logger logger = LoggerFactory.getLogger(HomeController.class);

   @Autowired
   protected JmsTemplate jmsTemplate;

   @Value("dev.${package}.foo")
   protected String destination;

   @RequestMapping(value = "/")
   public ModelAndView index() {
      logger.info("Home.index");
      jmsTemplate.convertAndSend(destination, "Hello: " + new Date());
      return new ModelAndView("home");
   }

}
