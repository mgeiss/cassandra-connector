# Tenant aware Cassandra connector

## Abstract
Providing a tenant per keyspace approach to allow clean separation of data.

A meta keyspace is used to retrieve available tenants and needed information to connect to a cluster. The information stored contains an identifier, the cluster name, contact points, and the name of the keyspace.

## Usage

### TenantContextHolder
Can be used to set an identifier using ThreadLocal.
 
    ...
    
    TenantContextHolder.setIdentifier("staging");
    
    ...
    
### TenantAwareEntityTemplate
Allows simple read and write operations recognizing the tenant internally.

    ...
    
    private final TenantAwareEntityTemplate tenantAwareEntityTemplate;
    
    @Autowired
    public Sample(final TenantAwareEntityTemplate tenantAwareEntityTemplate) {
        super();
        this.tenantAwareEntityTemplate = tenantAwareEntityTemplate;
    }
    
    ...
    
    public void shouldSaveSampleEntityUsingTemplate() throws Exception {
        final String identifier = UUID.randomUUID().toString();
        final SampleEntity sampleEntity = new SampleEntity();
        sampleEntity.setIdentifier(identifier);
        sampleEntity.setContent("test content");

        this.tenantAwareEntityTemplate.save(sampleEntity);

        final Optional<SampleEntity> fetchedSampleEntity = 
                this.tenantAwareEntityTemplate.findById(SampleEntity.class, identifier);
        ...
    }
    
    ...

### TenantAwareCassandraMapperProvider
Provides a tenant aware instance of Mapper.

    ...
    
    private final TenantAwareCassandraMapperProvider tenantAwareCassandraMapperProvider;
    
    ...
    
    @Autowired
    public Sample(final TenantAwareCassandraMapperProvider tenantAwareCassandraMapperProvider) {
        super();
        this.TenantAwareCassandraMapperProvider = tenantAwareCassandraMapperProvider;
    }
    
    ...
    
    public void shouldSaveSampleEntityUsingMapper() throws Exception {
        final String identifier = UUID.randomUUID().toString();
        final SampleEntity sampleEntity = new SampleEntity();
        sampleEntity.setIdentifier(identifier);
        sampleEntity.setContent("test content");

        final Mapper<SampleEntity> sampleEntityMapper = 
                this.tenantAwareCassandraMapperProvider.getMapper(SampleEntity.class);
        sampleEntityMapper.save(sampleEntity);

        final SampleEntity fetchedSampleEntity = sampleEntityMapper.get(identifier);
        ...
    }
    
    ...
    
### CassandraSessionProvider
Provides an administrative session, tenant sessions, or custom sessions.

    ...
    
    private final CassandraSessionProvider cassandraSessionProvider;
    
    ...
    
    @Autowired
    public Sample(final CassandraSessionProvider cassandraSessionProvider) {
        super();
        this.CassandraSessionProvider = cassandraSessionProvider;
    }
    
    ... 
    
    public void shouldRetrieveAdminSession() throws Exception {
        final Session testSession = this.cassandraSessionProvider.getAdminSession();
        ...
    }
    
    public void shouldRetrieveTenantSession() throws Exception {
        final Session testSession = this.cassandraSessionProvider.getTenantSession();
        ...
    }
    
    public void shouldRetrieveTenantSession(final String identifier) throws Exception {
        final Session testSession = this.cassandraSessionProvider.getTenantSession(identifier);
        ...
    }

    public void shouldRetrieveCustomSession(final String clusterName, final String contactPoints, 
                                            final String keyspace) throws Exception {
        final Session session = this.cassandraSessionProvider.getSession(clusterName, contactPoints, keyspace);
        ...
    }
    

## Versioning
The version numbers follow the [Semantic Versioning](http://semver.org/) scheme.

In addition to MAJOR.MINOR.PATCH the following postfixes are used to indicate the development state.

* snapshot - A release currently in development. 
* m - A _milestone_ release include specific sets of functions and are released as soon as the functionality is complete.
* rc - A _release candidate_ is a version with potential to be a final product, considered _code complete_.
* ga - _General availability_ indicates that this release is the best available version and is recommended for all usage.

The versioning layout is {MAJOR}.{MINOR}.{PATCH}-{INDICATOR}[.{PATCH}]. Only milestones and release candidates can  have patch versions. Some examples:

1.2.3-snapshot  
1.3.5-m.1  
1.5.7-rc.2  
2.0.0-ga

## License
See [LICENSE](LICENSE) file.