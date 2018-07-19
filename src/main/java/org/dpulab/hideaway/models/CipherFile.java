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
import java.security.GeneralSecurityException;
import java.security.Key;
import org.dpulab.hideaway.utils.CipherIO;

/**
 *
 * @author dipu
 */
public class CipherFile extends IndexEntry {
    
    /*------------------------------------------------------------------------*\
    |                          MAIN FILE CONTENT                               |   
    \*------------------------------------------------------------------------*/
    
    private final File cipherFile;
    private final Key rsaPrivateKey;
    
    public CipherFile(IndexEntry entry) throws GeneralSecurityException {
        super(entry);
        this.cipherFile = CipherIO.getDefault().getDataFile(this.getChecksum());
        this.rsaPrivateKey = CipherIO.getDefault().getSecretKey(this.getPrivateKeyAlias());
    }
}
