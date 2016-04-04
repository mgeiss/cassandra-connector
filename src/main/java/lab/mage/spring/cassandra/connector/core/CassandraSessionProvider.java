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
import com.datastax.driver.core.Session;
import com.datastax.driver.core.exceptions.InvalidQueryException;
import com.datastax.driver.mapping.Mapper;
import com.datastax.driver.mapping.MappingManager;
import lab.mage.spring.cassandra.connector.domain.TenantInfo;
import lab.mage.spring.cassandra.connector.util.CassandraConnectorConstants;
import lab.mage.spring.cassandra.connector.util.TenantContextHolder;
import org.slf4j.Logger;
import org.springframework.core.env.Environment;
import org.springframework.util.Assert;

import javax.annotation.Nonnull;
import javax.annotation.PreDestroy;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.StampedLock;

public final class CassandraSessionProvider {

    private final Environment env;
    private final Logger logger;
    private final ConcurrentHashMap<String, Cluster> clusterCache;
    private final ConcurrentHashMap<String, Session> sessionCache;

    private String adminClusterName;
    private String adminContactPoints;
    private String adminKeyspace;

    private MappingManager adminSessionMappingManager;

    private final StampedLock mapperLock = new StampedLock();

    public CassandraSessionProvider(@Nonnull final Environment env, @Nonnull final Logger logger) {
        super();
        Assert.notNull(env, "An environment must be given!");
        Assert.notNull(logger, "A logger must be given!");
        this.env = env;
        this.logger = logger;
        this.clusterCache = new ConcurrentHashMap<>();
        this.sessionCache = new ConcurrentHashMap<>();
    }

    public void setAdminClusterName(@Nonnull final String adminClusterName) {
        Assert.notNull(adminClusterName, "A cluster name must be given!");
        Assert.hasText(adminClusterName, "A cluster name must be given!");
        this.adminClusterName = adminClusterName;
    }

    public void setAdminContactPoints(@Nonnull final String adminContactPoints) {
        Assert.notNull(adminContactPoints, "At least one contact point must be given!");
        Assert.hasText(adminContactPoints, "At least one contact point must be given!");
        this.adminContactPoints = adminContactPoints;
    }

    public void setAdminKeyspace(@Nonnull final String adminKeyspace) {
        Assert.notNull(adminKeyspace, "An keyspace must be given!");
        Assert.hasText(adminKeyspace, "An keyspace must be given!");
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
        Assert.hasText(identifier, "A tenant identifier must be given!");

        final Mapper<TenantInfo> tenantInfoMapper = this.getAdminSessionMappingManager().mapper(TenantInfo.class);
        tenantInfoMapper.setDefaultDeleteOptions(OptionProvider.deleteConsistencyLevel(this.env));
        tenantInfoMapper.setDefaultGetOptions(OptionProvider.readConsistencyLevel(this.env));
        tenantInfoMapper.setDefaultSaveOptions(OptionProvider.writeConsistencyLevel(this.env));
        final TenantInfo tenantInfo = tenantInfoMapper.get(identifier);
        Assert.notNull(tenantInfo, "Tenant [" + identifier + "] unknown!");
        return this.getSession(tenantInfo.getClusterName(), tenantInfo.getContactPoints(), tenantInfo.getKeyspace());
    }

    @Nonnull
    public Session getSession(@Nonnull final String clusterName,
                              @Nonnull final String contactPoints,
                              @Nonnull final String keyspace) {
        Assert.notNull(clusterName, "A cluster name must be given!");
        Assert.hasText(clusterName, "A cluster name must be given!");
        Assert.notNull(contactPoints, "At least one contact point must be given!");
        Assert.hasText(contactPoints, "At least one contact point must be given!");
        Assert.notNull(keyspace, "A keyspace must be given!");
        Assert.hasText(keyspace, "A keyspace must be given!");

        this.sessionCache.computeIfAbsent(keyspace, (sessionKey) -> {
            this.logger.info("Create new session for keyspace [" + keyspace + "].");

            this.clusterCache.computeIfAbsent(clusterName, (clusterKey) -> {
                final String[] contactPointsAsArray = contactPoints.split(",");
                for (int i = 0; i < contactPointsAsArray.length; i++) {
                    contactPointsAsArray[i] = contactPointsAsArray[i].trim();
                }
                final Cluster cluster = Cluster.builder()
                        .withClusterName(clusterName)
                        .withPort(
                                Integer.valueOf(this.env.getProperty(CassandraConnectorConstants.CASSANDRA_PORT_PROP,
                                        CassandraConnectorConstants.CASSANDRA_PORT_DEFAULT))
                        )
                        .addContactPoints(contactPointsAsArray)
                        .build();
                return cluster;
            });
            try {
                return this.clusterCache.get(clusterName).connect(keyspace);
            } catch (final InvalidQueryException iqex) {
                throw new IllegalArgumentException("Could not connect keyspace!", iqex);
            }
        });

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

        return this.adminSessionMappingManager;
    }

    public void touchAdminSession() {
        this.getAdminSession();
    }

    @PreDestroy
    private void cleanUp() {
        this.logger.info("Clean up cluster connections.");

        this.sessionCache.values().forEach(Session::close);
        this.sessionCache.clear();

        this.clusterCache.values().forEach(Cluster::close);
        this.clusterCache.clear();
    }
}
