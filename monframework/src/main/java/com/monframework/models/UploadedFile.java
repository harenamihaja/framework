package com.monframework.models;


import jakarta.servlet.http.Part;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class UploadedFile {
    private Part part;
    private String filename;
    private String contentType;
    private long size;

    public UploadedFile(Part part) {
        this.part = part;
        this.filename = extractFilename(part);
        this.contentType = part.getContentType();
        this.size = part.getSize();
    }

    private String extractFilename(Part part) {
        String contentDisposition = part.getHeader("content-disposition");
        if (contentDisposition != null) {
            for (String token : contentDisposition.split(";")) {
                if (token.trim().startsWith("filename")) {
                    return token.substring(token.indexOf('=') + 1).trim()
                            .replace("\"", "");
                }
            }
        }
        return "unknown";
    }

    public String getFilename() {
        return filename;
    }

    public String getContentType() {
        return contentType;
    }

    public long getSize() {
        return size;
    }

    public InputStream getInputStream() throws IOException {
        return part.getInputStream();
    }

    /**
     * Sauvegarde le fichier dans le répertoire spécifié
     * @param uploadDir chemin du répertoire de destination
     * @return le chemin complet du fichier sauvegardé
     */
    public String saveTo(String uploadDir) throws IOException {
        File dir = new File(uploadDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        
        String filepath = uploadDir + File.separator + filename;
        part.write(filepath);
        return filepath;
    }

    /**
     * Sauvegarde avec un nom personnalisé
     */
    public String saveAs(String uploadDir, String customName) throws IOException {
        File dir = new File(uploadDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        
        String filepath = uploadDir + File.separator + customName;
        part.write(filepath);
        return filepath;
    }

    @Override
    public String toString() {
        return "UploadedFile{" +
                "filename='" + filename + '\'' +
                ", contentType='" + contentType + '\'' +
                ", size=" + size +
                '}';
    }
}