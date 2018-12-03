package com.agilityroots.invoicely.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.data.rest.core.annotation.RestResource;

import com.agilityroots.invoicely.entity.AuditableEntity;

@RepositoryRestResource
public interface DataApiRepository<T extends AuditableEntity, ID> extends JpaRepository<T, ID> {
	
	@RestResource(exported = false)
	@Override
	void delete(T entity);
	
	@RestResource(exported = false)
	@Override
	void deleteInBatch(Iterable<T> entities);
	
	@RestResource(exported = false)
	@Override
	void deleteAll();
	
	@RestResource(exported = false)
	@Override
	void deleteAll(Iterable<? extends T> entities);
	
	@RestResource(exported = false)
	@Override
	void deleteAllInBatch();
	
	@RestResource(exported = false)
	@Override
	void deleteById(ID id);

}
