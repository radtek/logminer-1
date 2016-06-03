package com.logminerplus.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.io.Writer;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;

/**
 * @author DongYa
 *
 */
public class XmlUtil {

    public XmlUtil() {
        super();
    }

    public static String toString(Document e, OutputFormat of) {
        StringWriter w = new StringWriter();
        try {
            write(e, w, of);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
        return w.toString();
    }

    public static void write(Document element, Writer wt, OutputFormat of) throws IOException {
        XMLWriter writer = new XMLWriter(wt, of);
        writer.write(element);
    }

    public static Document parse(InputStream is) throws IOException {
        SAXReader reader = new SAXReader();

        Document document;
        try {
            document = reader.read(is);
        } catch (DocumentException e) {
            throw new RuntimeException(e);
        }
        return document;
    }

    public static Document parse(String str) throws IOException {
        Document document;
        try {
            document = DocumentHelper.parseText(str);
        } catch (DocumentException e) {
            throw new RuntimeException(e);
        }
        return document;
    }

}
