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
package lab.mage.spring.cassandra.connector;

import com.datastax.driver.core.Session;
import com.datastax.driver.mapping.Mapper;
import lab.mage.spring.cassandra.connector.config.EnableCassandraConnector;
import lab.mage.spring.cassandra.connector.core.CassandraSessionProvider;
import lab.mage.spring.cassandra.connector.core.TenantAwareCassandraMapperProvider;
import lab.mage.spring.cassandra.connector.domain.SampleEntity;
import lab.mage.spring.cassandra.connector.fixture.DataLoader;
import lab.mage.spring.cassandra.connector.util.CassandraConnectorConstants;
import lab.mage.spring.cassandra.connector.util.TenantContextHolder;
import org.cassandraunit.utils.EmbeddedCassandraServerHelper;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

import java.util.UUID;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(
        classes = {
                CassandraSessionProviderIntegrationTest.TestConfiguration.class
        },
        loader = AnnotationConfigContextLoader.class
)
public class CassandraSessionProviderIntegrationTest {

    @Configuration
    @EnableCassandraConnector
    public static class TestConfiguration {

    }

    private static final String TEST_TENANT = "test";

    @Autowired
    @Qualifier(CassandraConnectorConstants.LOGGER_NAME)
    private Logger logger;

    @Autowired
    private CassandraSessionProvider cassandraSessionProvider;

    @Autowired
    private TenantAwareCassandraMapperProvider tenantAwareCassandraMapperProvider;

    public CassandraSessionProviderIntegrationTest() {
        super();
    }

    @BeforeClass
    public static void prepare() throws Exception {
        System.setProperty(CassandraConnectorConstants.CONSISTENCY_LEVEL_DELETE_PROP, "ONE");
        System.setProperty(CassandraConnectorConstants.CONSISTENCY_LEVEL_READ_PROP, "ONE");
        System.setProperty(CassandraConnectorConstants.CONSISTENCY_LEVEL_WRITE_PROP, "ONE");
        System.setProperty(CassandraConnectorConstants.CASSANDRA_PORT_PROP, "9142");
        TenantContextHolder.setIdentifier(TEST_TENANT);
        EmbeddedCassandraServerHelper.startEmbeddedCassandra();
        DataLoader.createTestSetup();
    }

    @AfterClass
    public static void cleanUp() {
        TenantContextHolder.clear();
        EmbeddedCassandraServerHelper.cleanEmbeddedCassandra();
    }

    @Test
    public void shouldConnectToTenant() throws Exception {
        final Session testSession = this.cassandraSessionProvider.getTenantSession();
        Assert.assertNotNull(testSession);
    }

    @Test
    public void shouldRetrieveMapper() throws Exception {
        final Mapper<SampleEntity> sampleEntityMapper = this.tenantAwareCassandraMapperProvider.getMapper(SampleEntity.class);
        Assert.assertNotNull(sampleEntityMapper);
    }

    @Test
    public void shouldFailCreateSessionNullTenant() {
        try {
            this.cassandraSessionProvider.getTenantSession(null);
            Assert.fail();
        } catch (IllegalArgumentException iaex) {
            // do nothing, expected
        }
    }

    @Test
    public void shouldFailCreateSessionUnknownTenant() {
        try {
            this.cassandraSessionProvider.getTenantSession("unknown");
            Assert.fail();
        } catch (IllegalArgumentException iaex) {
            // do nothing, expected
        }
    }

    @Test
    public void shouldFailRetrieveMapperNullType() {
        try {
            this.tenantAwareCassandraMapperProvider.getMapper(null);
            Assert.fail();
        } catch (IllegalArgumentException iaex) {
            // do nothing, expected
        }
    }

    @Test
    public void shouldFailRetrieveMapperUnmappedType() {
        try {
            this.tenantAwareCassandraMapperProvider.getMapper(String.class);
            Assert.fail();
        } catch (IllegalArgumentException iaex) {
            // do nothing, expected
        }
    }

    @Test
    public void shouldFailRetrieveMapperNullTenantNullType() {
        try {
            this.tenantAwareCassandraMapperProvider.getMapper(null, null);
            Assert.fail();
        } catch (IllegalArgumentException iaex) {
            // do nothing, expected
        }
    }

    @Test
    public void shouldFailRetrieveMapperUnknownTenantNullType() {
        try {
            this.tenantAwareCassandraMapperProvider.getMapper("unknown", null);
            Assert.fail();
        } catch (IllegalArgumentException iaex) {
            // do nothing, expected
        }
    }

    @Test
    public void shouldFailRetrieveMapperValidTenantNullType() {
        try {
            this.tenantAwareCassandraMapperProvider.getMapper(TEST_TENANT, null);
            Assert.fail();
        } catch (IllegalArgumentException iaex) {
            // do nothing, expected
        }
    }

    @Test
    public void shouldFailRetrieveMapperValidTenantUnmappedType() {
        try {
            this.tenantAwareCassandraMapperProvider.getMapper(TEST_TENANT, String.class);
            Assert.fail();
        } catch (IllegalArgumentException iaex) {
            // do nothing, expected
        }
    }

    @Test
    public void shouldRetrieveMapperValidTenantType() {
        final Mapper<SampleEntity> sampleEntityMapper = this.tenantAwareCassandraMapperProvider.getMapper(TEST_TENANT, SampleEntity.class);
        Assert.assertNotNull(sampleEntityMapper);
    }

    @Test
    public void shouldRetrieveSession() {
        final String clusterName = "mage_staging_cluster";
        final String contactPoints = "127.0.0.1";
        final String keyspace = "mage_system";

        final Session session = this.cassandraSessionProvider.getSession(clusterName, contactPoints, keyspace);
        Assert.assertNotNull(session);
    }

    @Test
    public void shouldFailRetrieveSessionNullCluster() {
        final String clusterName = null;
        final String contactPoints = "127.0.0.1";
        final String keyspace = "mage_system";

        try {
            this.cassandraSessionProvider.getSession(clusterName, contactPoints, keyspace);
            Assert.fail();
        } catch (IllegalArgumentException iaex) {
            // do nothing, expected
        }
    }

    @Test
    public void shouldFailRetrieveSessionNullContactPoints() {
        final String clusterName = "mage_staging_cluster";
        final String contactPoints = null;
        final String keyspace = "mage_system";

        try {
            this.cassandraSessionProvider.getSession(clusterName, contactPoints, keyspace);
            Assert.fail();
        } catch (IllegalArgumentException iaex) {
            // do nothing, expected
        }
    }

    @Test
    public void shouldFailRetrieveSessionNullKeySpace() {
        final String clusterName = "mage_staging_cluster";
        final String contactPoints = "127.0.0.1";
        final String keyspace = "unknown_kespace";

        try {
            this.cassandraSessionProvider.getSession(clusterName, contactPoints, keyspace);
            Assert.fail();
        } catch (IllegalArgumentException iaex) {
            // do nothing, expected
        }
    }

    @Test
    public void shouldFailRetrieveSessionUnknownCluster() {
        final String clusterName = "unknown_cluster";
        final String contactPoints = "127.0.0.1";
        final String keyspace = "unknown_keyspace";

        try {
            this.cassandraSessionProvider.getSession(clusterName, contactPoints, keyspace);
            Assert.fail();
        } catch (IllegalArgumentException iaex) {
            // do nothing, expected
        }
    }

    @Test
    public void shouldFailRetrieveSessionUnknownContactPoint() {
        final String clusterName = "mage_staging_cluster";
        final String contactPoints = "127.1.1.1";
        final String keyspace = "unknown_keyspace";

        try {
            this.cassandraSessionProvider.getSession(clusterName, contactPoints, keyspace);
            Assert.fail();
        } catch (IllegalArgumentException iaex) {
            // do nothing, expected
        }
    }

    @Test
    public void shouldFailRetrieveSessionUnknownKeyspace() {
        final String clusterName = "mage_staging_cluster";
        final String contactPoints = "127.0.0.1";
        final String keyspace = "unknown_keyspace";

        try {
            this.cassandraSessionProvider.getSession(clusterName, contactPoints, keyspace);
            Assert.fail();
        } catch (IllegalArgumentException iaex) {
            // do nothing, expected
        }
    }

    @Test
    public void shouldInsertNewSampleValue() {
        final String identifier = UUID.randomUUID().toString();
        final SampleEntity sampleEntity = new SampleEntity();
        sampleEntity.setIdentifier(identifier);
        sampleEntity.setContent("test content");

        final Mapper<SampleEntity> sampleEntityMapper = this.tenantAwareCassandraMapperProvider.getMapper(SampleEntity.class);
        sampleEntityMapper.save(sampleEntity);

        final SampleEntity fetchedSampleEntity = sampleEntityMapper.get(identifier);
        Assert.assertNotNull(fetchedSampleEntity);
        Assert.assertEquals(sampleEntity, fetchedSampleEntity);
    }

    @Test
    public void shouldReturnNullSampleEntityUnknownIdentifier() {
        final Mapper<SampleEntity> sampleEntityMapper = this.tenantAwareCassandraMapperProvider.getMapper(SampleEntity.class);
        final SampleEntity fetchedSampleEntity = sampleEntityMapper.get(UUID.randomUUID().toString());
        Assert.assertNull(fetchedSampleEntity);
    }
}
