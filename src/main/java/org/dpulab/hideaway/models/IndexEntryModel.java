/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.dpulab.hideaway.models;

import org.dpulab.hideaway.models.objecttable.TableColumnStyle;
import org.dpulab.hideaway.models.objecttable.TableColumnInfo;
import org.dpulab.hideaway.utils.GeneralUtils;

/**
 *
 * @author dipu
 */
public class IndexEntryModel {

    /**
     * Name of the file
     */
    @TableColumnInfo(
            name = "File Name",
            min = 180,
            max = Integer.MAX_VALUE,
            prefer = 250,
            editable = true)
    @TableColumnStyle("color: black")
    public String fileName;

    public long fileSize;

    public IndexEntryModel() {

    }

    @TableColumnInfo(
            name = "Size",
            min = 80,
            max = 90,
            prefer = 85,
            editable = false)
    @TableColumnStyle("color: #000")
    public String fileSizeReadable() {
        return GeneralUtils.formatFileSize(fileSize);
    }

}
