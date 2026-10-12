package com.example.demo.common.util;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * Utility class for File and I/O operations
 *
 * Features:
 * - File reading/writing
 * - File copying/moving/deleting
 * - Directory operations
 * - File compression (ZIP)
 * - File searching and filtering
 * - File metadata operations
 * - Path manipulation
 * - Resource handling
 *
 * @author Architecture Team
 * @version 1.0
 */
@Slf4j
@UtilityClass
public class FileUtils {

    // ========== Constants ==========

    private static final int BUFFER_SIZE = 8192;
    private static final int MAX_FILE_SIZE = 100 * 1024 * 1024; // 100MB default
    private static final String TEMP_FILE_PREFIX = "temp_";
    private static final String TEMP_FILE_SUFFIX = ".tmp";

    // ========== File Reading ==========

    /**
     * Read file to string (UTF-8)
     */
    public static String readFileToString(File file) {
        return readFileToString(file, StandardCharsets.UTF_8);
    }

    /**
     * Read file to string with specific charset
     */
    public static String readFileToString(File file, Charset charset) {
        if (file == null || !file.exists() || !file.isFile()) {
            throw new FileException("File does not exist or is not a file: " + file);
        }

        try {
            return Files.readString(file.toPath(), charset);
        } catch (IOException e) {
            log.error("Failed to read file: {}", file.getAbsolutePath(), e);
            throw new FileException("Failed to read file", e);
        }
    }

    /**
     * Read file to string using Path
     */
    public static String readFileToString(Path path) {
        return readFileToString(path, StandardCharsets.UTF_8);
    }

    /**
     * Read file to string using Path with charset
     */
    public static String readFileToString(Path path, Charset charset) {
        if (path == null || !Files.exists(path)) {
            throw new FileException("Path does not exist: " + path);
        }

        try {
            return Files.readString(path, charset);
        } catch (IOException e) {
            log.error("Failed to read file: {}", path, e);
            throw new FileException("Failed to read file", e);
        }
    }

    /**
     * Read file to byte array
     */
    public static byte[] readFileToByteArray(File file) {
        if (file == null || !file.exists() || !file.isFile()) {
            throw new FileException("File does not exist or is not a file: " + file);
        }

        try {
            return Files.readAllBytes(file.toPath());
        } catch (IOException e) {
            log.error("Failed to read file to byte array: {}", file.getAbsolutePath(), e);
            throw new FileException("Failed to read file to byte array", e);
        }
    }

    /**
     * Read file lines to list
     */
    public static List<String> readLines(File file) {
        return readLines(file, StandardCharsets.UTF_8);
    }

    /**
     * Read file lines to list with charset
     */
    public static List<String> readLines(File file, Charset charset) {
        if (file == null || !file.exists() || !file.isFile()) {
            throw new FileException("File does not exist or is not a file: " + file);
        }

        try {
            return Files.readAllLines(file.toPath(), charset);
        } catch (IOException e) {
            log.error("Failed to read lines from file: {}", file.getAbsolutePath(), e);
            throw new FileException("Failed to read lines from file", e);
        }
    }

    /**
     * Read file lines as stream
     */
    public static Stream<String> lines(Path path) {
        return lines(path, StandardCharsets.UTF_8);
    }

    /**
     * Read file lines as stream with charset
     */
    public static Stream<String> lines(Path path, Charset charset) {
        if (path == null || !Files.exists(path)) {
            throw new FileException("Path does not exist: " + path);
        }

        try {
            return Files.lines(path, charset);
        } catch (IOException e) {
            log.error("Failed to read lines from path: {}", path, e);
            throw new FileException("Failed to read lines", e);
        }
    }

    /**
     * Read resource file from classpath
     */
    public static String readResourceToString(String resourcePath) {
        return readResourceToString(resourcePath, StandardCharsets.UTF_8);
    }

    /**
     * Read resource file from classpath with charset
     */
    public static String readResourceToString(String resourcePath, Charset charset) {
        try (InputStream inputStream = FileUtils.class.getClassLoader()
                .getResourceAsStream(resourcePath)) {

            if (inputStream == null) {
                throw new FileException("Resource not found: " + resourcePath);
            }

            return new String(inputStream.readAllBytes(), charset);
        } catch (IOException e) {
            log.error("Failed to read resource: {}", resourcePath, e);
            throw new FileException("Failed to read resource", e);
        }
    }

    // ========== File Writing ==========

    /**
     * Write string to file (UTF-8)
     */
    public static void writeStringToFile(File file, String content) {
        writeStringToFile(file, content, StandardCharsets.UTF_8, false);
    }

    /**
     * Write string to file with append option
     */
    public static void writeStringToFile(File file, String content, boolean append) {
        writeStringToFile(file, content, StandardCharsets.UTF_8, append);
    }

    /**
     * Write string to file with charset and append option
     */
    public static void writeStringToFile(File file, String content, Charset charset, boolean append) {
        if (file == null) {
            throw new IllegalArgumentException("File must not be null");
        }

        try {
            createParentDirs(file);

            OpenOption[] options = append
                    ? new OpenOption[]{StandardOpenOption.CREATE, StandardOpenOption.APPEND}
                    : new OpenOption[]{StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING};

            Files.writeString(file.toPath(), content, charset, options);
            log.debug("Successfully wrote to file: {}", file.getAbsolutePath());
        } catch (IOException e) {
            log.error("Failed to write to file: {}", file.getAbsolutePath(), e);
            throw new FileException("Failed to write to file", e);
        }
    }

    /**
     * Write byte array to file
     */
    public static void writeByteArrayToFile(File file, byte[] data) {
        writeByteArrayToFile(file, data, false);
    }

    /**
     * Write byte array to file with append option
     */
    public static void writeByteArrayToFile(File file, byte[] data, boolean append) {
        if (file == null || data == null) {
            throw new IllegalArgumentException("File and data must not be null");
        }

        try {
            createParentDirs(file);

            OpenOption[] options = append
                    ? new OpenOption[]{StandardOpenOption.CREATE, StandardOpenOption.APPEND}
                    : new OpenOption[]{StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING};

            Files.write(file.toPath(), data, options);
            log.debug("Successfully wrote byte array to file: {}", file.getAbsolutePath());
        } catch (IOException e) {
            log.error("Failed to write byte array to file: {}", file.getAbsolutePath(), e);
            throw new FileException("Failed to write byte array to file", e);
        }
    }

    /**
     * Write lines to file
     */
    public static void writeLines(File file, Collection<String> lines) {
        writeLines(file, lines, StandardCharsets.UTF_8, false);
    }

    /**
     * Write lines to file with append option
     */
    public static void writeLines(File file, Collection<String> lines, boolean append) {
        writeLines(file, lines, StandardCharsets.UTF_8, append);
    }

    /**
     * Write lines to file with charset and append option
     */
    public static void writeLines(File file, Collection<String> lines, Charset charset, boolean append) {
        if (file == null || lines == null) {
            throw new IllegalArgumentException("File and lines must not be null");
        }

        try {
            createParentDirs(file);

            OpenOption[] options = append
                    ? new OpenOption[]{StandardOpenOption.CREATE, StandardOpenOption.APPEND}
                    : new OpenOption[]{StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING};

            Files.write(file.toPath(), lines, charset, options);
            log.debug("Successfully wrote lines to file: {}", file.getAbsolutePath());
        } catch (IOException e) {
            log.error("Failed to write lines to file: {}", file.getAbsolutePath(), e);
            throw new FileException("Failed to write lines to file", e);
        }
    }

    // ========== File Copying ==========

    /**
     * Copy file
     */
    public static void copyFile(File source, File destination) {
        copyFile(source, destination, true);
    }

    /**
     * Copy file with replace option
     */
    public static void copyFile(File source, File destination, boolean replace) {
        if (source == null || destination == null) {
            throw new IllegalArgumentException("Source and destination must not be null");
        }

        if (!source.exists() || !source.isFile()) {
            throw new FileException("Source file does not exist or is not a file: " + source);
        }

        try {
            createParentDirs(destination);

            CopyOption[] options = replace
                    ? new CopyOption[]{StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.COPY_ATTRIBUTES}
                    : new CopyOption[]{StandardCopyOption.COPY_ATTRIBUTES};

            Files.copy(source.toPath(), destination.toPath(), options);
            log.debug("Successfully copied file from {} to {}", source, destination);
        } catch (IOException e) {
            log.error("Failed to copy file from {} to {}", source, destination, e);
            throw new FileException("Failed to copy file", e);
        }
    }

    /**
     * Copy directory recursively
     */
    public static void copyDirectory(File sourceDir, File destDir) {
        if (sourceDir == null || destDir == null) {
            throw new IllegalArgumentException("Source and destination directories must not be null");
        }

        if (!sourceDir.exists() || !sourceDir.isDirectory()) {
            throw new FileException("Source is not a directory: " + sourceDir);
        }

        try {
            Files.walkFileTree(sourceDir.toPath(), new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs)
                        throws IOException {
                    Path targetDir = destDir.toPath().resolve(sourceDir.toPath().relativize(dir));
                    Files.createDirectories(targetDir);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
                        throws IOException {
                    Path targetFile = destDir.toPath().resolve(sourceDir.toPath().relativize(file));
                    Files.copy(file, targetFile, StandardCopyOption.REPLACE_EXISTING);
                    return FileVisitResult.CONTINUE;
                }
            });
            log.debug("Successfully copied directory from {} to {}", sourceDir, destDir);
        } catch (IOException e) {
            log.error("Failed to copy directory from {} to {}", sourceDir, destDir, e);
            throw new FileException("Failed to copy directory", e);
        }
    }

    // ========== File Moving ==========

    /**
     * Move file
     */
    public static void moveFile(File source, File destination) {
        moveFile(source, destination, true);
    }

    /**
     * Move file with replace option
     */
    public static void moveFile(File source, File destination, boolean replace) {
        if (source == null || destination == null) {
            throw new IllegalArgumentException("Source and destination must not be null");
        }

        if (!source.exists()) {
            throw new FileException("Source file does not exist: " + source);
        }

        try {
            createParentDirs(destination);

            CopyOption[] options = replace
                    ? new CopyOption[]{StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE}
                    : new CopyOption[]{StandardCopyOption.ATOMIC_MOVE};

            Files.move(source.toPath(), destination.toPath(), options);
            log.debug("Successfully moved file from {} to {}", source, destination);
        } catch (IOException e) {
            log.error("Failed to move file from {} to {}", source, destination, e);
            throw new FileException("Failed to move file", e);
        }
    }

    /**
     * Move directory
     */
    public static void moveDirectory(File sourceDir, File destDir) {
        if (sourceDir == null || destDir == null) {
            throw new IllegalArgumentException("Source and destination must not be null");
        }

        if (!sourceDir.exists() || !sourceDir.isDirectory()) {
            throw new FileException("Source is not a directory: " + sourceDir);
        }

        try {
            Files.move(sourceDir.toPath(), destDir.toPath(),
                    StandardCopyOption.REPLACE_EXISTING);
            log.debug("Successfully moved directory from {} to {}", sourceDir, destDir);
        } catch (IOException e) {
            log.error("Failed to move directory from {} to {}", sourceDir, destDir, e);
            throw new FileException("Failed to move directory", e);
        }
    }

    // ========== File Deletion ==========

    /**
     * Delete file
     */
    public static boolean deleteFile(File file) {
        if (file == null || !file.exists()) {
            return false;
        }

        try {
            Files.delete(file.toPath());
            log.debug("Successfully deleted file: {}", file.getAbsolutePath());
            return true;
        } catch (IOException e) {
            log.error("Failed to delete file: {}", file.getAbsolutePath(), e);
            return false;
        }
    }

    /**
     * Delete file quietly (no exceptions)
     */
    public static boolean deleteQuietly(File file) {
        try {
            return deleteFile(file);
        } catch (Exception e) {
            log.warn("Failed to delete file quietly: {}", file, e);
            return false;
        }
    }

    /**
     * Delete directory recursively
     */
    public static boolean deleteDirectory(File directory) {
        if (directory == null || !directory.exists()) {
            return false;
        }

        if (!directory.isDirectory()) {
            return deleteFile(directory);
        }

        try {
            Files.walkFileTree(directory.toPath(), new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
                        throws IOException {
                    Files.delete(file);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc)
                        throws IOException {
                    Files.delete(dir);
                    return FileVisitResult.CONTINUE;
                }
            });
            log.debug("Successfully deleted directory: {}", directory.getAbsolutePath());
            return true;
        } catch (IOException e) {
            log.error("Failed to delete directory: {}", directory.getAbsolutePath(), e);
            return false;
        }
    }

    /**
     * Clean directory (delete all contents but keep directory)
     */
    public static void cleanDirectory(File directory) {
        if (directory == null || !directory.exists() || !directory.isDirectory()) {
            throw new FileException("Directory does not exist or is not a directory: " + directory);
        }

        try {
            Files.walkFileTree(directory.toPath(), new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
                        throws IOException {
                    Files.delete(file);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc)
                        throws IOException {
                    if (!dir.equals(directory.toPath())) {
                        Files.delete(dir);
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
            log.debug("Successfully cleaned directory: {}", directory.getAbsolutePath());
        } catch (IOException e) {
            log.error("Failed to clean directory: {}", directory.getAbsolutePath(), e);
            throw new FileException("Failed to clean directory", e);
        }
    }

    /**
     * Force delete (delete even if readonly)
     */
    public static boolean forceDelete(File file) {
        if (file == null || !file.exists()) {
            return false;
        }

        if (!file.canWrite()) {
            file.setWritable(true);
        }

        if (file.isDirectory()) {
            return deleteDirectory(file);
        } else {
            return deleteFile(file);
        }
    }

    // ========== Directory Operations ==========

    /**
     * Create directory
     */
    public static boolean createDirectory(File directory) {
        if (directory == null) {
            throw new IllegalArgumentException("Directory must not be null");
        }

        if (directory.exists()) {
            return directory.isDirectory();
        }

        try {
            Files.createDirectories(directory.toPath());
            log.debug("Successfully created directory: {}", directory.getAbsolutePath());
            return true;
        } catch (IOException e) {
            log.error("Failed to create directory: {}", directory.getAbsolutePath(), e);
            return false;
        }
    }

    /**
     * Create parent directories
     */
    public static void createParentDirs(File file) {
        if (file == null) {
            return;
        }

        File parent = file.getParentFile();
        if (parent != null && !parent.exists()) {
            createDirectory(parent);
        }
    }

    /**
     * List files in directory
     */
    public static List<File> listFiles(File directory) {
        return listFiles(directory, false);
    }

    /**
     * List files in directory with recursive option
     */
    public static List<File> listFiles(File directory, boolean recursive) {
        if (directory == null || !directory.exists() || !directory.isDirectory()) {
            return Collections.emptyList();
        }

        if (!recursive) {
            File[] files = directory.listFiles();
            return files != null ? Arrays.asList(files) : Collections.emptyList();
        }

        List<File> result = new ArrayList<>();
        try {
            Files.walk(directory.toPath())
                    .filter(Files::isRegularFile)
                    .forEach(path -> result.add(path.toFile()));
        } catch (IOException e) {
            log.error("Failed to list files in directory: {}", directory, e);
        }
        return result;
    }

    /**
     * List files with filter
     */
    public static List<File> listFiles(File directory, FileFilter filter) {
        if (directory == null || !directory.exists() || !directory.isDirectory()) {
            return Collections.emptyList();
        }

        File[] files = directory.listFiles(filter);
        return files != null ? Arrays.asList(files) : Collections.emptyList();
    }

    /**
     * List files by extension
     */
    public static List<File> listFilesByExtension(File directory, String extension) {
        return listFilesByExtension(directory, extension, false);
    }

    /**
     * List files by extension with recursive option
     */
    public static List<File> listFilesByExtension(File directory, String extension, boolean recursive) {
        if (StringUtils.isBlank(extension)) {
            return listFiles(directory, recursive);
        }

        String ext = extension.startsWith(".") ? extension : "." + extension;
        List<File> allFiles = listFiles(directory, recursive);

        return allFiles.stream()
                .filter(file -> file.getName().toLowerCase().endsWith(ext.toLowerCase()))
                .collect(Collectors.toList());
    }

    /**
     * Find files matching pattern
     */
    public static List<File> findFiles(File directory, String pattern) {
        return findFiles(directory, pattern, false);
    }

    /**
     * Find files matching pattern with recursive option
     */
    public static List<File> findFiles(File directory, String pattern, boolean recursive) {
        if (directory == null || !directory.exists() || !directory.isDirectory()) {
            return Collections.emptyList();
        }

        List<File> result = new ArrayList<>();
        try {
            PathMatcher matcher = FileSystems.getDefault()
                    .getPathMatcher("glob:" + pattern);

            Stream<Path> stream = recursive
                    ? Files.walk(directory.toPath())
                    : Files.list(directory.toPath());

            stream.filter(Files::isRegularFile)
                    .filter(path -> matcher.matches(path.getFileName()))
                    .forEach(path -> result.add(path.toFile()));
        } catch (IOException e) {
            log.error("Failed to find files in directory: {}", directory, e);
        }
        return result;
    }

    // ========== File Information ==========

    /**
     * Get file size
     */
    public static long getFileSize(File file) {
        if (file == null || !file.exists() || !file.isFile()) {
            return 0L;
        }

        try {
            return Files.size(file.toPath());
        } catch (IOException e) {
            log.error("Failed to get file size: {}", file, e);
            return 0L;
        }
    }

    /**
     * Get file size formatted
     */
    public static String getFileSizeFormatted(File file) {
        return StringUtils.formatBytes(getFileSize(file));
    }

    /**
     * Get directory size
     */
    public static long getDirectorySize(File directory) {
        if (directory == null || !directory.exists() || !directory.isDirectory()) {
            return 0L;
        }

        try {
            return Files.walk(directory.toPath())
                    .filter(Files::isRegularFile)
                    .mapToLong(path -> {
                        try {
                            return Files.size(path);
                        } catch (IOException e) {
                            return 0L;
                        }
                    })
                    .sum();
        } catch (IOException e) {
            log.error("Failed to get directory size: {}", directory, e);
            return 0L;
        }
    }

    /**
     * Count files in directory
     */
    public static long countFiles(File directory) {
        return countFiles(directory, false);
    }

    /**
     * Count files in directory with recursive option
     */
    public static long countFiles(File directory, boolean recursive) {
        if (directory == null || !directory.exists() || !directory.isDirectory()) {
            return 0L;
        }

        try {
            Stream<Path> stream = recursive
                    ? Files.walk(directory.toPath())
                    : Files.list(directory.toPath());

            return stream.filter(Files::isRegularFile).count();
        } catch (IOException e) {
            log.error("Failed to count files in directory: {}", directory, e);
            return 0L;
        }
    }

    /**
     * Get file extension
     */
    public static String getExtension(File file) {
        if (file == null) {
            return "";
        }
        return getExtension(file.getName());
    }

    /**
     * Get file extension from filename
     */
    public static String getExtension(String filename) {
        if (StringUtils.isBlank(filename)) {
            return "";
        }

        int lastDot = filename.lastIndexOf('.');
        if (lastDot == -1 || lastDot == filename.length() - 1) {
            return "";
        }

        return filename.substring(lastDot + 1);
    }

    /**
     * Get file name without extension
     */
    public static String getBaseName(File file) {
        if (file == null) {
            return "";
        }
        return getBaseName(file.getName());
    }

    /**
     * Get file name without extension from filename
     */
    public static String getBaseName(String filename) {
        if (StringUtils.isBlank(filename)) {
            return "";
        }

        int lastDot = filename.lastIndexOf('.');
        if (lastDot == -1) {
            return filename;
        }

        return filename.substring(0, lastDot);
    }

    /**
     * Check if file is empty
     */
    public static boolean isFileEmpty(File file) {
        return getFileSize(file) == 0L;
    }

    /**
     * Check if directory is empty
     */
    public static boolean isDirectoryEmpty(File directory) {
        if (directory == null || !directory.exists() || !directory.isDirectory()) {
            return true;
        }

        String[] files = directory.list();
        return files == null || files.length == 0;
    }

    /**
     * Get last modified time
     */
    public static Instant getLastModifiedTime(File file) {
        if (file == null || !file.exists()) {
            return null;
        }

        try {
            FileTime fileTime = Files.getLastModifiedTime(file.toPath());
            return fileTime.toInstant();
        } catch (IOException e) {
            log.error("Failed to get last modified time: {}", file, e);
            return null;
        }
    }

    /**
     * Set last modified time
     */
    public static void setLastModifiedTime(File file, Instant instant) {
        if (file == null || !file.exists() || instant == null) {
            return;
        }

        try {
            Files.setLastModifiedTime(file.toPath(), FileTime.from(instant));
        } catch (IOException e) {
            log.error("Failed to set last modified time: {}", file, e);
        }
    }

    // ========== File Comparison ==========

    /**
     * Compare files content
     */
    public static boolean contentEquals(File file1, File file2) {
        if (file1 == null || file2 == null) {
            return false;
        }

        if (!file1.exists() || !file2.exists()) {
            return false;
        }

        if (file1.length() != file2.length()) {
            return false;
        }

        try {
            byte[] content1 = Files.readAllBytes(file1.toPath());
            byte[] content2 = Files.readAllBytes(file2.toPath());
            return Arrays.equals(content1, content2);
        } catch (IOException e) {
            log.error("Failed to compare files: {} and {}", file1, file2, e);
            return false;
        }
    }

    /**
     * Get file checksum (MD5)
     */
    public static String getChecksum(File file) {
        return getChecksum(file, "MD5");
    }

    /**
     * Get file checksum with specified algorithm
     */
    public static String getChecksum(File file, String algorithm) {
        if (file == null || !file.exists() || !file.isFile()) {
            return null;
        }

        try {
            MessageDigest digest = MessageDigest.getInstance(algorithm);
            try (InputStream is = Files.newInputStream(file.toPath())) {
                byte[] buffer = new byte[BUFFER_SIZE];
                int read;
                while ((read = is.read(buffer)) != -1) {
                    digest.update(buffer, 0, read);
                }
            }
            return EncryptionUtils.bytesToHex(digest.digest());
        } catch (Exception e) {
            log.error("Failed to get checksum for file: {}", file, e);
            return null;
        }
    }

    // ========== Temporary Files ==========

    /**
     * Create temporary file
     */
    public static File createTempFile() {
        return createTempFile(TEMP_FILE_PREFIX, TEMP_FILE_SUFFIX);
    }

    /**
     * Create temporary file with prefix and suffix
     */
    public static File createTempFile(String prefix, String suffix) {
        try {
            Path tempFile = Files.createTempFile(prefix, suffix);
            log.debug("Created temporary file: {}", tempFile);
            return tempFile.toFile();
        } catch (IOException e) {
            log.error("Failed to create temporary file", e);
            throw new FileException("Failed to create temporary file", e);
        }
    }

    /**
     * Create temporary file in specific directory
     */
    public static File createTempFile(File directory, String prefix, String suffix) {
        if (directory == null || !directory.exists() || !directory.isDirectory()) {
            throw new IllegalArgumentException("Invalid directory: " + directory);
        }

        try {
            Path tempFile = Files.createTempFile(directory.toPath(), prefix, suffix);
            log.debug("Created temporary file: {}", tempFile);
            return tempFile.toFile();
        } catch (IOException e) {
            log.error("Failed to create temporary file in directory: {}", directory, e);
            throw new FileException("Failed to create temporary file", e);
        }
    }

    /**
     * Create temporary directory
     */
    public static File createTempDirectory() {
        return createTempDirectory(TEMP_FILE_PREFIX);
    }

    /**
     * Create temporary directory with prefix
     */
    public static File createTempDirectory(String prefix) {
        try {
            Path tempDir = Files.createTempDirectory(prefix);
            log.debug("Created temporary directory: {}", tempDir);
            return tempDir.toFile();
        } catch (IOException e) {
            log.error("Failed to create temporary directory", e);
            throw new FileException("Failed to create temporary directory", e);
        }
    }

    // ========== Path Operations ==========

    /**
     * Normalize path
     */
    public static String normalizePath(String path) {
        if (StringUtils.isBlank(path)) {
            return path;
        }
        return Paths.get(path).normalize().toString();
    }

    /**
     * Get absolute path
     */
    public static String getAbsolutePath(File file) {
        if (file == null) {
            return null;
        }
        return file.getAbsolutePath();
    }

    /**
     * Get canonical path
     */
    public static String getCanonicalPath(File file) {
        if (file == null) {
            return null;
        }

        try {
            return file.getCanonicalPath();
        } catch (IOException e) {
            log.error("Failed to get canonical path: {}", file, e);
            return file.getAbsolutePath();
        }
    }

    /**
     * Resolve path
     */
    public static Path resolve(Path base, String path) {
        if (base == null || StringUtils.isBlank(path)) {
            return base;
        }
        return base.resolve(path);
    }

    /**
     * Get relative path
     */
    public static Path relativize(Path base, Path target) {
        if (base == null || target == null) {
            return null;
        }
        return base.relativize(target);
    }

    // ========== File Compression (ZIP) ==========

    /**
     * Zip file
     */
    public static void zipFile(File sourceFile, File zipFile) {
        if (sourceFile == null || !sourceFile.exists()) {
            throw new FileException("Source file does not exist: " + sourceFile);
        }

        try (ZipOutputStream zos = new ZipOutputStream(
                new FileOutputStream(zipFile))) {

            zipFile(sourceFile, sourceFile.getName(), zos);
            log.debug("Successfully zipped file: {} to {}", sourceFile, zipFile);
        } catch (IOException e) {
            log.error("Failed to zip file: {}", sourceFile, e);
            throw new FileException("Failed to zip file", e);
        }
    }

    /**
     * Zip directory
     */
    public static void zipDirectory(File sourceDir, File zipFile) {
        if (sourceDir == null || !sourceDir.exists() || !sourceDir.isDirectory()) {
            throw new FileException("Source directory does not exist: " + sourceDir);
        }

        try (ZipOutputStream zos = new ZipOutputStream(
                new FileOutputStream(zipFile))) {

            zipDirectory(sourceDir, sourceDir.getName(), zos);
            log.debug("Successfully zipped directory: {} to {}", sourceDir, zipFile);
        } catch (IOException e) {
            log.error("Failed to zip directory: {}", sourceDir, e);
            throw new FileException("Failed to zip directory", e);
        }
    }

    /**
     * Helper method to zip file
     */
    private static void zipFile(File file, String fileName, ZipOutputStream zos)
            throws IOException {
        ZipEntry zipEntry = new ZipEntry(fileName);
        zos.putNextEntry(zipEntry);

        try (FileInputStream fis = new FileInputStream(file)) {
            byte[] buffer = new byte[BUFFER_SIZE];
            int length;
            while ((length = fis.read(buffer)) >= 0) {
                zos.write(buffer, 0, length);
            }
        }
        zos.closeEntry();
    }

    /**
     * Helper method to zip directory recursively
     */
    private static void zipDirectory(File folder, String parentFolder, ZipOutputStream zos)
            throws IOException {
        File[] files = folder.listFiles();
        if (files == null) {
            return;
        }

        for (File file : files) {
            if (file.isDirectory()) {
                zipDirectory(file, parentFolder + "/" + file.getName(), zos);
                continue;
            }
            zipFile(file, parentFolder + "/" + file.getName(), zos);
        }
    }

    /**
     * Unzip file
     */
    public static void unzip(File zipFile, File destDir) {
        if (zipFile == null || !zipFile.exists()) {
            throw new FileException("Zip file does not exist: " + zipFile);
        }

        if (destDir == null) {
            throw new IllegalArgumentException("Destination directory must not be null");
        }

        createDirectory(destDir);

        try (ZipInputStream zis = new ZipInputStream(
                new FileInputStream(zipFile))) {

            ZipEntry zipEntry;
            while ((zipEntry = zis.getNextEntry()) != null) {
                File newFile = new File(destDir, zipEntry.getName());

                // Prevent zip slip vulnerability
                String destDirPath = destDir.getCanonicalPath();
                String destFilePath = newFile.getCanonicalPath();
                if (!destFilePath.startsWith(destDirPath + File.separator)) {
                    throw new FileException("Entry is outside of the target dir: " + zipEntry.getName());
                }

                if (zipEntry.isDirectory()) {
                    createDirectory(newFile);
                } else {
                    createParentDirs(newFile);
                    extractFile(zis, newFile);
                }
                zis.closeEntry();
            }
            log.debug("Successfully unzipped: {} to {}", zipFile, destDir);
        } catch (IOException e) {
            log.error("Failed to unzip file: {}", zipFile, e);
            throw new FileException("Failed to unzip file", e);
        }
    }

    /**
     * Helper method to extract file from zip
     */
    private static void extractFile(ZipInputStream zis, File destFile) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(destFile)) {
            byte[] buffer = new byte[BUFFER_SIZE];
            int length;
            while ((length = zis.read(buffer)) > 0) {
                fos.write(buffer, 0, length);
            }
        }
    }

    // ========== Stream Operations ==========

    /**
     * Copy input stream to file
     */
    public static void copyInputStreamToFile(InputStream inputStream, File file) {
        if (inputStream == null || file == null) {
            throw new IllegalArgumentException("InputStream and file must not be null");
        }

        try {
            createParentDirs(file);
            Files.copy(inputStream, file.toPath(), StandardCopyOption.REPLACE_EXISTING);
            log.debug("Successfully copied input stream to file: {}", file);
        } catch (IOException e) {
            log.error("Failed to copy input stream to file: {}", file, e);
            throw new FileException("Failed to copy input stream to file", e);
        }
    }

    /**
     * Copy file to output stream
     */
    public static void copyFileToOutputStream(File file, OutputStream outputStream) {
        if (file == null || !file.exists() || outputStream == null) {
            throw new IllegalArgumentException("File and output stream must not be null");
        }

        try (InputStream inputStream = new FileInputStream(file)) {
            byte[] buffer = new byte[BUFFER_SIZE];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }
            outputStream.flush();
        } catch (IOException e) {
            log.error("Failed to copy file to output stream: {}", file, e);
            throw new FileException("Failed to copy file to output stream", e);
        }
    }

    /**
     * Open input stream for file
     */
    public static InputStream openInputStream(File file) {
        if (file == null || !file.exists()) {
            throw new FileException("File does not exist: " + file);
        }

        try {
            return new FileInputStream(file);
        } catch (FileNotFoundException e) {
            log.error("Failed to open input stream: {}", file, e);
            throw new FileException("Failed to open input stream", e);
        }
    }

    /**
     * Open output stream for file
     */
    public static OutputStream openOutputStream(File file) {
        return openOutputStream(file, false);
    }

    /**
     * Open output stream for file with append option
     */
    public static OutputStream openOutputStream(File file, boolean append) {
        if (file == null) {
            throw new IllegalArgumentException("File must not be null");
        }

        try {
            createParentDirs(file);
            return new FileOutputStream(file, append);
        } catch (IOException e) {
            log.error("Failed to open output stream: {}", file, e);
            throw new FileException("Failed to open output stream", e);
        }
    }

    // ========== Validation ==========

    /**
     * Check if file exists
     */
    public static boolean exists(File file) {
        return file != null && file.exists();
    }

    /**
     * Check if path exists
     */
    public static boolean exists(Path path) {
        return path != null && Files.exists(path);
    }

    /**
     * Check if is file
     */
    public static boolean isFile(File file) {
        return file != null && file.isFile();
    }

    /**
     * Check if is directory
     */
    public static boolean isDirectory(File file) {
        return file != null && file.isDirectory();
    }

    /**
     * Check if file is readable
     */
    public static boolean isReadable(File file) {
        return file != null && file.canRead();
    }

    /**
     * Check if file is writable
     */
    public static boolean isWritable(File file) {
        return file != null && file.canWrite();
    }

    /**
     * Check if file is executable
     */
    public static boolean isExecutable(File file) {
        return file != null && file.canExecute();
    }

    /**
     * Check if file is hidden
     */
    public static boolean isHidden(File file) {
        if (file == null || !file.exists()) {
            return false;
        }

        try {
            return Files.isHidden(file.toPath());
        } catch (IOException e) {
            return file.isHidden();
        }
    }

    /**
     * Check if file is symbolic link
     */
    public static boolean isSymbolicLink(File file) {
        return file != null && Files.isSymbolicLink(file.toPath());
    }

    /**
     * Validate file size
     */
    public static boolean isFileSizeValid(File file, long maxSize) {
        if (file == null || !file.exists()) {
            return false;
        }
        return getFileSize(file) <= maxSize;
    }

    /**
     * Validate file extension
     */
    public static boolean hasExtension(File file, String... extensions) {
        if (file == null || extensions == null || extensions.length == 0) {
            return false;
        }

        String fileExt = getExtension(file).toLowerCase();
        return Arrays.stream(extensions)
                .map(String::toLowerCase)
                .anyMatch(ext -> ext.equals(fileExt));
    }

    // ========== Custom Exception ==========

    /**
     * Custom file exception
     */
    public static class FileException extends RuntimeException {
        public FileException(String message) {
            super(message);
        }

        public FileException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}