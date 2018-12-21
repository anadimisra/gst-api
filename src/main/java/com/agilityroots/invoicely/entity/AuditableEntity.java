/**
 *  19-Oct-2018 AuditableEntity.java
 *  data-api
 *  Copyright 2018 Agility Roots Private Limited. All Rights Reserved
 */
package com.agilityroots.invoicely.entity;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.EntityListeners;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Version;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.hateoas.Identifiable;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.Getter;
import lombok.Setter;

/**
 * @author anadi
 */
@Setter
@Getter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
@JsonInclude(value = Include.NON_EMPTY)
public class AuditableEntity implements Identifiable<Long> {

  @Id
  @JsonIgnore
  @GeneratedValue(strategy = GenerationType.AUTO)
  private Long id;

  @Temporal(TemporalType.TIMESTAMP)
  @Column(nullable = false, updatable = false)
  @JsonIgnore
  @CreatedDate
  private Date createdAt;

  @Temporal(TemporalType.TIMESTAMP)
  @Column(nullable = false)
  @JsonIgnore
  @LastModifiedDate
  private Date updatedAt;

  @JsonIgnore
  @Version
  private Integer version;

  @Override
  public int hashCode() {
    return 71;
  }

}
