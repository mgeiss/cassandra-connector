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
