# Hideaway
Store files hidden away from others with the help of latest cryptographic algorithms.

---

## TODO

### Models

- CipherFile: `[created at][modified at][content digest length][content digest][file length][file content]`

Operations of IndexEntry:
- isExists
- getCipherFile
- getFileSize
- getFilePath
- getParentFolder -> CipherFolder
- 

### Design Dashboard

#### Sidebar
It will be a normal file explorer. On the left are some fixed items:

- Settings: keystore config, generate new key, change password.
- Favorite files and folders.
- Recently accessed files.
- Trash can.

It should have fixed width. Items should have icons.

#### Explorer
<ul>
<li>On the right is a resizable window.</li>
<li>Each folder will be shown with a folder icon with lock sign. When unlocked, the background will change and unlock sign will appear.</li>
<li>Two view shall be supported: details view, tile view.</li>
<li>Double click on a folder will take you to sub directories.</li>
<li>Right click should open context menu (open file, copy, paste, create folder, create file, etc.).</li>
<li>
  Double click on a file will do these things:
  <ul>
    <li>if it is a text file, open with a custom text editor.</li>
    <li>if it is a image, display it a custom image viewer.</li>
    <li>otherwise display save option to store it elsewhere to view.</li>
  </ul>
</li>
<li>Deleting will move all files under a directory to trash can.</li>
<li>Emptying trash can will permanently remove the files along with encrypted entry in data folder.</li>
</ul>

#### Breadcrumb
- A nice breadcrumb will be available on top of the explorer.
- On right side, a search button shall appear. When clicked, a search form will appear above the explorer.
- Also view selection between (details/tile) mode will be available.
- Will have Forward, backward button.

### Before Production

- Set favicons to frames.
- Set main icons.
- Build for Windows, Linux, and MacOS.

### Future Development

- Sync with google drive.
- Create mobile app.
- Advance backup with error recovery info.
