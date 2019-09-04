/**
 *
 */
package com.agilityroots.invoicely.repository;

import com.agilityroots.invoicely.entity.Contact;
import org.springframework.data.domain.Example;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import javax.persistence.LockModeType;
import java.util.Optional;

/**
 * @author anadi
 *
 */
public interface ContactRepository extends JpaRepository<Contact, Long> {

  @Lock(LockModeType.OPTIMISTIC)
  @Override
  <S extends Contact> Optional<S> findOne(Example<S> example);

  @Lock(LockModeType.OPTIMISTIC)
  @Override
  Optional<Contact> findById(Long aLong);

  @Lock(LockModeType.OPTIMISTIC)
  @Override
  <S extends Contact> S save(S entity);

  @Lock(LockModeType.OPTIMISTIC)
  @Override
  <S extends Contact> S saveAndFlush(S entity);
}
