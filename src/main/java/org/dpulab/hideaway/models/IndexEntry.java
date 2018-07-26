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
import org.dpulab.hideaway.models.objecttable.TableColumn;
import org.dpulab.hideaway.models.objecttable.TableColumnEditable;
import org.dpulab.hideaway.models.objecttable.TableColumnName;
import org.dpulab.hideaway.models.objecttable.TableColumnStyle;
import org.dpulab.hideaway.models.objecttable.TableColumnWidth;
import org.dpulab.hideaway.utils.CipherIO;
import org.dpulab.hideaway.utils.GeneralUtils;
import org.dpulab.hideaway.utils.Reporter;

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
    private long fileSize = 0;
    private IndexEntry parentEntry;
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
        this.fileName = other.fileName;
        this.fileSize = other.fileSize;
        this.checksum = other.checksum;
        this.parentEntry = other.parentEntry;
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
            entry.fileName = in.readUTF();
            if (file) {
                entry.fileSize = in.readLong();
                entry.checksum = in.readUTF();
            } else {
                long totalFileSize = 0;
                int childrenCount = in.readInt();
                for (int i = 0; i < childrenCount; ++i) {
                    IndexEntry child = IndexEntry.readExternal(in);
                    totalFileSize += child.fileSize;
                    entry.addChild(child);
                }
                entry.fileSize = totalFileSize;
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
        return this.fileName.compareTo(that.fileName);
    }

    /*------------------------------------------------------------------------*\
    |                             Table Entries                                 |   
    \*------------------------------------------------------------------------*/
    @TableColumn(1)
    @TableColumnEditable(true)
    @TableColumnStyle("font-weight: bold")
    @TableColumnWidth(min = 180, prefer = 350, max = Integer.MAX_VALUE)
    public String fileName = "";

    @TableColumn(2)
    @TableColumnName("Size")
    @TableColumnStyle("color: navy")
    @TableColumnWidth(min = 80, max = 90, prefer = 85)
    public final String getFileSizeReadable() {
        return GeneralUtils.formatFileSize(this.fileSize);
    }

    @TableColumn(3)
    @TableColumnName("Type")
    @TableColumnWidth(min = 80, max = 90, prefer = 85)
    public final String getFileType() {
        return isFile() ? "File" : "Directory";
    }

    @TableColumn(3)
    @TableColumnName("Last Modified")
    @TableColumnStyle("color: green")
    @TableColumnWidth(min = 130, max = 145, prefer = 135)
    public final String getLastModifiedReadable() {
        return GeneralUtils.formatDate(getLastModified());
    }

    @TableColumn(5)
    @TableColumnStyle("color: gray")
    @TableColumnWidth(min = 180, prefer = 350, max = Integer.MAX_VALUE)
    public String checksum = null;


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
     * @return the file size in bytes
     */
    public final long getFileSize() {
        return this.fileSize;
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
    \*------------------------------------------------------------------------*/
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
     * @return the absolute path
     */
    public final String getPath() {
        if (this.isRoot()) {
            return "";
        }
        return this.getParentEntry().getPath() + SEPARATOR + this.fileName;
    }

    /**
     * Gets the CipherFile that this entry represents.
     *
     * @return a CipherFile instance
     * @throws java.io.IOException
     * @throws java.security.GeneralSecurityException
     */
    public final File getCipherFile() throws IOException, GeneralSecurityException {
        if (this.isFile()) {
            return CipherIO.instance().getDataFile(this.checksum);
        }
        return null;
    }

    /**
     * Gets the last time when the file content was modified in Unix timestamp.
     *
     * @return
     */
    public final long getLastModified() {
        try {
            return getCipherFile().lastModified();
        } catch (IOException | GeneralSecurityException ex) {
        }
        return 0;
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
        if (child != null && this.children.containsKey(name)) {
            this.children.remove(name);
            this.fileSize -= child.fileSize;
            child.getCipherFile().delete();
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
    public final boolean remove() {
        if (this.isRoot()) {
            return false;
        }
        try {
            return this.getParentEntry().removeChild(this.fileName) == this;
        } catch (IOException | GeneralSecurityException ex) {
            Reporter.put(getClass(), ex);
        }
        return false;
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
        return this.children.containsKey(entry.fileName);
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
        entry.fileName = name;
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
        entry.fileName = name;
        entry.checksum = checksum;
        entry.fileSize = fileSize;
        entry.parentEntry = this;
        this.children.put(name, entry);
        return entry;
    }

}
