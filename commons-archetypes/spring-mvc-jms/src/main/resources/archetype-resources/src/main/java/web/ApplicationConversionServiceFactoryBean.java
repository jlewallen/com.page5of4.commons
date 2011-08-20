package ${package}.web;

import org.springframework.format.FormatterRegistry;
import org.springframework.format.support.FormattingConversionServiceFactoryBean;

public class ApplicationConversionServiceFactoryBean extends FormattingConversionServiceFactoryBean {

   @Override
   protected void installFormatters(FormatterRegistry registry) {
      super.installFormatters(registry);
   }

   public void installLabelConverters(FormatterRegistry registry) {

   }

   @Override
   public void afterPropertiesSet() {
      super.afterPropertiesSet();
      installLabelConverters(getObject());
   }

}
