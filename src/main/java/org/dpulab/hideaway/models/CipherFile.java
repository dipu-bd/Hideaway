/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.dpulab.hideaway.models;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Date;

/**
 *
 * @author dipu
 */
public class CipherFile implements Externalizable {
    
    private String filePath;
    private long fileSize;
    private String publicKey;
    private Date modfiedAt;
    private Date createdAt;
    private byte[] checkSum;
    
    public CipherFile() {
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeLong(this.getFileSize());
        out.writeLong(this.getModfiedAt().getTime());
        out.writeLong(this.getCreatedAt().getTime());
        out.writeUTF(this.getFilePath());
        out.writeUTF(this.getPublicKey());
        out.writeInt(this.getCheckSum().length);
        out.write(this.getCheckSum());
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        this.fileSize = in.readLong();
        this.modfiedAt = new Date(in.readLong());
        this.createdAt = new Date(in.readLong());
        this.filePath = in.readUTF();
        this.publicKey = in.readUTF();
        int cksumLength = in.readInt();
        this.checkSum = new byte[cksumLength];
        in.read(getCheckSum());        
    }

    /**
     * @return the filePath
     */
    public String getFilePath() {
        return filePath;
    }

    /**
     * @return the fileSize
     */
    public long getFileSize() {
        return fileSize;
    }

    /**
     * @return the publicKey
     */
    public String getPublicKey() {
        return publicKey;
    }

    /**
     * @return the modfiedAt
     */
    public Date getModfiedAt() {
        return modfiedAt;
    }

    /**
     * @return the createdAt
     */
    public Date getCreatedAt() {
        return createdAt;
    }

    /**
     * @return the checkSum
     */
    public byte[] getCheckSum() {
        return checkSum;
    }
    
}
