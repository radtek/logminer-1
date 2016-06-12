package com.logminerplus.bean;

import java.sql.Timestamp;

public class Log  implements Comparable<Log>{
	
	private Timestamp ts;
	
	private String filename;
	
	private String dirname;
	
	public Log()
	{
	    ts = new Timestamp(-1);
	    filename = new String();
	    dirname = new String();
	}
	
	public Log(Timestamp ts, String filename, String dirname)
	{
	    this.ts = ts;
	    this.filename = filename;
	    this.dirname = dirname;
	}
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
		return dirname + "/" + filename;
	}

    public String getDirname() {
        return dirname;
    }

    public void setDirname(String dirname) {
        this.dirname = dirname;
    }

	@Override
	public int compareTo(Log o) {
		return getTs().compareTo(o.getTs());
	}
}
