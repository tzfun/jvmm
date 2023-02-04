package org.beifengtz.jvmm.server.entity.dto;

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
public class JvmmDataDTO implements JsonParsable {
    private String node;

    private ProcessInfo process;
    private List<DiskInfo> disk;
    private List<DiskIOInfo> diskIO;
    private CPUInfo cpu;
    private NetInfo network;
    private SysInfo sys;
    private SysMemInfo sysMem;
    private List<SysFileInfo> sysFile;
    private JvmClassLoadingInfo jvmClassLoading;
    private List<JvmClassLoaderInfo> jvmClassLoader;
    private JvmCompilationInfo jvmCompilation;
    private List<JvmGCInfo> jvmGc;
    private JvmMemoryInfo jvmMemory;
    private List<JvmMemoryManagerInfo> jvmMemoryManager;
    private List<JvmMemoryPoolInfo> jvmMemoryPool;
    private JvmThreadInfo jvmThread;

    public String getNode() {
        return node;
    }

    public JvmmDataDTO setNode(String node) {
        this.node = node;
        return this;
    }

    public ProcessInfo getProcess() {
        return process;
    }

    public JvmmDataDTO setProcess(ProcessInfo process) {
        this.process = process;
        return this;
    }

    public List<DiskInfo> getDisk() {
        return disk;
    }

    public JvmmDataDTO setDisk(List<DiskInfo> disk) {
        this.disk = disk;
        return this;
    }

    public List<DiskIOInfo> getDiskIO() {
        return diskIO;
    }

    public JvmmDataDTO setDiskIO(List<DiskIOInfo> diskIO) {
        this.diskIO = diskIO;
        return this;
    }

    public CPUInfo getCpu() {
        return cpu;
    }

    public JvmmDataDTO setCpu(CPUInfo cpu) {
        this.cpu = cpu;
        return this;
    }

    public NetInfo getNetwork() {
        return network;
    }

    public JvmmDataDTO setNetwork(NetInfo network) {
        this.network = network;
        return this;
    }

    public SysInfo getSys() {
        return sys;
    }

    public JvmmDataDTO setSys(SysInfo sys) {
        this.sys = sys;
        return this;
    }

    public SysMemInfo getSysMem() {
        return sysMem;
    }

    public JvmmDataDTO setSysMem(SysMemInfo sysMem) {
        this.sysMem = sysMem;
        return this;
    }

    public List<SysFileInfo> getSysFile() {
        return sysFile;
    }

    public JvmmDataDTO setSysFile(List<SysFileInfo> sysFile) {
        this.sysFile = sysFile;
        return this;
    }

    public JvmClassLoadingInfo getJvmClassLoading() {
        return jvmClassLoading;
    }

    public JvmmDataDTO setJvmClassLoading(JvmClassLoadingInfo jvmClassLoading) {
        this.jvmClassLoading = jvmClassLoading;
        return this;
    }

    public List<JvmClassLoaderInfo> getJvmClassLoader() {
        return jvmClassLoader;
    }

    public JvmmDataDTO setJvmClassLoader(List<JvmClassLoaderInfo> jvmClassLoader) {
        this.jvmClassLoader = jvmClassLoader;
        return this;
    }

    public JvmCompilationInfo getJvmCompilation() {
        return jvmCompilation;
    }

    public JvmmDataDTO setJvmCompilation(JvmCompilationInfo jvmCompilation) {
        this.jvmCompilation = jvmCompilation;
        return this;
    }

    public List<JvmGCInfo> getJvmGc() {
        return jvmGc;
    }

    public JvmmDataDTO setJvmGc(List<JvmGCInfo> jvmGc) {
        this.jvmGc = jvmGc;
        return this;
    }

    public JvmMemoryInfo getJvmMemory() {
        return jvmMemory;
    }

    public JvmmDataDTO setJvmMemory(JvmMemoryInfo jvmMemory) {
        this.jvmMemory = jvmMemory;
        return this;
    }

    public List<JvmMemoryManagerInfo> getJvmMemoryManager() {
        return jvmMemoryManager;
    }

    public JvmmDataDTO setJvmMemoryManager(List<JvmMemoryManagerInfo> jvmMemoryManager) {
        this.jvmMemoryManager = jvmMemoryManager;
        return this;
    }

    public List<JvmMemoryPoolInfo> getJvmMemoryPool() {
        return jvmMemoryPool;
    }

    public JvmmDataDTO setJvmMemoryPool(List<JvmMemoryPoolInfo> jvmMemoryPool) {
        this.jvmMemoryPool = jvmMemoryPool;
        return this;
    }

    public JvmThreadInfo getJvmThread() {
        return jvmThread;
    }

    public JvmmDataDTO setJvmThread(JvmThreadInfo jvmThread) {
        this.jvmThread = jvmThread;
        return this;
    }
}
