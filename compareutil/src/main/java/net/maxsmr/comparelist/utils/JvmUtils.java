package net.maxsmr.comparelist.utils;

import net.maxsmr.comparelist.utils.support.Pair;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

public final class JvmUtils {

    public static Pair<String, String> getJvmVersion() {
        String classPath = String.class.getResource(String.class.getSimpleName() + ".class").toString();
        int index = classPath.lastIndexOf("!");
        if (index >= 0) {
            String libPath = classPath.substring(0, index);
            String filePath = libPath + "!/META-INF/MANIFEST.MF";
            Manifest manifest = null;
            try {
                manifest = new Manifest(new URL(filePath).openStream());
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (manifest != null) {
                Attributes attrs = manifest.getMainAttributes();
                return new Pair<>(attrs.getValue("Manifest-Version"), attrs.getValue("Implementation-Version"));
            }
        }
        return null;
    }

    public static Manifest getJarManifest(Class<?> clazz) {
        try {
            Enumeration<URL> resources = clazz.getClassLoader()
                    .getResources("META-INF/MANIFEST.MF");
            while (resources.hasMoreElements()) {
                InputStream is = resources.nextElement().openStream();
                if (is != null) {
                    return new Manifest(is);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String getJarManifestVersion(Class<?> clazz) {
        Manifest manifest = getJarManifest(clazz);
        if (manifest != null) {
            Attributes attrs = manifest.getMainAttributes();
            if (attrs != null) {
                return attrs.getValue("Manifest-Version");
            }
        }
        return null;
    }
}
