# Data Report Generator
The Data Report Generator Component is designed to read raw data stored in HBase, analyze them and export analyzed data into new tables(also stored in HBase) used for report.
This component will keep update newer data every a period of time(based on configuration), ensure the information usually up-to-date.

The component's main dependencies:
- Spark Streaming
- Spark Hive
- HBase Client

## Build The Project

> The project requires JDK 1.8 and Maven

To build the project run:
```
mvn clean package
```

## Run The Application

Assume that docker container for `cloudera-quickstart-jdk8` was up and running
> Directory [mounts/cloudera](../mounts/cloudera) is mounted into `/home/cloudera/app` inside the docker container

- Copy `target/data-report-generator-XXX.jar` to `mounts/cloudera`
- Copy [src/main/resources/data-report-generator.properties](./src/main/resources/data-report-generator.properties) to [mounts/cloudera](../mounts/cloudera)
- Update properties in the `data-report-generator.properties`
- Attach to running `cloudera-quickstart-jdk8` docker container's shell
```
docker attach <container-id>
```
To get the docker container ID run:
```
docker ps
```
- Change the directory to `/home/cloudera/app`:
```
cd /home/cloudera/app
```
- Submit a Spark Job:
```
spark-submit --class "edu.miu.cs.cs523.DataReportGenerator" --files data-report-generator.properties,report.sql --master yarn --verbose data-report-generator-1.0-SNAPSHOT.jar "data-report-generator.properties"
```
##Addtional Information
-If Data Report Generator does not work well, such as: There is not new table, or data does not insert into them.This is a prolem of Spark SQL 1.6.0 that it cannot execute "INSERT INTO" statement
Issue details: https://github.com/apache/spark/pull/17989 
There fore, let's try to excute manual:
- Go forward to [/data-report-generator/src/main/resources](../data-report-generator/src/main/resources), you will see 2 files: 'delete_tables.sql' and 'report.sql'
- Open Hive Browser or Hive shell
- Paste and Run the contents of 'delete_tables.sql' first, 'then report.sql'

## Explanation

First step, creates an external table named reddit_posts in Hive, which is linked to data stored in an HBase table named reddit_posts. The table structure and the mapping between Hive columns and HBase columns are defined, allowing users to query and manipulate the HBase data using Hive SQL commands 
```sh
CREATE EXTERNAL TABLE reddit_posts(key string, text string, username string, creation_data bigint, like int)
STORED BY 'org.apache.hadoop.hive.hbase.HBaseStorageHandler'
WITH SERDEPROPERTIES (
  "hbase.columns.mapping" = ":key,contents:txt,info:usr,info:cdate,info:like"
)
TBLPROPERTIES( 
  "hbase.table.name" = "reddit_posts",
  "hbase.table.default.storage.type" = "binary"
);
```
Here is the details: 
```sh
CREATE EXTERNAL TABLE reddit_posts
```
This is a SQL command that creates a new external table named reddit_posts.
```sh
(key string, text string, username string, creation_data bigint, like int)
```
This line defines the structure of the table. It specifies the column names (key, text, username, creation_data, like) along with their data types (string, string, string, bigint, int respectively).
```sh
STORED BY 'org.apache.hadoop.hive.hbase.HBaseStorageHandler'
```
This line specifies that the data for this table will be stored using the HBaseStorageHandler, indicating that the table will be linked to data stored in Apache HBase.
```sh
 WITH SERDEPROPERTIES ("hbase.columns.mapping" ":key,contents:txt,info:usr,info:cdate,info:like")
```
This line specifies how the columns of the table map to the columns in the underlying HBase storage. For example, :key corresponds to the primary key of the HBase table, contents:txt corresponds to the text column in a column family named contents, and so on.
```sh
TBLPROPERTIES("hbase.table.name" = "reddit_posts", "hbase.table.default.storage.type" = "binary")
```
This line specifies additional properties for the table. It sets the HBase table name to be reddit_posts, and it specifies that the default storage type for this HBase table is binary.

Second Step, in order to get data for hourly Reddit's user activity, let's create an external Hbase table to store the query result.
```sh
CREATE TABLE statement_each_hours
  ( 
     key       STRING, 
     total_question       BIGINT, 
     total_like BIGINT, 
	 d_hour INT
  )
STORED BY 'org.apache.hadoop.hive.hbase.HBaseStorageHandler'
WITH SERDEPROPERTIES ("hbase.columns.mapping" = ":key,detail:total_question,detail:total_like,info:d_hour")
TBLPROPERTIES ("hbase.table.name" = "statement_each_hours");
 
INSERT INTO statement_each_hours
SELECT 
    CAST(HOUR(FROM_UNIXTIME(creation_data)) AS STRING),
    COUNT(1) AS total_question,
    SUM(like) AS total_like,
	HOUR(FROM_UNIXTIME(creation_data)) AS created_hour
FROM 
    reddit_posts
WHERE creation_data IS NOT NULL
GROUP BY 
    HOUR(FROM_UNIXTIME(creation_data))
ORDER BY 
    created_hour;
```

Here is the details:
```sh
CREATE TABLE statement_each_hours
```
This is a SQL command that creates a new table named statement_each_hours.
```sh
( key STRING, total_question BIGINT, total_like BIGINT, d_hour INT )
```
This line defines the structure of the table. It specifies the column names (key, total_question, total_like, d_hour) along with their data types (STRING, BIGINT, BIGINT, INT respectively).
```sh
STORED BY 'org.apache.hadoop.hive.hbase.HBaseStorageHandler'
```
This line specifies that the data for this table will be stored using the HBaseStorageHandler, indicating that the table will be stored in Apache HBase, a NoSQL database built on top of Hadoop.
```sh
WITH SERDEPROPERTIES ("hbase.columns.mapping" = ":key,detail:total_question,detail:total_like,info:d_hour")
```
This line specifies how the columns of the table map to the columns in the underlying HBase storage. For example, :key corresponds to the primary key of the HBase table, detail:total_question corresponds to the total_question column in a column family named detail, and so on.
```sh
TBLPROPERTIES ("hbase.table.name" = "statement_each_hours")
```
This line specifies additional properties for the table, in this case, setting the HBase table name to be statement_each_hours.
```sh
INSERT INTO statement_each_hours ...
```
This is an SQL command that inserts data into the statement_each_hours table. The data being inserted is the result of a query.
```sh
SELECT ...
```
This is a SQL query that retrieves data from the reddit_posts table. It selects the hour of creation (HOUR(FROM_UNIXTIME(creation_data))), counts the number of posts created in each hour (COUNT(1) AS total_question), calculates the total number of likes for posts in each hour (SUM(like) AS total_like), and retrieves the hour of creation again (HOUR(FROM_UNIXTIME(creation_data)) AS created_hour).
```sh
FROM reddit_posts
```
This specifies the source table for the data being queried, which is reddit_posts.
```sh
WHERE creation_data IS NOT NULL
```
This filters the rows from reddit_posts where the creation_data column is not null, inorge trash data.
```sh
GROUP BY HOUR(FROM_UNIXTIME(creation_data))
```
This groups the results by the hour of creation.
```sh
ORDER BY created_hour
```
This orders the results by the hour of creation.

Finally, open hbase shell to check the result, here is the result:
```sh
hbase(main):001:0> scan 'statement_each_hours'
ROW                                                   COLUMN+CELL
 19                                                   column=detail:total_like, timestamp=1707081493583, value=571
 19                                                   column=detail:total_question, timestamp=1707081493583, value=252
 19                                                   column=info:d_hour, timestamp=1707081493583, value=19
 20                                                   column=detail:total_like, timestamp=1707081493583, value=1379
 20                                                   column=detail:total_question, timestamp=1707081493583, value=464
 20                                                   column=info:d_hour, timestamp=1707081493583, value=20
 21                                                   column=detail:total_like, timestamp=1707081493583, value=1088
 21                                                   column=detail:total_question, timestamp=1707081493583, value=507
 21                                                   column=info:d_hour, timestamp=1707081493583, value=21
 22                                                   column=detail:total_like, timestamp=1707081493583, value=1680
 22                                                   column=detail:total_question, timestamp=1707081493583, value=444
 22                                                   column=info:d_hour, timestamp=1707081493583, value=22
 23                                                   column=detail:total_like, timestamp=1707081493583, value=905
 23                                                   column=detail:total_question, timestamp=1707081493583, value=405
 23                                                   column=info:d_hour, timestamp=1707081493583, value=23
5 row(s) in 0.2950 seconds

```