package org.beifengtz.jvmm.core.entity;

import org.beifengtz.jvmm.common.JsonParsable;
import org.beifengtz.jvmm.core.entity.info.*;

import java.util.List;

/**
 * Description: TODO
 * <p>
 * Created in 9:58 2022/8/30
 *
 * @author beifengtz
 */
public class JvmmData implements JsonParsable {
    private String node;

    private ProcessInfo process;
    private List<DiskInfo> disk;
    private List<DiskIOInfo> diskIO;
    private CPUInfo cpu;
    private NetInfo network;
    private SysInfo sys;
    private SysMemInfo sysMem;
    private List<SysFileInfo> sysFile;
    private List<Integer> invalidPort;
    private JvmClassLoadingInfo jvmClassLoading;
    private List<JvmClassLoaderInfo> jvmClassLoader;
    private JvmCompilationInfo jvmCompilation;
    private List<JvmGCInfo> jvmGc;
    private JvmMemoryInfo jvmMemory;
    private List<JvmMemoryManagerInfo> jvmMemoryManager;
    private List<JvmMemoryPoolInfo> jvmMemoryPool;
    private JvmThreadInfo jvmThread;
    private String[] jvmStack;
    private JvmThreadDetailInfo[] jvmThreadDetail;
    private List<ThreadPoolInfo> threadPool;

    public String getNode() {
        return node;
    }

    public JvmmData setNode(String node) {
        this.node = node;
        return this;
    }

    public ProcessInfo getProcess() {
        return process;
    }

    public JvmmData setProcess(ProcessInfo process) {
        this.process = process;
        return this;
    }

    public List<DiskInfo> getDisk() {
        return disk;
    }

    public JvmmData setDisk(List<DiskInfo> disk) {
        this.disk = disk;
        return this;
    }

    public List<DiskIOInfo> getDiskIO() {
        return diskIO;
    }

    public JvmmData setDiskIO(List<DiskIOInfo> diskIO) {
        this.diskIO = diskIO;
        return this;
    }

    public CPUInfo getCpu() {
        return cpu;
    }

    public JvmmData setCpu(CPUInfo cpu) {
        this.cpu = cpu;
        return this;
    }

    public NetInfo getNetwork() {
        return network;
    }

    public JvmmData setNetwork(NetInfo network) {
        this.network = network;
        return this;
    }

    public SysInfo getSys() {
        return sys;
    }

    public JvmmData setSys(SysInfo sys) {
        this.sys = sys;
        return this;
    }

    public SysMemInfo getSysMem() {
        return sysMem;
    }

    public JvmmData setSysMem(SysMemInfo sysMem) {
        this.sysMem = sysMem;
        return this;
    }

    public List<SysFileInfo> getSysFile() {
        return sysFile;
    }

    public JvmmData setSysFile(List<SysFileInfo> sysFile) {
        this.sysFile = sysFile;
        return this;
    }

    public List<Integer> getInvalidPort() {
        return invalidPort;
    }

    public JvmmData setInvalidPort(List<Integer> invalidPort) {
        this.invalidPort = invalidPort;
        return this;
    }

    public JvmClassLoadingInfo getJvmClassLoading() {
        return jvmClassLoading;
    }

    public JvmmData setJvmClassLoading(JvmClassLoadingInfo jvmClassLoading) {
        this.jvmClassLoading = jvmClassLoading;
        return this;
    }

    public List<JvmClassLoaderInfo> getJvmClassLoader() {
        return jvmClassLoader;
    }

    public JvmmData setJvmClassLoader(List<JvmClassLoaderInfo> jvmClassLoader) {
        this.jvmClassLoader = jvmClassLoader;
        return this;
    }

    public JvmCompilationInfo getJvmCompilation() {
        return jvmCompilation;
    }

    public JvmmData setJvmCompilation(JvmCompilationInfo jvmCompilation) {
        this.jvmCompilation = jvmCompilation;
        return this;
    }

    public List<JvmGCInfo> getJvmGc() {
        return jvmGc;
    }

    public JvmmData setJvmGc(List<JvmGCInfo> jvmGc) {
        this.jvmGc = jvmGc;
        return this;
    }

    public JvmMemoryInfo getJvmMemory() {
        return jvmMemory;
    }

    public JvmmData setJvmMemory(JvmMemoryInfo jvmMemory) {
        this.jvmMemory = jvmMemory;
        return this;
    }

    public List<JvmMemoryManagerInfo> getJvmMemoryManager() {
        return jvmMemoryManager;
    }

    public JvmmData setJvmMemoryManager(List<JvmMemoryManagerInfo> jvmMemoryManager) {
        this.jvmMemoryManager = jvmMemoryManager;
        return this;
    }

    public List<JvmMemoryPoolInfo> getJvmMemoryPool() {
        return jvmMemoryPool;
    }

    public JvmmData setJvmMemoryPool(List<JvmMemoryPoolInfo> jvmMemoryPool) {
        this.jvmMemoryPool = jvmMemoryPool;
        return this;
    }

    public JvmThreadInfo getJvmThread() {
        return jvmThread;
    }

    public JvmmData setJvmThread(JvmThreadInfo jvmThread) {
        this.jvmThread = jvmThread;
        return this;
    }

    public String[] getJvmStack() {
        return jvmStack;
    }

    public JvmmData setJvmStack(String[] jvmStack) {
        this.jvmStack = jvmStack;
        return this;
    }

    public JvmThreadDetailInfo[] getJvmThreadDetail() {
        return jvmThreadDetail;
    }

    public JvmmData setJvmThreadDetail(JvmThreadDetailInfo[] jvmThreadDetail) {
        this.jvmThreadDetail = jvmThreadDetail;
        return this;
    }

    public List<ThreadPoolInfo> getThreadPool() {
        return threadPool;
    }

    public JvmmData setThreadPool(List<ThreadPoolInfo> threadPool) {
        this.threadPool = threadPool;
        return this;
    }

    @Override
    public String toString() {
        return toJsonStr();
    }
}
