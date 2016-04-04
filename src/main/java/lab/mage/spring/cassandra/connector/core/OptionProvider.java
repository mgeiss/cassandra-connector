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

import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.mapping.Mapper;
import lab.mage.spring.cassandra.connector.util.CassandraConnectorConstants;
import org.springframework.core.env.Environment;
import org.springframework.util.Assert;

import javax.annotation.Nonnull;
import java.util.HashMap;

public final class OptionProvider {

    private static final HashMap<String, Mapper.Option> CACHED_OPTIONS = new HashMap<>();

    private OptionProvider() {
        super();
    }

    @Nonnull
    public static final Mapper.Option deleteConsistencyLevel(@Nonnull final Environment env) {
        Assert.notNull(env, "An environment must be given!");
        if (OptionProvider.CACHED_OPTIONS.isEmpty()) {
            OptionProvider.lazyOptionInit(env);
        }
        return OptionProvider.CACHED_OPTIONS.get(CassandraConnectorConstants.CONSISTENCY_LEVEL_DELETE_PROP);
    }

    @Nonnull
    public static final Mapper.Option readConsistencyLevel(@Nonnull final Environment env) {
        Assert.notNull(env, "An environment must be given!");
        if (OptionProvider.CACHED_OPTIONS.isEmpty()) {
            OptionProvider.lazyOptionInit(env);
        }
        return OptionProvider.CACHED_OPTIONS.get(CassandraConnectorConstants.CONSISTENCY_LEVEL_READ_PROP);
    }

    @Nonnull
    public static final Mapper.Option writeConsistencyLevel(@Nonnull final Environment env) {
        Assert.notNull(env, "An environment must be given!");
        if (OptionProvider.CACHED_OPTIONS.isEmpty()) {
            OptionProvider.lazyOptionInit(env);
        }
        return OptionProvider.CACHED_OPTIONS.get(CassandraConnectorConstants.CONSISTENCY_LEVEL_WRITE_PROP);
    }

    private static void lazyOptionInit(final Environment env) {
        OptionProvider.CACHED_OPTIONS.put(CassandraConnectorConstants.CONSISTENCY_LEVEL_DELETE_PROP,
                Mapper.Option.consistencyLevel(
                        ConsistencyLevel.valueOf(
                                env.getProperty(CassandraConnectorConstants.CONSISTENCY_LEVEL_DELETE_PROP,
                                        CassandraConnectorConstants.CONSISTENCY_LEVEL_PROP_DEFAULT))));

        OptionProvider.CACHED_OPTIONS.put(CassandraConnectorConstants.CONSISTENCY_LEVEL_READ_PROP,
                Mapper.Option.consistencyLevel(
                        ConsistencyLevel.valueOf(
                                env.getProperty(CassandraConnectorConstants.CONSISTENCY_LEVEL_READ_PROP,
                                        CassandraConnectorConstants.CONSISTENCY_LEVEL_PROP_DEFAULT))));

        OptionProvider.CACHED_OPTIONS.put(CassandraConnectorConstants.CONSISTENCY_LEVEL_WRITE_PROP,
                Mapper.Option.consistencyLevel(
                        ConsistencyLevel.valueOf(
                                env.getProperty(CassandraConnectorConstants.CONSISTENCY_LEVEL_WRITE_PROP,
                                        CassandraConnectorConstants.CONSISTENCY_LEVEL_PROP_DEFAULT))));

    }
}
