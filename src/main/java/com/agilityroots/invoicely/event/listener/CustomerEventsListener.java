/**
 * 
 */
package com.agilityroots.invoicely.event.listener;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

import com.agilityroots.invoicely.event.service.CustomerContactAddedEvent;
import com.agilityroots.invoicely.service.NotificationService;
import com.agilityroots.invoicely.service.TemplateProcessorService;

import lombok.extern.slf4j.Slf4j;

/**
 * @author anadi
 *
 */
@Slf4j
@Component
public class CustomerEventsListener {

  @Value("${finance.email:finance@company.com}")
  private String fromEmail;

  @Value("${no-reply.email:no-reply@company.com}")
  private String replyToEmail;

  @Autowired
  private NotificationService mailService;

  @Autowired
  private TemplateProcessorService templateProcrssor;

  // @Async
  @TransactionalEventListener
  public void handleCustomerContactAddedEvent(CustomerContactAddedEvent event) {
    log.debug("Processing event {}", event);
    String content = templateProcrssor.processTemplate(event.getName());
    mailService.sendEmail(fromEmail, event.toString(), "You have been added to the GST Billing System", content,
        replyToEmail);
  }

}
