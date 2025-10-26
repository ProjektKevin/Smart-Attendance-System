package com.smartattendance.util;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;

/**
 * Utility for loading files from resources folder.
 */
public class FileLoader {

    // Cache to avoid creating duplicate temp files
    private static final Map<String, String> tempFileCache = new HashMap<>();

    /**
     * Load a file from resources as an InputStream.
     * Use this for most Java libraries that accept InputStreams.
     * 
     * @param resourcePath Path relative to resources folder
     * @return InputStream of the resource
     * @throws IOException if resource not found
     */
    public static InputStream loadAsStream(String resourcePath) throws IOException {
        // Ensure path starts with /
        if (!resourcePath.startsWith("/")) {
            resourcePath = "/" + resourcePath;
        }

        InputStream inputStream = FileLoader.class.getResourceAsStream(resourcePath);
        if (inputStream == null) {
            throw new IOException("Resource not found: " + resourcePath);
        }

        return inputStream;
    }

    /**
     * Load resource to a temporary file
     * This method extracts the resource to a temp file and returns its path.
     * 
     * @param resourcePath Path relative to resources folder
     * @return Absolute path to the temporary file
     * @throws IOException if resource not found or cannot create temp file
     */
    public static synchronized String loadToTempFile(String resourcePath) throws IOException {
        String normalizedPath = normalizePath(resourcePath);

        // Check cache first - avoid creating duplicate temp files
        if (tempFileCache.containsKey(normalizedPath)) {
            String cachedPath = tempFileCache.get(normalizedPath);
            // Verify file still exists
            if (Files.exists(Path.of(cachedPath))) {
                return cachedPath;
            } else {
                // File was deleted, remove from cache
                tempFileCache.remove(normalizedPath);
            }
        }

        // Load resource as InputStream
        InputStream inputStream = loadAsStream(normalizedPath);

        // Extract filename components for meaningful temp file name
        String fileName = extractFileName(normalizedPath);
        String prefix = extractPrefix(fileName);
        String suffix = extractSuffix(fileName);

        // Create temp file with meaningful name
        // e.g., "haarcascade_frontalface_default_12345.xml"
        Path tempFile = Files.createTempFile(prefix + "_", suffix);
        tempFile.toFile().deleteOnExit(); // Auto-cleanup on JVM exit

        // Copy resource to temp file
        try (InputStream is = inputStream) {
            Files.copy(is, tempFile, StandardCopyOption.REPLACE_EXISTING);
        }

        String absolutePath = tempFile.toAbsolutePath().toString();

        // Cache the temp file path
        tempFileCache.put(normalizedPath, absolutePath);

        return absolutePath;
    }

    /**
     * Check if a resource exists.
     * 
     * @param resourcePath Path relative to resources folder
     * @return true if resource exists, false otherwise
     */
    public static boolean exists(String resourcePath) {
        String normalizedPath = normalizePath(resourcePath);
        return FileLoader.class.getResource(normalizedPath) != null;
    }

    /**
     * Clear all cached temporary files.
     * Call this on application shutdown.
     */
    public static synchronized void clearTempFiles() {
        for (Map.Entry<String, String> entry : tempFileCache.entrySet()) {
            try {
                Files.deleteIfExists(Path.of(entry.getValue()));
            } catch (IOException e) {
                System.err.println("Failed to delete temp file: " + entry.getValue());
            }
        }
        tempFileCache.clear();
    }

    /**
     * Get number of cached temp files.
     * Useful for debugging/monitoring.
     */
    public static synchronized int getTempFileCount() {
        return tempFileCache.size();
    }

    // ========== Helper Methods ==========

    /**
     * Normalize resource path to start with /
     */
    private static String normalizePath(String path) {
        if (path == null || path.isEmpty()) {
            throw new IllegalArgumentException("Resource path cannot be null or empty");
        }
        return path.startsWith("/") ? path : "/" + path;
    }

    /**
     * Extract filename from path
     */
    private static String extractFileName(String path) {
        int lastSlash = path.lastIndexOf('/');
        return lastSlash >= 0 ? path.substring(lastSlash + 1) : path;
    }

    /**
     * Extract prefix (filename without extension)
     */
    private static String extractPrefix(String fileName) {
        int lastDot = fileName.lastIndexOf('.');
        if (lastDot > 0) {
            return fileName.substring(0, lastDot);
        }
        return fileName;
    }

    /**
     * Extract suffix (file extension with dot)
     */
    private static String extractSuffix(String fileName) {
        int lastDot = fileName.lastIndexOf('.');
        if (lastDot > 0) {
            return fileName.substring(lastDot);
        }
        return ".tmp";
    }
}