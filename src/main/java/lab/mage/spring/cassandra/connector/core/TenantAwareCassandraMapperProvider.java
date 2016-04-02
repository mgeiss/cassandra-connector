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

import com.datastax.driver.core.Session;
import com.datastax.driver.mapping.Mapper;
import com.datastax.driver.mapping.MappingManager;
import lab.mage.spring.cassandra.connector.util.OptionProvider;
import lab.mage.spring.cassandra.connector.util.TenantContextHolder;
import org.slf4j.Logger;
import org.springframework.core.env.Environment;
import org.springframework.util.Assert;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.concurrent.locks.StampedLock;

public class TenantAwareCassandraMapperProvider {

    private static final class CacheKey<T> {

        private final String identifier;
        private final Class<T> type;

        public CacheKey(@Nonnull final String identifier, @Nonnull final Class<T> type) {
            super();
            Assert.notNull(identifier);
            Assert.notNull(type);
            this.identifier = identifier;
            this.type = type;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            CacheKey<?> cacheKey = (CacheKey<?>) o;

            if (!identifier.equals(cacheKey.identifier)) return false;
            return type.equals(cacheKey.type);

        }

        @Override
        public int hashCode() {
            int result = identifier.hashCode();
            result = 31 * result + type.hashCode();
            return result;
        }
    }

    private final Environment env;
    private final Logger logger;
    private final CassandraSessionProvider cassandraSessionProvider;
    private final HashMap<String, MappingManager> managerCache;
    private final HashMap<CacheKey<?>, Mapper<?>> mapperCache;

    private final StampedLock lock = new StampedLock();

    public TenantAwareCassandraMapperProvider(final Environment env, final Logger logger, final CassandraSessionProvider cassandraSessionProvider) {
        super();
        this.env = env;
        this.logger = logger;
        this.cassandraSessionProvider = cassandraSessionProvider;
        this.managerCache = new HashMap<>();
        this.mapperCache = new HashMap<>();
    }

    @Nonnull
    public <T> Mapper<T> getMapper(@Nonnull final Class<T> type) {
        Assert.notNull(type, "A type must be given!");
        if (TenantContextHolder.getIdentifier().isPresent()) {
            final String identifier = TenantContextHolder.getIdentifier().get();
            return this.getMapper(identifier, type);
        } else {
            throw new IllegalArgumentException("Could not find tenant identifier, make sure you set an identifier using TenantContextHolder.");
        }
    }

    @SuppressWarnings("unchecked")
    @Nonnull
    public <T> Mapper<T> getMapper(@Nonnull final String identifier, @Nonnull final Class<T> type) {
        Assert.notNull(identifier, "A tenant identifier must be given!");
        Assert.notNull(type, "A type must be given!");

        final CacheKey<T> cacheKey = new CacheKey<>(identifier, type);

        if (!this.mapperCache.containsKey(cacheKey)) {
            final long lockStamp = this.lock.writeLock();
            try {
                if (!this.mapperCache.containsKey(cacheKey)) {
                    this.logger.info("Create new mapper for tenant [" + identifier + "] and type [" + type.getSimpleName() + "].");
                    if (!this.managerCache.containsKey(identifier)) {
                        final Session session = this.cassandraSessionProvider.getTenantSession(identifier);
                        this.managerCache.put(identifier, new MappingManager(session));
                    }
                    final Mapper<T> typedMapper = this.managerCache.get(identifier).mapper(type);
                    typedMapper.setDefaultDeleteOptions(OptionProvider.deleteConsistencyLevel(this.env));
                    typedMapper.setDefaultGetOptions(OptionProvider.readConsistencyLevel(this.env));
                    typedMapper.setDefaultSaveOptions(OptionProvider.writeConsistencyLevel(this.env));
                    this.mapperCache.put(cacheKey, typedMapper);
                }
            } finally {
                this.lock.unlockWrite(lockStamp);
            }
        }

        return (Mapper<T>) this.mapperCache.get(cacheKey);
    }
}
