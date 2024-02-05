CREATE EXTERNAL TABLE IF NOT EXISTS reddit_posts(key string, text string, username string, creation_data bigint, like int)
STORED BY 'org.apache.hadoop.hive.hbase.HBaseStorageHandler'
    WITH SERDEPROPERTIES (
    "hbase.columns.mapping" = ":key,contents:txt,info:usr,info:cdate,info:like"
    )
TBLPROPERTIES (
    "hbase.table.name" = "reddit_posts",
    "hbase.table.default.storage.type" = "binary"
    );


CREATE TABLE IF NOT EXISTS statement_each_hours
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

CREATE TABLE IF NOT EXISTS most_common_questions
(
    key       STRING,
    question STRING,
    like	   BIGINT
)
    STORED BY 'org.apache.hadoop.hive.hbase.HBaseStorageHandler'
        WITH SERDEPROPERTIES ("hbase.columns.mapping" = ":key,cf:question,cf:like")
    TBLPROPERTIES ("hbase.table.name" = "most_common_questions");

INSERT INTO most_common_questions
SELECT key,
       text,
    like
FROM reddit_posts
ORDER BY like DESC
LIMIT 5;

CREATE TABLE IF NOT EXISTS most_contributor
(
    key       STRING,
    username STRING,
    total_like    BIGINT
)
    STORED BY 'org.apache.hadoop.hive.hbase.HBaseStorageHandler'
        WITH SERDEPROPERTIES ("hbase.columns.mapping" = ":key,cf:username,cf:total_like")
    TBLPROPERTIES ("hbase.table.name" = "most_contributer");

INSERT INTO most_contributor
SELECT
    username,
    username,
    SUM(like) AS total_like
FROM
    reddit_posts
WHERE username IS NOT NULL
GROUP BY
    username
ORDER BY
    total_like DESC
LIMIT 10;