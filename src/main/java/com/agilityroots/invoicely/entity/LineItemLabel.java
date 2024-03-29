/**
 * 23-Oct-2018 LineItemLabel.java
 * data-api
 * Copyright 2018 Agility Roots Private Limited. All Rights Reserved
 */
package com.agilityroots.invoicely.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * @author anadi
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
public class LineItemLabel extends AuditableEntity implements Serializable {

  private static final long serialVersionUID = 1L;

  @Column(nullable = false, length = 7)
  private String labelType;

  @ElementCollection
  @CollectionTable(name = "line_item_labels", joinColumns = @JoinColumn(name = "line_item_label_id"))
  @Column(name = "labels")
  private Set<String> labels = new HashSet<>();
}
