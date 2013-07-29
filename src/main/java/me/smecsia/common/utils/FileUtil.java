package me.smecsia.common.utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import static org.apache.commons.lang.StringUtils.isEmpty;

/**
 * @author: Ilya Sadykov
 */
public class FileUtil {
    public static File createTempDirectory()
            throws IOException {
        final File temp;

        temp = File.createTempFile("temp", Long.toString(System.nanoTime()));

        if (!(temp.delete())) {
            throw new IOException("Could not delete temp file: " + temp.getAbsolutePath());
        }

        if (!(temp.mkdir())) {
            throw new IOException("Could not create temp directory: " + temp.getAbsolutePath());
        }
        return (temp);
    }


    /**
     * Create simple temp file
     *
     * @return
     * @throws java.io.IOException
     */
    public static File createTempFile() throws IOException {
        return createTempFileWithPostfix("");
    }


    /**
     * Reads the contents of a file into a String using the default encoding for the VM.
     * The file is always closed.
     *
     * @param file the file to read, must not be <code>null</code>
     * @return the file contents, never <code>null</code>
     * @throws IOException in case of an I/O error
     * @since 1.3.1
     */
    public static String readFileToString(File file) throws IOException {
        return org.apache.commons.io.FileUtils.readFileToString(file);
    }

    /**
     * Create temp file with the postfix in its name
     *
     * @param postfix
     * @return
     * @throws IOException
     */
    public static File createTempFileWithPostfix(String postfix) throws IOException {
        return File.createTempFile("temp", Long.toString(System.nanoTime()) + postfix);
    }

    /**
     * Crate temp file with the temp content
     */
    public static File createTempFileWithContent(String content) throws IOException {
        File tempFile = createTempFile();
        writeStringToFile(content, tempFile);
        return tempFile;
    }


    /**
     * Write string to the file
     */
    public static void writeStringToFile(String content, File file) throws IOException {
        FileWriter fileWriter = new FileWriter(file.getAbsolutePath());
        BufferedWriter out = new BufferedWriter(fileWriter);
        out.write(content);
        out.close();
    }


    /**
     * Example: "/path/to/my/file.zip.jpg" --> "file.zip.jpg"
     *
     * @param filePath path to file
     * @param sep      path separator ("/")
     * @return extension with the leading dot
     */
    public static String getFileName(String filePath, String sep) {
        return (!isEmpty(filePath)) ?
                filePath.substring(filePath.lastIndexOf(sep) + 1, filePath.length()) : "";
    }

    /**
     * Example: "/path/to/my/file.zip.jpg" --> ".jpg"
     *
     * @param filePath path to file
     * @return extension with the leading dot
     */
    public static String getFileExt(String filePath) {
        return (filePath != null) ? filePath.substring(filePath.lastIndexOf("."), filePath.length()) : "";
    }

}
