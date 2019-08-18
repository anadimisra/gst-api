/**
 *
 */
package com.agilityroots.invoicely.feature.steps;

import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.ArrayList;
import java.util.List;

/**
 * @author anadi
 *
 */
public class RestResponsePage<T> extends PageImpl<T> {

  private static final long serialVersionUID = -7546808863090250919L;

  public RestResponsePage(List<T> content, Pageable pageable, long total) {
    super(content, pageable, total);
  }

  public RestResponsePage(List<T> content) {
    super(content);
  }

  public RestResponsePage() {
    super(new ArrayList<T>());
  }
}
