/**
 *  22-Oct-2018 Organisation.java
 *  data-api
 *  Copyright 2018 Agility Roots Private Limited. All Rights Reserved
 */
package com.agilityroots.invoicely.entity;

import java.util.List;
import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.OneToMany;
import javax.validation.constraints.NotEmpty;

import org.hibernate.annotations.NaturalId;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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

  @NotEmpty(message = "Cannot save customer without registered name")
  @Column(unique = true, length = 50, nullable = false)
  @NaturalId
  private String name;

  @NotEmpty(message = "PAN is mandatory while adding a new Customer")
  @Column(unique = true, length = 10, updatable = false, nullable = false)
  private String pan;

  @Column(length = 11)
  private String vatTin;

  @OneToMany(fetch = FetchType.LAZY)
  @JoinTable(name = "org_branches", joinColumns = @JoinColumn(name = "owner_id", referencedColumnName = "id"), inverseJoinColumns = @JoinColumn(name = "branch_id", referencedColumnName = "id"))
  private List<Branch> branches;

  @Override
  public int hashCode() {
    return (Objects.hash(name) * 79);
  }

  @OneToMany()

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
