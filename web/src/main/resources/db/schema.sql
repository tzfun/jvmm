drop table if exists node_t;
create table if not exists node_t
(
    `id`          varchar(64) primary key not null,
    `name`        varchar(32)             not null,
    `address`     varchar(32)             not null default '127.0.0.1:5010',
    `auth_name`   varchar(32)             null,
    `auth_pass`   varchar(128)            null,
    `create_time` bigint
) engine InnoDB
  default charset utf8mb4;