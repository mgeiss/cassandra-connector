# Tenant aware Cassandra connector

## Abstract
Providing a tenant per keyspace approach to allow clean separation of data.

A meta keyspace is used to retrieve available tenants and needed information to connect to a cluster. The information stored contains an identifier, the cluster name, contact points, and the name of the keyspace.

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