# Collections

下面提供一些简单的数据采集示例，以 `json` 格式展示，每一个信息都与 core 模块中的 Java Entity对应。

## Machine Collections

下面是物理机器相关的采集示例

### process

```shell
info -t process
```

```json
{
   "name":"16056@DESKTOP-236E652",
   "startTime":1682215965485,
   "uptime":80371,
   "pid":16056,
   "vmVersion":"11.0.13+10-LTS-370",
   "vmVendor":"Oracle Corporation",
   "vmName":"Java HotSpot(TM) 64-Bit Server VM",
   "vmHome":"D:\\Java\\jdk-11.0.13",
   "vmManagementSpecVersion":"2.0",
   "vmSpecName":"Java Virtual Machine Specification",
   "vmSpecVendor":"Oracle Corporation",
   "vmSpecVersion":"11",
   "inputArgs": [
      "-Dlog4j.skipJansi=false",
      "-agentlib:jdwp=transport=dt_socket, server=n, suspend=y,address=127.0.0.1:63212",
      "-Dfile.encoding=UTF-8",
      "-Duser.country=CN",
      "-Duser.language=zh",
      "-Duser.variant"
   ],
   "workDir":"E:\\Project\\ProjectChain\\C1\\C1Server"
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

### diskio

```shell
info -t diskio
```

```json
[
   {
      "name":"PHYSICALDRIVE1",
      "readPerSecond":0.0,
      "writePerSecond":16.0,
      "readBytesPerSecond":0.0,
      "writeBytesPerSecond":65536.0,
      "currentQueueLength":0
   },
   {
      "name":"PHYSICALDRIVE0",
      "readPerSecond":6.0,
      "writePerSecond":41.0,
      "readBytesPerSecond":827392.0,
      "writeBytesPerSecond":2296320.0,
      "currentQueueLength":0
   }
]
```

### cpu

```shell
info -t cpu
```

```json
{
   "cpuNum":6,
   "sys":0.1510177281680893,
   "user":0.02314510833880499,
   "ioWait":0.0,
   "idle":0.8205843729481287
}
```

### net

```shell
info -t net
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

```json
{
   "name":"Windows 10",
   "version":"10.0",
   "arch":"amd64",
   "cpuNum":6,
   "timeZone":"Asia/Shanghai",
   "ip":"192.168.0.198",
   "user":"Administrator"
}
```

### sysMem

```shell
info -t sysMem
```

```json
{
   "committedVirtual":435396608,
   "totalPhysical":17120530432,
   "freePhysical":2682462208,
   "totalSwap":34562719744,
   "freeSwap":2682462208,
   "bufferCache":0,
   "shared":0
}
```

### sysFile

```shell
info -t sysFile
```

```json
[
   {
      "name":"本地固定磁盘 (C:)",
      "mount":"C:\\",
      "label":"System",
      "type":"NTFS",
      "size":255817510912,
      "free":81914798080,
      "usable":81914798080
   },
   {
      "name":"本地固定磁盘 (D:)",
      "mount":"D:\\",
      "label":"Soft",
      "type":"NTFS",
      "size":334340550656,
      "free":239368650752,
      "usable":239368650752
   },
   {
      "name":"本地固定磁盘 (E:)",
      "mount":"E:\\",
      "label":"Work",
      "type":"NTFS",
      "size":665844707328,
      "free":383934816256,
      "usable":383934816256
   }
]
```


## JVM Collections

下面是 Java 虚拟机相关的采集示例

### cLoading

```shell
info -t cLoading
```

```json
{
   "verbose":false,
   "loadedClassCount":5807,
   "unLoadedClassCount":0,
   "totalLoadedClassCount":5807
}
```

### cLoader

```shell
info -t cLoader
```

```json
[
   {
      "name":"jdk.internal.loader.ClassLoaders$AppClassLoader",
      "hash":1768305536,
      "parents": [
         "jdk.internal.loader.ClassLoaders$PlatformClassLoader"
      ]
   },
   {
      "name":"org.beifengtz.jvmm.agent.JvmmAgentClassLoader",
      "hash":1656061862,
      "parents": [
         "jdk.internal.loader.ClassLoaders$AppClassLoader",
         "jdk.internal.loader.ClassLoaders$PlatformClassLoader"
      ]
   }
]
```

### comp

```shell
info -t comp
```

```json
{
   "name":"HotSpot 64-Bit Tiered Compilers",
   "timeMonitoringSupported":true,
   "totalCompilationTime":3439
}
```

### gc

```shell
info -t gc
```

```json
[
   {
      "name":"G1 Young Generation",
      "valid":true,
      "collectionCount":4,
      "collectionTime":21,
      "memoryPoolNames": [
         "G1 Eden Space",
         "G1 Survivor Space",
         "G1 Old Gen"
      ]
   },
   {
      "name":"G1 Old Generation",
      "valid":true,
      "collectionCount":0,
      "collectionTime":0,
      "memoryPoolNames": [
         "G1 Eden Space",
         "G1 Survivor Space",
         "G1 Old Gen"
      ]
   }
]
```

### jvmMem

```shell
info -t jvmMem
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

### memManager

```shell
info -t memManager
```

```json
[
   {
      "name":"CodeCacheManager",
      "valid":true,
      "memoryPoolNames": [
         "CodeHeap 'non-nmethods'",
         "CodeHeap 'profiled nmethods'",
         "CodeHeap 'non-profiled nmethods'"
      ]
   },
   {
      "name":"Metaspace Manager",
      "valid":true,
      "memoryPoolNames": [
         "Metaspace",
         "Compressed Class Space"
      ]
   },
   {
      "name":"G1 Young Generation",
      "valid":true,
      "memoryPoolNames": [
         "G1 Eden Space",
         "G1 Survivor Space",
         "G1 Old Gen"
      ]
   },
   {
      "name":"G1 Old Generation",
      "valid":true,
      "memoryPoolNames": [
         "G1 Eden Space",
         "G1 Survivor Space",
         "G1 Old Gen"
      ]
   }
]
```

### memPool

```shell
info -t memPool
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

### thread

```shell
info -t thread
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

### threadDetail

```shell
info -t threadDetail
```

```json
[
   {
      "id":2,
      "name":"Reference Handler",
      "group":"system",
      "state":"RUNNABLE",
      "osState":5,
      "daemon":true,
      "priority":10,
      "userTime":15625000,
      "cpuTime":15625000,
      "blockedCount":1,
      "blockedTime":-1,
      "waitedCount":0,
      "waitedTime":-1,
      "locks": [

      ]
   },
   {
      "id":3,
      "name":"Finalizer",
      "group":"system",
      "state":"WAITING",
      "osState":401,
      "daemon":true,
      "priority":8,
      "userTime":0,
      "cpuTime":0,
      "blockedCount":3,
      "blockedTime":-1,
      "waitedCount":4,
      "waitedTime":-1,
      "locks": [

      ]
   },
   {
      "id":4,
      "name":"Signal Dispatcher",
      "group":"system",
      "state":"RUNNABLE",
      "osState":5,
      "daemon":true,
      "priority":9,
      "userTime":0,
      "cpuTime":0,
      "blockedCount":0,
      "blockedTime":-1,
      "waitedCount":0,
      "waitedTime":-1,
      "locks": [

      ]
   },
   {
      "id":5,
      "name":"Attach Listener",
      "group":"system",
      "state":"RUNNABLE",
      "osState":5,
      "daemon":true,
      "priority":5,
      "userTime":31250000,
      "cpuTime":31250000,
      "blockedCount":1,
      "blockedTime":-1,
      "waitedCount":1,
      "waitedTime":-1,
      "locks": [

      ]
   },
   {
      "id":26,
      "name":"DestroyJavaVM",
      "group":"main",
      "state":"RUNNABLE",
      "osState":5,
      "daemon":false,
      "priority":5,
      "userTime":875000000,
      "cpuTime":1078125000,
      "blockedCount":0,
      "blockedTime":-1,
      "waitedCount":0,
      "waitedTime":-1,
      "locks": [

      ]
   },
   {
      "id":34,
      "name":"JNA Cleaner",
      "group":"jvmm",
      "state":"WAITING",
      "osState":401,
      "daemon":true,
      "priority":5,
      "userTime":15625000,
      "cpuTime":15625000,
      "blockedCount":10,
      "blockedTime":-1,
      "waitedCount":2,
      "waitedTime":-1,
      "locks": [

      ]
   },
   {
      "id":35,
      "name":"pool-5-thread-1",
      "group":"jvmm",
      "state":"WAITING",
      "osState":657,
      "daemon":false,
      "priority":5,
      "userTime":46875000,
      "cpuTime":140625000,
      "blockedCount":1,
      "blockedTime":-1,
      "waitedCount":7,
      "waitedTime":-1,
      "locks": [

      ]
   },
   {
      "id":37,
      "name":"ForkJoinPool.commonPool-worker-5",
      "group":"jvmm",
      "state":"WAITING",
      "osState":657,
      "daemon":true,
      "priority":5,
      "userTime":15625000,
      "cpuTime":15625000,
      "blockedCount":0,
      "blockedTime":-1,
      "waitedCount":3,
      "waitedTime":-1,
      "locks": [

      ]
   }
]
```

### threadStack

```shell
info -t threadStack
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