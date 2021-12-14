package org.beifengtz.jvmm.agent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * Created in 15:31 2021/5/12
 *
 * @author beifengtz
 */
public class FileUtil {

    private static final Logger logger = LoggerFactory.getLogger(FileUtil.class);

    private static final int SAFE_BYTE_LENGTH = 2048;

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
        long start = System.currentTimeMillis();
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

            logger.info("Start download file from {}", url);
            HttpURLConnection conn = (HttpURLConnection) httpUrl.openConnection();
            conn.setConnectTimeout(3000);
            conn.setRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 5.0; Windows NT; DigExt)");

            long totalSize = 0;
            try (InputStream inputStream = conn.getInputStream(); FileOutputStream fos = new FileOutputStream(file)) {
                logger.debug("Start save file to local path...");
                int temp = 0;
                byte[] bytes = new byte[SAFE_BYTE_LENGTH];
                while ((temp = inputStream.read(bytes)) != -1) {
                    fos.write(bytes, 0, temp);
                    totalSize += temp;
                }
            }
            logger.info("Save file from network successful. use: {} ms, totalSize: {}.",
                    System.currentTimeMillis() - start, parseByteSize(totalSize, 2));

            return true;
        } catch (Exception e) {
            logger.error(String.format("Download file form network filed. url:'%s'. %s", url, e.getMessage()), e);

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
            if (entry.getName().equals(relativePath)) {
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
                    byte[] bytes = new byte[2048];
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
                delFile(f);
            }
        }
        return file.delete();
    }
}