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

import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.mapping.Mapper;
import org.springframework.core.env.Environment;

public class OptionProvider {

    private OptionProvider() {
        super();
    }

    public static final Mapper.Option deleteConsistencyLevel(final Environment env) {
        return Mapper.Option.consistencyLevel(
                ConsistencyLevel.valueOf(
                        env.getProperty(CassandraConnectorConstants.CONSISTENCY_LEVEL_DELETE_PROP,
                                CassandraConnectorConstants.CONSISTENCY_LEVEL_PROP_DEFAULT)));
    }

    public static final Mapper.Option readConsistencyLevel(final Environment env) {
        return Mapper.Option.consistencyLevel(
                ConsistencyLevel.valueOf(
                        env.getProperty(CassandraConnectorConstants.CONSISTENCY_LEVEL_READ_PROP,
                                CassandraConnectorConstants.CONSISTENCY_LEVEL_PROP_DEFAULT)));
    }

    public static final Mapper.Option writeConsistencyLevel(final Environment env) {
        return Mapper.Option.consistencyLevel(
                ConsistencyLevel.valueOf(
                        env.getProperty(CassandraConnectorConstants.CONSISTENCY_LEVEL_WRITE_PROP,
                                CassandraConnectorConstants.CONSISTENCY_LEVEL_PROP_DEFAULT)));
    }
}
