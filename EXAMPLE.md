# info

下面提供一些简单的数据采集示例，以 `json` 和 `table` 格式展示，每一个信息都与 core 模块中的 Java Entity对应。

## Machine Collections

下面是物理机器相关的采集示例

### process

```shell
info -t process
```

```json
{
  "name": "13106@beifengtzs-MacBook-Pro.local",
  "startTime": 1694325547579,
  "uptime": 34138,
  "pid": 13106,
  "vmVersion": "11.0.13+10-LTS-370",
  "vmVendor": "Oracle Corporation",
  "vmName": "Java HotSpot(TM) 64-Bit Server VM",
  "vmHome": "/Library/Java/JavaVirtualMachines/jdk-11.0.13.jdk/Contents/Home",
  "vmManagementSpecVersion": "2.0",
  "vmSpecName": "Java Virtual Machine Specification",
  "vmSpecVendor": "Oracle Corporation",
  "vmSpecVersion": "11",
  "inputArgs": [
    "--add-opens\u003djava.base/jdk.internal.loader\u003dALL-UNNAMED",
    "--add-opens\u003djdk.zipfs/jdk.nio.zipfs\u003dALL-UNNAMED",
    "--add-opens\u003djava.management/sun.management\u003dALL-UNNAMED"
  ],
  "workDir": "/Users/beifengtz/Program/jvmm-dev/jvmm-2.2.0"
}
```

### disk

```shell
info -t disk
```

```json
[
   {
      "name":"PHYSICALDRIVE1",
      "model":"ST1000DM010-2EP102 (标准磁盘驱动器)",
      "size":1000202273280,
      "currentQueueLength":0,
      "partitions": [
         {
            "mount":"E:\\",
            "identification":"磁盘 #1，分区 #0",
            "size":665844711424
         },
         {
            "mount":"D:\\",
            "identification":"磁盘 #1，分区 #1",
            "size":334340554752
         }
      ]
   },
   {
      "name":"PHYSICALDRIVE0",
      "model":"Flashwar SSD S500 Pro 256GB (标准磁盘驱动器)",
      "size":256052966400,
      "currentQueueLength":0,
      "partitions": [
         {
            "mount":"C:\\",
            "identification":"磁盘 #0，分区 #1",
            "size":255817511936
         }
      ]
   }
]
```

### disk_io

```shell
info -t disk_io
```

```text
+------+----------+-----------+----------+-----------+----------+
|Name  |Read(n/s) |Write(n/s) |Read(b/s) |Write(b/s) |Queue Len |
+------+----------+-----------+----------+-----------+----------+
|disk0 |1         |21         |4096      |126976     |0         |
|disk1 |1         |21         |4096      |126976     |0         |
+------+----------+-----------+----------+-----------+----------+
```

### cpu

```shell
info -t cpu
```

```text
+--------+-------------+--------------+-----------+--------+
|CPU Num |Sys Usage(%) |User Usage(%) |IO Wait(%) |Idle(%) |
+--------+-------------+--------------+-----------+--------+
|16      |0.80         |1.61          |0.00       |97.57   |
+--------+-------------+--------------+-----------+--------+
```

### network

```shell
info -t network
```

```json
{
   "connections":427,
   "tcpV4": {
      "connectionsEstablished":172,
      "connectionsActive":261007,
      "connectionsPassive":60532,
      "connectionFailures":121555,
      "connectionsReset":13723,
      "segmentsSent":126764027,
      "segmentsReceived":127521077,
      "segmentsRetransmitted":498722,
      "inErrors":0,
      "outResets":395630
   },
   "udpV4": {
      "datagramsSent":4342152,
      "datagramsReceived":8922459,
      "datagramsNoPort":310987,
      "datagramsReceivedErrors":28
   },
   "tcpV6": {
      "connectionsEstablished":0,
      "connectionsActive":76602,
      "connectionsPassive":3624,
      "connectionFailures":75367,
      "connectionsReset":6,
      "segmentsSent":479149,
      "segmentsReceived":784082,
      "segmentsRetransmitted":300556,
      "inErrors":0,
      "outResets":375288
   },
   "udpV6": {
      "datagramsSent":28874,
      "datagramsReceived":2301082,
      "datagramsNoPort":227,
      "datagramsReceivedErrors":11
   },
   "networkIFInfos": [
      {
         "name":"eth0",
         "alias":"以太网",
         "mac":"b4:2e:99:d6:4f:14",
         "ipV4": [
            "192.168.0.198"
         ],
         "ipV6": [
            "fe80:0:0:0:1fbb:362a:13d3:8c0d"
         ],
         "status":"UP",
         "mtu":1500,
         "sentBytesPerSecond":2784.0,
         "recvBytesPerSecond":4467.0,
         "recvBytes":5611290200,
         "sentBytes":3513033443,
         "recvCount":9170088,
         "sentCount":9955071
      },
      {
         "name":"eth3",
         "alias":"SSTAP 1",
         "mac":"00:ff:a4:f6:35:38",
         "ipV4": [

         ],
         "ipV6": [
            "fe80:0:0:0:a4ca:e218:8c0e:b0bd"
         ],
         "status":"DOWN",
         "mtu":1500,
         "sentBytesPerSecond":0.0,
         "recvBytesPerSecond":0.0,
         "recvBytes":89458276,
         "sentBytes":29099598,
         "recvCount":138339,
         "sentCount":128500
      },
      {
         "name":"net6",
         "alias":"本地连接",
         "mac":"00:ff:be:f2:c8:1e",
         "ipV4": [

         ],
         "ipV6": [
            "fe80:0:0:0:fbd0:6ee1:be8f:10a4"
         ],
         "status":"DOWN",
         "mtu":1500,
         "sentBytesPerSecond":0.0,
         "recvBytesPerSecond":0.0,
         "recvBytes":65106740,
         "sentBytes":26346282,
         "recvCount":83802,
         "sentCount":72894
      }
   ]
}
```

### sys

```shell
info -t sys
```

```text
+---------+--------+-------+----+--------------+---------------+----------+
|Name     |Version |Arch   |CPU |Time Zone     |IP             |User      |
+---------+--------+-------+----+--------------+---------------+----------+
|Mac OS X |11.1    |x86_64 |16  |Asia/Shanghai |192.168.31.163 |beifengtz |
+---------+--------+-------+----+--------------+---------------+----------+
```

### sys_memory

```shell
info -t sys_memory
```

```text
+------------+--------------+-----------+----------+------------------+-------------+-------+
|Physical    |Physical Free |Swap       |Swap Free |Committed Virtual |Buffer Cache |Shared |
+------------+--------------+-----------+----------+------------------+-------------+-------+
|17179869184 |145301504     |5368709120 |145301504 |41118126080       |0            |0      |
+------------+--------------+-----------+----------+------------------+-------------+-------+
```

### sys_file

```shell
info -t sys_file
```

```text
+--------------------+----------------------------+--------------------+-----+-------------+-------------+-------------+
|Name                |Mount                       |Label               |Type |Size(B)      |Free(B)      |Usable(B)    |
+--------------------+----------------------------+--------------------+-----+-------------+-------------+-------------+
|Macintosh HD        |/                           |Macintosh HD        |apfs |886000558080 |432142954496 |432142954496 |
|Preboot             |/System/Volumes/Preboot     |Preboot             |apfs |886000558080 |432142954496 |432142954496 |
|VM                  |/System/Volumes/VM          |VM                  |apfs |886000558080 |432142954496 |432142954496 |
|Update              |/System/Volumes/Update      |Update              |apfs |886000558080 |432142954496 |432142954496 |
|Macintosh HD - Data |/System/Volumes/Data        |Macintosh HD - Data |apfs |886000558080 |432142954496 |432142954496 |
|BOOTCAMP            |/Volumes/BOOTCAMP           |BOOTCAMP            |lifs |114240253952 |88964022272  |88964022272  |
|Macintosh HD        |/System/Volumes/Update/mnt1 |Macintosh HD        |apfs |886000558080 |432142954496 |432142954496 |
+--------------------+----------------------------+--------------------+-----+-------------+-------------+-------------+
```

## JVM Collections

下面是 Java 虚拟机相关的采集示例

### jvm_classloading

```shell
info -t jvm_classloading
```

```text
+---------------+-----------------+-------------+--------+
|Loaded Classes |Unloaded Classes |Loaded Total |Verbose |
+---------------+-----------------+-------------+--------+
|3221           |0                |3221         |false   |
+---------------+-----------------+-------------+--------+
```

### jvm_classloader

```shell
info -t jvm_classloader
```

```text
+------------------------------------------------+-----------+-----------------------------------------------------+
|Name                                            |Hash       |Parents                                              |
+------------------------------------------------+-----------+-----------------------------------------------------+
|jdk.internal.loader.ClassLoaders$AppClassLoader |1267032364 |jdk.internal.loader.ClassLoaders$PlatformClassLoader |
+------------------------------------------------+-----------+-----------------------------------------------------+
```

### jvm_compilation

```shell
info -t jvm_compilation
```

```text
+--------------------------------+-----------------+--------------+
|Name                            |Compilation Time |Support Timer |
+--------------------------------+-----------------+--------------+
|HotSpot 64-Bit Tiered Compilers |3234             |true          |
+--------------------------------+-----------------+--------------+
```

### jvm_gc

```shell
info -t jvm_gc
```

```text
+--------------------+------+---------+--------+-------------------------------------------+
|Name                |Valid |GC Count |GC Time |Memory Pools                               |
+--------------------+------+---------+--------+-------------------------------------------+
|G1 Young Generation |true  |3        |24      |G1 Eden Space;G1 Survivor Space;G1 Old Gen |
|G1 Old Generation   |true  |0        |0       |G1 Eden Space;G1 Survivor Space;G1 Old Gen |
+--------------------+------+---------+--------+-------------------------------------------+
```

### jvm_memory

```shell
info -t jvm_memory
```

```json
{
   "heapUsage": {
      "init":268435456,
      "used":33084896,
      "committed":268435456,
      "max":4280287232
   },
   "nonHeapUsage": {
      "init":7667712,
      "used":52329608,
      "committed":55590912,
      "max":-1
   },
   "pendingCount":0,
   "verbose":false
}
```

### jvm_memory_manager

```shell
info -t jvm_memory_manager
```

```text
+--------------------+------+--------------------------------------------------------------------------------------+
|Name                |Valid |Pools                                                                                 |
+--------------------+------+--------------------------------------------------------------------------------------+
|CodeCacheManager    |true  |CodeHeap 'non-nmethods';CodeHeap 'profiled nmethods';CodeHeap 'non-profiled nmethods' |
|Metaspace Manager   |true  |Metaspace;Compressed Class Space                                                      |
|G1 Young Generation |true  |G1 Eden Space;G1 Survivor Space;G1 Old Gen                                            |
|G1 Old Generation   |true  |G1 Eden Space;G1 Survivor Space;G1 Old Gen                                            |
+--------------------+------+--------------------------------------------------------------------------------------+
```

### jvm_memory_pool

```shell
info -t jvm_memory_pool
```

```json
[
   {
      "name":"CodeHeap 'non-nmethods'",
      "valid":true,
      "managerNames": [
         "CodeCacheManager"
      ],
      "type":"NON_HEAP",
      "usage": {
         "init":2555904,
         "used":1358464,
         "committed":2555904,
         "max":5832704
      },
      "collectionUsage": {
         "init":0,
         "used":0,
         "committed":0,
         "max":0
      },
      "peakUsage": {
         "init":2555904,
         "used":1401216,
         "committed":2555904,
         "max":5832704
      },
      "usageThresholdSupported":true,
      "usageThresholdExceeded":false,
      "usageThreshold":0,
      "usageThresholdCount":0,
      "collectionUsageThresholdSupported":false,
      "collectionUsageThresholdExceeded":false,
      "collectionUsageThreshold":0,
      "collectionUsageThresholdCount":0
   },
   {
      "name":"Metaspace",
      "valid":true,
      "managerNames": [
         "Metaspace Manager"
      ],
      "type":"NON_HEAP",
      "usage": {
         "init":0,
         "used":37491616,
         "committed":38354944,
         "max":-1
      },
      "collectionUsage": {
         "init":0,
         "used":0,
         "committed":0,
         "max":0
      },
      "peakUsage": {
         "init":0,
         "used":37491616,
         "committed":38354944,
         "max":-1
      },
      "usageThresholdSupported":true,
      "usageThresholdExceeded":false,
      "usageThreshold":0,
      "usageThresholdCount":0,
      "collectionUsageThresholdSupported":false,
      "collectionUsageThresholdExceeded":false,
      "collectionUsageThreshold":0,
      "collectionUsageThresholdCount":0
   },
   {
      "name":"CodeHeap 'profiled nmethods'",
      "valid":true,
      "managerNames": [
         "CodeCacheManager"
      ],
      "type":"NON_HEAP",
      "usage": {
         "init":2555904,
         "used":9084672,
         "committed":9109504,
         "max":122880000
      },
      "collectionUsage": {
         "init":0,
         "used":0,
         "committed":0,
         "max":0
      },
      "peakUsage": {
         "init":2555904,
         "used":9084672,
         "committed":9109504,
         "max":122880000
      },
      "usageThresholdSupported":true,
      "usageThresholdExceeded":false,
      "usageThreshold":0,
      "usageThresholdCount":0,
      "collectionUsageThresholdSupported":false,
      "collectionUsageThresholdExceeded":false,
      "collectionUsageThreshold":0,
      "collectionUsageThresholdCount":0
   },
   {
      "name":"Compressed Class Space",
      "valid":true,
      "managerNames": [
         "Metaspace Manager"
      ],
      "type":"NON_HEAP",
      "usage": {
         "init":0,
         "used":4275064,
         "committed":4587520,
         "max":1073741824
      },
      "collectionUsage": {
         "init":0,
         "used":0,
         "committed":0,
         "max":0
      },
      "peakUsage": {
         "init":0,
         "used":4275064,
         "committed":4587520,
         "max":1073741824
      },
      "usageThresholdSupported":true,
      "usageThresholdExceeded":false,
      "usageThreshold":0,
      "usageThresholdCount":0,
      "collectionUsageThresholdSupported":false,
      "collectionUsageThresholdExceeded":false,
      "collectionUsageThreshold":0,
      "collectionUsageThresholdCount":0
   },
   {
      "name":"G1 Eden Space",
      "valid":true,
      "managerNames": [
         "G1 Old Generation",
         "G1 Young Generation"
      ],
      "type":"HEAP",
      "usage": {
         "init":25165824,
         "used":20971520,
         "committed":116391936,
         "max":-1
      },
      "collectionUsage": {
         "init":25165824,
         "used":0,
         "committed":116391936,
         "max":-1
      },
      "peakUsage": {
         "init":25165824,
         "used":40894464,
         "committed":165675008,
         "max":-1
      },
      "usageThresholdSupported":false,
      "usageThresholdExceeded":false,
      "usageThreshold":0,
      "usageThresholdCount":0,
      "collectionUsageThresholdSupported":true,
      "collectionUsageThresholdExceeded":false,
      "collectionUsageThreshold":0,
      "collectionUsageThresholdCount":0
   },
   {
      "name":"G1 Old Gen",
      "valid":true,
      "managerNames": [
         "G1 Old Generation",
         "G1 Young Generation"
      ],
      "type":"HEAP",
      "usage": {
         "init":243269632,
         "used":12113376,
         "committed":145752064,
         "max":4280287232
      },
      "collectionUsage": {
         "init":243269632,
         "used":0,
         "committed":0,
         "max":4280287232
      },
      "peakUsage": {
         "init":243269632,
         "used":12596528,
         "committed":243269632,
         "max":4280287232
      },
      "usageThresholdSupported":true,
      "usageThresholdExceeded":false,
      "usageThreshold":0,
      "usageThresholdCount":0,
      "collectionUsageThresholdSupported":true,
      "collectionUsageThresholdExceeded":false,
      "collectionUsageThreshold":0,
      "collectionUsageThresholdCount":0
   },
   {
      "name":"G1 Survivor Space",
      "valid":true,
      "managerNames": [
         "G1 Old Generation",
         "G1 Young Generation"
      ],
      "type":"HEAP",
      "usage": {
         "init":0,
         "used":6291456,
         "committed":6291456,
         "max":-1
      },
      "collectionUsage": {
         "init":0,
         "used":6291456,
         "committed":6291456,
         "max":-1
      },
      "peakUsage": {
         "init":0,
         "used":8388608,
         "committed":8388608,
         "max":-1
      },
      "usageThresholdSupported":false,
      "usageThresholdExceeded":false,
      "usageThreshold":0,
      "usageThresholdCount":0,
      "collectionUsageThresholdSupported":true,
      "collectionUsageThresholdExceeded":false,
      "collectionUsageThreshold":0,
      "collectionUsageThresholdCount":0
   },
   {
      "name":"CodeHeap 'non-profiled nmethods'",
      "valid":true,
      "managerNames": [
         "CodeCacheManager"
      ],
      "type":"NON_HEAP",
      "usage": {
         "init":2555904,
         "used":2017920,
         "committed":2555904,
         "max":122945536
      },
      "collectionUsage": {
         "init":0,
         "used":0,
         "committed":0,
         "max":0
      },
      "peakUsage": {
         "init":2555904,
         "used":2017920,
         "committed":2555904,
         "max":122945536
      },
      "usageThresholdSupported":true,
      "usageThresholdExceeded":false,
      "usageThreshold":0,
      "usageThresholdCount":0,
      "collectionUsageThresholdSupported":false,
      "collectionUsageThresholdExceeded":false,
      "collectionUsageThreshold":0,
      "collectionUsageThresholdCount":0
   }
]
```

### jvm_thread

```shell
info -t jvm_thread
```

```json
{
  "deadlockedThreads": [

  ],
  "peakThreadCount":23,
  "daemonThreadCount":8,
  "threadCount":19,
  "totalStartedThreadCount":28,
  "stateCount": {
    "RUNNABLE":3,
    "WAITING":1
  }
}
```

### jvm_thread_detail

```shell
info -t jvm_thread_detail
```

```text
+---+----------------------------------+---------------------+--------------+---------+-------+---------+-----------+-----------+--------+------------+-------+-----------+
|ID |Name                              |Group                |State         |OS State |Daemon |Priority |User(ns)   |CPU(ns)    |Blocked |Blocked(ns) |Waited |Waited(ns) |
+---+----------------------------------+---------------------+--------------+---------+-------+---------+-----------+-----------+--------+------------+-------+-----------+
|2  |Reference Handler                 |system               |RUNNABLE      |5        |true   |10       |30622000   |31806000   |6       |-1          |0      |-1         |
|3  |Finalizer                         |system               |WAITING       |401      |true   |8        |172000     |235000     |1       |-1          |2      |-1         |
|4  |Signal Dispatcher                 |system               |RUNNABLE      |5        |true   |9        |16000      |44000      |0       |-1          |0      |-1         |
|19 |Common-Cleaner                    |InnocuousThreadGroup |TIMED_WAITING |417      |true   |8        |659000     |983000     |3       |-1          |11     |-1         |
|22 |jvmm-1-thread-1                   |jvmm                 |RUNNABLE      |5        |false  |5        |1668509000 |1779777000 |5       |-1          |3      |-1         |
|23 |DestroyJavaVM                     |main                 |RUNNABLE      |5        |false  |5        |475892000  |535431000  |0       |-1          |0      |-1         |
|25 |JNA Cleaner                       |jvmm                 |WAITING       |401      |true   |5        |38175000   |39948000   |623     |-1          |3      |-1         |
|26 |pool-1-thread-1                   |jvmm                 |WAITING       |657      |false  |5        |44815000   |67730000   |1       |-1          |7      |-1         |
|29 |ForkJoinPool.commonPool-worker-5  |jvmm                 |WAITING       |657      |true   |5        |921000     |1469000    |0       |-1          |2      |-1         |
|31 |ForkJoinPool.commonPool-worker-23 |jvmm                 |WAITING       |657      |true   |5        |335000     |606000     |0       |-1          |2      |-1         |
|30 |ForkJoinPool.commonPool-worker-9  |jvmm                 |WAITING       |657      |true   |5        |555000     |998000     |0       |-1          |2      |-1         |
|32 |ForkJoinPool.commonPool-worker-27 |jvmm                 |WAITING       |657      |true   |5        |355000     |716000     |0       |-1          |2      |-1         |
|36 |ForkJoinPool.commonPool-worker-3  |jvmm                 |WAITING       |657      |true   |5        |401000     |717000     |0       |-1          |2      |-1         |
|35 |ForkJoinPool.commonPool-worker-17 |jvmm                 |WAITING       |657      |true   |5        |233000     |510000     |0       |-1          |2      |-1         |
|34 |ForkJoinPool.commonPool-worker-31 |jvmm                 |WAITING       |657      |true   |5        |143000     |354000     |0       |-1          |2      |-1         |
|33 |ForkJoinPool.commonPool-worker-13 |jvmm                 |WAITING       |657      |true   |5        |263000     |586000     |0       |-1          |3      |-1         |
|37 |ForkJoinPool.commonPool-worker-21 |jvmm                 |WAITING       |657      |true   |5        |184000     |352000     |0       |-1          |2      |-1         |
|41 |ForkJoinPool.commonPool-worker-29 |jvmm                 |TIMED_WAITING |673      |true   |5        |130000     |224000     |0       |-1          |2      |-1         |
+---+----------------------------------+---------------------+--------------+---------+-------+---------+-----------+-----------+--------+------------+-------+-----------+
```

### jvm_thread_stack

```shell
info -t jvm_thread_stack
```

```text
2023-04-23 10:59:22 +0800
Full thread dump Java HotSpot(TM) 64-Bit Server VM Oracle Corporation (11.0.13+10-LTS-370):

"Reference Handler" daemon Id=2 group='system' pri=10 cpu=15625000 ns usr=15625000 ns blocked 1 for -1 ms waited 0 for -1 ms
   vm_state: RUNNABLE, os_state: 5
        at java.base@11.0.13/java.lang.ref.Reference.waitForReferencePendingList(Native Method)
        at java.base@11.0.13/java.lang.ref.Reference.processPendingReferences(Reference.java:241)
        at java.base@11.0.13/java.lang.ref.Reference$ReferenceHandler.run(Reference.java:213)

"Finalizer" daemon Id=3 group='system' pri=8 cpu=0 ns usr=0 ns blocked 3 for -1 ms waited 4 for -1 ms
   vm_state: WAITING, os_state: 401
        at java.base@11.0.13/java.lang.Object.wait(Native Method)
        - waiting on (a java.lang.ref.ReferenceQueue$Lock@5218f17a)
        at java.base@11.0.13/java.lang.ref.ReferenceQueue.remove(ReferenceQueue.java:155)
        at java.base@11.0.13/java.lang.ref.ReferenceQueue.remove(ReferenceQueue.java:176)
        at java.base@11.0.13/java.lang.ref.Finalizer$FinalizerThread.run(Finalizer.java:170)

"Signal Dispatcher" daemon Id=4 group='system' pri=9 cpu=0 ns usr=0 ns blocked 0 for -1 ms waited 0 for -1 ms
   vm_state: RUNNABLE, os_state: 5

"Attach Listener" daemon Id=5 group='system' pri=5 cpu=31250000 ns usr=31250000 ns blocked 1 for -1 ms waited 1 for -1 ms
   vm_state: RUNNABLE, os_state: 5

"JDWP Transport Listener: dt_socket" Id=11 cpu=0 ns usr=0 ns blocked 0 for -1 ms waited 0 for -1 ms
   vm_state: RUNNABLE

"JDWP Event Helper Thread" Id=12 cpu=125000000 ns usr=31250000 ns blocked 0 for -1 ms waited 0 for -1 ms
   vm_state: RUNNABLE

"JDWP Command Reader" Id=13 cpu=0 ns usr=0 ns blocked 0 for -1 ms waited 0 for -1 ms (in native)
   vm_state: RUNNABLE

"Common-Cleaner" daemon Id=14 group='InnocuousThreadGroup' pri=8 cpu=0 ns usr=0 ns blocked 6 for -1 ms waited 51 for -1 ms
   vm_state: TIMED_WAITING, os_state: 417
        at java.base@11.0.13/java.lang.Object.wait(Native Method)
        - waiting on (a java.lang.ref.ReferenceQueue$Lock@1eabb31a)
        at java.base@11.0.13/java.lang.ref.ReferenceQueue.remove(ReferenceQueue.java:155)
        at java.base@11.0.13/jdk.internal.ref.CleanerImpl.run(CleanerImpl.java:148)
        at java.base@11.0.13/java.lang.Thread.run(Thread.java:834)
        at java.base@11.0.13/jdk.internal.misc.InnocuousThread.run(InnocuousThread.java:134)

"DestroyJavaVM" Id=26 group='main' pri=5 cpu=1078125000 ns usr=875000000 ns blocked 0 for -1 ms waited 0 for -1 ms
   vm_state: RUNNABLE, os_state: 5

"lc-pool-4-2" Id=27 group='main' pri=5 cpu=0 ns usr=0 ns blocked 0 for -1 ms waited 233 for -1 ms
   vm_state: WAITING, os_state: 657
        at java.base@11.0.13/jdk.internal.misc.Unsafe.park(Native Method)
        - waiting on (a java.util.concurrent.locks.AbstractQueuedSynchronizer$ConditionObject@6395ff2f)
        at java.base@11.0.13/java.util.concurrent.locks.LockSupport.park(LockSupport.java:194)
        at java.base@11.0.13/java.util.concurrent.locks.AbstractQueuedSynchronizer$ConditionObject.await(AbstractQueuedSynchronizer.java:2081)
        at java.base@11.0.13/java.util.concurrent.ScheduledThreadPoolExecutor$DelayedWorkQueue.take(ScheduledThreadPoolExecutor.java:1177)
        at java.base@11.0.13/java.util.concurrent.ScheduledThreadPoolExecutor$DelayedWorkQueue.take(ScheduledThreadPoolExecutor.java:899)
        at java.base@11.0.13/java.util.concurrent.ThreadPoolExecutor.getTask(ThreadPoolExecutor.java:1054)
        at java.base@11.0.13/java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1114)
        at java.base@11.0.13/java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:628)
        at app//io.netty.util.concurrent.FastThreadLocalRunnable.run(FastThreadLocalRunnable.java:30)
        at java.base@11.0.13/java.lang.Thread.run(Thread.java:834)

"jvmm-1-thread-1" Id=32 group='jvmm' pri=5 cpu=1015625000 ns usr=781250000 ns blocked 2 for -1 ms waited 1 for -1 ms
   vm_state: RUNNABLE, os_state: 5
        locks java.util.concurrent.ThreadPoolExecutor$Worker@8bc5027
        at java.management@11.0.13/sun.management.ThreadImpl.dumpThreads0(Native Method)
        at java.management@11.0.13/sun.management.ThreadImpl.dumpAllThreads(ThreadImpl.java:521)
        at java.management@11.0.13/sun.management.ThreadImpl.dumpAllThreads(ThreadImpl.java:509)
        at org.beifengtz.jvmm.core.DefaultJvmmCollector.dumpAllThreads(DefaultJvmmCollector.java:324)
        at org.beifengtz.jvmm.server.controller.CollectController.jvmDumpThread(CollectController.java:160)
        at java.base@11.0.13/jdk.internal.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
        at java.base@11.0.13/jdk.internal.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:62)
        at java.base@11.0.13/jdk.internal.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
        at java.base@11.0.13/java.lang.reflect.Method.invoke(Method.java:566)
        at org.beifengtz.jvmm.convey.handler.JvmmChannelHandler.handleRequest(JvmmChannelHandler.java:283)
        at org.beifengtz.jvmm.convey.handler.JvmmChannelHandler.channelRead0(JvmmChannelHandler.java:149)
        at org.beifengtz.jvmm.convey.handler.JvmmChannelHandler.channelRead0(JvmmChannelHandler.java:57)
        at io.netty.channel.SimpleChannelInboundHandler.channelRead(SimpleChannelInboundHandler.java:99)
        at io.netty.channel.AbstractChannelHandlerContext.invokeChannelRead(AbstractChannelHandlerContext.java:379)
        at io.netty.channel.AbstractChannelHandlerContext.invokeChannelRead(AbstractChannelHandlerContext.java:365)
        at io.netty.channel.AbstractChannelHandlerContext.fireChannelRead(AbstractChannelHandlerContext.java:357)
        at io.netty.handler.codec.MessageToMessageDecoder.channelRead(MessageToMessageDecoder.java:103)
        at io.netty.channel.AbstractChannelHandlerContext.invokeChannelRead(AbstractChannelHandlerContext.java:379)
        at io.netty.channel.AbstractChannelHandlerContext.invokeChannelRead(AbstractChannelHandlerContext.java:365)
        at io.netty.channel.AbstractChannelHandlerContext.fireChannelRead(AbstractChannelHandlerContext.java:357)
        at io.netty.handler.codec.MessageToMessageDecoder.channelRead(MessageToMessageDecoder.java:103)
        at io.netty.channel.AbstractChannelHandlerContext.invokeChannelRead(AbstractChannelHandlerContext.java:379)
        at io.netty.channel.AbstractChannelHandlerContext.invokeChannelRead(AbstractChannelHandlerContext.java:365)
        at io.netty.channel.AbstractChannelHandlerContext.fireChannelRead(AbstractChannelHandlerContext.java:357)
        at io.netty.handler.codec.ByteToMessageDecoder.fireChannelRead(ByteToMessageDecoder.java:324)
        at io.netty.handler.codec.ByteToMessageDecoder.channelRead(ByteToMessageDecoder.java:296)
        at io.netty.channel.AbstractChannelHandlerContext.invokeChannelRead(AbstractChannelHandlerContext.java:379)
        at io.netty.channel.AbstractChannelHandlerContext.invokeChannelRead(AbstractChannelHandlerContext.java:365)
        at io.netty.channel.AbstractChannelHandlerContext.fireChannelRead(AbstractChannelHandlerContext.java:357)
        at io.netty.handler.timeout.IdleStateHandler.channelRead(IdleStateHandler.java:286)
        at io.netty.channel.AbstractChannelHandlerContext.invokeChannelRead(AbstractChannelHandlerContext.java:379)
        at io.netty.channel.AbstractChannelHandlerContext.invokeChannelRead(AbstractChannelHandlerContext.java:365)
        at io.netty.channel.AbstractChannelHandlerContext.fireChannelRead(AbstractChannelHandlerContext.java:357)
        at io.netty.channel.DefaultChannelPipeline$HeadContext.channelRead(DefaultChannelPipeline.java:1410)
        at io.netty.channel.AbstractChannelHandlerContext.invokeChannelRead(AbstractChannelHandlerContext.java:379)
        at io.netty.channel.AbstractChannelHandlerContext.invokeChannelRead(AbstractChannelHandlerContext.java:365)
        at io.netty.channel.DefaultChannelPipeline.fireChannelRead(DefaultChannelPipeline.java:919)
        at io.netty.channel.nio.AbstractNioByteChannel$NioByteUnsafe.read(AbstractNioByteChannel.java:166)
        at io.netty.channel.nio.NioEventLoop.processSelectedKey(NioEventLoop.java:719)
        at io.netty.channel.nio.NioEventLoop.processSelectedKeysOptimized(NioEventLoop.java:655)
        at io.netty.channel.nio.NioEventLoop.processSelectedKeys(NioEventLoop.java:581)
        at io.netty.channel.nio.NioEventLoop.run(NioEventLoop.java:493)
        at io.netty.util.concurrent.SingleThreadEventExecutor$4.run(SingleThreadEventExecutor.java:989)
        at io.netty.util.internal.ThreadExecutorMap$2.run(ThreadExecutorMap.java:74)
        at java.base@11.0.13/java.util.concurrent.Executors$RunnableAdapter.call(Executors.java:515)
        at java.base@11.0.13/java.util.concurrent.FutureTask.run(FutureTask.java:264)
        at java.base@11.0.13/java.util.concurrent.ScheduledThreadPoolExecutor$ScheduledFutureTask.run(ScheduledThreadPoolExecutor.java:304)
        at java.base@11.0.13/java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1128)
        at java.base@11.0.13/java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:628)
        at java.base@11.0.13/java.lang.Thread.run(Thread.java:834)

"JNA Cleaner" daemon Id=34 group='jvmm' pri=5 cpu=15625000 ns usr=15625000 ns blocked 10 for -1 ms waited 2 for -1 ms
   vm_state: WAITING, os_state: 401
        at java.base@11.0.13/java.lang.Object.wait(Native Method)
        - waiting on (a java.lang.ref.ReferenceQueue$Lock@19c422c6)
        at java.base@11.0.13/java.lang.ref.ReferenceQueue.remove(ReferenceQueue.java:155)
        at java.base@11.0.13/java.lang.ref.ReferenceQueue.remove(ReferenceQueue.java:176)
        at com.sun.jna.internal.Cleaner$1.run(Cleaner.java:58)

"pool-5-thread-1" Id=35 group='jvmm' pri=5 cpu=140625000 ns usr=46875000 ns blocked 1 for -1 ms waited 7 for -1 ms
   vm_state: WAITING, os_state: 657
        at java.base@11.0.13/jdk.internal.misc.Unsafe.park(Native Method)
        - waiting on (a java.util.concurrent.locks.AbstractQueuedSynchronizer$ConditionObject@5ebac2c6)
        at java.base@11.0.13/java.util.concurrent.locks.LockSupport.park(LockSupport.java:194)
        at java.base@11.0.13/java.util.concurrent.locks.AbstractQueuedSynchronizer$ConditionObject.await(AbstractQueuedSynchronizer.java:2081)
        at java.base@11.0.13/java.util.concurrent.ScheduledThreadPoolExecutor$DelayedWorkQueue.take(ScheduledThreadPoolExecutor.java:1170)
        at java.base@11.0.13/java.util.concurrent.ScheduledThreadPoolExecutor$DelayedWorkQueue.take(ScheduledThreadPoolExecutor.java:899)
        at java.base@11.0.13/java.util.concurrent.ThreadPoolExecutor.getTask(ThreadPoolExecutor.java:1054)
        at java.base@11.0.13/java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1114)
        at java.base@11.0.13/java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:628)
        at java.base@11.0.13/java.lang.Thread.run(Thread.java:834)

"ForkJoinPool.commonPool-worker-5" daemon Id=37 group='jvmm' pri=5 cpu=15625000 ns usr=15625000 ns blocked 0 for -1 ms waited 3 for -1 ms
   vm_state: WAITING, os_state: 657
        at java.base@11.0.13/jdk.internal.misc.Unsafe.park(Native Method)
        - waiting on (a java.util.concurrent.ForkJoinPool@6067af6c)
        at java.base@11.0.13/java.util.concurrent.locks.LockSupport.park(LockSupport.java:194)
        at java.base@11.0.13/java.util.concurrent.ForkJoinPool.runWorker(ForkJoinPool.java:1628)
        at java.base@11.0.13/java.util.concurrent.ForkJoinWorkerThread.run(ForkJoinWorkerThread.java:183)
```

### jvm_thread_pool

```shell
info -t jvm_thread_pool -clazz org.beifengtz.jvmm.common.factory.ExecutorFactory -field SCHEDULE_THREAD_POOL
```

```json
{
  "threadFactory": "org.beifengtz.jvmm.common.factory.ExecutorFactory$DefaultThreadFactory",
  "rejectHandler": "java.util.concurrent.ThreadPoolExecutor$CallerRunsPolicy",
  "corePoolSize": 2,
  "maximumPoolSize": 2147483647,
  "keepAliveMillis": 0,
  "queue": "java.util.concurrent.ScheduledThreadPoolExecutor$DelayedWorkQueue",
  "state": "Running",
  "allowsCoreThreadTimeOut": false,
  "queueSize": 0,
  "threadCount": 1,
  "activeThreadCount": 1,
  "largestThreadCount": 1,
  "taskCount": 1,
  "completedTaskCount": 0
}
```

### port

```shell
info -t port -p 3306,5010,5011,8080
```

```json
{
  "running": [
    3306,
    5010,
    8080
  ],
  "stopped": [
    5011
  ]
}
```

# metric

## thread_cpu_time

从指令执行开始，进行采集 `d` 秒，输出线程占用CPU情况（按 CPU Time 降序排序）

```shell
metric -t thread_cpu_time -d 3
```

```text
+---+----------------------------------+---------------------+--------------+--------------+-------------+
|ID |Name                              |Group                |State         |User Time(ns) |CPU Time(ns) |
+---+----------------------------------+---------------------+--------------+--------------+-------------+
|22 |jvmm-1-thread-1                   |jvmm                 |RUNNABLE      |3357000       |3837000      |
|2  |Reference Handler                 |system               |RUNNABLE      |0             |0            |
|3  |Finalizer                         |system               |WAITING       |0             |0            |
|4  |Signal Dispatcher                 |system               |RUNNABLE      |0             |0            |
|19 |Common-Cleaner                    |InnocuousThreadGroup |TIMED_WAITING |0             |0            |
|23 |DestroyJavaVM                     |main                 |RUNNABLE      |0             |0            |
|25 |JNA Cleaner                       |jvmm                 |WAITING       |0             |0            |
|26 |pool-1-thread-1                   |jvmm                 |WAITING       |0             |0            |
|31 |ForkJoinPool.commonPool-worker-23 |jvmm                 |WAITING       |0             |0            |
|32 |ForkJoinPool.commonPool-worker-27 |jvmm                 |WAITING       |0             |0            |
|36 |ForkJoinPool.commonPool-worker-3  |jvmm                 |WAITING       |0             |0            |
|34 |ForkJoinPool.commonPool-worker-31 |jvmm                 |TIMED_WAITING |0             |0            |
|33 |ForkJoinPool.commonPool-worker-13 |jvmm                 |WAITING       |0             |0            |
|37 |ForkJoinPool.commonPool-worker-21 |jvmm                 |WAITING       |0             |0            |
+---+----------------------------------+---------------------+--------------+--------------+-------------+
```

你也可以输出线程堆栈（按 CPU Time 降序排序）
```shell
metric -t thread_cpu_time -d 3 -f stack
```

```text
"jvmm-1-thread-1" Id=22 group='jvmm' pri=5 cpu=1862261000 ns usr=1739576000 ns blocked 10 for -1 ms waited 7 for -1 ms (in native)
   vm_state: RUNNABLE, os_state: 5
	locks java.util.concurrent.ThreadPoolExecutor$Worker@f107c50
	at app//io.netty.channel.kqueue.Native.keventWait(Native Method)
	at app//io.netty.channel.kqueue.Native.keventWait(Native.java:124)
	at app//io.netty.channel.kqueue.KQueueEventLoop.kqueueWait(KQueueEventLoop.java:179)
	at app//io.netty.channel.kqueue.KQueueEventLoop.kqueueWait(KQueueEventLoop.java:171)
	at app//io.netty.channel.kqueue.KQueueEventLoop.run(KQueueEventLoop.java:240)
	at app//io.netty.util.concurrent.SingleThreadEventExecutor$4.run(SingleThreadEventExecutor.java:997)
	at app//io.netty.util.internal.ThreadExecutorMap$2.run(ThreadExecutorMap.java:74)
	at java.base@11.0.13/java.util.concurrent.Executors$RunnableAdapter.call(Executors.java:515)
	at java.base@11.0.13/java.util.concurrent.FutureTask.run(FutureTask.java:264)
	at java.base@11.0.13/java.util.concurrent.ScheduledThreadPoolExecutor$ScheduledFutureTask.run(ScheduledThreadPoolExecutor.java:304)
	at java.base@11.0.13/java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1128)
	at java.base@11.0.13/java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:628)
	at java.base@11.0.13/java.lang.Thread.run(Thread.java:834)


"jvmm-1-thread-2" Id=43 group='jvmm' pri=5 cpu=4897000 ns usr=4401000 ns blocked 0 for -1 ms waited 3 for -1 ms
   vm_state: RUNNABLE, os_state: 5
	locks java.util.concurrent.ThreadPoolExecutor$Worker@4c00fe3f
	at java.management@11.0.13/sun.management.ThreadImpl.dumpThreads0(Native Method)
	at java.management@11.0.13/sun.management.ThreadImpl.dumpAllThreads(ThreadImpl.java:521)
	at java.management@11.0.13/sun.management.ThreadImpl.dumpAllThreads(ThreadImpl.java:509)
	at app//org.beifengtz.jvmm.core.DefaultJvmmCollector.lambda$getOrderedThreadTimedStack$3(DefaultJvmmCollector.java:613)
	at app//org.beifengtz.jvmm.core.DefaultJvmmCollector$$Lambda$150/0x0000000800266440.run(Unknown Source)
	at java.base@11.0.13/java.util.concurrent.Executors$RunnableAdapter.call(Executors.java:515)
	at java.base@11.0.13/java.util.concurrent.FutureTask.run(FutureTask.java:264)
	at java.base@11.0.13/java.util.concurrent.ScheduledThreadPoolExecutor$ScheduledFutureTask.run(ScheduledThreadPoolExecutor.java:304)
	at java.base@11.0.13/java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1128)
	at java.base@11.0.13/java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:628)
	at java.base@11.0.13/java.lang.Thread.run(Thread.java:834)


"Reference Handler" daemon Id=2 group='system' pri=10 cpu=31806000 ns usr=30622000 ns blocked 6 for -1 ms waited 0 for -1 ms
   vm_state: RUNNABLE, os_state: 5
	at java.base@11.0.13/java.lang.ref.Reference.waitForReferencePendingList(Native Method)
	at java.base@11.0.13/java.lang.ref.Reference.processPendingReferences(Reference.java:241)
	at java.base@11.0.13/java.lang.ref.Reference$ReferenceHandler.run(Reference.java:213)


"Finalizer" daemon Id=3 group='system' pri=8 cpu=235000 ns usr=172000 ns blocked 1 for -1 ms waited 2 for -1 ms
   vm_state: WAITING, os_state: 401
	at java.base@11.0.13/java.lang.Object.wait(Native Method)
	- waiting on (a java.lang.ref.ReferenceQueue$Lock@5da0cc32)
	at java.base@11.0.13/java.lang.ref.ReferenceQueue.remove(ReferenceQueue.java:155)
	at java.base@11.0.13/java.lang.ref.ReferenceQueue.remove(ReferenceQueue.java:176)
	at java.base@11.0.13/java.lang.ref.Finalizer$FinalizerThread.run(Finalizer.java:170)


"Signal Dispatcher" daemon Id=4 group='system' pri=9 cpu=44000 ns usr=16000 ns blocked 0 for -1 ms waited 0 for -1 ms
   vm_state: RUNNABLE, os_state: 5


"Common-Cleaner" daemon Id=19 group='InnocuousThreadGroup' pri=8 cpu=1347000 ns usr=879000 ns blocked 3 for -1 ms waited 18 for -1 ms
   vm_state: TIMED_WAITING, os_state: 417
	at java.base@11.0.13/java.lang.Object.wait(Native Method)
	- waiting on (a java.lang.ref.ReferenceQueue$Lock@626146cf)
	at java.base@11.0.13/java.lang.ref.ReferenceQueue.remove(ReferenceQueue.java:155)
	at java.base@11.0.13/jdk.internal.ref.CleanerImpl.run(CleanerImpl.java:148)
	at java.base@11.0.13/java.lang.Thread.run(Thread.java:834)
	at java.base@11.0.13/jdk.internal.misc.InnocuousThread.run(InnocuousThread.java:134)


"DestroyJavaVM" Id=23 group='main' pri=5 cpu=535431000 ns usr=475892000 ns blocked 0 for -1 ms waited 0 for -1 ms
   vm_state: RUNNABLE, os_state: 5


"JNA Cleaner" daemon Id=25 group='jvmm' pri=5 cpu=39948000 ns usr=38175000 ns blocked 623 for -1 ms waited 3 for -1 ms
   vm_state: WAITING, os_state: 401
	at java.base@11.0.13/java.lang.Object.wait(Native Method)
	- waiting on (a java.lang.ref.ReferenceQueue$Lock@45795de3)
	at java.base@11.0.13/java.lang.ref.ReferenceQueue.remove(ReferenceQueue.java:155)
	at java.base@11.0.13/java.lang.ref.ReferenceQueue.remove(ReferenceQueue.java:176)
	at app//com.sun.jna.internal.Cleaner$1.run(Cleaner.java:58)


"pool-1-thread-1" Id=26 group='jvmm' pri=5 cpu=67730000 ns usr=44815000 ns blocked 1 for -1 ms waited 7 for -1 ms
   vm_state: WAITING, os_state: 657
	at java.base@11.0.13/jdk.internal.misc.Unsafe.park(Native Method)
	- waiting on (a java.util.concurrent.locks.AbstractQueuedSynchronizer$ConditionObject@71c41f)
	at java.base@11.0.13/java.util.concurrent.locks.LockSupport.park(LockSupport.java:194)
	at java.base@11.0.13/java.util.concurrent.locks.AbstractQueuedSynchronizer$ConditionObject.await(AbstractQueuedSynchronizer.java:2081)
	at java.base@11.0.13/java.util.concurrent.ScheduledThreadPoolExecutor$DelayedWorkQueue.take(ScheduledThreadPoolExecutor.java:1170)
	at java.base@11.0.13/java.util.concurrent.ScheduledThreadPoolExecutor$DelayedWorkQueue.take(ScheduledThreadPoolExecutor.java:899)
	at java.base@11.0.13/java.util.concurrent.ThreadPoolExecutor.getTask(ThreadPoolExecutor.java:1054)
	at java.base@11.0.13/java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1114)
	at java.base@11.0.13/java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:628)
	at java.base@11.0.13/java.lang.Thread.run(Thread.java:834)


"ForkJoinPool.commonPool-worker-23" daemon Id=31 group='jvmm' pri=5 cpu=649000 ns usr=372000 ns blocked 0 for -1 ms waited 3 for -1 ms
   vm_state: TIMED_WAITING, os_state: 673
	at java.base@11.0.13/jdk.internal.misc.Unsafe.park(Native Method)
	- waiting on (a java.util.concurrent.ForkJoinPool@5b6b497f)
	at java.base@11.0.13/java.util.concurrent.locks.LockSupport.parkUntil(LockSupport.java:275)
	at java.base@11.0.13/java.util.concurrent.ForkJoinPool.runWorker(ForkJoinPool.java:1619)
	at java.base@11.0.13/java.util.concurrent.ForkJoinWorkerThread.run(ForkJoinWorkerThread.java:183)


"ForkJoinPool.commonPool-worker-27" daemon Id=32 group='jvmm' pri=5 cpu=716000 ns usr=355000 ns blocked 0 for -1 ms waited 2 for -1 ms
   vm_state: WAITING, os_state: 657
	at java.base@11.0.13/jdk.internal.misc.Unsafe.park(Native Method)
	- waiting on (a java.util.concurrent.ForkJoinPool@5b6b497f)
	at java.base@11.0.13/java.util.concurrent.locks.LockSupport.park(LockSupport.java:194)
	at java.base@11.0.13/java.util.concurrent.ForkJoinPool.runWorker(ForkJoinPool.java:1628)
	at java.base@11.0.13/java.util.concurrent.ForkJoinWorkerThread.run(ForkJoinWorkerThread.java:183)


"ForkJoinPool.commonPool-worker-21" daemon Id=37 group='jvmm' pri=5 cpu=352000 ns usr=184000 ns blocked 0 for -1 ms waited 2 for -1 ms
   vm_state: WAITING, os_state: 657
	at java.base@11.0.13/jdk.internal.misc.Unsafe.park(Native Method)
	- waiting on (a java.util.concurrent.ForkJoinPool@5b6b497f)
	at java.base@11.0.13/java.util.concurrent.locks.LockSupport.park(LockSupport.java:194)
	at java.base@11.0.13/java.util.concurrent.ForkJoinPool.runWorker(ForkJoinPool.java:1628)
	at java.base@11.0.13/java.util.concurrent.ForkJoinWorkerThread.run(ForkJoinWorkerThread.java:183)
```

# jad

jad可以查看运行时的Java代码

```shell
jad -c org.beifengtz.jvmm.common.util.AssertUtil
```

```java
/*
 * Decompiled with CFR.
 */
package org.beifengtz.jvmm.common.util;

public class AssertUtil {
    public static void checkArguments(boolean condition) {
        AssertUtil.checkArguments(condition, "Invalid argument");
    }

    public static void checkArguments(boolean condition, String message) {
        if (!condition) {
            throw new IllegalArgumentException(message);
        }
    }

    public static <T> T notNull(T target, String format, Object ... args) {
        if (target == null) {
            throw new NullPointerException(String.format(format, args));
        }
        return target;
    }

    public static <T> T[] notEmpty(T[] array, String message, Object ... values) {
        if (array == null) {
            throw new NullPointerException(String.format(message, values));
        }
        if (array.length == 0) {
            throw new IllegalArgumentException(String.format(message, values));
        }
        return array;
    }
}
```

# jtool

jtool可以远程执行 jdk tool指令

```shell
jtool jstat -gc 13121
```

```text
S0C    S1C    S0U    S1U      EC       EU        OC         OU       MC     MU    CCSC   CCSU   YGC     YGCT    FGC    FGCT    CGC    CGCT     GCT   
0.0   3072.0  0.0   3072.0 161792.0  2048.0   97280.0     1687.5   17152.0 16646.1 1792.0 1688.9      1    0.013   0      0.000   0      0.000    0.013
```

# sw

查看和设置一些采集开关

```shell
sw
```

```text
+------------------+----------------+---------------+------------------+
|Thread Contention |Thread CPU Time |Memory Verbose |Classload Verbose |
+------------------+----------------+---------------+------------------+
|false             |true            |false          |false             |
+------------------+----------------+---------------+------------------+
```

关闭某一个开关
```shell
sw -close threadCpuTime
```

打开某一个开关
```shell
sw -open threadCpuTime
```
