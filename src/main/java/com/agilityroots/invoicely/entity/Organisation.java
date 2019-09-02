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

  @NotEmpty(message = "Cannot save an Organisation without registered name")
  @Column(length = 100, nullable = false)
  private String name;

  @NaturalId
  @Column(nullable = false, updatable = false, length = 8, unique = true)
  private String organisationId;

  @OneToMany(fetch = FetchType.LAZY)
  @JoinTable(name = "org_branches", joinColumns = @JoinColumn(name = "owner_id", referencedColumnName = "id"), inverseJoinColumns = @JoinColumn(name = "branch_id", referencedColumnName = "id"))
  private Set<Branch> branches;

  @Override
  public int hashCode() {
    return (Objects.hash(name) * 79);
  }

}
