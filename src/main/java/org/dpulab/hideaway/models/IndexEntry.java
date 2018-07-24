/*
 * Copyright (C) 2018 dipu
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.dpulab.hideaway.models;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.Serializable;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import org.apache.commons.lang3.StringUtils;
import org.dpulab.hideaway.utils.CipherIO;

/**
 *
 * @author dipu
 */
public class IndexEntry implements Serializable, Comparable<IndexEntry> {

    public static final String SEPARATOR = "/";

    private static final int VERSION = 1;

    public static IndexEntry getRoot() {
        return new IndexEntry();
    }

    public static final String join(String... path) {
        return String.join(SEPARATOR, path);
    }

    /*------------------------------------------------------------------------*\
    |                          MAIN FILE CONTENT                               |   
    \*------------------------------------------------------------------------*/
    private IndexEntry parentEntry;
    private String fileName = "";
    private long fileSize = 0;
    private String checksum = null;
    private final HashMap<String, IndexEntry> children = new HashMap<>();

    // hide access to new IndexEntry
    private IndexEntry() {
    }

    /**
     * Clones a index entry
     *
     * @param other The entry to clone
     */
    public IndexEntry(IndexEntry other) {
        this.setFileName(other.getFileName());
        this.setFileSize(other.getFileSize());
        this.setChecksum(other.getChecksum());
        this.setParentEntry(other.getParentEntry());
        this.children.putAll(other.children);
    }

    public synchronized void writeExternal(ObjectOutput out) throws IOException {
        out.writeInt(VERSION);
        out.writeBoolean(this.isFile());
        out.writeUTF(this.fileName);
        if (this.isFile()) {
            out.writeLong(this.fileSize);
            out.writeUTF(this.checksum);
        } else {
            out.writeInt(this.children.size());
            for (IndexEntry entry : this.children.values()) {
                entry.writeExternal(out);
            }
        }
    }

    public static IndexEntry readExternal(ObjectInput in) throws IOException, UnsupportedClassVersionError {
        int version = in.readInt();
        if (version == 1) {
            IndexEntry entry = new IndexEntry();
            boolean file = in.readBoolean();
            entry.setFileName(in.readUTF());
            if (file) {
                entry.setFileSize(in.readLong());
                entry.setChecksum(in.readUTF());
            } else {
                long totalFileSize = 0;
                int childrenCount = in.readInt();
                for (int i = 0; i < childrenCount; ++i) {
                    IndexEntry child = IndexEntry.readExternal(in);
                    totalFileSize += child.fileSize;
                    entry.addChild(child);
                }
                entry.setFileSize(totalFileSize);
            }
            return entry;
        } else {
            //Reporter.format(Level.WARNING, "Unsupported version: ", version);
            throw new UnsupportedClassVersionError(String.format("Version %d is not supported", version));
        }
    }

    @Override
    public int compareTo(IndexEntry that) {
        if (this.isFile() && that.isDirectory()) {
            return -1; // a < b
        }
        if (this.isDirectory() && that.isFile()) {
            return 1; // a > b
        }
        return this.getFileName().compareTo(that.getFileName());
    }

    /*------------------------------------------------------------------------*\
    |                               GETTERS                                    |   
    \*------------------------------------------------------------------------*/
    /**
     * @return true if the top folder
     */
    public final boolean isRoot() {
        return this.getParentEntry() == null;
    }

    /**
     * @return true if this is a file
     */
    public final boolean isFile() {
        return this.checksum != null;
    }

    /**
     * @return true if this is a folder
     */
    public final boolean isDirectory() {
        return this.checksum == null;
    }

    /**
     * @return the absolute path
     */
    public final String getPath() {
        if (this.isRoot()) {
            return "";
        }
        return this.getParentEntry().getPath() + SEPARATOR + this.getFileName();
    }

    /**
     * @return the fileName
     */
    public final String getFileName() {
        return this.fileName;
    }

    /**
     * @return the fileSize
     */
    public final long getFileSize() {
        return this.fileSize;
    }

    /**
     * @return the fileSize
     */
    public final String getFileSizeReadable() {
        final String[] suffix = {"B", "KB", "MB", "GB", "TB"};
        int p = 0;
        double size = this.fileSize;
        while (size > 1024) {
            size /= 1024;
            p++;
        }
        return String.format("%.2f %s", size, suffix[p]);
    }

    /**
     * @return the checksum
     */
    public final String getChecksum() {
        return this.checksum;
    }

    /**
     * @return the parentEntry
     */
    public final IndexEntry getParentEntry() {
        return this.parentEntry;
    }

    /**
     * @return the children
     */
    public final IndexEntry[] getChildren() {
        return this.children.values().toArray(new IndexEntry[0]);
    }

    /*------------------------------------------------------------------------*\
    |                               SETTERS                                    |   
    \*------------------------------------------------------------------------*/ /**
     * @param fileName the fileName to set
     */

    protected final void setFileName(String fileName) {
        this.fileName = fileName;
    }

    /**
     * @param fileSize the fileSize to set
     */
    protected final void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    /**
     * @param checksum the checksum to set
     */
    protected final void setChecksum(String checksum) {
        this.checksum = checksum;
    }

    /**
     * @param parentEntry the parentEntry to set
     */
    protected final void setParentEntry(IndexEntry parentEntry) {
        this.parentEntry = parentEntry;
    }

    /*------------------------------------------------------------------------*\
    |                               METHODS                                    |   
    \*------------------------------------------------------------------------*/
    /**
     * Gets the CipherFile that this entry represents.
     *
     * @return a CipherFile instance
     * @throws java.io.IOException
     * @throws java.security.GeneralSecurityException
     */
    public final File getCipherFile() throws IOException, GeneralSecurityException {
        if (this.isFile()) {
            return CipherIO.instance().getDataFile(this.getChecksum());
        }
        return null;
    }

    /**
     * @param name
     * @return true if the child exists
     */
    public final IndexEntry getChild(String name) {
        return this.children.getOrDefault(name, null);
    }

    /**
     * @param entry
     * @return true if the child exists
     */
    public final IndexEntry addChild(IndexEntry entry) {
        this.children.put(entry.fileName, entry);
        entry.parentEntry = this;
        return entry;
    }

    /**
     * Removes a child entry, and returns it
     *
     * @param name the entry name to remove
     * @return the removed entry
     * @throws java.io.IOException
     * @throws java.security.GeneralSecurityException
     */
    public final IndexEntry removeChild(String name) throws IOException, GeneralSecurityException {
        IndexEntry child = getChild(name);
        if (child != null) {
            this.children.remove(name);
            this.fileSize -= child.getFileSize();
            this.getCipherFile().delete();
        }
        return child;
    }

    /**
     * Removes the current entry.
     *
     * @return true if successfully deleted.
     * @throws java.io.IOException
     * @throws java.security.GeneralSecurityException
     */
    public final boolean remove() throws IOException, GeneralSecurityException {
        if (this.isRoot()) {
            throw new IllegalAccessError("Root entry can not be removed");
        }
        return this.getParentEntry().removeChild(this.getFileName()) == this;
    }

    /**
     * Resolve an index entry by given path. Each of the path value can be
     * either a valid fileName, or a path separated by the default separator.
     *
     * @param path A single path or list of path parts.
     * @return
     */
    public final IndexEntry resolve(String... path) {
        ArrayList<String> normalized = new ArrayList<>();
        for (String item : path) {
            String[] parts = StringUtils.split(item, SEPARATOR);
            normalized.addAll(Arrays.asList(parts));
        }
        String first = normalized.get(0);
        IndexEntry child = this.children.getOrDefault(first, null);
        if (child != null) {
            normalized.remove(0);
            return child.resolve(normalized.toArray(path));
        }
        return null;
    }

    /**
     * @param name
     * @return true if the child exists
     */
    public final boolean hasChild(String name) {
        if (StringUtils.isEmpty(name)) {
            return false;
        }
        return this.children.containsKey(name);
    }

    /**
     *
     * @param entry The index entry
     * @return True if the child exists
     */
    public final boolean hasChild(IndexEntry entry) {
        if (entry == null) {
            return false;
        }
        return this.children.containsKey(entry.getFileName());
    }

    /**
     * Creates and returns a new folder.
     *
     * @param name
     * @return
     */
    public final IndexEntry createNewFolder(String name) {
        if (name == null) {
            return null;
        }
        IndexEntry entry = new IndexEntry();
        entry.setFileName(name);
        entry.setParentEntry(this);
        this.children.put(name, entry);
        return entry;
    }

    /**
     * Creates a new file under this entry.
     *
     * @param name
     * @param fileSize
     * @param checksum
     * @return the created entry
     */
    public final IndexEntry createNewFile(String name, long fileSize, String checksum) {
        if (name == null || checksum == null) {
            return null;
        }
        IndexEntry entry = new IndexEntry();
        entry.setFileName(name);
        entry.setChecksum(checksum);
        entry.setFileSize(fileSize);
        entry.setParentEntry(this);
        this.children.put(name, entry);
        return entry;
    }

}
