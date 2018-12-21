/*
 * Copyright 2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.agilityroots.invoicely.resource.assembler;

import org.springframework.hateoas.ResourceSupport;
import org.springframework.hateoas.Resources;

/**
 * Analogous to {@link ResourcesAssembler} but for resource collections.
 * 
 * @author Greg Turnquist
 */
public interface ResourcesAssembler<T, D extends ResourceSupport> {

  /**
   * Converts all given entities into resources and wraps the collection as a
   * resource as well.
   *
   * @see ResourcesAssembler#toResource(Object)
   * @param entities must not be {@literal null}.
   * @return {@link Resources} containing {@link Resource} of {@code T}.
   */
  Resources<D> toResources(Iterable<? extends T> entities);

}