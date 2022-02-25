drop table if exists node_t;
create table if not exists node_t
(
    `id`          int primary key auto_increment not null,
    `name`        varchar(32)                    not null,
    `ip`          varchar(32)                    null,
    `port`        int                            null,
    `auth_enable` bool default false,
    `auth_name`   varchar(32)                    null,
    `auth_pass`   varchar(128)                   null,
    `create_time` bigint,
    `update_time` bigint
) engine InnoDB
  default charset utf8mb4;

drop table if exists node_conf_t;
create table if not exists node_conf_t
(
    `id`      int primary key not null,
    `auto`      bool default false comment '是否开启自动采集',
    `frequency` int  default 0 comment '采集频率，单位秒',
    `pick_cl`   bool default true comment '采集Classloading',
    `pick_c`    bool default true comment '采集Compilation',
    `pick_p`    bool default true comment '采集Process',
    `pick_gc`   bool default true comment '采集GarbageCollection',
    `pick_mm`   bool default true comment '采集MemoryManager',
    `pick_mp`   bool default true comment '采集MemoryPool',
    `pick_m`    bool default true comment '采集Memory',
    `pick_sd`   bool default true comment '采集SystemDynamic',
    `pick_td`   bool default true comment '采集ThreadDynamic'
) engine InnoDB
  default charset utf8mb4;