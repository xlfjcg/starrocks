# StarRocks version 2.3

## 2.3.5

Release date: November 30, 2022

### Improvements

- Colocate Join supports Equi Join. [#13546](https://github.com/StarRocks/starrocks/pull/13546)
- Fix the problem that primary key index files are too large due to continuously appending WAL records when data is frequently loaded. [#12862](https://github.com/StarRocks/starrocks/pull/12862)
- FE scans all tablets in batches so that FE releases db.readLock at scanning intervals in case of holding db.readLock for too long. [#13070](https://github.com/StarRocks/starrocks/pull/13070)

### Bug Fixes

The following bugs are fixed:

- When a view is created based directly on the result of UNION ALL, and the UNION ALL operator's input columns include NULL values, the schema of the view is incorrect since the data type of columns is NULL_TYPE rather than UNION ALL's input columns. [#13917](https://github.com/StarRocks/starrocks/pull/13917)
- The query result of `SELECT * FROM ...` and `SELECT * FROM ... LIMIT ...`  is inconsistent. [#13585](https://github.com/StarRocks/starrocks/pull/13585)
- External tablet metadata synchronized to FE may overwrite local tablet metadata, which causes data loading from Flink to fail. [#12579](https://github.com/StarRocks/starrocks/pull/12579)
- BE nodes crash when null filter in Runtime Filter handles literal constants. [#13526](https://github.com/StarRocks/starrocks/pull/13526)
- An error is returned when you execute CTAS. [#12388](https://github.com/StarRocks/starrocks/pull/12388)
- The metrics `ScanRows` collected by pipeline engine in audit log may be wrong. [#12185](https://github.com/StarRocks/starrocks/pull/12185)
- The query result is incorrect when you query compressed  HIVE data. [#11546](https://github.com/StarRocks/starrocks/pull/11546)
- Queries are timeout and StarRocks responds slowly after a BE node crashes. [#12955](https://github.com/StarRocks/starrocks/pull/12955)
- The error of Kerberos authentication failure occurs when you use Broker Load to load data. [#13355](https://github.com/StarRocks/starrocks/pull/13355)
- Too many OR predicates cause statistics estimation to take too long. [#13086](https://github.com/StarRocks/starrocks/pull/13086)
- BE node crashes if Broker Load loads ORC files (Snappy compression) contain uppercase column names. [#12724](https://github.com/StarRocks/starrocks/pull/12724)
- An error is returned when unloading or querying Primary Key table takes more than 30 minutes. [#13403](https://github.com/StarRocks/starrocks/pull/13403)
- The backup task fails when you back up large data volumes to HDFS by using a broker. [#12836](https://github.com/StarRocks/starrocks/pull/12836)
- The data StarRocks read from Iceberg may be incorrect, which is caused by the `parquet_late_materialization_enable` parameter. [#13132](https://github.com/StarRocks/starrocks/pull/13132)
- An error `failed to init view stmt`  is returned when a view is created. [#13102](https://github.com/StarRocks/starrocks/pull/13102)
- An error is returned when you use JDBC to connect StarRock and execute SQL statements. [#13526](https://github.com/StarRocks/starrocks/pull/13526)
- The query is timeout because the query involves too many buckets and uses tablet hint. [#13272](https://github.com/StarRocks/starrocks/pull/13272)
- A BE node crashes and cannot be restarted, and in the meantime, the loading job  into a newly built table reports an error. [#13701](https://github.com/StarRocks/starrocks/pull/13701)
- All BE nodes crash when a materialized view is created. [#13184](https://github.com/StarRocks/starrocks/pull/13184)
- When you execute ALTER ROUTINE LOAD to update the offset of consumed partitions, an error `The specified partition 1 is not in the consumed partitions`may be returned, and followers eventually crash. [#12227](https://github.com/StarRocks/starrocks/pull/12227)

## 2.3.4

Release date: November 10, 2022

### Improvements

- The error message provides a solution when StarRocks fails to create a Routine Load job because the number of running Routine Load job exceeds the limit. [#12204]( https://github.com/StarRocks/starrocks/pull/12204)
- The query fails when StarRocks queries data from Hive and fails to parse CSV files. [#13013](https://github.com/StarRocks/starrocks/pull/13013)

### Bug Fixes

The following bugs are fixed:

- The query may fail if HDFS files paths contain `()`. [#12660](https://github.com/StarRocks/starrocks/pull/12660)
- The result of ORDER BY ... LIMIT ... OFFSET is incorrect when the subquery contains LIMIT. [#9698](https://github.com/StarRocks/starrocks/issues/9698)
- StarRocks is case-insensitive when querying ORC files. [#12724](https://github.com/StarRocks/starrocks/pull/12724)
- BE may crash when RuntimeFilter is closed without invoking the prepare method. [#12906](https://github.com/StarRocks/starrocks/issues/12906)
- BE may crash because of memory leak. [#12906](https://github.com/StarRocks/starrocks/issues/12906)
- The query result may be incorrect after you add a new column and immediately delete data. [#12907](https://github.com/StarRocks/starrocks/pull/12907)
- BE may crash because of sorting data. [#11185](https://github.com/StarRocks/starrocks/pull/11185)
- If StarRocks and MySQL client are not on the same LAN, the loading job created by using INSERT INTO SELECT can not be terminated successfully by executing KILL only once. [#11879](https://github.com/StarRocks/starrocks/pull/11897)
- The metrics `ScanRows` collected by pipeline engine in audit log may be wrong. [#12185](https://github.com/StarRocks/starrocks/pull/12185)

## 2.3.3

Release date: September 27, 2022

### Bug Fixes

The following bugs are fixed:

- Query result may be inaccurate when you query an Hive external table stored as a text file. [#11546](https://github.com/StarRocks/starrocks/pull/11546)
- Nested arrays are not supported when you query Parquet files. [#10983](https://github.com/StarRocks/starrocks/pull/10983)
- Queries or a query may time out if concurrent queries that read data from StarRocks and external data sources are routed to the same resource group, or a query reads data from StarRocks and external data sources. [#10983](https://github.com/StarRocks/starrocks/pull/10983)
- When the Pipeline execution engine is enabled by default, the parameter parallel_fragment_exec_instance_num is changed to 1. It will cause data loading by using INSERT INTO to be slow. [#11462](https://github.com/StarRocks/starrocks/pull/11462)
- BE may crash if there are mistakes when a expression is initialized. [#11396](https://github.com/StarRocks/starrocks/pull/11396)
- The error heap-buffer-overflow may occur if you execute ORDER BY LIMIT.  [#11185](https://github.com/StarRocks/starrocks/pull/11185)
- Schema change fails if you restart Leader FE in the meantime. [#11561](https://github.com/StarRocks/starrocks/pull/11561)

## 2.3.2

Release date: September 7, 2022

### New Features

- Late materialization is supported to accelerate range filter-based queries on external tables in Parquet format. [#9738](https://github.com/StarRocks/starrocks/pull/9738)
- The SHOW AUTHENTICATION statement is added to display user authentication-related information. [#9996](https://github.com/StarRocks/starrocks/pull/9996)

### Improvements

- A configuration item is provided to control whether StarRocks recursively traverses all data files for the bucketed Hive table from which StarRocks queries data. [#10239](https://github.com/StarRocks/starrocks/pull/10239)
- The resource group type `realtime` is renamed as `short_query`. [#10247](https://github.com/StarRocks/starrocks/pull/10247)
- StarRocks no longer distinguishes between uppercase letters and lowercase letters in Hive external tables by default. [#10187](https://github.com/StarRocks/starrocks/pull/10187)

### Bug Fixes

The following bugs are fixed:

- Queries on an Elasticsearch external table may unexpectedly exit when the table is divided into multiple shards. [#10369](https://github.com/StarRocks/starrocks/pull/10369)
- StarRocks throws errors when sub-queries are rewritten as common table expressions (CTEs). [#10397](https://github.com/StarRocks/starrocks/pull/10397)
- StarRocks throws errors when a large amount of data is loaded. [#10370](https://github.com/StarRocks/starrocks/issues/10370) [#10380](https://github.com/StarRocks/starrocks/issues/10380)
- When the same Thrift service IP address is configured for multiple catalogs, deleting one catalog invalidates the incremental metadata updates in the other catalogs. [#10511](https://github.com/StarRocks/starrocks/pull/10511)
- The statistics of memory consumption from BEs are inaccurate. [#9837](https://github.com/StarRocks/starrocks/pull/9837)
- StarRocks throws errors for queries on Primary Key tables. [#10811](https://github.com/StarRocks/starrocks/pull/10811)
- Queries on logical views are not allowed even when you have SELECT permissions on these views. [#10563](https://github.com/StarRocks/starrocks/pull/10563)
- StarRocks does not impose limits on the naming of logical views. Now logical views need to follow the same naming conventions as tables. [#10558](https://github.com/StarRocks/starrocks/pull/10558)

### Behavior Change

- Add BE configuration `max_length_for_bitmap_function` with a default value 1000000 for bitmap function, and add `max_length_for_to_base64` with a default value 200000 for base64 to prevent crash. [#10851](https://github.com/StarRocks/starrocks/pull/10851)

## 2.3.1

Release date: August 22, 2022

### Improvements

- Broker Load supports transforming the List type in Parquet files into non-nested ARRAY data type. [#9150](https://github.com/StarRocks/starrocks/pull/9150)
- Optimized the performance of JSON-related functions (json_query, get_json_string, and get_json_int). [#9623](https://github.com/StarRocks/starrocks/pull/9623)
- Optimized the error message: During a query on Hive, Iceberg, or Hudi, if the data type of the column to query is not supported by StarRocks, the system throws an exception on the column. [#10139](https://github.com/StarRocks/starrocks/pull/10139)
- Reduced the scheduling latency of resource groups to optimize resource isolation performance. [#10122](https://github.com/StarRocks/starrocks/pull/10122)

### Bug Fixes

The following bugs are fixed:

- Wrong result is returned from the query on Elasticsearch external tables due to incorrect pushdown of the `limit` operator. [#9952](https://github.com/StarRocks/starrocks/pull/9952)
- Query on Oracle external tables fails when the `limit` operator is used. [#9542](https://github.com/StarRocks/starrocks/pull/9542)
- BE is blocked when all Kafka Brokers are stopped during a Routine Load. [#9935](https://github.com/StarRocks/starrocks/pull/9935)
- BE crashes during a query on a Parquet file whose data type mismatches that of the corresponding external table. [#10107](https://github.com/StarRocks/starrocks/pull/10107)
- Query times out because the scan range of external tables is empty. [#10091](https://github.com/StarRocks/starrocks/pull/10091)
- The system throws an exception when the ORDER BY clause is included in a sub-query. [#10180](https://github.com/StarRocks/starrocks/pull/10180)
- Hive Metastore hangs when Hive metadata is reloaded asynchronously. [#10132](https://github.com/StarRocks/starrocks/pull/10132)

## 2.3.0

Release date: July 29, 2022

### New Features

- The Primary Key model supports complete DELETE WHERE syntax. For more information, see [DELETE](../sql-reference/sql-statements/data-manipulation/DELETE.md#delete-and-primary-key-model).

- The Primary Key model supports persistent primary key indexes. You can choose to persist the primary key index on disk rather than in memory, significantly reducing memory usage. For more information, see [Primary Key model](../table_design/Data_model.md#how-to-use-it-3).
- Global dictionary can be updated during real-time data ingestion，optimizing query performance and delivering 2X query performance for string data.
- The CREATE TABLE AS SELECT statement can be executed asynchronously. For more information, see [CREATE TABLE AS SELECT](../sql-reference/sql-statements/data-definition/CREATE%20TABLE%20AS%20SELECT.md).
- Support the following resource group-related features:
  - Monitor resource groups: You can view the resource group of the query in the audit log and obtain the metrics of the resource group by calling APIs. For more information, see [Monitor and Alerting](../administration/Monitor_and_Alert.md#monitor-and-alerting).
  - Limit the consumption of large queries on CPU, memory, and I/O resources: You can route queries to specific resource groups based on the classifiers or by configuring session variables. For more information, see [Resource group](../administration/resource_group.md).
- JDBC external tables can be used to conveniently query data in Oracle, PostgreSQL, MySQL, SQLServer, ClickHouse, and other databases. StarRocks also supports predicate pushdown, improving query performance. For more information, see [External table for a JDBC-compatible database](../data_source/External_table.md#external-table-for-a-JDBC-compatible-database).
- [Preview] A new Data Source Connector framework is released to support external catalogs. You can use external catalogs to directly access and query Hive data without creating external tables. For more information, see [Use catalogs to manage internal and external data](https://docs.starrocks.io/en-us/2.3/data_source/Manage_data).
- Added the following functions:
  - [window_funnel](../sql-reference/sql-functions/aggregate-functions/window_funnel.md)
  - [ntile](../sql-reference/sql-functions/Window_function.md)
  - [bitmap_union_count](../sql-reference/sql-functions/bitmap-functions/bitmap_union_count.md), [base64_to_bitmap](../sql-reference/sql-functions/bitmap-functions/base64_to_bitmap.md), [array_to_bitmap](../sql-reference/sql-functions/array-functions/array_to_bitmap.md)
  - [week](../sql-reference/sql-functions/date-time-functions/week.md), [time_slice](../sql-reference/sql-functions/date-time-functions/time_slice.md)

### Improvements

- The compaction mechanism can merge large volume of metadata more quickly. This prevents metadata squeezing and excessive disk usage that can occur shortly after frequent data updates.
- Optimized the performance of loading Parquet files and compressed files.
- Optimized the mechanism of creating materialized views. After the optimization, materialized views can be created at a speed up to 10 times faster than before.
- Optimized the performance of the following operators:
  - TopN and sort operators
  - Equivalence comparison operators that contain functions can use Zone Map indexes when these operators are pushed down to scan operators.
- Optimized Apache Hive™ external tables.
  - When Apache Hive™ tables are stored in Parquet, ORC, or CSV format, schema changes caused by ADD COLUMN or REPLACE COLUMN on Hive can be synchronized to StarRocks when you execute the REFRESH statement on the corresponding Hive external table. For more information, see [Hive external table](../data_source/External_table.md#hive-external-table).
  - `hive.metastore.uris` can be modified for Hive resources. For more information, see [ALTER RESOURCE](../sql-reference/sql-statements/data-definition/ALTER%20RESOURCE.md).
- Optimized the performance of Apache Iceberg external tables. A custom catalog can be used to create an Iceberg resource. For more information, see [Apache Iceberg external table](../data_source/External_table.md#apache-iceberg-external-table).
- Optimized the performance of Elasticsearch external tables. Sniffing the addresses of the data nodes in an Elasticsearch cluster can be disabled. For more information, see [Elasticsearch external table](../data_source/External_table.md#elasticsearch-external-table).
- When the sum() function accepts a numeric string, it implicitly converts the numeric string.
- The year(), month(), and day() functions support the DATE data type.

### Bug Fixes

Fixed the following bugs:

- CPU utilization surges due to an excessive number of tablets.
- Issues that cause "fail to prepare tablet reader" to occur.
- The FEs fail to restart.[#5642](https://github.com/StarRocks/starrocks/issues/5642 )  [#4969](https://github.com/StarRocks/starrocks/issues/4969 )  [#5580](https://github.com/StarRocks/starrocks/issues/5580)
- The CTAS statement cannot be run successfully when the statement includes a JSON function. [#6498](https://github.com/StarRocks/starrocks/issues/6498)

### Others

- StarGo, a cluster management tool, can deploy, start, upgrade, and roll back clusters and manage multiple clusters. For more information, see [Deploy StarRocks with StarGo](../administration/stargo.md).
- The pipeline engine is enabled by default when you upgrade StarRocks to version 2.3 or deploy StarRocks. The pipeline engine can improve the performance of simple queries in high concurrency scenarios and complex queries. If you detect significant performance regressions when using StarRocks 2.3, you can disable the pipeline engine by executing the `SET GLOBAL` statement to set `enable_pipeline_engine` to `false`.
- The [SHOW GRANTS](../sql-reference/sql-statements/account-management/SHOW%20GRANTS.md) statement is compatible with the MySQL syntax and displays the privileges assigned to a user in the form of GRANT statements.
- It is recommended that the memory_limitation_per_thread_for_schema_change ( BE configuration item)  use the default value 2 GB, and data is written to disk when data volume exceeds this limit. Therefore, if you have previously set this parameter to a larger value, it is recommended that you set it to 2 GB, otherwise a schema change task may take up a large amount of memory.
