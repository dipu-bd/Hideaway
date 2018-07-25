/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.dpulab.hideaway.models;

import org.dpulab.hideaway.models.objecttable.TableColumn;
import org.dpulab.hideaway.models.objecttable.TableColumnStyle;
import org.dpulab.hideaway.models.objecttable.TableColumnWidth;
import org.dpulab.hideaway.utils.GeneralUtils;

/**
 *
 * @author dipu
 */
public class IndexEntryModel {

    /**
     * Name of the file
     */
    @TableColumn("File Name")
    @TableColumnWidth(min = 180, max = Integer.MAX_VALUE, prefer = 250)
    @TableColumnStyle("color: black")
    public String fileName;

    @TableColumn("")
    @TableColumnStyle("color: white")
    public String testEntry;

    public long fileSize;

    public IndexEntryModel() {

    }

    @TableColumn("Size")
    @TableColumnStyle("color: #000")
    @TableColumnWidth(min = 80, max = 90, prefer = 85)
    public String fileSizeReadable() {
        return GeneralUtils.formatFileSize(fileSize);
    }

}
