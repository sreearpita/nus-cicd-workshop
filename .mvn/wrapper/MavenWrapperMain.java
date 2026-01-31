/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *    https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;

public class MavenWrapperMain {
    private static final String WRAPPER_VERSION = "3.2.0";
    private static final String DEFAULT_DOWNLOAD_URL = "https://repo.maven.apache.org/maven2/org/apache/maven/apache-maven";

    public static void main(String[] args) throws Exception {
        File mavenUserHome = new File(System.getProperty("user.home"), ".m2");
        File repositoryDirectory = new File(mavenUserHome, "repository");

        String wrapperUrl = readWrapperUrl();
        String wrapperVersion = extractVersion(wrapperUrl);

        File localRepository = new File(repositoryDirectory, "org/apache/maven/apache-maven/" + wrapperVersion);
        File mavenHome = new File(localRepository, "apache-maven-" + wrapperVersion);

        if (!mavenHome.exists()) {
            downloadAndExtractMaven(wrapperUrl, mavenHome);
        }

        executeMaven(mavenHome, args);
    }

    private static String readWrapperUrl() throws IOException {
        Properties properties = new Properties();
        File wrapperProperties = new File(".mvn/wrapper/maven-wrapper.properties");
        
        if (wrapperProperties.exists()) {
            try (FileInputStream fis = new FileInputStream(wrapperProperties)) {
                properties.load(fis);
                return properties.getProperty("distributionUrl", DEFAULT_DOWNLOAD_URL + "/" + WRAPPER_VERSION + "/apache-maven-" + WRAPPER_VERSION + "-bin.zip");
            }
        }
        
        return DEFAULT_DOWNLOAD_URL + "/" + WRAPPER_VERSION + "/apache-maven-" + WRAPPER_VERSION + "-bin.zip";
    }

    private static String extractVersion(String url) {
        // Extract version from URL like: .../apache-maven-3.8.1-bin.zip
        int lastSlash = url.lastIndexOf('/');
        String filename = url.substring(lastSlash + 1);
        int firstDash = filename.indexOf('-');
        int secondDash = filename.indexOf('-', firstDash + 1);
        int binIndex = filename.indexOf("-bin");
        
        return filename.substring(secondDash + 1, binIndex);
    }

    private static void downloadAndExtractMaven(String url, File targetDir) throws IOException {
        System.out.println("Downloading Maven from: " + url);
        
        File zipFile = new File(targetDir.getParent(), "maven.zip");
        zipFile.getParentFile().mkdirs();
        
        // Download
        URL website = new URL(url);
        ReadableByteChannel rbc = Channels.newChannel(website.openStream());
        try (FileOutputStream fos = new FileOutputStream(zipFile)) {
            fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
        }
        
        System.out.println("Extracting Maven to: " + targetDir);
        
        // Extract ZIP file
        extractZip(zipFile, targetDir.getParentFile());
        
        // Rename extracted folder
        File[] extracted = targetDir.getParentFile().listFiles(File::isDirectory);
        for (File file : extracted) {
            if (file.getName().startsWith("apache-maven-")) {
                file.renameTo(targetDir);
                break;
            }
        }
        
        // Clean up
        zipFile.delete();
        
        System.out.println("Maven downloaded and extracted successfully");
    }

    private static void extractZip(File zipFile, File targetDir) throws IOException {
        // Simple ZIP extraction using Java NIO
        try (java.util.zip.ZipFile zip = new java.util.zip.ZipFile(zipFile)) {
            java.util.Enumeration<? extends java.util.zip.ZipEntry> entries = zip.entries();
            
            while (entries.hasMoreElements()) {
                java.util.zip.ZipEntry entry = entries.nextElement();
                File file = new File(targetDir, entry.getName());
                
                if (entry.isDirectory()) {
                    file.mkdirs();
                } else {
                    file.getParentFile().mkdirs();
                    try (java.io.InputStream is = zip.getInputStream(entry);
                         java.io.OutputStream os = new FileOutputStream(file)) {
                        byte[] buffer = new byte[1024];
                        int len;
                        while ((len = is.read(buffer)) > 0) {
                            os.write(buffer, 0, len);
                        }
                    }
                }
            }
        }
    }

    private static void executeMaven(File mavenHome, String[] args) throws Exception {
        String javaHome = System.getProperty("java.home");
        File javaBin = new File(javaHome, "bin");
        File javaExe = new File(javaBin, "java");

        if (!javaExe.exists()) {
            javaExe = new File(javaBin, "java.exe");
        }

        String mavenBin = new File(mavenHome, "bin").getAbsolutePath();
        String mvnScript = new File(mavenBin, isWindows() ? "mvn.cmd" : "mvn").getAbsolutePath();

        ProcessBuilder pb = new ProcessBuilder(mvnScript);
        pb.command().addAll(java.util.Arrays.asList(args));
        pb.inheritIO();

        Process process = pb.start();
        int exitCode = process.waitFor();
        System.exit(exitCode);
    }

    private static boolean isWindows() {
        return System.getProperty("os.name").toLowerCase().contains("win");
    }
}
