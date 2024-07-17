package me.redstoner2019.util;

import org.apache.commons.io.IOUtils;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.file.Files;

public class Util {
    public static String readFile(String path) throws IOException {
        return new String(Util.class.getClassLoader().getResourceAsStream(path).readAllBytes());
    }

    public static void writeStringToFile(String str, File file) throws IOException {
        FileOutputStream outputStream = new FileOutputStream(file);
        byte[] strToBytes = str.getBytes();
        outputStream.write(strToBytes);
        outputStream.close();
    }

    public static void writeJSONToFile(JSONObject str, File file) throws IOException {
        writeStringToFile(str.toString(),file);
    }

    public static String readFile(File path) throws IOException {
        byte[] encoded = Files.readAllBytes(path.toPath());
        return new String(encoded, Charset.defaultCharset());
    }

    public static ByteBuffer createBuffer(String resourcePath){
        System.out.println("Creating Buffer for " + resourcePath);
        try {
            InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(resourcePath);
            byte[] bytes = IOUtils.toByteArray(is);
            ByteBuffer imageBuffer = ByteBuffer.allocateDirect(bytes.length);
            imageBuffer.put(bytes);
            imageBuffer.flip();
            return imageBuffer;
        }catch (Exception e){
            e.printStackTrace();
            System.out.println("Failed to load Buffer from resource: " + resourcePath);
        }
        return null;
    }
}
