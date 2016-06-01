package com.logminerplus.bean;

public class Fileset {
	
	private String dictdir;
	
	private String logdir;
	
	private String watcherdir;
	
	private String bakdir;

	public String getWatcherdir() {
		return watcherdir;
	}

	public void setWatcherdir(String watcherdir) {
		this.watcherdir = watcherdir;
	}

	public String getDictdir() {
		return dictdir;
	}

	public void setDictdir(String dictdir) {
		this.dictdir = dictdir;
	}

	public String getLogdir() {
		return logdir;
	}

	public void setLogdir(String logdir) {
		this.logdir = logdir;
	}

	public String getBakdir() {
		return bakdir;
	}

	public void setBakdir(String bakdir) {
		this.bakdir = bakdir;
	}

}
