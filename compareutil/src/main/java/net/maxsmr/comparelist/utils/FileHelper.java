package net.maxsmr.comparelist.utils;

import net.maxsmr.comparelist.utils.logger.BaseLogger;
import net.maxsmr.comparelist.utils.logger.holder.BaseLoggerHolder;

import net.maxsmr.comparelist.utils.support.Pair;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.lang.reflect.Field;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.channels.OverlappingFileLockException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import static net.maxsmr.comparelist.utils.StreamUtils.readBytesFromInputStream;
import static net.maxsmr.comparelist.utils.StreamUtils.readStringsFromInputStream;
import static net.maxsmr.comparelist.utils.StreamUtils.revectorStream;
import static net.maxsmr.comparelist.utils.Units.sizeToString;


public final class FileHelper {

    private final static BaseLogger logger = BaseLoggerHolder.getInstance().getLogger(FileHelper.class);

    public final static int DEPTH_UNLIMITED = -1;

    private FileHelper() {
        throw new AssertionError("no instances.");
    }

    public static double getPartitionTotalSpace(String path, @NotNull Units.SizeUnit unit) {
        if (isDirExists(path)) {
            try {
                return Units.SizeUnit.convert(new File(path).getTotalSpace(), Units.SizeUnit.BYTES, unit);
            } catch (SecurityException e) {
                logger.e("a SecurityException occurred during convert(): " + e.getMessage(), e);
            }
        }
        return 0;
    }

    public static double getPartitionFreeSpace(String path, @NotNull Units.SizeUnit unit) {
        if (isDirExists(path)) {
            try {
                return Units.SizeUnit.convert(new File(path).getFreeSpace(), Units.SizeUnit.BYTES, unit);
            } catch (SecurityException e) {
                logger.e("a SecurityException occurred during convert(): " + e.getMessage(), e);
            }
        }
        return 0;
    }

    @Nullable
    public static String getCanonicalPath(File file) {
        if (file != null) {
            try {
                return file.getCanonicalPath();
            } catch (IOException e) {
                logger.e("an IOException occurred during getCanonicalPath(): " + e.getMessage(), e);
                return file.getAbsolutePath();
            }
        }
        return null;
    }

    public static boolean isFileLocked(File f) {
        final FileLock l = lockFileChannel(f, false);
        try {
            return l == null;
        } finally {
            releaseLockNoThrow(l);
        }
    }

    @Nullable
    public static FileLock lockFileChannel(@Nullable File f, boolean blocking) {

        if (!isFileExists(f)) {
            logger.e("File '" + f + "' is not exists");
            return null;
        }

        RandomAccessFile randomAccFile = null;
        FileChannel channel = null;

        try {
            randomAccFile = new RandomAccessFile(f, "rw");
            channel = randomAccFile.getChannel();

            try {
                return !blocking ? channel.tryLock() : channel.lock();

            } catch (IOException e) {
                logger.e("an IOException occurred during tryLock()", e);
            } catch (OverlappingFileLockException e) {
                logger.e("an OverlappingFileLockException occurred during tryLock()", e);
            }

        } catch (FileNotFoundException e) {
            logger.e("a FileNotFoundException occurred during new RandomAccessFile()", e);

        } finally {
            try {
                if (channel != null)
                    channel.close();
                if (randomAccFile != null)
                    randomAccFile.close();
            } catch (IOException e) {
                logger.e("an IOException occurred during close()", e);
            }
        }

        return null;
    }

    public static boolean releaseLockNoThrow(@Nullable FileLock lock) {
        try {
            if (lock != null) {
                lock.release();
                return true;
            }
        } catch (IOException e) {
            logger.e("an IOException occurred during release()", e);
        }
        return false;
    }

    public static boolean isFileCorrect(File file) {
        return isFileExists(file) && file.length() > 0;
    }

    public static boolean isFileExists(String fileName, String parentPath) {

        if (TextUtils.isEmpty(fileName) || fileName.contains("/")) {
            return false;
        }

        if (TextUtils.isEmpty(parentPath)) {
            return false;
        }

        File f = new File(parentPath, fileName);
        return f.exists() && f.isFile();
    }

    public static boolean isFileExists(File file) {
        return file != null && isFileExists(file.getAbsolutePath());
    }

    public static boolean isFileExists(String filePath) {
        if (!TextUtils.isEmpty(filePath)) {
            File f = new File(filePath);
            return (f.exists() && f.isFile());
        }
        return false;
    }

    public static boolean isFileReadAccessible(@Nullable File file) {
        return isFileExists(file) && file.canRead();
    }

    public static boolean isFileWriteAccessible(@Nullable File file) {
        return isFileExists(file) && file.canWrite();
    }

    public static boolean isDirExists(@Nullable File dir) {
        return dir != null && isDirExists(dir.getAbsolutePath());
    }

    public static boolean isDirExists(@Nullable String dirPath) {
        if (dirPath == null) {
            return false;
        }
        File dir = new File(dirPath);
        return dir.exists() && dir.isDirectory();
    }

    public static boolean isDirReadAccessible(@Nullable File dir) {
        return isDirExists(dir) && dir.canRead();
    }

    public static boolean isDirWriteAccessible(@Nullable File dir) {
        return isDirExists(dir) && dir.canWrite();
    }

    public static boolean isDirEmpty(File dir) {
        if (isDirExists(dir)) {
            File[] files = dir.listFiles();
            return files == null || files.length == 0;
        }
        return false;
    }

    public static void checkFile(File file) {
        checkFile(file, true);
    }

    public static void checkFile(File file, boolean createIfNotExists) {
        if (!checkFileNoThrow(file, createIfNotExists)) {
            throw new IllegalArgumentException("incorrect file: " + file);
        }
    }

    public static void checkFile(String file) {
        checkFile(file, true);
    }

    public static void checkFile(String file, boolean createIfNotExists) {
        if (!checkFileNoThrow(file, createIfNotExists)) {
            throw new IllegalArgumentException("incorrect file: " + file);
        }
    }

    public static boolean checkFileNoThrow(File file) {
        return checkFileNoThrow(file, true);
    }

    public static boolean checkFileNoThrow(File file, boolean createIfNotExists) {
        return file != null && (file.exists() && file.isFile() || (createIfNotExists && createNewFile(file.getName(), file.getParent()) != null));
    }

    public static boolean checkFileNoThrow(String file) {
        return checkFileNoThrow(file, true);
    }

    public static boolean checkFileNoThrow(String file, boolean createIfNotExists) {
        return !TextUtils.isEmpty(file) && checkFileNoThrow(new File(file), createIfNotExists);
    }

    public static void checkDir(String dirPath) {
        checkDir(dirPath, true);
    }

    public static void checkDir(String dirPath, boolean createIfNotExists) {
        if (!checkDirNoThrow(dirPath, createIfNotExists)) {
            throw new IllegalArgumentException("incorrect directory path: " + dirPath);
        }
    }

    public static boolean checkDirNoThrow(String dirPath) {
        return checkDirNoThrow(dirPath, true);
    }

    public static boolean checkDirNoThrow(String dirPath, boolean createIfNotExists) {
        if (!isDirExists(dirPath)) {
            if (!createIfNotExists) {
                return false;
            }
            if (createNewDir(dirPath) == null) {
                return false;
            }
        }
        return true;
    }

    public static File checkPath(String parent, String fileName) {
        return checkPath(parent, fileName, true);
    }

    public static File checkPath(String parent, String fileName, boolean createIfNotExists) {
        File f = checkPathNoThrow(parent, fileName, createIfNotExists);
        if (f == null) {
            throw new IllegalArgumentException("incorrect path: " + parent + File.separator + fileName);
        }
        return f;
    }

    public static File checkPathNoThrow(String parent, String fileName) {
        return checkPathNoThrow(parent, fileName, true);
    }

    public static File checkPathNoThrow(String parent, String fileName, boolean createIfNotExists) {
        if (checkDirNoThrow(parent, createIfNotExists)) {
            if (!TextUtils.isEmpty(fileName)) {
                File f = new File(parent, fileName);
                if (checkFileNoThrow(f, createIfNotExists)) {
                    return f;
                }
            }
        }
        return null;
    }

    /**
     * @return created or existing file
     */
    @Nullable
    private static File createFile(String fileName, String parentPath, boolean recreate) {
        final File file;

        if (recreate) {
            file = createNewFile(fileName, parentPath);
        } else {
            if (!isFileExists(fileName, parentPath))
                file = createNewFile(fileName, parentPath);
            else
                file = new File(parentPath, fileName);
        }

        if (file == null) {
            logger.e("can't create file: " + parentPath + File.separator + fileName);
        }

        return file;
    }

    /**
     * @return null if target file already exists and was not recreated
     */
    public static File createNewFile(String fileName, String parentPath) {
        return createNewFile(fileName, parentPath, true);
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Nullable
    public static File createNewFile(String fileName, String parentPath, boolean recreate) {

        if (TextUtils.isEmpty(fileName) || fileName.contains(File.separator)) {
            return null;
        }

        if (TextUtils.isEmpty(parentPath)) {
            return null;
        }

        File newFile = null;

        File parentDir = new File(parentPath);

        boolean created = false;
        try {
            created = parentDir.mkdirs();
        } catch (SecurityException e) {
            logger.e("an Exception occurred during mkdirs(): " + e.getMessage());
        }

        if (created || isDirExists(parentDir)) {

            newFile = new File(parentDir, fileName);

            if (isFileExists(newFile)) {
                if (recreate && !newFile.delete()) {
                    logger.e("Cannot delete file: " + newFile);
                    newFile = null;
                }
            }

            if (recreate && newFile != null) {
                try {
                    if (!newFile.createNewFile()) {
                        newFile = null;
                    }
                } catch (IOException e) {
                    logger.e("an Exception occurred during createNewFile(): " + e.getMessage());
                    return null;
                }
            }
        }

        return newFile;
    }

    /**
     * @return existing or created empty directory
     */
    @Nullable
    public static File createNewDir(String dirPath) {

        if (TextUtils.isEmpty(dirPath)) {
            logger.e("path is empty");
            return null;
        }

        File dir = new File(dirPath);

        if (dir.isDirectory() && dir.exists())
            return dir;

        if (dir.mkdirs())
            return dir;

        return null;
    }

    @Nullable
    public static File renameFile(File sourceFile, String destinationDir, String newFileName, boolean deleteIfExists, boolean deleteEmptyDirs) {

        if (!isFileExists(sourceFile)) {
            logger.e("Source file not exists: " + sourceFile);
            return null;
        }

        if (TextUtils.isEmpty(newFileName)) {
            logger.e("File name for new file is not specified");
            return null;
        }

        File newFile = null;

        File newDir = createNewDir(destinationDir);
        if (newDir != null) {
            newFile = new File(newDir, newFileName);

            if (!CompareUtils.objectsEqual(newFile, sourceFile)) {

                if (isFileExists(newFile)) {
                    logger.d("Target file " + newFile + " already exists");
                    if (deleteIfExists) {
                        if (!deleteFile(newFile)) {
                            logger.e("Delete file " + newFile + " failed");
                            newFile = null;
                        }
                    } else {
                        logger.w("Not deleting existing file " + newFile);
                        newFile = null;
                    }
                }

                if (newFile != null) {
                    logger.d("Renaming file " + sourceFile + " to " + newFile + "...");
                    if (sourceFile.renameTo(newFile)) {
                        logger.d("File " + sourceFile + " renamed successfully to " + newFile);
                        File sourceParentDir = sourceFile.getParentFile();
                        if (deleteEmptyDirs) {
                            deleteEmptyDir(sourceParentDir);
                        }
                    } else {
                        logger.e("File " + sourceFile + " rename failed to " + newFile);
                        newFile = null;
                    }
                }
            } else {
                logger.e("New file " + newFile + " is same as source file");
            }

        } else {
            logger.e("Create new dir: " + destinationDir + " failed");
        }

        return newFile;
    }

    public static boolean isBinaryFile(File f) throws FileNotFoundException, IOException {

        byte[] data = readBytesFromFile(f);

        if (data == null) {
            return false;
        }

        int ascii = 0;
        int other = 0;

        for (byte b : data) {
            if (b < 0x09)
                return true;

            if (b == 0x09 || b == 0x0A || b == 0x0C || b == 0x0D)
                ascii++;
            else if (b >= 0x20 && b <= 0x7E)
                ascii++;
            else
                other++;
        }

        return other != 0 && 100 * other / (ascii + other) > 95;
    }

    @Nullable
    public static byte[] readBytesFromFile(File file) {

        if (!isFileCorrect(file)) {
            logger.e("incorrect file: " + file);
            return null;
        }

        if (!file.canRead()) {
            logger.e("can't read from file: " + file);
            return null;
        }

        try {
            return readBytesFromInputStream(new FileInputStream(file), true);
        } catch (FileNotFoundException e) {
            logger.e("a FileNotFoundException occurred", e);
            return null;
        }
    }

    @NotNull
    public static List<String> readStringsFromFile(File file) {

        List<String> lines = new ArrayList<>();

        if (!isFileCorrect(file)) {
            logger.e("incorrect file: " + file);
            return lines;
        }

        if (!file.canRead()) {
            logger.e("can't read from file: " + file);
            return lines;
        }

        try {
            return readStringsFromInputStream(new FileInputStream(file), true);
        } catch (FileNotFoundException e) {
            logger.e("an IOException occurred", e);
            return lines;
        }
    }

    @Nullable
    public static String readStringFromFile(File file) {
        List<String> strings = readStringsFromFile(file);
        return !strings.isEmpty() ? TextUtils.join(System.getProperty("line.separator"), strings) : null;
    }


    public static boolean writeBytesToFile(@NotNull File file, byte[] data, boolean append) {
        if (data == null || data.length == 0) {
            return false;
        }
        if (!isFileExists(file.getAbsolutePath()) && (file = createNewFile(file.getName(), file.getAbsolutePath(), !append)) == null) {
            return false;
        }
        if (!file.canWrite()) {
            logger.e("can't write to file: " + file);
            return false;
        }
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(file.getAbsolutePath(), append);
        } catch (FileNotFoundException e) {
            logger.e("an Exception occurred", e);
        }
        if (fos != null) {
            try {
                fos.write(data);
                fos.flush();
                return true;
            } catch (IOException e) {
                logger.e("an Exception occurred", e);
            } finally {
                try {
                    fos.close();
                } catch (IOException e) {
                    logger.e("an Exception occurred", e);
                }
            }
        }
        return false;

    }

    @Nullable
    public static File writeFromStreamToFile(InputStream data, File targetFile, boolean append) {
        return writeFromStreamToFile(data, targetFile, append, null);
    }

    @Nullable
    public static File writeFromStreamToFile(InputStream data, String fileName, String parentPath, boolean append) {
        return writeFromStreamToFile(data, fileName, parentPath, append, null);
    }

    @Nullable
    public static File writeFromStreamToFile(InputStream data, File targetFile, boolean append, StreamUtils.IStreamNotifier notifier) {
        return writeFromStreamToFile(data, targetFile != null ? targetFile.getName() : null, targetFile != null ? targetFile.getParent() : null, append, notifier);
    }

    @Nullable
    public static File writeFromStreamToFile(InputStream data, String fileName, String parentPath, boolean append, StreamUtils.IStreamNotifier notifier) {
        logger.d("writeFromStreamToFile(), data=" + data + ", fileName=" + fileName + ", parentPath=" + parentPath + ", append=" + append);

        final File file = createFile(fileName, parentPath, !append);

        if (file == null) {
            return null;
        }

        if (!file.canWrite()) {
            return file;
        }

        try {
            if (revectorStream(data, new FileOutputStream(file), notifier)) {
                return file;
            }
        } catch (FileNotFoundException e) {
            logger.e("an Exception occurred", e);
        }

        return null;
    }

    public static boolean writeStringToFile(File file, String data, boolean append) {
        return writeStringsToFile(file, Collections.singletonList(data), append);
    }

    public static boolean writeStringsToFile(@Nullable File file, @Nullable Collection<String> data, boolean append) {

        if (data == null || data.isEmpty()) {
            return false;
        }

        if (file == null || !isFileExists(file.getAbsolutePath()) && (file = createNewFile(file.getName(), file.getAbsolutePath(), !append)) == null) {
            return false;
        }
        if (!file.canWrite()) {
            logger.e("can't write to file: " + file);
            return false;
        }

        FileWriter writer;
        try {
            writer = new FileWriter(file);
        } catch (IOException e) {
            logger.d("an IOException occurred", e);
            return false;
        }

        BufferedWriter bw = new BufferedWriter(writer);

        try {
            for (String line : data) {
                bw.append(line);
                bw.append(System.getProperty("line.separator"));
                bw.flush();
            }
            return true;
        } catch (IOException e) {
            logger.e("an IOException occurred during write", e);
        } finally {
            try {
                bw.close();
            } catch (IOException e) {
                logger.e("an IOException occurred during close()", e);
            }
        }

        return false;
    }

    @Nullable
    public static File compressFilesToZip(Collection<File> srcFiles, String destZipName, String destZipParent, boolean recreate) {

        if (srcFiles == null || srcFiles.isEmpty()) {
            logger.e("source files is null or empty");
            return null;
        }

        File zipFile = createFile(destZipName, destZipParent, recreate);

        if (FileHelper.isFileExists(zipFile)) {
            logger.e("cannot create zip file");
            return null;
        }

        try {
            OutputStream os = new FileOutputStream(destZipName);
            ZipOutputStream zos = new ZipOutputStream(new BufferedOutputStream(os));

            try {
                int zippedFiles = 0;

                for (File srcFile : new ArrayList<>(srcFiles)) {

                    if (!isFileCorrect(srcFile)) {
                        logger.e("incorrect file to zip: " + srcFile);
                        continue;
                    }

                    byte[] bytes = readBytesFromFile(srcFile);

                    ZipEntry entry = new ZipEntry(srcFile.getName());
                    zos.putNextEntry(entry);
                    if (bytes != null) {
                        zos.write(bytes);
                    }
                    zos.closeEntry();

                    zippedFiles++;
                }

                return zippedFiles > 0 ? new File(destZipName) : null;

            } catch (Exception e) {
                logger.e("an Exception occurred", e);

            } finally {

                try {
                    zos.close();
                    os.close();
                } catch (IOException e) {
                    logger.e("an IOException occurred during close()", e);
                }

            }

        } catch (IOException e) {
            logger.e("an IOException occurred", e);
        }

        return null;
    }

    public static boolean unzipFile(File zipFile, File destPath, boolean saveDirHierarchy) {

        if (!isFileCorrect(zipFile)) {
            logger.e("incorrect zip file: " + zipFile);
            return false;
        }

        if (destPath == null) {
            logger.e("destPath is null");
            return false;
        }

        ZipFile zip = null;

        InputStream zis = null;
        OutputStream fos = null;

        try {
            zip = new ZipFile(zipFile);

            for (ZipEntry e : Collections.list(zip.entries())) {

                if (e.isDirectory() && !saveDirHierarchy) {
                    continue;
                }

                final String[] parts = e.getName().split(File.separator);
                final String entryName = !saveDirHierarchy && parts.length > 0 ? parts[parts.length - 1] : e.getName();

                File path = new File(destPath, entryName);

                if (e.isDirectory()) {
                    if (createNewDir(path.getAbsolutePath()) == null) {
                        logger.e("can't create directory: " + path);
                        return false;
                    }

                } else {
                    if (createNewFile(path.getName(), path.getParent()) == null) {
                        logger.e("can't create new file: " + path);
                        return false;
                    }

                    zis = zip.getInputStream(e);
                    fos = new FileOutputStream(path);

                    if (!revectorStream(zis, fos)) {
                        logger.e("revectorStream() failed");
                        return false;
                    }

                    zis.close();
                    fos.close();
                }
            }

        } catch (IOException e) {
            logger.e("an IOException occurred", e);
            return false;

        } finally {

            try {
                if (zip != null) {
                    zip.close();
                }
                if (zis != null) {
                    zis.close();
                }
                if (fos != null) {
                    fos.close();
                }
            } catch (IOException e) {
                logger.e("an IOException occurred during close()", e);
            }
        }

        return true;
    }

    @NotNull
    public static Set<File> getFiles(Collection<File> fromFiles, @NotNull GetMode mode, @Nullable Comparator<? super File> comparator, @Nullable IGetNotifier notifier, int depth) {
        Set<File> collected = new LinkedHashSet<>();
        if (fromFiles != null) {
            for (File fromFile : fromFiles) {
                collected.addAll(getFiles(fromFile, mode, comparator, notifier, depth));
            }
        }
        return collected;
    }

    /**
     * @param fromFile file or directory
     * @return collected set of files or directories from specified directories without source files
     */
    @NotNull
    public static Set<File> getFiles(File fromFile, @NotNull GetMode mode, @Nullable Comparator<? super File> comparator, @Nullable IGetNotifier notifier, int depth) {
        return getFiles(fromFile, mode, comparator, notifier, depth, 0, null);
    }

    @NotNull
    private static Set<File> getFiles(File fromFile, @NotNull GetMode mode,
                                      @Nullable Comparator<? super File> comparator, @Nullable IGetNotifier notifier,
                                      int depth, int currentLevel, @Nullable Set<File> collected) {

        final Set<File> result = new LinkedHashSet<>();

        if (collected == null) {
            collected = new LinkedHashSet<>();
        }

        if (fromFile != null && fromFile.exists()) {

            boolean shouldBreak = false;

            if (notifier != null) {
                if (!notifier.onProcessing(fromFile, Collections.unmodifiableSet(collected), currentLevel)) {
                    shouldBreak = true;
                }
            }

            if (!shouldBreak) {

//            if (mode == GetMode.FOLDERS || mode == GetMode.ALL) {
//                if (notifier == null || notifier.onGet(fromFile)) {
//                    result.add(fromFile);
//                }
//            }

                boolean isCorrect = true;

                if (fromFile.isDirectory()) {

                    File[] files = fromFile.listFiles();

                    if (files != null) {

                        for (File f : files) {

                            if (f.isDirectory()) {

                                if (depth == DEPTH_UNLIMITED || depth > currentLevel) {
                                    result.addAll(getFiles(f, mode, comparator, notifier, depth, currentLevel + 1, collected));
                                }

                            } else if (f.isFile()) {
                                result.addAll(getFiles(f, mode, comparator, notifier, depth, currentLevel, collected));
                            } else {
                                logger.e("incorrect file or folder: " + f);
                            }
                        }
                    }
                } else if (!fromFile.isFile()) {
                    logger.e("incorrect file or folder: " + fromFile);
                    isCorrect = false;
                }

                if (isCorrect) {
                    if (fromFile.isFile() ? mode == GetMode.FILES : mode == GetMode.FOLDERS || mode == GetMode.ALL) {
                        if (notifier == null || (fromFile.isFile() ? notifier.onGetFile(fromFile) : notifier.onGetFolder(fromFile))) {
                            result.add(fromFile);
                            collected.add(fromFile);
                        }
                    }
                }
            }
        }

        if (comparator != null) {
            List<File> sortedList = new ArrayList<>(result);
            Collections.sort(sortedList, comparator);
            result.clear();
            result.addAll(sortedList);
        }

        return result;
    }

    @NotNull
    public static Set<File> searchByName(String name, Collection<File> searchFiles, @NotNull GetMode mode, int searchFlags, @Nullable Comparator<? super File> comparator, @Nullable IGetNotifier notifier, int depth) {
        Set<File> collected = new LinkedHashSet<>();
        if (searchFiles != null) {
            for (File searchFile : searchFiles) {
                collected.addAll(searchByName(name, searchFile, mode, searchFlags, comparator, notifier, depth, 0, null));
            }
        }
        return collected;
    }

    /**
     * @param comparator to sort each folders list and result set
     * @return found set of files or directories with matched name
     */
    @NotNull
    public static Set<File> searchByName(String name, File searchFile, @NotNull GetMode mode, int searchFlags, @Nullable Comparator<? super File> comparator, @Nullable IGetNotifier notifier, int depth) {
        return searchByName(name, searchFile, mode, searchFlags, comparator, notifier, depth, 0, null);
    }

    @NotNull
    private static Set<File> searchByName(String name, File searchFile, @NotNull GetMode mode, int searchFlags, @Nullable Comparator<? super File> comparator, @Nullable IGetNotifier notifier,
                                          int depth, int currentLevel, @Nullable Set<File> foundFiles) {

        Set<File> result = new LinkedHashSet<>();

        if (foundFiles == null) {
            foundFiles = new LinkedHashSet<>();
        }

        if (searchFile != null && searchFile.exists()) {

            if (notifier != null) {
                if (!notifier.onProcessing(searchFile, Collections.unmodifiableSet(foundFiles), currentLevel)) {
                    return result;
                }
            }

            boolean isCorrect = true;

            if (searchFile.isDirectory()) {

                File[] files = searchFile.listFiles();

                if (files != null) {

                    if (comparator != null) {
                        List<File> sorted = new ArrayList<>(Arrays.asList(files));
                        Collections.sort(sorted, comparator);
                        files = sorted.toArray(new File[sorted.size()]);
                    }

                    for (File f : files) {

                        if (f.isDirectory()) {
                            if (depth == DEPTH_UNLIMITED || depth > currentLevel) {
                                result.addAll(searchByName(name, f, mode, searchFlags, comparator, notifier, depth, currentLevel + 1, foundFiles));
                            }
                        } else if (f.isFile()) {
                            result.addAll(searchByName(name, f, mode, searchFlags, comparator, notifier, depth, currentLevel, foundFiles));
                        } else {
                            logger.e("incorrect file or folder: " + f);
                        }

                    }
                }

            } else if (!searchFile.isFile()) {
                logger.e("incorrect file or folder: " + searchFile);
                isCorrect = false;
            }

            if (isCorrect) {
                if (searchFile.isFile() ? mode == GetMode.FILES : mode == GetMode.FOLDERS || mode == GetMode.ALL) {
                    if (CompareUtils.stringMatches(searchFile.getName(), name, searchFlags)) {
                        if (notifier == null || (searchFile.isFile() ? notifier.onGetFile(searchFile) : notifier.onGetFolder(searchFile))) {
                            result.add(searchFile);
                            foundFiles.add(searchFile);
                        }
                    }
                }
            }
        }

        if (comparator != null) {
            List<File> sortedList = new ArrayList<>(result);
            Collections.sort(sortedList, comparator);
            result.clear();
            result.addAll(sortedList);
        }

        return result;
    }

    @NotNull
    public static Set<File> searchByNameFirst(String name, Collection<File> searchFiles, @NotNull GetMode getMode, int searchFlags, @Nullable Comparator<? super File> comparator, @Nullable final IGetNotifier notifier, int depth) {
        Set<File> collected = new LinkedHashSet<>();
        for (File file : searchFiles) {
            collected.addAll(searchByName(name, file, getMode, searchFlags, comparator, notifier, depth));
        }
        return collected;
    }

    @Nullable
    public static File searchByNameFirst(String name, File searchFile, @NotNull GetMode getMode, int searchFlags, @Nullable Comparator<? super File> comparator, @Nullable final IGetNotifier notifier, int depth) {
        Set<File> found = searchByName(name, searchFile, getMode, searchFlags, comparator, new IGetNotifier() {
            @Override
            public boolean onProcessing(@NotNull File current, @NotNull Set<File> found, int currentLevel) {
                return (notifier == null || notifier.onProcessing(current, found, currentLevel)) && found.size() == 0;
            }

            @Override
            public boolean onGetFile(@NotNull File file) {
                return notifier == null || notifier.onGetFile(file);
            }

            @Override
            public boolean onGetFolder(@NotNull File folder) {
                return notifier == null || notifier.onGetFolder(folder);
            }
        }, depth);
        return !found.isEmpty() ? new ArrayList<>(found).get(0) : null;
    }

    public static boolean deleteEmptyDir(File dir) {
        return isDirEmpty(dir) && dir.delete();
    }

    public static boolean deleteFile(File file) {
        return isFileExists(file) && file.delete();
    }

    public static boolean deleteFile(String fileName, String parentPath) {
        if (isFileExists(fileName, parentPath)) {
            File f = new File(parentPath, fileName);
            return f.delete();
        }
        return false;
    }

    public static boolean deleteFile(String filePath) {
        if (isFileExists(filePath)) {
            File f = new File(filePath);
            return f.delete();
        }
        return false;
    }

    @NotNull
    public static Set<File> delete(Collection<File> fromFiles, boolean deleteEmptyDirs, @Nullable Collection<File> excludeFiles, @Nullable Comparator<? super File> comparator, @Nullable IDeleteNotifier notifier, int depth) {
        Set<File> collected = new LinkedHashSet<>();
        if (fromFiles != null) {
            for (File file : fromFiles) {
                collected.addAll(delete(file, deleteEmptyDirs, excludeFiles, comparator, notifier, depth));
            }
        }
        return collected;
    }

    /**
     * @param comparator to sort each folders list
     * @return set of deleted files
     */
    @NotNull
    public static Set<File> delete(File fromFile, boolean deleteEmptyDirs, @Nullable Collection<File> excludeFiles, @Nullable Comparator<? super File> comparator, @Nullable IDeleteNotifier notifier, int depth) {
        return delete(fromFile, deleteEmptyDirs, excludeFiles, comparator, notifier, depth, 0, null);
    }

    @NotNull
    private static Set<File> delete(File fromFile, boolean deleteEmptyDirs, @Nullable Collection<File> excludeFiles, @Nullable Comparator<? super File> comparator, @Nullable IDeleteNotifier notifier,
                                    int depth, int currentLevel, @Nullable Set<File> deletedFiles) {

        Set<File> result = new LinkedHashSet<>();

        if (deletedFiles == null) {
            deletedFiles = new LinkedHashSet<>();
        }

        if (fromFile != null && fromFile.exists()) {

            boolean shouldBreak = false;

            if (notifier != null) {
                if (!notifier.onProcessing(fromFile, Collections.unmodifiableSet(deletedFiles), currentLevel)) {
                    shouldBreak = true;
                }
            }

            if (!shouldBreak) {

                if (fromFile.isDirectory()) {
                    File[] files = fromFile.listFiles();

                    if (files != null) {

                        if (comparator != null) {
                            List<File> sorted = new ArrayList<>(Arrays.asList(files));
                            Collections.sort(sorted, comparator);
                            files = sorted.toArray(new File[sorted.size()]);
                        }

                        for (File f : files) {

                            if (excludeFiles == null || !excludeFiles.contains(f)) {
                                if (f.isDirectory()) {
                                    if (depth == DEPTH_UNLIMITED || depth > currentLevel) {
                                        result.addAll(delete(f, deleteEmptyDirs, excludeFiles, comparator, notifier, depth, currentLevel + 1, deletedFiles));
                                    }
                                    if (deleteEmptyDirs && isDirEmpty(f)) {
                                        if (notifier == null || notifier.confirmDeleteFolder(f)) {
                                            if (f.delete()) {
                                                result.add(f);
                                                deletedFiles.add(f);
                                            } else if (notifier != null) {
                                                notifier.onDeleteFolderFailed(f);
                                            }
                                        }
                                    }
                                } else if (f.isFile()) {
                                    result.addAll(delete(f, deleteEmptyDirs, excludeFiles, comparator, notifier, depth, currentLevel, deletedFiles));
                                } else {
                                    logger.e("incorrect file or folder: " + f);
                                }
                            }
                        }

                        if (deleteEmptyDirs && isDirEmpty(fromFile)) {
                            if (notifier == null || notifier.confirmDeleteFolder(fromFile)) {
                                if (fromFile.delete()) {
                                    result.add(fromFile);
                                    deletedFiles.add(fromFile);
                                } else if (notifier != null) {
                                    notifier.onDeleteFolderFailed(fromFile);
                                }
                            }
                        }
                    }
                } else if (fromFile.isFile()) {

                    if (notifier == null || notifier.confirmDeleteFile(fromFile)) {
                        if (fromFile.delete()) {
                            result.add(fromFile);
                            deletedFiles.add(fromFile);
                        } else if (notifier != null) {
                            notifier.onDeleteFileFailed(fromFile);
                        }
                    }

                } else {
                    logger.e("incorrect file or folder: " + fromFile);
                }
            }
        }

        return result;
    }

    public static long getSize(File f, int depth) {
        return getSize(f, depth, 0);
    }

    private static long getSize(File f, int depth, int currentLevel) {
        long size = 0;
        if (f.isDirectory()) {
            File[] files = f.listFiles();
            if (files != null && files.length > 0) {
                for (File file : files) {
                    if (depth == DEPTH_UNLIMITED || depth > currentLevel) {
                        size += getSize(file, depth, currentLevel + 1);
                    }
                }
            }
        } else if (f.isFile()) {
            size = f.length();
        }
        return size;
    }

    /**
     * @return dest file
     */
    public static File copyFile(File sourceFile, String destName, String destDir, boolean rewrite, boolean preserveFileDate) {

        if (!isFileCorrect(sourceFile)) {
            logger.e("source file not exists: " + sourceFile);
            return null;
        }

        String targetName = TextUtils.isEmpty(destName) ? sourceFile.getName() : destName;

        File destFile = createNewFile(targetName, destDir, rewrite);

        if (destFile == null) {
            logger.e("can't create dest file: " + destDir + File.separator + targetName);
            return null;
        }

        if (writeBytesToFile(destFile, readBytesFromFile(sourceFile), !rewrite)) {
            if (preserveFileDate) {
                destFile.setLastModified(sourceFile.lastModified());
            }
            return destFile;
        } else {
            logger.e("can't write to dest file: " + destDir + File.separator + targetName);
        }


        return null;
    }

    /**
     * @return dest file
     */
    @Nullable
    public static File copyFileWithBuffering(final File sourceFile, String destName, String destDir, boolean rewrite, boolean preserveFileDate,
                                             @Nullable final ISingleCopyNotifier notifier) {

        if (!isFileExists(sourceFile)) {
            logger.e("source file not exists: " + sourceFile);
            return null;
        }

        String targetName = TextUtils.isEmpty(destName) ? sourceFile.getName() : destName;

        File destFile = destDir != null && !TextUtils.isEmpty(targetName) ? new File(destDir, targetName) : null;

        if (destFile == null || destFile.equals(sourceFile)) {
            logger.e("Incorrect destination file: " + destDir + " (source file: " + sourceFile + ")");
            return null;
        }

        destFile = createNewFile(targetName, destDir, rewrite);

        if (destFile == null) {
            logger.e("Can't create destination file: " + destDir + File.separator + targetName);
            return null;
        }

        final long totalBytesCount = sourceFile.length();

        try {
            File finalDestFile = destFile;
            if (writeFromStreamToFile(new FileInputStream(sourceFile), destFile.getName(), destFile.getParent(), !rewrite, notifier != null ? new StreamUtils.IStreamNotifier() {
                @Override
                public long notifyInterval() {
                    return notifier.notifyInterval();
                }

                @Override
                public boolean onProcessing(@NotNull InputStream inputStream, @NotNull OutputStream outputStream, long bytesWrite, long bytesLeft) {
                    return notifier.onProcessing(sourceFile, finalDestFile, bytesWrite, totalBytesCount);
                }
            } : null) != null) {
                if (preserveFileDate) {
                    if (!destFile.setLastModified(sourceFile.lastModified())) {
                        logger.e("Can't set last modified on destination file: " + destFile);
                    }
                }
                return destFile;
            }
        } catch (FileNotFoundException e) {
            logger.e("an Exception occurred", e);
        }

        return null;
    }

    /**
     * @param fromFile file or directory
     */
    @NotNull
    @Deprecated
    public static Set<File> copyFilesWithBuffering(File fromFile, File destDir,
                                                   @Nullable Comparator<? super File> comparator,
                                                   @Nullable final ISingleCopyNotifier singleNotifier, @Nullable final IMultipleCopyNotifier multipleCopyNotifier,
                                                   boolean preserveFileDate, int depth) {
        return copyFilesWithBuffering(fromFile, destDir, comparator, singleNotifier, multipleCopyNotifier, preserveFileDate, depth, 0, 0, null, null);
    }

    @NotNull
    @Deprecated
    private static Set<File> copyFilesWithBuffering(File fromFile, File destDir,
                                                    @Nullable Comparator<? super File> comparator,
                                                    @Nullable final ISingleCopyNotifier singleNotifier, @Nullable final IMultipleCopyNotifier multipleCopyNotifier,
                                                    boolean preserveFileDate, int depth,
                                                    int currentLevel, int totalFilesCount, @Nullable Set<File> copied, List<String> exclusionList) {

        Set<File> result = new LinkedHashSet<>();

        if (copied == null) {
            copied = new LinkedHashSet<>();
        }

        boolean isCorrect = false;

        if (destDir != null) {

            if (fromFile != null && fromFile.exists()) {

                isCorrect = true;

                if (currentLevel == 0) {
                    totalFilesCount = getFiles(fromFile, GetMode.FILES, comparator, multipleCopyNotifier != null ? new IGetNotifier() {
                        @Override
                        public boolean onProcessing(@NotNull File current, @NotNull Set<File> collected, int currentLevel) {
                            return multipleCopyNotifier.onCalculatingSize(current, collected, currentLevel);
                        }

                        @Override
                        public boolean onGetFile(@NotNull File file) {
                            return true;
                        }

                        @Override
                        public boolean onGetFolder(@NotNull File folder) {
                            return false;
                        }
                    } : null, depth).size();


                    if (fromFile.isDirectory() && destDir.getAbsolutePath().startsWith(fromFile.getAbsolutePath())) {

                        File[] srcFiles = fromFile.listFiles();

                        if (srcFiles != null && srcFiles.length > 0) {
                            exclusionList = new ArrayList<>(srcFiles.length);
                            for (File srcFile : srcFiles) {
                                exclusionList.add(new File(destDir, srcFile.getName()).getAbsolutePath());
                            }
                        }
                    }

                }

                if (multipleCopyNotifier != null) {
                    if (!multipleCopyNotifier.onProcessing(fromFile, destDir, Collections.unmodifiableSet(copied), totalFilesCount, currentLevel)) {
                        return result;
                    }
                }

                if (fromFile.isDirectory()) {

                    File[] files = fromFile.listFiles();

                    if (files != null) {

                        if (comparator != null) {
                            List<File> sorted = new ArrayList<>(Arrays.asList(files));
                            Collections.sort(sorted, comparator);
                            files = sorted.toArray(new File[sorted.size()]);
                        }

                        for (File f : files) {

//                            if (currentLevel >= 1) {
//                                String tmpPath = destDir.getAbsolutePath();
//                                int index = tmpPath.lastIndexOf(File.separator);
//                                if (index > 0 && index < tmpPath.length() - 1) {
//                                    tmpPath = tmpPath.substring(0, index);
//                                }
//                                destDir = new File(tmpPath);
//                            }

                            if (f.isDirectory()) {
                                if (depth == DEPTH_UNLIMITED || depth > currentLevel) {
                                    result.addAll(copyFilesWithBuffering(f, /*new File(destDir + File.separator + fromFile.getName(), f.getName())*/ destDir, comparator,
                                            singleNotifier, multipleCopyNotifier, preserveFileDate, depth, currentLevel + 1, totalFilesCount, copied, exclusionList));
                                }
                            } else {
                                result.addAll(copyFilesWithBuffering(f, /*new File(destDir, fromFile.getName()) */ destDir, comparator,
                                        singleNotifier, multipleCopyNotifier, preserveFileDate, depth, currentLevel, totalFilesCount, copied, exclusionList));
                            }
                        }

                    }

                    if (files == null || files.length == 0) {
                        String emptyDir = currentLevel == 0 ? destDir + File.separator + fromFile.getName() : destDir.getAbsolutePath();
                        if (!isDirExists(emptyDir)) {
                            createNewDir(emptyDir);
                        }
                    }
                } else if (isFileExists(fromFile)) {

                    File destFile = null;

                    boolean confirmCopy = true;

                    if (multipleCopyNotifier != null) {
                        confirmCopy = multipleCopyNotifier.confirmCopy(fromFile, destDir, currentLevel);
                    }

                    if (confirmCopy) {

                        if (multipleCopyNotifier != null) {
                            destFile = multipleCopyNotifier.onBeforeCopy(fromFile, destDir, currentLevel);
                        }

                        if (destFile == null || destFile.equals(fromFile)) {
                            destFile = new File(destDir, fromFile.getName());
                        }

                        boolean rewrite = false;

                        if (multipleCopyNotifier != null && isFileExists(destFile)) {
                            rewrite = multipleCopyNotifier.onExists(destFile, currentLevel);
                        }

                        File resultFile = null;

                        if (exclusionList == null || !exclusionList.contains(fromFile.getAbsolutePath())) {
                            resultFile = copyFileWithBuffering(fromFile, destFile.getName(), destFile.getParent(), rewrite,
                                    preserveFileDate, singleNotifier);
                        }

                        if (resultFile != null) {
                            result.add(resultFile);
                            copied.add(resultFile);
                        } else {
                            isCorrect = false;
                        }
                    }
                } else {
                    isCorrect = false;
                }
            }

            if (!isCorrect) {
                logger.e("incorrect file or folder or failed to copy from: " + fromFile);
                if (multipleCopyNotifier != null) {
                    multipleCopyNotifier.onFailed(fromFile, destDir, currentLevel);
                }
            }
        } else {
            logger.e("destination dir is not specified");
        }

        if (comparator != null) {
            List<File> sortedList = new ArrayList<>(result);
            Collections.sort(sortedList, comparator);
            result.clear();
            result.addAll(sortedList);
        }
        return result;
    }

    public static Set<File> copyFilesWithBuffering2(File fromFile, File destDir,
                                                    Comparator<? super File> comparator,
                                                    final ISingleCopyNotifier singleNotifier, final IMultipleCopyNotifier2 multipleCopyNotifier,
                                                    boolean preserveFileDate, int depth,
                                                    List<File> exclusionList) {

        Set<File> result = new LinkedHashSet<>();

        if (destDir != null) {
            destDir = FileHelper.createNewDir(destDir.getAbsolutePath());
        }

        if (destDir == null) {
            logger.e("Can't create destination directory");
            return result;
        }

        if (destDir.equals(fromFile)) {
            logger.e("Destination directory " + destDir + " is same as source directory/file " + fromFile);
            return result;
        }

        final Set<File> files = getFiles(fromFile, GetMode.FILES, comparator, multipleCopyNotifier != null ? new IGetNotifier() {
            @Override
            public boolean onProcessing(@NotNull File current, @NotNull Set<File> collected, int currentLevel) {
                return multipleCopyNotifier.onCalculatingSize(current, collected);
            }

            @Override
            public boolean onGetFile(@NotNull File file) {
                return true;
            }

            @Override
            public boolean onGetFolder(@NotNull File folder) {
                return false;
            }
        } : null, depth);

        if (comparator != null) {
            List<File> sorted = new ArrayList<>(files);
            Collections.sort(sorted, comparator);
            files.clear();
            files.addAll(sorted);
        }

        int filesProcessed = 0;
        for (File f : files) {

            if (!isFileExists(f)) {
                continue;
            }

            File currentDestDir = null;
            if (!f.equals(fromFile)) {
                String part = f.getParent();
                if (part.startsWith(fromFile.getAbsolutePath())) {
                    part = part.substring(fromFile.getAbsolutePath().length(), part.length());
                }
                if (!TextUtils.isEmpty(part)) {
                    currentDestDir = new File(destDir, part);
                }
            }
            if (currentDestDir == null) {
                currentDestDir = destDir;
            }

            if (multipleCopyNotifier != null) {
                if (!multipleCopyNotifier.onProcessing(f, currentDestDir, Collections.unmodifiableSet(result), filesProcessed, files.size())) {
                    break;
                }
            }

            if (exclusionList == null || !exclusionList.contains(f)) {

                File destFile = null;

                boolean confirmCopy = true;

                if (multipleCopyNotifier != null) {
                    confirmCopy = multipleCopyNotifier.confirmCopy(f, currentDestDir);
                }

                if (confirmCopy) {

                    if (multipleCopyNotifier != null) {
                        destFile = multipleCopyNotifier.onBeforeCopy(f, currentDestDir);
                    }

                    if (destFile == null || destFile.equals(f)) {
                        destFile = new File(currentDestDir, f.getName());
                    }

                    boolean rewrite = false;

                    if (multipleCopyNotifier != null && isFileExists(destFile)) {
                        rewrite = multipleCopyNotifier.onExists(destFile);
                    }

                    File resultFile = copyFileWithBuffering(f, destFile.getName(), destFile.getParent(), rewrite,
                            preserveFileDate, singleNotifier);

                    if (resultFile != null) {
                        if (multipleCopyNotifier != null) {
                            multipleCopyNotifier.onSucceeded(f, resultFile);
                        }
                        result.add(resultFile);
                    } else {
                        if (multipleCopyNotifier != null) {
                            multipleCopyNotifier.onFailed(f, currentDestDir);
                        }
                    }
                }
            }

            filesProcessed++;
        }

        if (comparator != null) {
            List<File> sorted = new ArrayList<>(result);
            Collections.sort(sorted, comparator);
            result.clear();
            result.addAll(sorted);
        }

        return result;
    }


    public static boolean resetFile(File f) {
        if (f.isFile() && f.exists()) {

            if (f.length() == 0) {
                return true;
            }

            RandomAccessFile ra = null;
            try {
                ra = new RandomAccessFile(f, "rw");
                ra.setLength(0);
                return true;

            } catch (IOException e) {
                logger.e("an IOException occurred", e);
            } finally {
                if (ra != null) {
                    try {
                        ra.close();
                    } catch (IOException e) {
                        logger.e("an IOException occurred during close()", e);
                    }
                }
            }
        }
        return false;
    }

    /**
     * @param id      the constant value of resource subclass field
     * @param resType subclass where the static final field with given id value declared
     */
    public static boolean checkResourceIdExists(int id, String resName, Class<?> resType) throws NullPointerException, IllegalArgumentException, IllegalAccessException {

        if (resType == null)
            throw new NullPointerException("resType is null");

        Field[] fields = resType.getDeclaredFields();

        if (fields == null || fields.length == 0)
            return false;

        for (Field field : fields) {
            field.setAccessible(true);
            if (field.getName().equals(resName)) {
                try {
                    if (CompareUtils.objectsEqual(field.getInt(null), id))
                        return true;
                } catch (Exception e) {
                    logger.e("an Exception occurred during getInt()");
                }
            }
        }

        return false;
    }

    public static String filesToString(Collection<File> files, int depth) {
        if (files != null) {
            Map<File, Long> map = new LinkedHashMap<>();
            for (File f : files) {
                if (f != null) {
                    map.put(f, getSize(f, depth));
                }
            }
            return filesWithSizeToString(map);
        }
        return "";
    }

    public static String filePairsToString(Collection<Pair<File, File>> files, int depth) {
        if (files != null) {
            Map<Pair<File, File>, Long> map = new LinkedHashMap<>();
            for (Pair<File, File> p : files) {
                if (p != null && p.first != null) {
                    map.put(p, getSize(p.first, depth));
                }
            }
            return filePairsWithSizeToString(map);
        }
        return "";
    }

    public static String filesWithSizeToString(Map<File, Long> files) {
        return filesWithSizeToString(files.entrySet());
    }

    /**
     * @param files file < - > size in bytes
     */
    public static String filesWithSizeToString(Collection<Map.Entry<File, Long>> files) {
        StringBuilder sb = new StringBuilder();
        if (files != null) {
            boolean isFirst = false;
            for (Map.Entry<File, Long> f : files) {
                if (f != null && f.getKey() != null) {
                    if (!isFirst) {
                        isFirst = true;
                    } else {
                        sb.append(System.getProperty("line.separator"));
                    }
                    sb.append(f.getKey().getAbsolutePath());
                    sb.append(": ");
                    final Long size = f.getValue();
                    sb.append(size != null ? sizeToString(size, Units.SizeUnit.BYTES) : 0);
                }
            }
        }
        return sb.toString();
    }

    public static String filePairsWithSizeToString(Map<Pair<File, File>, Long> files) {
        return filePairsWithSizeToString(files.entrySet());
    }

    /**
     * @param files pair < source file - destination file > <-> size in bytes
     */
    public static String filePairsWithSizeToString(Collection<Map.Entry<Pair<File, File>, Long>> files) {
        StringBuilder sb = new StringBuilder();
        if (files != null) {
            boolean isFirst = false;
            for (Map.Entry<Pair<File, File>, Long> f : files) {
                if (f != null && f.getKey() != null) {
                    final File sourceFile = f.getKey().first;
                    final File destinationFile = f.getKey().second;
                    if (sourceFile != null) {
                        if (!isFirst) {
                            isFirst = true;
                        } else {
                            sb.append(System.getProperty("line.separator"));
                        }
                        sb.append(sourceFile.getAbsolutePath());
                        if (destinationFile != null) {
                            sb.append(" -> ");
                            sb.append(destinationFile.getAbsolutePath());
                        }
                        sb.append(": ");
                        final Long size = f.getValue();
                        sb.append(size != null ? sizeToString(size, Units.SizeUnit.BYTES) : 0);
                    }
                }
            }
        }
        return sb.toString();
    }

    public enum GetMode {
        FILES, FOLDERS, ALL
    }

    public interface IGetNotifier {

        /**
         * @return false if client code wants to interrupt collecting
         */
        boolean onProcessing(@NotNull File current, @NotNull Set<File> collected, int currentLevel);

        /**
         * @return false if client code doesn't want to append this file to result
         */
        boolean onGetFile(@NotNull File file);


        /**
         * @return false if client code doesn't want to append this folder to result
         */
        boolean onGetFolder(@NotNull File folder);
    }

    public interface IDeleteNotifier {

        /**
         * @return false if client code wants to interrupt deleting
         */
        boolean onProcessing(@NotNull File current, @NotNull Set<File> deleted, int currentLevel);

        /**
         * @return false if client code doesn't want to delete this file
         */
        boolean confirmDeleteFile(File file);

        /**
         * @return false if client code doesn't want to delete this folder
         */
        boolean confirmDeleteFolder(File folder);

        void onDeleteFileFailed(File file);

        void onDeleteFolderFailed(File folder);
    }

    public interface ISingleCopyNotifier {

        long notifyInterval();

        boolean onProcessing(@NotNull File sourceFile, @NotNull File destFile, long bytesCopied, long bytesTotal);
    }

    public interface IMultipleCopyNotifier {

        boolean onCalculatingSize(@NotNull File current, @NotNull Set<File> collected, int currentLevel);

        boolean onProcessing(@NotNull File currentFile, @NotNull File destDir, @NotNull Set<File> copied, long filesTotal, int currentLevel);

        boolean confirmCopy(@NotNull File currentFile, @NotNull File destDir, int currentLevel);

        File onBeforeCopy(@NotNull File currentFile, @NotNull File destDir, int currentLevel);

        boolean onExists(@NotNull File destFile, int currentLevel);

        void onFailed(@Nullable File currentFile, @NotNull File destFile, int currentLevel);
    }

    public interface IMultipleCopyNotifier2 {

        /**
         * @return false if process should be interrupted
         */
        boolean onCalculatingSize(File current, Set<File> collected);

        /**
         * @return false if process should be interrupted
         */
        boolean onProcessing(File currentFile, File destDir, Set<File> copied, long filesProcessed, long filesTotal);

        /**
         * true if copying confirmed by client code, false to cancel
         */
        boolean confirmCopy(File currentFile, File destDir);

        /**
         * @return target file to copy in or null for default
         */
        File onBeforeCopy(File currentFile, File destDir);

        /**
         * @return true if specified destination file is should be replaced (it currently exists)
         */
        boolean onExists(File destFile);

        void onSucceeded(File currentFile, File resultFile);

        void onFailed(File currentFile, File destDir);
    }

}
