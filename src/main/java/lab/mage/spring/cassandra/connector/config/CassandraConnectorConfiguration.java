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
package lab.mage.spring.cassandra.connector.config;

import lab.mage.spring.cassandra.connector.core.CassandraSessionProvider;
import lab.mage.spring.cassandra.connector.core.TenantAwareCassandraMapperProvider;
import lab.mage.spring.cassandra.connector.core.TenantAwareEntityTemplate;
import lab.mage.spring.cassandra.connector.util.CassandraConnectorConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

@Configuration
public class CassandraConnectorConfiguration {

    @Autowired
    private Environment env;

    public CassandraConnectorConfiguration() {
        super();
    }

    @Bean(name = CassandraConnectorConstants.LOGGER_NAME)
    public Logger loggerBean() {
        return LoggerFactory.getLogger(CassandraConnectorConstants.LOGGER_NAME);
    }

    @Bean
    @Autowired
    public CassandraSessionProvider cassandraSessionProvider(@Qualifier(CassandraConnectorConstants.LOGGER_NAME) final Logger logger) {
        final CassandraSessionProvider cassandraSessionProvider = new CassandraSessionProvider(this.env, logger);
        cassandraSessionProvider.setAdminClusterName(
                this.env.getProperty(CassandraConnectorConstants.CLUSTER_NAME_PROP, CassandraConnectorConstants.CLUSTER_NAME_PROP_DEFAULT));
        cassandraSessionProvider.setAdminContactPoints(
                this.env.getProperty(CassandraConnectorConstants.CONTACT_POINTS_PROP, CassandraConnectorConstants.CONTACT_POINTS_PROP_DEFAULT));
        cassandraSessionProvider.setAdminKeyspace(
                this.env.getProperty(CassandraConnectorConstants.KEYSPACE_PROP, CassandraConnectorConstants.KEYSPACE_PROP_DEFAULT));

        cassandraSessionProvider.touchAdminSession();

        return cassandraSessionProvider;
    }

    @Bean
    @Autowired
    public TenantAwareCassandraMapperProvider cassandraMapperProvider(@Qualifier(CassandraConnectorConstants.LOGGER_NAME) final Logger logger, final CassandraSessionProvider cassandraSessionProvider) {
        return new TenantAwareCassandraMapperProvider(this.env, logger, cassandraSessionProvider);
    }

    @Bean
    @Autowired
    public TenantAwareEntityTemplate tenantAwareEntityTemplate(final TenantAwareCassandraMapperProvider tenantAwareCassandraMapperProvider) {
        return new TenantAwareEntityTemplate(tenantAwareCassandraMapperProvider);
    }
}
