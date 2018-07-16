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

/**
 *
 * @author dipu
 */
public class CipherFile extends CipherDocument implements Serializable {

    private static final byte VERSION = 1;

    /*------------------------------------------------------------------------*\
    |                             STATIC METHODS                               |   
    \*------------------------------------------------------------------------*/
    /**
     * Reads an instance of this class from an input stream.
     *
     * @param in The stream to read from.
     * @return An instance of this class.
     * @throws IOException
     */
    public static CipherFile fromStream(InputStream in) throws IOException {
        return new CipherFile(in);
    }

    /**
     * Converts byte array to an instance of this class.
     *
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
    private CipherFile(InputStream in) throws IOException {
        int version = in.read();
        switch (version) {
            case 1:
                this.readBytesV1(in);
                break;
            default:
                throw new IOException("Unsupported version");
        }
    }

    /**
     * Converts this instance into an array of bytes.
     *
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
     * Writes the current instance to an output stream.
     *
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
            dos.writeInt(this.getSignature().length);
            dos.write(this.getSignature());
        }
    }
    
    /**
     * Reads the data of current instance from an input stream. [VERSION = 1]
     * @param in Input stream
     * @throws IOException 
     */
    protected final void readBytesV1(InputStream in) throws IOException {
        try (DataInputStream dis = new DataInputStream(in)) {
            this.setFileSize(dis.readLong());
            this.setModfiedAt(dis.readLong());
            this.setCreatedAt(dis.readLong());
            this.setFilePath(dis.readUTF());
            this.setPublicKey(dis.readUTF());
            int signLength = dis.readInt();
            byte[] signature = new byte[signLength];
            this.setSignature(signature);
        }
    }   

    @Override
    public boolean isFile() {
        return true;
    }

    @Override
    public boolean isFolder() {
        return false;
    }

    @Override
    public boolean exists() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
