/**
 * 
 */
package com.agilityroots.invoicely.service;

/**
 * @author anadi
 *
 */
public interface NotificationService {

  void sendEmail(String from, String to, String subject, String content, String replyTo);
}
