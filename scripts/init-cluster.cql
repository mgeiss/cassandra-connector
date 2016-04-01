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

/*
 * The following statements will create two keyspaces, mage_system and mage_test.
 * mage_system is the meta keyspace to manage all tenants, mage_test is a test
 * tenant keyspace used for integration test.
 */
CREATE KEYSPACE mage_system WITH REPLICATION = {
  'class' : 'SimpleStrategy',
  'replication_factor' : 3
};

USE mage_system;

CREATE TABLE tenants (
  identifier TEXT,
  cluster_name TEXT,
  contact_points TEXT,
  keyspace_name TEXT,
  PRIMARY KEY (identifier)
);

CREATE KEYSPACE mage_test WITH REPLICATION = {
  'class' : 'SimpleStrategy',
  'replication_factor' : 3
};

INSERT INTO tenants (
  identifier, cluster_name, contact_points, keyspace_name
) values (
  'test', 'mage_staging_cluster', '127.0.0.1,127.0.0.2,127.0.0.3', 'mage_test'
);

use mage_test;

CREATE TABLE samples (
  identifier TEXT,
  content TEXT,
  PRIMARY KEY (identifier)
);
