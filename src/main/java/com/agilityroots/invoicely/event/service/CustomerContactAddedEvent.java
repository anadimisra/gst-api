/**
 * 
 */
package com.agilityroots.invoicely.event.service;

import org.springframework.context.ApplicationEvent;

import com.agilityroots.invoicely.entity.Contact;

import lombok.Getter;
import lombok.ToString;

/**
 * @author anadi
 *
 */
@ToString
public class CustomerContactAddedEvent extends ApplicationEvent {

  private static final long serialVersionUID = 8668055952501304660L;

  @Getter
  private final String toEmail;

  @Getter
  private final String name;

  public CustomerContactAddedEvent(Contact source) {
    super(source);
    this.toEmail = source.getEmail();
    this.name = source.getName();
  }

}
