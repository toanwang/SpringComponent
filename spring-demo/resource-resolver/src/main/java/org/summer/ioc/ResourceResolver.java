package org.summer.ioc;


import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Array;
import java.util.*;
import java.util.stream.Stream;

public class ResourceResolver {
    String basePackage;

    public ResourceResolver(String basePackage){
        this.basePackage = basePackage;
    }

    public List<String> scan() throws IOException, URISyntaxException {
        String basePackagePath = this.basePackage.replace(".", "/");

        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        /* getResource在指定的类路径下搜索，寻找与资源名称匹配的文件，返回第一个资源的 URL
        */
        Enumeration<URL> resources = classLoader.getResources(basePackagePath);

        ArrayList<String> collector = new ArrayList<>();

        while (resources.hasMoreElements()){
            URL url = resources.nextElement();
            URI uri = url.toURI();

            String uriStr = removeTrailingSlash(URLDecoder.decode(uri.toString(), "UTF-8"));
            String uriBaseStr = uriStr.substring(0, uriStr.length() - basePackagePath.length());
            // 获取绝对路径
            if (uriBaseStr.startsWith("file:")) {
                uriBaseStr = uriBaseStr.substring(5);
            }
            Path root = Paths.get(uri);
            scanFile(uriBaseStr, root, collector);
        }
        return collector;
    }

    public void scanFile(String base, Path root, List<String> collector) throws IOException {
        Files.walk(root)
                .filter(Files::isRegularFile)
                .forEach(
                        file -> {
                            String path = file.toString();
                            String name = removeTrailingSlash(path.substring(base.length()));
                            if (name.endsWith(".class")){
                                collector.add(name.substring(0, name.length() - 6).replace("/", ".").replace("\\", "."));
                            }
                        }
                );
    }

    public String removeTrailingSlash(String s) {
        if (s.endsWith("/") || s.endsWith("\\")) {
            s = s.substring(0, s.length() - 1);
        }
        return s;
    }
}