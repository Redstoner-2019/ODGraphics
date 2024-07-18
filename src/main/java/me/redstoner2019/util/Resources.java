package me.redstoner2019.util;

import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

//keytool -import -alias github -keystore "C:\Program Files\Java\jdk-17\lib\security" -file "C:\Users\l.paepke\github.crt"

public class Resources {
    private static Path getJarPath() throws URISyntaxException {
        URL jarUrl = Resources.class.getProtectionDomain().getCodeSource().getLocation();
        return Paths.get(jarUrl.toURI());
    }
    public static List<String> listResources(String resourceFolder){
        try{
            String jarFilePath = getJarPath().toFile().getPath();
            List<String> files = new ArrayList<>();
            if(jarFilePath.endsWith(".jar")){
                try (JarFile jarFile = new JarFile(jarFilePath)) {
                    Enumeration<JarEntry> entries = jarFile.entries();
                    while (entries.hasMoreElements()) {
                        JarEntry entry = entries.nextElement();
                        if (entry.getName().startsWith(resourceFolder) && !entry.isDirectory()) {
                            files.add(entry.getName());
                        }
                    }
                }
            } else {
                for (Path p : Files.list(Path.of("src/main/resources/" + resourceFolder)).toList()) {
                    files.add(p.toString().substring("src/main/resources/".length()));
                }
            }
            return files;
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }
}