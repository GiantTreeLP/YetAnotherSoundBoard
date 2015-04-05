package org.gtlp.yasb;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.File;

public class FileInfo implements Parcelable {
	public static final Parcelable.Creator<FileInfo> CREATOR = new Parcelable.Creator<FileInfo>() {
		@Override
		public FileInfo createFromParcel(Parcel in) {
			return new FileInfo(in);
		}

		@Override
		public FileInfo[] newArray(int size) {
			return new FileInfo[size];
		}
	};
	public String remoteHash;
	public String localHash;
	public String source;
	public String name;
	public String filePath;
	public File localFile;
	public int id;
	public boolean needsToBeDownloaded = false;

	protected FileInfo(Parcel in) {
		remoteHash = in.readString();
		localHash = in.readString();
		source = in.readString();
		name = in.readString();
		filePath = in.readString();
		id = in.readInt();
		needsToBeDownloaded = in.readByte() != 0;

		localFile = new File(filePath);
	}

	protected FileInfo(String remoteHash, String localHash, String source, String name, String filePath, int id, File file) {
		this.remoteHash = remoteHash;
		this.localHash = localHash;
		this.source = source;
		this.name = name;
		this.filePath = filePath;
		this.id = id;
		this.localFile = file;
	}

	@Override
	public String toString() {
		return id + ": name=" + name + ", localHash=" + localHash + ", remoteHash=" + remoteHash + ", filePath=" + filePath + ", needsToBeDownloaded=" + needsToBeDownloaded;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(remoteHash);
		dest.writeString(localHash);
		dest.writeString(source);
		dest.writeString(name);
		dest.writeString(filePath);
		dest.writeInt(id);
		dest.writeByte((byte) (needsToBeDownloaded ? 1 : 0));
	}
}