package com.logminerplus.core;

import java.util.List;

import com.logminerplus.bean.Mapper;

public class MinerConvert {
	
	public static String convertSql(String redo, String sourceName, String targetName, List<Mapper> mapperList, boolean update) throws Exception {
		if(update)
		{
		  redo = redo.replaceAll("UPDATE", "update "+"/*+ UD_MULTI_BATCH*/ ");
		  //return redo;
		}
		
		//1.替换双引号；
		redo = redo.replaceAll("\"", "");
		//2.替换数据库名称；
		redo = redo.replaceAll(sourceName, targetName);
		//3.替换日期格式
		redo = redo.replaceAll("'DD-MON-RR'", "'yyyy-mm-dd'");
		//4.替换INSERT INTO为REPLACE INTO
		if(redo.startsWith("INSERT INTO")) {
			redo = redo.replaceAll("INSERT INTO", "REPLACE INTO");
		}
		if(redo.startsWith("insert into")) {
			redo = redo.replaceAll("insert into", "replace into");
		}
		
		if(redo.startsWith("eplace")) {
			redo = redo.replaceAll("eplace", "replace");
		}
		//5.替换映射
		redo = convertMap(redo, mapperList);
		
		redo = redo.replaceAll("TEST\\(\\)", "'test'");
		
		//去掉结尾的分号
		redo = redo.substring(0, redo.length()-1);
		return redo;
	}
	
	/**
	 * 转换字段映射
	 * @param redo
	 * @return
	 * @throws Exception
	 */
	private static String convertMap(String redo, List<Mapper> mapList) throws Exception {
		if(mapList!=null&&!mapList.isEmpty()) {
			for (Mapper mapper : mapList) {
				String s = mapper.getSource();
				String t = mapper.getTarget();
				if(redo.contains(s)) {
					redo = redo.replaceAll(s, t);
				}
			}
		}
		return redo;
	}
	
}
