package common;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import net.librec.data.model.ArffAttribute;
import net.librec.data.model.ArffInstance;

public class Util {
	
	/**
     * Read data from the data file.
     *
     * @throws IOException
     *         if the path is not valid
     */
    public static ArrayList<ArffInstance> readData(String dataPath) throws IOException {
    	ArrayList<ArffInstance> instances = new ArrayList<>();
        ArrayList<ArffAttribute> attributes = new ArrayList<>();
        ArrayList<String> attrTypes = new ArrayList<>();
        
        BufferedReader br = new BufferedReader(new FileReader(dataPath));
        boolean dataFlag = false;

        int attrIdx = 0;

        String attrName = null;
        String attrType = null;
        String line = null;

        while (true) {

            // parse DATA if valid
            if (dataFlag) {
                // get all attribute types
                for (ArffAttribute attr : attributes) {
                    attrTypes.add(attr.getType());
                }
                // let data reader control the bufferedReader
                dataReader(br, attrTypes, instances);
            }

            line = br.readLine();

            if (line == null) // finish reading
                break;
            if (line.isEmpty() || line.startsWith("%")) // skip empty or
                // annotation
                continue;

            String[] data = line.trim().split("[ \t]");

            // parse ATTRIBUTE
            if (data[0].toUpperCase().equals("@ATTRIBUTE")) {
                attrName = data[1];
                attrType = data[2];
                attributes.add(new ArffAttribute(attrName, attrType.toUpperCase(), attrIdx++));
            }
            // set DATA flag (finish reading ATTRIBUTES)
            else if (data[0].toUpperCase().equals("@DATA")) {
                dataFlag = true;
            }
        }
        br.close();
        
        // initialize instance attributes
        ArffInstance.attrs = attributes;
        
        return instances;
    }
    
    /**
     * Parse @DATA part of the file.
     *
     * @param rd  the reader of the input file.
     * @throws IOException
     */
    public static void dataReader(Reader rd, ArrayList<String> attrTypes, ArrayList<ArffInstance> instances) throws IOException {
        ArrayList<String> dataLine = new ArrayList<>();
        StringBuilder subString = new StringBuilder();

        int c = 0;
        while ((c = rd.read()) != -1) {
            char ch = (char) c;
            // read line by line
            if (ch == '\n') {
                if (dataLine.size() != 0) { // check if empty line
                    if (!dataLine.get(0).startsWith("%")) { // check if
                        // annotation line
                        dataLine.add(subString.toString());
                        // raise error if inconsistent with attribute define
                        if (dataLine.size() < attrTypes.size()) {
                            throw new IOException("Read data error, inconsistent attribute number!");
                        }

                        // pul column value into columnIds, for one-hot encoding
                        for (int i = 0; i < attrTypes.size(); i++) {
                            String col = dataLine.get(i).trim();
                            dataLine.set(i, col);
                        }

                        instances.add(new ArffInstance(dataLine));

                        subString = new StringBuilder();
                        dataLine = new ArrayList<>();
                    }
                }
            } else if (ch == '|') {
                dataLine.add(subString.toString());
                subString = new StringBuilder();
            } else {
                subString.append(ch);
            }
        }
    }
    
    /** 
     * 获取某包下（包括该包的所有子包）所有类 
     * @param packageName 包名 
     * @return 类的完整名称 
     */  
    public static List<String> getClassName(String packageName) {  
        return getClassName(packageName, true);  
    }  
  
    /** 
     * 获取某包下所有类 
     * @param packageName 包名 
     * @param childPackage 是否遍历子包 
     * @return 类的完整名称 
     */  
    public static List<String> getClassName(String packageName, boolean childPackage) {  
        List<String> fileNames = null;  
        ClassLoader loader = Thread.currentThread().getContextClassLoader();  
        String packagePath = packageName.replace(".", "/");  
        URL url = loader.getResource(packagePath);  
        if (url != null) {  
            String type = url.getProtocol();  
            if (type.equals("file")) {  
                fileNames = getClassNameByFile(url.getPath(), null, childPackage);  
            } else if (type.equals("jar")) {  
                fileNames = getClassNameByJar(url.getPath(), childPackage);  
            }  
        } else {  
            fileNames = getClassNameByJars(((URLClassLoader) loader).getURLs(), packagePath, childPackage);  
        }  
        return fileNames;  
    }  
  
    /** 
     * 从项目文件获取某包下所有类 
     * @param filePath 文件路径 
     * @param className 类名集合 
     * @param childPackage 是否遍历子包 
     * @return 类的完整名称 
     */  
    private static List<String> getClassNameByFile(String filePath, List<String> className, boolean childPackage) {  
        List<String> myClassName = new ArrayList<String>();  
        File file = new File(filePath);  
        File[] childFiles = file.listFiles();  
        for (File childFile : childFiles) {  
            if (childFile.isDirectory()) {  
                if (childPackage) {  
                    myClassName.addAll(getClassNameByFile(childFile.getPath(), myClassName, childPackage));  
                }  
            } else {  
                String childFilePath = childFile.getPath();  
                if (childFilePath.endsWith(".class")) {  
                    childFilePath = childFilePath.substring(childFilePath.indexOf("\\classes") + 9, childFilePath.lastIndexOf("."));  
                    childFilePath = childFilePath.replace("\\", ".");  
                    myClassName.add(childFilePath);  
                }  
            }  
        }  
  
        return myClassName;  
    }  
  
    /** 
     * 从jar获取某包下所有类 
     * @param jarPath jar文件路径 
     * @param childPackage 是否遍历子包 
     * @return 类的完整名称 
     */  
    private static List<String> getClassNameByJar(String jarPath, boolean childPackage) {  
        List<String> myClassName = new ArrayList<String>();  
        String[] jarInfo = jarPath.split("!");  
        String jarFilePath = jarInfo[0].substring(jarInfo[0].indexOf("/"));  
        String packagePath = jarInfo[1].substring(1);  
        try {  
            @SuppressWarnings("resource")
			JarFile jarFile = new JarFile(jarFilePath);  
            Enumeration<JarEntry> entrys = jarFile.entries();  
            while (entrys.hasMoreElements()) {  
                JarEntry jarEntry = entrys.nextElement();  
                String entryName = jarEntry.getName();  
                if (entryName.endsWith(".class")) {  
                    if (childPackage) {  
                        if (entryName.startsWith(packagePath)) {  
                            entryName = entryName.replace("/", ".").substring(0, entryName.lastIndexOf("."));  
                            myClassName.add(entryName);  
                        }  
                    } else {  
                        int index = entryName.lastIndexOf("/");  
                        String myPackagePath;  
                        if (index != -1) {  
                            myPackagePath = entryName.substring(0, index);  
                        } else {  
                            myPackagePath = entryName;  
                        }  
                        if (myPackagePath.equals(packagePath)) {  
                            entryName = entryName.replace("/", ".").substring(0, entryName.lastIndexOf("."));  
                            myClassName.add(entryName);  
                        }  
                    }  
                }  
            }  
        } catch (Exception e) {  
            e.printStackTrace();  
        }  
        return myClassName;  
    }  
  
    /** 
     * 从所有jar中搜索该包，并获取该包下所有类 
     * @param urls URL集合 
     * @param packagePath 包路径 
     * @param childPackage 是否遍历子包 
     * @return 类的完整名称 
     */  
    private static List<String> getClassNameByJars(URL[] urls, String packagePath, boolean childPackage) {  
        List<String> myClassName = new ArrayList<String>();  
        if (urls != null) {  
            for (int i = 0; i < urls.length; i++) {  
                URL url = urls[i];  
                String urlPath = url.getPath();  
                // 不必搜索classes文件夹  
                if (urlPath.endsWith("classes/")) {  
                    continue;  
                }  
                String jarPath = urlPath + "!/" + packagePath;  
                myClassName.addAll(getClassNameByJar(jarPath, childPackage));  
            }  
        }  
        return myClassName;  
    }
    
}
