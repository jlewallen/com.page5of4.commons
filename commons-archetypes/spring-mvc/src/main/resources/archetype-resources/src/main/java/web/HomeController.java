package ${package}.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class HomeController {

   private static final Logger logger = LoggerFactory.getLogger(HomeController.class);
  
   @RequestMapping(value = "/")
   public ModelAndView index() {
      logger.info("Home.index");
      return new ModelAndView("home");
   }

}
