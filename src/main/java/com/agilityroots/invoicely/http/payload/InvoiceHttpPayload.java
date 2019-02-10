/**
 * 
 */
package com.agilityroots.invoicely.http.payload;

import javax.validation.constraints.NotNull;

import com.agilityroots.invoicely.entity.Invoice;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * @author anadi
 *
 */
@Getter
@Setter
@ToString
public class InvoiceHttpPayload {

  private Invoice invoice;
  
  @NotNull(message = "Cannot save invoice without Billed From Branch")
  private Long billedFrom;
  
  @NotNull(message = "Cannot save invoice without Billed To Branch")
  private Long billedTo;
  
  @NotNull(message = "Cannot save invoice without Shipped To Branch")
  private Long shippedTo;
  
}
