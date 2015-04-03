package org.gtlp.yasb;

import java.io.File;

public class FileInfo {
	public String remoteHash;
	public String localHash;
	public String source;
	public String name;
	public String fileName;
	public File localFile;
	public int id;
	public boolean needsToBeDownloaded;

	@Override
	public String toString() {
		return id + ": name=" + name + ", localHash=" + localHash + ", remoteHash=" + remoteHash + ", filename=" + fileName + ", needsToBeDownloaded=" + needsToBeDownloaded;
	}
}
