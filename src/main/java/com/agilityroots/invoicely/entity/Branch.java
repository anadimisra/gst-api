/*
  19-Oct-2018 Branch.java
  data-api
  Copyright 2018 Agility Roots Private Limited. All Rights Reserved
 */
package com.agilityroots.invoicely.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.NaturalId;
import org.hibernate.annotations.Where;
import org.springframework.hateoas.core.Relation;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;
import java.io.Serializable;
import java.util.Objects;

/**
 * @author anadi
 */
@Entity
@Getter
@Setter
@ToString
@DynamicInsert
@NoArgsConstructor
@Where(clause = "DELETED = 0")
@Relation(collectionRelation = "branches")
@Table(indexes = {@Index(name = "branch_name_index", columnList = "branchName"),
    @Index(name = "branch_pincode_index", columnList = "pincode")})
public class Branch extends AuditableEntity implements Serializable {

  private static final long serialVersionUID = -8841725432779534218L;

  @NotEmpty(message = "Cannot add a branch without branch name.")
  @Column(length = 25, nullable = false)
  private String branchName;

  @NaturalId
  @NotEmpty(message = "Cannot add a branch without GSTIN")
  @Column(length = 15, unique = true, nullable = false)
  private String gstin;

  @Column(length = 11)
  private String vat;

  @Column(length = 11)
  private String tin;

  private Address address;

  @Column(nullable = false, updatable = false)
  @ColumnDefault("false")
  private Boolean sez;

  @OneToOne(fetch = FetchType.LAZY)
  @JoinTable(name = "branch_contact", joinColumns = @JoinColumn(name = "branch_id", referencedColumnName = "id"), inverseJoinColumns = @JoinColumn(name = "contact_id", referencedColumnName = "id"))
  private Contact contact;

  @ManyToOne
  @JoinTable(name = "org_branches", joinColumns = @JoinColumn(name = "branch_id", referencedColumnName = "id"), inverseJoinColumns = @JoinColumn(name = "owner_id", referencedColumnName = "id"))
  private Organisation owner;

  @JsonIgnore
  @Column(nullable = false)
  @ColumnDefault("0")
  private Integer deleted;

  @Transient
  public Boolean isSez() {
    return this.getSez();
  }

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
      return other.getGstin() == null;
    } else return gstin.equals(other.getGstin());
  }

}
