/**
 * 
 */
package com.agilityroots.invoicely.service;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.sendgrid.Content;
import com.sendgrid.Email;
import com.sendgrid.Mail;
import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.SendGrid;

import lombok.extern.slf4j.Slf4j;

/**
 * @author anadi
 *
 */
@Slf4j
@Service
@Profile("production")
public class MailService implements NotificationService {

  @Autowired
  private SendGrid sendGridClient;

  @Async(value = "mailExecutor")
  public void sendEmail(String from, String to, String subject, String content, String replyTo) {
    Content mailContent = new Content("text/html", content);
    Mail mail = new Mail(new Email(from), subject, new Email(to), mailContent);
    if (null != replyTo)
      mail.setReplyTo(new Email(replyTo));
    Request request = new Request();
    try {
      request.setMethod(Method.POST);
      request.setEndpoint("mail/send");
      request.setBody(mail.build());
      this.sendGridClient.api(request);
      log.debug("Sent message from: {} to: {}", from, to);
    } catch (IOException ex) {
      log.error("Cannot send email to:{} due to error", to, ex);
    }
  }

}
