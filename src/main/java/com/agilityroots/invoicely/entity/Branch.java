/**
 *  19-Oct-2018 Branch.java
 *  data-api
 *  Copyright 2018 Agility Roots Private Limited. All Rights Reserved
 */
package com.agilityroots.invoicely.entity;

import java.io.Serializable;
import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotEmpty;

import org.hibernate.annotations.NaturalId;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * @author anadi
 *
 */
@Entity
@Getter
@Setter
@ToString
@NoArgsConstructor
@Table(indexes = { @Index(name = "branch_name_index", columnList = "branchName", unique = false) })
public class Branch extends AuditableEntity implements Serializable {

  private static final long serialVersionUID = -8841725432779534218L;

  @NotEmpty(message = "Cannot add a branch without branch name.")
  @Column(length = 25, nullable = false)
  private String branchName;

  @NotEmpty(message = "Cannot add a branch without GSTIN")
  @NaturalId
  @Column(length = 15)
  private String gstin;

  @Column(length = 11, nullable = true)
  private String vatTin;

  private Address address;

  @Column(nullable = false, updatable = false)
  private Boolean sez = Boolean.FALSE;

  public Boolean isSez() {
    return sez;
  }

  @OneToOne(fetch = FetchType.LAZY)
  @JoinTable(name = "branch_contact", joinColumns = @JoinColumn(name = "branch_id", referencedColumnName = "id"), inverseJoinColumns = @JoinColumn(name = "contact_id", referencedColumnName = "id"))
  private Contact contact;

  @ManyToOne
  @JoinTable(name = "org_branches", joinColumns = @JoinColumn(name = "branch_id", referencedColumnName = "id"), inverseJoinColumns = @JoinColumn(name = "owner_id", referencedColumnName = "id"))
  private Organisation owner;

  @Override
  public int hashCode() {
    return Objects.hashCode(gstin);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (!super.equals(obj))
      return false;
    if (getClass() != obj.getClass())
      return false;
    Branch other = (Branch) obj;
    if (gstin == null) {
      if (other.getGstin() != null)
        return false;
    } else if (!gstin.equals(other.getGstin()))
      return false;
    return true;
  }

}
