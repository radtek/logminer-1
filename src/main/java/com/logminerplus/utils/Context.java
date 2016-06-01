package com.logminerplus.utils;

import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import javax.swing.JFrame;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.Element;

import com.logminerplus.bean.DataSource;
import com.logminerplus.bean.Fileset;
import com.logminerplus.bean.Mapper;

public class Context {
	
	private static Logger logger = Logger.getLogger(Context.class.getName());
	
	private static Context instance;
	
	private Document doc;
	
	private JFrame mainFrame;
	
	private Properties propertiesConstants;
	
	private Properties env    = new Properties();
	
	private DataSource sourceDS;
	
	private DataSource targetDS;
	
	private Fileset fileSet;
	
	private List<Mapper> mapperList;
	
	private Context() {
		String contextPath = ContextConstants.CONFIG_CONTEXT;
		InputStream is = this.getClass().getClassLoader().getResourceAsStream(contextPath);
		try {
			this.doc = XmlUtil.parse(is);
			InputStream fis = this.getClass().getClassLoader().getResourceAsStream("config/env.properties");//属性文件流  
			this.env.load(fis);
			//System.out.println("prop = "+this.propertiesConstants.get("threadnum"));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	public DataSource getDataSource(String element) throws Exception {
		return getDataSource(element, false);
	}
	
	public DataSource getDataSource(String element, boolean refresh) throws Exception {
		if("source".equals(element)) {
			if (sourceDS == null || refresh) {
				sourceDS = readDataSource(element);
				logger.debug("Refresh DataSource For "+element);
	        }
			return sourceDS;
		} else if("target".equals(element)) {
			if (targetDS == null || refresh) {
				targetDS = readDataSource(element);
				logger.debug("Refresh DataSource For "+element);
	        }
			return targetDS;
		}
		return null;
	}
	
	@SuppressWarnings("rawtypes")
	private DataSource readDataSource(String element) throws Exception {
		logger.debug("Read DataSource For "+element);
		DataSource bean = new DataSource();
		Element root = this.doc.getRootElement();
		Element datasource = root.element("datasource");
		Element source = datasource.element(element);
		for (Iterator it = source.elementIterator("property"); it.hasNext();) {
            Element e = (Element) it.next();
            String name = e.attributeValue("name");
            String value = e.attributeValue("value");
            BeanUtils.setProperty(bean, name, value);
        }
		return bean;
	}
	
	@SuppressWarnings("rawtypes")
	public void writeDataSource(String element, DataSource bean) throws Exception {
		logger.debug("Write DataSource For "+element);
		Element root = this.doc.getRootElement();
		Element datasource = root.element("datasource");
		Element ele = datasource.element(element);
		for (Iterator it = ele.elementIterator("property"); it.hasNext();) {
            Element e = (Element) it.next();
            String name = e.attributeValue("name");
            String value = BeanUtils.getProperty(bean, name);
            e.attribute("value").setValue(value);
        }
		writeFile(doc);
		getDataSource(element, true);
	}
	
	public Fileset getFileSet() throws Exception {
		return getFileSet(false);
	}
	
	public Fileset getFileSet(boolean refresh) throws Exception {
		if (fileSet == null || refresh) {
			fileSet = readFileSet();
			logger.debug("Refresh FileSet");
        }
		return fileSet;
	}
	
	@SuppressWarnings("rawtypes")
	private Fileset readFileSet() throws Exception {
		logger.debug("Read FileSet");
		Fileset bean = new Fileset();
		Element root = this.doc.getRootElement();
		Element ele = root.element("fileset");
		for (Iterator it = ele.elementIterator("property"); it.hasNext();) {
            Element e = (Element) it.next();
            String name = e.attributeValue("name");
            String value = e.attributeValue("value");
            BeanUtils.setProperty(bean, name, value);
        }
		return bean;
	}
	
	@SuppressWarnings("rawtypes")
	public void writeFileSet(Fileset bean) throws Exception {
		logger.debug("Write FileSet");
		Element root = this.doc.getRootElement();
		Element ele = root.element("fileset");
		for (Iterator it = ele.elementIterator("property"); it.hasNext();) {
            Element e = (Element) it.next();
            String name = e.attributeValue("name");
            String value = BeanUtils.getProperty(bean, name);
            e.attribute("value").setValue(value);
        }
		writeFile(doc);
		getFileSet(true);
	}
	
	public List<Mapper> getMapperList() throws Exception {
		return getMapperList(false);
	}
	
	public List<Mapper> getMapperList(boolean refresh) throws Exception {
		String element = "field";
		if (mapperList == null || refresh) {
			mapperList = readMapper(element);
			logger.debug("Refresh Mapper For "+element);
        }
		return mapperList;
	}
	
	@SuppressWarnings("rawtypes")
	private List<Mapper> readMapper(String element) throws Exception {
		logger.debug("Read Mapper For "+element);
		List<Mapper> beanList = new ArrayList<Mapper>();
		Element root = this.doc.getRootElement();
		Element mapping = root.element("mapper");
		Element ele = mapping.element(element);
		for (Iterator it = ele.elementIterator("item"); it.hasNext();) {
            Element e = (Element) it.next();
            String tablename = e.element("tablename").getText();
            String source = e.element("source").getText();
            String target = e.element("target").getText();
            Mapper bean = new Mapper();
            bean.setTablename(tablename);
            bean.setSource(source);
            bean.setTarget(target);
            beanList.add(bean);
		}
		return beanList;
	}
	
	public void writeMapper(String element, List<Mapper> beanList) throws Exception {
		logger.debug("Write Mapper For "+element);
		if(beanList!=null) {
			Element root = this.doc.getRootElement();
			Element mapping = root.element("mapper");
			Element ele = mapping.element(element);
			mapping.remove(ele);
			Element newEle = mapping.addElement(element);
			for (Mapper bean : beanList) {
				Element item = newEle.addElement("item");
				item.addElement("tablename").setText(bean.getTablename());
				item.addElement("source").setText(bean.getSource());
				item.addElement("target").setText(bean.getTarget());
			}
		}
		writeFile(doc);
		getMapperList(true);
	}
	
	public void writeFile(Document doc) throws Exception{
		String fileName = FileUtil.getURIPath(ContextConstants.CONFIG_CONTEXT);
		FileWriter out = new FileWriter(fileName);
		doc.write(out);
		out.flush();
		out.close();
	}
	
	public void refresh() throws Exception {
		getDataSource("source", true);
		getDataSource("target", true);
		getFileSet(true);
		getMapperList(true);
	}
	
	public static synchronized Context getInstance() {
        return getInstance(false);
    }

    private static synchronized Context getInstance(boolean refresh) {
        if (instance == null || refresh) {
            instance = new Context();
        }
        return instance;
    }

	public JFrame getMainFrame() {
		return mainFrame;
	}

	public void setMainFrame(JFrame mainFrame) {
		this.mainFrame = mainFrame;
	}

	public Properties getPropertiesConstants() {
		return propertiesConstants;
	}

	public void setPropertiesConstants(Properties propertiesConstants) {
		this.propertiesConstants = propertiesConstants;
	}
	
	public String getProperty(String key) {
		return propertiesConstants.getProperty(key);
	}
	
	public int getPropertyToInteger(String key) {
		//System.out.println("proper = "+key+" "+this.propertiesConstants.getProperty(key));
		return Integer.parseInt(this.propertiesConstants.getProperty(key));
	}
	
	public int getEnvToInteger(String key)
	{
	  return Integer.parseInt(this.env.getProperty(key));
	}

}
