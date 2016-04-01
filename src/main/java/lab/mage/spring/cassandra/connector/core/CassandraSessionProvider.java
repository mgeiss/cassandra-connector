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

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.exceptions.InvalidQueryException;
import com.datastax.driver.mapping.Mapper;
import com.datastax.driver.mapping.MappingManager;
import lab.mage.spring.cassandra.connector.util.TenantContextHolder;
import lab.mage.spring.cassandra.connector.domain.TenantInfo;
import org.slf4j.Logger;
import org.springframework.util.Assert;

import javax.annotation.Nonnull;
import javax.annotation.PreDestroy;
import java.util.HashMap;
import java.util.concurrent.locks.StampedLock;

/**
 *
 *
 * @author Markus Geiss
 */
public class CassandraSessionProvider {

    private final Logger logger;
    private final HashMap<String, Cluster> clusterCache;
    private final HashMap<String, Session> sessionCache;

    private String adminClusterName;
    private String adminContactPoints;
    private String adminKeyspace;

    private MappingManager adminSessionMappingManager;

    private final StampedLock sessionLock = new StampedLock();
    private final StampedLock mapperLock = new StampedLock();

    public CassandraSessionProvider(final Logger logger) {
        super();
        this.logger = logger;
        this.clusterCache = new HashMap<>();
        this.sessionCache = new HashMap<>();
    }

    public void setAdminClusterName(@Nonnull final String adminClusterName) {
        Assert.notNull(adminClusterName);
        this.adminClusterName = adminClusterName;
    }

    public void setAdminContactPoints(@Nonnull final String adminContactPoints) {
        Assert.notNull(adminContactPoints);
        this.adminContactPoints = adminContactPoints;
    }

    public void setAdminKeyspace(@Nonnull final String adminKeyspace) {
        Assert.notNull(adminKeyspace);
        this.adminKeyspace = adminKeyspace;
    }

    @Nonnull
    public Session getAdminSession() {
        if (this.adminClusterName == null
                || this.adminContactPoints == null
                || this.adminKeyspace == null) {
            throw new IllegalStateException("Cluster name, contact points, and keyspace must be set to retrieve an admin session!");
        }

        return this.getSession(this.adminClusterName, this.adminContactPoints, this.adminKeyspace);
    }

    @Nonnull
    public Session getTenantSession() {
        if (TenantContextHolder.getIdentifier().isPresent()) {
            final String identifier = TenantContextHolder.getIdentifier().get();
            return this.getTenantSession(identifier);
        } else {
            throw new IllegalArgumentException("Could not find tenant identifier, make sure you set an identifier using TenantContextHolder.");
        }
    }

    @Nonnull
    public Session getTenantSession(@Nonnull final String identifier) {
        Assert.notNull(identifier, "A tenant identifier must be given!");

        final Mapper<TenantInfo> tenantInfoMapper = this.getAdminSessionMappingManager().mapper(TenantInfo.class);
        tenantInfoMapper.setDefaultGetOptions(Mapper.Option.consistencyLevel(ConsistencyLevel.LOCAL_QUORUM));
        final TenantInfo tenantInfo = tenantInfoMapper.get(identifier);
        Assert.notNull(tenantInfo, "Tenant [" + identifier + "] unknown!");
        return this.getSession(tenantInfo.getClusterName(), tenantInfo.getContactPoints(), tenantInfo.getKeyspace());
    }

    @Nonnull
    public Session getSession(@Nonnull final String clusterName,
                              @Nonnull final String contactPoints,
                              @Nonnull final String keyspace) {
        Assert.notNull(clusterName, "A cluster name must be given!");
        Assert.notNull(contactPoints, "At least one contact point must be given!");
        Assert.notNull(keyspace, "A keyspace must be given!");

        if (!this.sessionCache.containsKey(keyspace)) {
            final long lockStamp = this.sessionLock.writeLock();
            try {
                if (!this.sessionCache.containsKey(keyspace)) {
                    this.logger.info("Create new session for keyspace [" + keyspace + "].");
                    if (!this.clusterCache.containsKey(clusterName)) {
                        final Cluster cluster = Cluster.builder()
                                .withClusterName(clusterName)
                                .addContactPoints(contactPoints.split(","))
                                .build();
                        this.clusterCache.put(clusterName, cluster);
                    }
                    try {
                        final Session session = this.clusterCache.get(clusterName).connect(keyspace);
                        this.sessionCache.put(keyspace, session);
                    } catch (final InvalidQueryException iqex) {
                        throw new IllegalArgumentException("Could not connect keyspace!", iqex);
                    }
                }
            } finally {
                this.sessionLock.unlockWrite(lockStamp);
            }
        }

        return this.sessionCache.get(keyspace);
    }

    @Nonnull
    public MappingManager getAdminSessionMappingManager() {
        if (this.adminSessionMappingManager == null) {
            final long lockStamp = this.mapperLock.writeLock();
            try {
                if (this.adminSessionMappingManager == null) {
                    this.adminSessionMappingManager = new MappingManager(this.getAdminSession());
                }
            } finally {
                this.mapperLock.unlockWrite(lockStamp);
            }
        }

        return adminSessionMappingManager;
    }

    public void touchAdminSession () {
        this.getAdminSession();
    }

    @PreDestroy
    public void cleanUp() {
        this.logger.info("Clean up cluster connections.");
        this.clusterCache.values().forEach(Cluster::close);
        this.clusterCache.clear();
        this.sessionCache.clear();
    }
}
