/**
 *
 */
package com.agilityroots.invoicely.event.service;

import com.agilityroots.invoicely.entity.Contact;
import lombok.Getter;
import lombok.ToString;
import org.springframework.context.ApplicationEvent;

/**
 * @author anadi
 *
 */
@ToString
public class ContactAddedEvent extends ApplicationEvent {

  private static final long serialVersionUID = 8668055952501304660L;

  @Getter
  private final String toEmail;

  @Getter
  private final String name;

  public ContactAddedEvent(Contact source) {
    super(source);
    this.toEmail = source.getEmail();
    this.name = source.getName();
  }

}
