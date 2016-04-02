package lab.mage.spring.cassandra.connector.fixture;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Session;
import lab.mage.spring.cassandra.connector.util.CassandraConnectorConstants;

/**
 * @author Markus Geiss
 */
public class DataLoader {

    private DataLoader() {
        super();
    }

    public static void createTestSetup() {
        final Cluster cluster = Cluster.builder()
                .withClusterName(CassandraConnectorConstants.CLUSTER_NAME_PROP_DEFAULT)
                .withPort(9142)
                .addContactPoint("127.0.0.1")
                .build();

        final Session session = cluster.newSession();
        session.execute("CREATE KEYSPACE mage_system WITH REPLICATION = {\n" +
                "  'class' : 'SimpleStrategy',\n" +
                "  'replication_factor' : 3\n" +
                "}");
        session.execute("USE mage_system;");
        session.execute("CREATE TABLE tenants (\n" +
                "  identifier TEXT,\n" +
                "  cluster_name TEXT,\n" +
                "  contact_points TEXT,\n" +
                "  keyspace_name TEXT,\n" +
                "  PRIMARY KEY (identifier)\n" +
                ")");
        session.execute("CREATE KEYSPACE mage_test WITH REPLICATION = {\n" +
                "  'class' : 'SimpleStrategy',\n" +
                "  'replication_factor' : 3\n" +
                "}");
        session.execute("INSERT INTO tenants (\n" +
                "  identifier, cluster_name, contact_points, keyspace_name\n" +
                ") values (\n" +
                "  'test', 'mage_staging_cluster', '127.0.0.1,127.0.0.2,127.0.0.3', 'mage_test'\n" +
                ")");
        session.execute("use mage_test");
        session.execute("CREATE TABLE samples (\n" +
                "  identifier TEXT,\n" +
                "  content TEXT,\n" +
                "  PRIMARY KEY (identifier)\n" +
                ")");
        session.close();
        cluster.close();
    }
}
