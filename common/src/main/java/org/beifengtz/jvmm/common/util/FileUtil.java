package org.beifengtz.jvmm.common.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;
import java.util.function.Function;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * <p>
 * Description: TODO
 * </p>
 * <p>
 * Created in 15:31 2021/5/12
 *
 * @author beifengtz
 */
public class FileUtil {

    private static final int SAFE_BYTE_LENGTH = 2048;
    private static final String TEMP_PATH = ".jvmm";

    public static String getTempPath() {
        return TEMP_PATH;
    }

    public static File createTempFile(String filename) throws IOException {
        File file = new File(TEMP_PATH, filename);
        if (!file.exists()) {
            if (file.getParentFile() != null) {
                file.getParentFile().mkdirs();
            }
            if (!file.createNewFile()) {
                throw new IOException("Can not create temp file");
            }
        }
        return file;
    }

    public static File createTempDir(String dirname) throws IOException {
        File dir = new File(TEMP_PATH, dirname);
        if (!dir.exists()) {
            if (!dir.mkdirs()) {
                throw new IOException("Can not create temp file");
            }
        }
        return dir;
    }

    public static void writeByteArrayToFile(File file, byte[] data) throws IOException {
        writeByteArrayToFile(file, data, false);
    }

    public static void writeByteArrayToFile(File file, byte[] data, boolean append) throws IOException {
        try (OutputStream out = openOutputStream(file, append)) {
            out.write(data);
        }
    }

    public static FileOutputStream openOutputStream(File file, boolean append) throws IOException {
        if (file.exists()) {
            if (file.isDirectory()) {
                throw new IOException("File '" + file + "' exists but is a directory");
            }
            if (!file.canWrite()) {
                throw new IOException("File '" + file + "' cannot be written to");
            }
        } else {
            File parent = file.getParentFile();
            if (parent != null) {
                if (!parent.mkdirs() && !parent.isDirectory()) {
                    throw new IOException("Directory '" + parent + "' could not be created");
                }
            }
        }
        return new FileOutputStream(file, append);
    }

    public static String readFileToString(File file, Charset encoding) throws IOException {
        try (FileInputStream stream = new FileInputStream(file)) {
            Reader reader = new BufferedReader(new InputStreamReader(stream, encoding));
            StringBuilder builder = new StringBuilder();
            char[] buffer = new char[8192];
            int read;
            while ((read = reader.read(buffer, 0, buffer.length)) > 0) {
                builder.append(buffer, 0, read);
            }
            return builder.toString();
        }
    }

    public static Map<String, String> readProperties(String file) throws IOException {
        return readProperties(file, null);
    }

    public static Map<String, String> readProperties(String file, String globalPrefix) throws IOException {
        Properties properties = new Properties();
        try (FileInputStream in = new FileInputStream(file)) {
            properties.load(in);
            Map<String, String> map = new HashMap<>(properties.size());
            properties.forEach((k, v) -> {
                String key;
                if (globalPrefix != null) {
                    if (!k.toString().startsWith(globalPrefix)) {
                        return;
                    }
                    key = k.toString().replaceFirst(globalPrefix, "");
                } else {
                    key = k.toString();
                }
                map.put(key, v.toString());
            });
            return map;
        }
    }

    /**
     * 从网络中读取数据并写入文件
     *
     * @param url      网络地址，http / https协议
     * @param dir      文件目录
     * @param fileName 文件名
     * @return 是否成功
     */
    public static boolean readFileFromNet(String url, String dir, String fileName) {
        File file = null;
        try {
            URL httpUrl = new URL(url);

            File saveDir = new File(dir);
            if (!saveDir.exists()) {
                saveDir.mkdirs();
            }
            file = new File(saveDir + File.separator + fileName);
            if (file.exists()) {
                file.delete();
            }

            HttpURLConnection conn = (HttpURLConnection) httpUrl.openConnection();
            conn.setConnectTimeout(3000);
            conn.setRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 5.0; Windows NT; DigExt)");

            try (InputStream inputStream = conn.getInputStream(); FileOutputStream fos = new FileOutputStream(file)) {
                int temp = 0;
                byte[] bytes = new byte[SAFE_BYTE_LENGTH];
                while ((temp = inputStream.read(bytes)) != -1) {
                    fos.write(bytes, 0, temp);
                }
            }

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            if (file != null && file.exists()) {
                file.delete();
            }

            return false;
        }
    }

    /**
     * 大小可视化，进制为 1024
     *
     * @param bytes 单位为byte的大小
     * @param scale 保留小数位
     * @return 可视化的字符
     */
    public static String parseByteSize(long bytes, int scale) {
        final String suffix;
        final double size;
        if (bytes >= 0 && bytes < 1024) {
            suffix = " B";
            size = bytes;
        } else if (bytes >= 1024 && bytes < 1048576) {
            suffix = " KB";
            size = (double) bytes / 1024;
        } else if (bytes >= 1048576 && bytes < 1073741824) {
            suffix = " MB";
            size = (double) bytes / 1048576;
        } else if (bytes >= 1073741824 && bytes < 1099511627776L) {
            suffix = " GB";
            size = (double) bytes / 1073741824;
        } else {
            suffix = " TB";
            size = (double) bytes / 1099511627776L;
        }
        BigDecimal bigDecimal = new BigDecimal(size).setScale(scale, BigDecimal.ROUND_HALF_UP);
        if (scale == 0) {
            return bigDecimal.longValue() + suffix;
        } else {
            return bigDecimal.doubleValue() + suffix;
        }
    }

    /**
     * Find file from jar, and copy it to destination.
     *
     * @param destinationDir destination directory
     * @param jarPath        jar path
     * @param relativePath   relative path in jar
     * @return success
     * @throws IOException unzip filed
     */
    public static boolean findAndUnzipJar(String destinationDir, String jarPath, String relativePath) throws IOException {
        JarFile jar = new JarFile(new File(jarPath));
        File destination = new File(destinationDir);
        if (!destination.exists()) {
            destination.mkdirs();
        }
        Enumeration<JarEntry> entries = jar.entries();
        while (entries.hasMoreElements()) {
            JarEntry entry = entries.nextElement();
            if (!entry.isDirectory() && entry.getName().equals(relativePath)) {
                String fileName = destinationDir + File.separator + entry.getName();
                File f = new File(fileName);
                File dir = f.getParentFile();
                if (!dir.exists()) {
                    dir.mkdirs();
                }
                if (f.exists()) {
                    f.delete();
                }
                try (InputStream is = jar.getInputStream(entry);
                     FileOutputStream fos = new FileOutputStream(f)) {
                    byte[] bytes = new byte[SAFE_BYTE_LENGTH];
                    int read = 0;
                    while ((read = is.read(bytes, 0, bytes.length)) > 0) {
                        fos.write(bytes, 0, read);
                    }
                }
                return true;
            }
        }
        return false;
    }

    public static void copyFromJar(JarFile jarFile, File targetDir, String regex) throws IOException {
        copyFromJar(jarFile, targetDir, regex, null);
    }

    /**
     * 从jar包中复制指定文件
     *
     * @param jarFile        jar文件对象
     * @param targetDir      复制目标目录
     * @param regex          文件正则匹配
     * @param subDirFunction 生成目标文件子目录function
     * @throws IOException 复制失败时抛出
     */
    public static void copyFromJar(JarFile jarFile, File targetDir, String regex, Function<String, String> subDirFunction) throws IOException {
        Enumeration<JarEntry> entries = jarFile.entries();
        while (entries.hasMoreElements()) {
            JarEntry entry = entries.nextElement();
            if (!entry.isDirectory() && entry.getName().matches(regex)) {
                File f = new File(targetDir, subDirFunction == null ? entry.getName() : subDirFunction.apply(entry.getName()));
                if (f.getParentFile() != null && !f.getParentFile().exists()) {
                    f.getParentFile().mkdirs();
                }
                if (f.exists()) {
                    f.delete();
                }
                try (InputStream is = jarFile.getInputStream(entry);
                     FileOutputStream fos = new FileOutputStream(f)) {
                    byte[] bytes = new byte[SAFE_BYTE_LENGTH];
                    int read = 0;
                    while ((read = is.read(bytes, 0, bytes.length)) > 0) {
                        fos.write(bytes, 0, read);
                    }
                }
            }
        }
    }

    /**
     * zip文件压缩
     *
     * @param inputFile    待压缩文件夹/文件名
     * @param outputFile   生成的压缩包
     * @param containsRoot 是否包含根目录
     * @throws IOException 压缩异常
     */
    public static void zip(File inputFile, File outputFile, boolean containsRoot) throws IOException {
        if (outputFile.getParentFile() != null && !outputFile.getParentFile().exists()) {
            outputFile.getParentFile().mkdirs();
        }
        try (ZipOutputStream out = new ZipOutputStream(Files.newOutputStream(outputFile.toPath()))) {
            zip(inputFile, containsRoot ? inputFile.getName() : "", out);
        }
    }

    /**
     * 将fileToZip文件夹及其子目录文件递归压缩到zip文件中
     *
     * @param sourceFile 递归当前处理对象，可能是文件夹，也可能是文件
     * @param fileName   fileToZip文件或文件夹名称
     * @param zipOut     压缩文件输出流
     * @throws IOException 压缩异常
     */
    private static void zip(File sourceFile, String fileName, ZipOutputStream zipOut) throws IOException {
        //不压缩隐藏文件夹
        if (sourceFile.isHidden()) {
            return;
        }
        if (sourceFile.isDirectory()) {
            if (fileName.endsWith("/")) {
                zipOut.putNextEntry(new ZipEntry(fileName));
            } else if (!fileName.isEmpty()) {
                zipOut.putNextEntry(new ZipEntry(fileName + "/"));
            }
            zipOut.closeEntry();
            //遍历文件夹子目录，进行递归的zipFile
            File[] children = sourceFile.listFiles();
            for (File childFile : children) {
                zip(childFile, fileName.isEmpty() ? childFile.getName() : (fileName + "/" + childFile.getName()), zipOut);
            }
            return;
        }
        FileInputStream fis = new FileInputStream(sourceFile);
        ZipEntry zipEntry = new ZipEntry(fileName);
        zipOut.putNextEntry(zipEntry);
        byte[] bytes = new byte[1024];
        int length;
        while ((length = fis.read(bytes)) >= 0) {
            zipOut.write(bytes, 0, length);
        }
        fis.close();
    }

    /**
     * 解压jar文件到指定目录
     *
     * @param file      jar文件
     * @param outputDir 输出目录
     * @throws IOException IO error
     */
    public static void unJar(File file, File outputDir) throws IOException {
        if (outputDir.exists() && outputDir.listFiles() != null) {
            delFile(outputDir);
        }
        if (outputDir.isFile()) {
            throw new IOException("Can not write to file " + outputDir + " (must be directory)");
        }
        outputDir.mkdirs();
        try (JarFile jarFile = new JarFile(file)) {
            Enumeration<JarEntry> entries = jarFile.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                if (!entry.isDirectory()) {
                    File f = new File(outputDir, entry.getName());
                    if (f.getParentFile() != null) {
                        f.getParentFile().mkdirs();
                    }
                    try (InputStream is = jarFile.getInputStream(entry);
                         FileOutputStream fos = new FileOutputStream(f)) {
                        byte[] bytes = new byte[SAFE_BYTE_LENGTH];
                        int read = 0;
                        while ((read = is.read(bytes, 0, bytes.length)) > 0) {
                            fos.write(bytes, 0, read);
                        }
                    }
                }
            }
        }
    }

    /**
     * 删除文件，如果file是目录，递归删除目录下所有文件
     *
     * @param file 被删除的文件或文件目录
     * @return 是否删除成功
     */
    public static boolean delFile(File file) {
        if (!file.exists()) {
            return true;
        }

        if (file.isDirectory()) {
            File[] files = file.listFiles();
            for (File f : files) {
                if (!delFile(f)) {
                    return false;
                }
            }
        }
        return file.delete();
    }

    /**
     * 读取文件成为字符串
     *
     * @param f 源文件
     * @return hex string
     * @throws IOException when file is not exists
     */
    public static String readToHexStr(File f) throws IOException {
        if (!f.exists()) {
            throw new IOException("File not found: " + f);
        }
        byte[] bytes = Files.readAllBytes(f.toPath());
        return CodingUtil.bytes2HexString(bytes);
    }

    /**
     * 将hex字符串转为bytes并写成文件
     *
     * @param to     target file
     * @param hexStr hex string sources
     * @throws IOException when write file failed
     */
    public static void saveFromHexStr(File to, String hexStr) throws IOException {
        if (!to.getParentFile().exists()) {
            to.getParentFile().mkdirs();
        }
        if (!to.exists()) {
            to.createNewFile();
        }
        Files.write(to.toPath(), CodingUtil.hexStr2Bytes(hexStr));
    }

    /**
     * 从Jar包中删除文件
     *
     * @param jarPath Jar
     * @param regex   正则匹配
     */
    public static void delFromJar(String jarPath, String regex) throws IOException {
        File tmpFile = new File(getTempPath(), UUID.randomUUID().toString());
        try (JarFile jarFile = new JarFile(jarPath)) {
            boolean found = jarFile.stream().anyMatch(o -> !o.isDirectory() && o.getName().matches(regex));
            if (found) {
                Enumeration<JarEntry> entries = jarFile.entries();
                while (entries.hasMoreElements()) {
                    JarEntry entry = entries.nextElement();
                    if (!entry.isDirectory() && !entry.getName().matches(regex)) {
                        File f = new File(tmpFile, entry.getName());
                        if (f.getParentFile() != null && !f.getParentFile().exists()) {
                            f.getParentFile().mkdirs();
                        }
                        if (f.exists()) {
                            f.delete();
                        }
                        try (InputStream is = jarFile.getInputStream(entry);
                             FileOutputStream fos = new FileOutputStream(f)) {
                            byte[] bytes = new byte[SAFE_BYTE_LENGTH];
                            int read = 0;
                            while ((read = is.read(bytes, 0, bytes.length)) > 0) {
                                fos.write(bytes, 0, read);
                            }
                        }
                    }
                }
            }
        }
        if (tmpFile.exists()) {
            File file = new File(jarPath);
            file.delete();
            zip(tmpFile, file, false);
            delFile(tmpFile);
        }
    }
}
