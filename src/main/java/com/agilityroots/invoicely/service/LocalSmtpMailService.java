/**
 * 
 */
package com.agilityroots.invoicely.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

/**
 * @author anadi
 *
 */
@Slf4j
@Service
@Profile({ "feature_tests", "dev" })
public class LocalSmtpMailService implements NotificationService {

  @Autowired
  private JavaMailSender mailSender;

  public LocalSmtpMailService(JavaMailSender mailSender) {
    this.mailSender = mailSender;
  }

  @Override
  public void sendEmail(String from, String to, String subject, String content, String replyTo) {
    MimeMessagePreparator messagePreparator = mimeMessage -> {
      MimeMessageHelper messageHelper = new MimeMessageHelper(mimeMessage);
      messageHelper.setFrom(from);
      messageHelper.setTo(to);
      messageHelper.setSubject("Sample mail subject");
      messageHelper.setText(content);
    };
    try {
      mailSender.send(messagePreparator);
    } catch (MailException e) {
      log.error("Cannot send email due to error: {}", e.getMessage(), e);
    }

  }

}
