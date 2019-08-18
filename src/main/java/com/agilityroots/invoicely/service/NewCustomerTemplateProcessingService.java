/**
 *
 */
package com.agilityroots.invoicely.service;

import com.sendgrid.SendGrid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

/**
 * @author anadi
 *
 */
@Service
public class NewCustomerTemplateProcessingService implements TemplateProcessorService {

  private TemplateEngine templateEngine;

  @Autowired
  public NewCustomerTemplateProcessingService(TemplateEngine templateEngine, SendGrid sendGridClient) {
    this.templateEngine = templateEngine;
  }

  /*
   * (non-Javadoc)
   *
   * @see
   * com.agilityroots.invoicely.service.TemplateProcessorService#processTemplate(
   * java.lang.String[])
   */
  @Override
  public String processTemplate(String... variables) {
    Context context = new Context();
    context.setVariable("name", variables[0]);
    return templateEngine.process("newCustomerMail", context);
  }

}
