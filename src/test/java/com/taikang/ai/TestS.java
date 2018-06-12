package com.taikang.ai;

import java.io.IOException;
import java.io.StringBufferInputStream;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.jdom2.JDOMException;
import org.junit.Test;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

@SuppressWarnings("deprecation")
public class TestS {

	@Test
	public void fun1() {

		Map<String, String> map = new HashMap<String, String>();
		map.put("dd", "ss");
		System.out.println(map);
		System.out.println(map.toString());
	}

	@Test
	public void fun2() throws JDOMException, IOException, ParserConfigurationException, SAXException {
		byte[] bt = { 60, 120, 109, 108, 62, 60, 84, 111, 85, 115, 101, 114, 78, 97, 109, 101, 62, 60, 33, 91, 67, 68,
				65, 84, 65, 91, 103, 104, 95, 102, 100, 53, 49, 49, 48, 53, 97, 102, 50, 99, 52, 93, 93, 62, 60, 47, 84,
				111, 85, 115, 101, 114, 78, 97, 109, 101, 62, 10, 60, 70, 114, 111, 109, 85, 115, 101, 114, 78, 97, 109,
				101, 62, 60, 33, 91, 67, 68, 65, 84, 65, 91, 111, 90, 68, 115, 78, 49, 75, 108, 122, 122, 82, 66, 76,
				50, 80, 85, 103, 55, 51, 118, 57, 49, 65, 84, 53, 49, 45, 77, 93, 93, 62, 60, 47, 70, 114, 111, 109, 85,
				115, 101, 114, 78, 97, 109, 101, 62, 10, 60, 67, 114, 101, 97, 116, 101, 84, 105, 109, 101, 62, 49, 53,
				50, 56, 54, 51, 49, 57, 49, 53, 60, 47, 67, 114, 101, 97, 116, 101, 84, 105, 109, 101, 62, 10, 60, 77,
				115, 103, 84, 121, 112, 101, 62, 60, 33, 91, 67, 68, 65, 84, 65, 91, 116, 101, 120, 116, 93, 93, 62, 60,
				47, 77, 115, 103, 84, 121, 112, 101, 62, 10, 60, 67, 111, 110, 116, 101, 110, 116, 62, 60, 33, 91, 67,
				68, 65, 84, 65, 91, -28, -67, -96, -27, -91, -67, 93, 93, 62, 60, 47, 67, 111, 110, 116, 101, 110, 116,
				62, 10, 60, 77, 115, 103, 73, 100, 62, 54, 53, 54, 53, 52, 50, 52, 48, 56, 50, 57, 57, 50, 54, 54, 52,
				54, 52, 57, 60, 47, 77, 115, 103, 73, 100, 62, 10, 60, 47, 120, 109, 108, 62 };

		String str = new String(bt);
		System.out.println(str);
		StringBufferInputStream strIns = new StringBufferInputStream(str);

		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder newDocumentBuilder = factory.newDocumentBuilder();
		org.w3c.dom.Document parse = newDocumentBuilder.parse(strIns);
		org.w3c.dom.Element documentElement = parse.getDocumentElement();
		System.out.println(documentElement.getNodeName());
		NodeList childNodes = documentElement.getChildNodes();
		int length = childNodes.getLength();
		for (int i = 0; i < length; i++) {
			Node item = childNodes.item(i);
			short nodeType = item.getNodeType();
			if (nodeType == Node.ELEMENT_NODE) {
				String nodeName = item.getNodeName();
				Node firstChild = item.getFirstChild();
				// if(firstChild.getNodeType()==Node.CDATA_SECTION_NODE){
				// System.out.println(nodeName+":"+item.getFirstChild().getNodeValue());
				// }
				// if(firstChild.getNodeType()==Node.TEXT_NODE){
				// System.out.println(nodeName+":"+firstChild.getNodeValue());
				// }
				System.out.println(nodeName + ":" + firstChild.getNodeValue());
			}
		}

	}
	
}
