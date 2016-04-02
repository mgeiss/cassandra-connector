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
package lab.mage.spring.cassandra.connector.util;

public interface CassandraConnectorConstants {

    String LOGGER_NAME = "mage-connector-logger";

    String CLUSTER_NAME_PROP = "lab.mage.connector.clustername";
    String CLUSTER_NAME_PROP_DEFAULT = "mage_staging_cluster";

    String CONTACT_POINTS_PROP = "lab.mage.connector.contactpoints";
    String CONTACT_POINTS_PROP_DEFAULT = "127.0.0.1,127.0.0.2,127.0.0.3";

    String CASSANDRA_PORT_PROP = "lab.mage.connector.port";
    int CASSANDRA_PORT_DEFAULT = 9042;

    String KEYSPACE_PROP = "lab.mage.connector.keyspace";
    String KEYSPACE_PROP_DEFAULT = "mage_system";

    String CONSISTENCY_LEVEL_READ_PROP = "lab.mage.connector.cl.read";
    String CONSISTENCY_LEVEL_WRITE_PROP = "lab.mage.connector.cl.write";
    String CONSISTENCY_LEVEL_DELETE_PROP = "lab.mage.connector.cl.delete";
    String CONSISTENCY_LEVEL_PROP_DEFAULT = "LOCAL_QUORUM";
}
