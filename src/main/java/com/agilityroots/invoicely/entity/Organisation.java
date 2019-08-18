/**
 * 22-Oct-2018 Organisation.java
 * data-api
 * Copyright 2018 Agility Roots Private Limited. All Rights Reserved
 */
package com.agilityroots.invoicely.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.NaturalId;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * @author anadi
 *
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class Organisation extends AuditableEntity {

  @NaturalId
  @Column(unique = true, length = 8, updatable = false, nullable = false)
  private String domain;

  @NotEmpty(message = "Cannot save an Organisation without registered name")
  @Column(unique = true, length = 50, nullable = false)
  private String name;

  @Column(length = 11)
  private String vatTin;

  @OneToMany(fetch = FetchType.LAZY)
  @JoinTable(name = "org_branches", joinColumns = @JoinColumn(name = "owner_id", referencedColumnName = "id"), inverseJoinColumns = @JoinColumn(name = "branch_id", referencedColumnName = "id"))
  private Set<Branch> branches;

  @OneToMany(fetch = FetchType.LAZY)
  @JoinTable(name = "org_invoices", joinColumns = @JoinColumn(name = "owner_id", referencedColumnName = "id"), inverseJoinColumns = @JoinColumn(name = "invoice_id", referencedColumnName = "id"))
  private Set<Invoice> invoices;

  @Override
  public int hashCode() {
    return (Objects.hash(name) * 79);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    Organisation other = (Organisation) obj;
    return Objects.equals(name, other.getName());
  }

}
