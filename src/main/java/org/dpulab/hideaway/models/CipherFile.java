/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.dpulab.hideaway.models;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.Date;

/**
 *
 * @author dipu
 */
public class CipherFile implements Serializable {
    
    private static final byte VERSION = 1;
    
    /*------------------------------------------------------------------------*\
    |                             STATIC METHODS                               |   
    \*------------------------------------------------------------------------*/
    
    /**
     * Reads an instance of this class from an input stream.
     * @param in The stream to read from.
     * @return An instance of this class.
     * @throws IOException 
     */
    public static CipherFile fromStream(InputStream in) throws IOException {
        return new CipherFile(in);
    }
    
    /**
     * Converts byte array to an instance of this class.
     * @param data The array of bytes representing the instance.
     * @return An instance of this class.
     * @throws IOException 
     */
    public static CipherFile fromBytes(byte[] data) throws IOException {
        try (ByteArrayInputStream bais = new ByteArrayInputStream(data)) {
            return new CipherFile(bais);
        }
    }
    
    /*------------------------------------------------------------------------*\
    |                          MAIN FILE CONTENT                               |   
    \*------------------------------------------------------------------------*/
    
    private final String filePath;
    private final long fileSize;
    private final String publicKey;
    private final Date modfiedAt;
    private final Date createdAt;
    private final byte[] signature;
    
    private CipherFile(InputStream in) throws IOException {
        int version = in.read();
        try (DataInputStream dis = new DataInputStream(in)) {
            switch(version) {
                case 1:
                    this.fileSize = dis.readLong();
                    this.modfiedAt = new Date(dis.readLong());
                    this.createdAt = new Date(dis.readLong());
                    this.filePath = dis.readUTF();
                    this.publicKey = dis.readUTF();
                    int signLength = dis.readInt();
                    this.signature = new byte[signLength];
                    dis.read(this.signature);
                    break;
                default:
                    throw new IOException("Unsupported version");
            }
        }   
    }
    
    /**
     * Writes the current instance to an output stream.
     * @param out The output stream to write.
     * @throws IOException 
     */
    public void writeBytes(OutputStream out) throws IOException {
        out.write(VERSION);
        try (DataOutputStream dos = new DataOutputStream(out)) {
            dos.writeLong(this.getFileSize());
            dos.writeLong(this.getModfiedAt().getTime());
            dos.writeLong(this.getCreatedAt().getTime());
            dos.writeUTF(this.getFilePath());
            dos.writeUTF(this.getPublicKey());
            dos.writeInt(this.getCheckSum().length);
            dos.write(this.getCheckSum());
        }
    }

    /**
     * Converts this instance into an array of bytes.
     * @return The bytes representing this instance.
     * @throws IOException
     */
    public byte[] toBytes() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        this.writeBytes(baos);
        baos.close();
        return baos.toByteArray();
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
     * @return the signature
     */
    public byte[] getCheckSum() {
        return signature;
    }

}
