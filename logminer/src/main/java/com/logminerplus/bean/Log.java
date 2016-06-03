package com.logminerplus.bean;

import java.sql.Timestamp;

public class Log  implements Comparable<Log>{
	
	private Timestamp ts;
	
	private String filename;
	
	private String absolutePath;

	public Timestamp getTs() {
		return ts;
	}

	public void setTs(Timestamp ts) {
		this.ts = ts;
	}

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

	public String getAbsolutePath() {
		return absolutePath;
	}

	public void setAbsolutePath(String absolutePath) {
		this.absolutePath = absolutePath;
	}

	@Override
	public int compareTo(Log o) {
		return getTs().compareTo(o.getTs());
	}

}
