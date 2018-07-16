/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.dpulab.hideaway.models;

import java.io.InputStream;
import java.util.Date;

/**
 *
 * @author dipu
 */
public abstract class CipherDocument {
    
    private String filePath;
    private long fileSize;
    private String publicKey;
    private Date modfiedAt;
    private Date createdAt;
    private byte[] signature;
    
    /**
     * Checks if the document is a file or folder.
     * @return True if the document is a file, False otherwise.
     */
    public abstract boolean isFile();
    
    /**
     * Checks if the document is a file or folder.
     * @return True if the document is a file, False otherwise.
     */
    public abstract boolean isFolder();
    
    /**
     * Checks whether the document exists
     * @return True if the document exists, False otherwise.
     */
    public abstract boolean exists();
        
    /**
     * @return the filePath
     */
    public final String getFilePath() {
        return filePath;
    }

    /**
     * @return the fileSize
     */
    public final long getFileSize() {
        return fileSize;
    }

    /**
     * @return the publicKey
     */
    public final String getPublicKey() {
        return publicKey;
    }

    /**
     * @return the modfiedAt
     */
    public final Date getModfiedAt() {
        return modfiedAt;
    }

    /**
     * @return the createdAt
     */
    public final Date getCreatedAt() {
        return createdAt;
    }

    /**
     * @return the signature
     */
    public final byte[] getSignature() {
        return signature;
    }

    /**
     * @param filePath the filePath to set
     */
    protected final void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    /**
     * @param fileSize the fileSize to set
     */
    protected final void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    /**
     * @param publicKey the publicKey to set
     */
    protected final void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }

    /**
     * @param modfiedAt the modfiedAt to set
     */
    protected final void setModfiedAt(Date modfiedAt) {
        this.modfiedAt = modfiedAt;
    }

    /**
     * @param createdAt the createdAt to set
     */
    protected final void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    /**
     * @param signature the signature to set
     */
    protected final void setSignature(byte[] signature) {
        this.signature = signature;
    }
    
    /**
     * @param modfiedAt the modfiedAt to set
     */
    protected final void setModfiedAt(long modfiedAt) {
        this.modfiedAt = new Date(modfiedAt);
    }

    /**
     * @param createdAt the createdAt to set
     */
    protected final void setCreatedAt(long createdAt) {
        this.createdAt = new Date(createdAt);
    }
}
