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

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.logging.Level;
import org.apache.commons.lang3.StringUtils;
import org.dpulab.hideaway.utils.Reporter;

/**
 *
 * @author dipu
 */
public final class IndexEntry implements Externalizable {

    public static final String SEPARATOR = "/";

    private static final int VERSION = 1;

    /*------------------------------------------------------------------------*\
    |                          MAIN FILE CONTENT                               |   
    \*------------------------------------------------------------------------*/
    private IndexEntry parentEntry;
    private String name = "";
    private long fileSize = 0;
    private String checksum = null;
    private String privateKeyAlias = null;
    private final HashMap<String, IndexEntry> children = new HashMap<>();

    public IndexEntry() {
        // creates a new index entry as root
    }

    /**
     * Clones a index entry
     *
     * @param other The entry to clone
     */
    public IndexEntry(IndexEntry other) {
        this.setName(other.getName());
        this.setFileSize(other.getFileSize());
        this.setChecksum(other.getChecksum());
        this.setPrivateKeyAlias(other.getPrivateKeyAlias());
        this.setParentEntry(other.getParentEntry());
        this.children.putAll(other.children);
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeInt(VERSION);
        out.writeBoolean(this.isFile());
        out.writeUTF(this.name);
        if (this.isFile()) {
            out.writeLong(this.fileSize);
            out.writeUTF(this.checksum);
            out.writeUTF(this.privateKeyAlias);
        } else {
            out.writeInt(this.children.size());
            for (IndexEntry entry : this.children.values()) {
                out.writeObject(entry);
            }
        }
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        int version = in.readInt();
        if (version == 1) {
            boolean file = in.readBoolean();
            this.name = in.readUTF();
            if (file) {
                this.fileSize = in.readLong();
                this.checksum = in.readUTF();
                this.privateKeyAlias = in.readUTF();
            } else {
                long totalFileSize = 0;
                int childrenCount = in.readInt();
                for (int i = 0; i < childrenCount; ++i) {
                    IndexEntry entry = (IndexEntry) in.readObject();
                    this.children.put(entry.name, entry);
                    entry.parentEntry = this;
                    totalFileSize += entry.fileSize;
                }
                this.fileSize = totalFileSize;
            }
        } else {
            Reporter.format(Level.WARNING, "Unsupported version: ", version);
        }
    }

    /*------------------------------------------------------------------------*\
    |                               GETTERS                                    |   
    \*------------------------------------------------------------------------*/

    /**
     * @return true if the top folder
     */
    public boolean isRoot() {
        return this.getParentEntry() == null;
    }

    /**
     * @return true if this is a file
     */
    public boolean isFile() {
        return this.checksum != null;
    }

    /**
     * @return true if this is a folder
     */
    public boolean isDirectory() {
        return this.checksum == null;
    }

    /**
     * @return the absolute path
     */
    public String getPath() {
        if (this.isRoot()) {
            return "";
        }
        return this.getParentEntry().getPath() + SEPARATOR + this.getName();
    }

    /**
     * @return the name
     */
    public String getName() {
        return this.name;
    }

    /**
     * @return the fileSize
     */
    public long getFileSize() {
        return this.fileSize;
    }

    /**
     * @return the fileSize
     */
    public String getFileSizeReadable() {
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
    public String getChecksum() {
        return this.checksum;
    }

    /**
     * @return the privateKeyAlias
     */
    public String getPrivateKeyAlias() {
        return this.privateKeyAlias;
    }

    /**
     * @return the parentEntry
     */
    public IndexEntry getParentEntry() {
        return this.parentEntry;
    }

    /**
     * @return the children
     */
    public Collection<IndexEntry> getChildren() {
        return this.children.values();
    }

    /*------------------------------------------------------------------------*\
    |                               SETTERS                                    |   
    \*------------------------------------------------------------------------*/
    /**
     * @param name the name to set
     */
    protected void setName(String name) {
        this.name = name;
    }

    /**
     * @param fileSize the fileSize to set
     */
    protected void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    /**
     * @param checksum the checksum to set
     */
    protected void setChecksum(String checksum) {
        this.checksum = checksum;
    }

    /**
     * @param privateKeyAlias the privateKeyAlias to set
     */
    protected void setPrivateKeyAlias(String privateKeyAlias) {
        this.privateKeyAlias = privateKeyAlias;
    }

    /**
     * @param parentEntry the parentEntry to set
     */
    protected void setParentEntry(IndexEntry parentEntry) {
        this.parentEntry = parentEntry;
    }

    /*------------------------------------------------------------------------*\
    |                               METHODS                                    |   
    \*------------------------------------------------------------------------*/
    /**
     * @param name
     * @return true if the child exists
     */
    public IndexEntry getChild(String name) {
        return this.children.getOrDefault(name, null);
    }

    /**
     * Resolve an index entry by given path. Each of the path value can be
     * either a valid name, or a path separated by the default separator.
     *
     * @param path A single path or list of path parts.
     * @return
     */
    public IndexEntry resolve(String... path) {
        ArrayList<String> normalized = new ArrayList<>();
        for (String item : path) {
            String[] parts = StringUtils.split(item, SEPARATOR);
            normalized.addAll(Arrays.asList(parts));
        }
        String first = normalized.get(0);
        IndexEntry child = this.children.getOrDefault(name, null);
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
    public boolean hasChild(String name) {
        return this.children.containsKey(name);
    }

    /**
     *
     * @param entry The index entry
     * @return True if the child exists
     */
    public boolean hasChild(IndexEntry entry) {
        return this.children.containsKey(entry.getName());
    }

    /**
     * Creates and returns a new folder.
     *
     * @param name
     * @return
     */
    public IndexEntry createNewFolder(String name) {
        if (name == null) {
            return null;
        }
        IndexEntry entry = new IndexEntry();
        entry.setName(name);
        entry.setParentEntry(this);
        this.children.put(name, entry);
        return entry;
    }
}
