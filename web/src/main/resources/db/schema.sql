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
    `id`                int primary key not null,
    `auto`              bool default false comment '是否开启自动采集',
    `store`             bool default false comment '是否进行储存',
    `frequency`         int  default -1 comment '采集频率，单位秒',
    `pick_classloading` bool default true comment '采集Classloading',
    `pick_gc`           bool default true comment '采集GarbageCollection',
    `pick_memory_pool`  bool default true comment '采集MemoryPool',
    `pick_memory`       bool default true comment '采集Memory',
    `pick_system`       bool default true comment '采集SystemDynamic',
    `pick_thread`       bool default true comment '采集Thread'
) engine InnoDB
  default charset utf8mb4;

drop table if exists log_system_t;
create table if not exists log_system_t
(
    `id`          bigint primary key not null auto_increment,
    `node_id`     int                not null,
    `cvms`        bigint default 0 comment 'committedVirtualMemorySize',
    `fpms`        bigint default 0 comment 'freePhysicalMemorySize',
    `tpms`        bigint default 0 comment 'totalPhysicalMemorySize',
    `fsss`        bigint default 0 comment 'freeSwapSpaceSize',
    `tsss`        bigint default 0 comment 'totalSwapSpaceSize',
    `pcl`         double default 0.0 comment 'processCpuLoad',
    `pct`         bigint default 0 comment 'processCpuTime',
    `scl`         double default 0.0 comment 'systemCpuLoad',
    `la`          double default 0.0 comment 'loadAverage',
    `create_time` bigint
) engine InnoDB
  default charset utf8mb4;

drop table if exists log_classloading_t;
create table if not exists log_classloading_t
(
    `id`          bigint primary key not null auto_increment,
    `node_id`     int                not null,
    `verbose`     bool default false,
    `lcc`         int  default 0 comment 'loadedClassCount',
    `ulcc`        int  default 0 comment 'unLoadedClassCount',
    `tlcc`        int  default 0 comment 'totalLoadedClassCount',
    `create_time` bigint
) engine InnoDB
  default charset utf8mb4;

drop table if exists log_gc_t;
create table if not exists log_gc_t
(
    `id`          bigint primary key not null auto_increment,
    `node_id`     int                not null,
    `name`        varchar(64)        null,
    `valid`       bool,
    `gc_count`    int,
    `gc_time`     bigint,
    `memory_pool` text,
    `create_time` bigint
) engine InnoDB
  default charset utf8mb4;

drop table if exists log_memory_pool_t;
create table if not exists log_memory_pool_t
(
    `id`            bigint primary key not null auto_increment,
    `node_id`       int                not null,
    `name`          varchar(64)        null,
    `valid`         bool,
    `manager_names` text,
    `type`          varchar(64)        null,
    `init`          bigint default 0,
    `used`          bigint default 0,
    `committed`     bigint default 0,
    `max`           bigint default 0,
    `create_time`   bigint
) engine InnoDB
  default charset utf8mb4;

drop table if exists log_memory_t;
create table if not exists log_memory_t
(
    `id`                 bigint primary key not null auto_increment,
    `node_id`            int                not null,
    `verbose`            bool,
    `pending_count`      bigint,
    `heap_init`          bigint default 0,
    `heap_used`          bigint default 0,
    `heap_committed`     bigint default 0,
    `heap_max`           bigint default 0,
    `non_heap_init`      bigint default 0,
    `non_heap_used`      bigint default 0,
    `non_heap_committed` bigint default 0,
    `non_heap_max`       bigint default 0,
    `create_time`        bigint
) engine InnoDB
  default charset utf8mb4;

drop table if exists log_thread_t;
create table if not exists log_thread_t
(
    `id`            bigint primary key not null auto_increment,
    `node_id`       int                not null,
    `peak`          int    default 0 comment '线程数量峰值',
    `daemon`        int    default 0 comment '守护线程数',
    `current`       int    default 0 comment '当前线程数',
    `total_started` bigint default 0 comment '总共启动线程数',
    `create_time`   bigint
) engine InnoDB
  default charset utf8mb4;