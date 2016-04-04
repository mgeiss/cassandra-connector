/*
 * Copyright 2016 Markus Geiss.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package lab.mage.spring.cassandra.connector.core;

import com.datastax.driver.mapping.Mapper;

import java.util.Optional;

public final class TenantAwareEntityTemplate {

    private final TenantAwareCassandraMapperProvider tenantAwareCassandraMapperProvider;

    public TenantAwareEntityTemplate(final TenantAwareCassandraMapperProvider tenantAwareCassandraMapperProvider) {
        super();
        this.tenantAwareCassandraMapperProvider = tenantAwareCassandraMapperProvider;
    }

    @SuppressWarnings("unchecked")
    public <T> void save(final T entity) {
        final Mapper<T> mapper = this.tenantAwareCassandraMapperProvider.getMapper((Class<T>) entity.getClass());
        mapper.save(entity);
    }

    public <T> Optional<T> findById(final Class<T> type, final Object... identifier) {
        final Mapper<T> mapper = this.tenantAwareCassandraMapperProvider.getMapper(type);
        return Optional.ofNullable(mapper.get(identifier));
    }

    @SuppressWarnings("unchecked")
    public <T> void delete(final T entity) {
        final Mapper<T> mapper = this.tenantAwareCassandraMapperProvider.getMapper((Class<T>) entity.getClass());
        mapper.delete(entity);
    }
}
