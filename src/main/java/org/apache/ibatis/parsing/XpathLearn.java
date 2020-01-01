package org.apache.ibatis.parsing;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.ByteArrayInputStream;
import java.io.IOException;

/**
 * Xpath学习
 * XPath主要用于解析XML文档
 * @author kylin
 * Date: 2019/12/29 11:50
 **/
public class XpathLearn {


  /**
   * 模拟xml文件读取
   * @return String
   */
  public static String produceXmlFile() {

    return "<class>\n" +
      "   <student rollno=\"393\">\n" +
      "      <firstname>dinkar</firstname>\n" +
      "      <lastname>kad</lastname>\n" +
      "      <nickname>dinkar</nickname>\n" +
      "      <marks>85</marks>\n" +
      "   </student>\n" +
      "   <student rollno=\"493\">\n" +
      "      <firstname>Vaneet</firstname>\n" +
      "      <lastname>Gupta</lastname>\n" +
      "      <nickname>vinni</nickname>\n" +
      "      <marks>95</marks>\n" +
      "   </student>\n" +
      "   <student rollno=\"593\">\n" +
      "      <firstname>jasvir</firstname>\n" +
      "      <lastname>singh</lastname>\n" +
      "      <nickname>jazz</nickname>\n" +
      "      <marks>90</marks>\n" +
      "   </student>\n" +
      "</class>\n";


//    return "<?xml version=\"1.0\"?>\n" +
//      "<class>\n" +
//      "   <student rollno=\"393\">\n" +
//      "      <firstname>dinkar</firstname>\n" +
//      "      <lastname>kad</lastname>\n" +
//      "      <nickname>dinkar</nickname>\n" +
//      "      <marks>85</marks>\n" +
//      "   </student>\n" +
//      "   <student rollno=\"493\">\n" +
//      "      <firstname>Vaneet</firstname>\n" +
//      "      <lastname>Gupta</lastname>\n" +
//      "      <nickname>vinni</nickname>\n" +
//      "      <marks>95</marks>\n" +
//      "   </student>\n" +
//      "   <student rollno=\"593\">\n" +
//      "      <firstname>jasvir</firstname>\n" +
//      "      <lastname>singh</lastname>\n" +
//      "      <nickname>jazz</nickname>\n" +
//      "      <marks>90</marks>\n" +
//      "   </student>\n" +
//      "</class>\n";

  }

  /**
   * 执行顺序
   * 1.获取xml配置文件
   * 2.解析成Document对象
   * 3.xpath解析===路径表达式解析元素
   * 4.获取到解析到的element对象，进行value获取
   */
  public static void main(String []args) throws ParserConfigurationException, IOException, SAXException, XPathExpressionException {
    String xmlEntity = produceXmlFile();
    //1.构建Document对象
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    DocumentBuilder documentBuilder = factory.newDocumentBuilder();
    Document document = documentBuilder.parse(new ByteArrayInputStream(xmlEntity.getBytes()));

    //2.构建Xpath对象 并且使用解析路径解析元素
    XPath xPath = XPathFactory.newInstance().newXPath();
    //解析class标签下面的student标签【这个应该是xml文件编写的时候约定的，或者说使用正则表达式】
    String parseExpression = "/class/student";
    NodeList nodeList = (NodeList) xPath.compile(parseExpression).evaluate(document, XPathConstants.NODESET);

    //3.获取到Node节点后 遍历获取Element元素以及内部的值
    for (int i = 0; i < nodeList.getLength(); i++) {
      Node node = nodeList.item(i);
      System.out.println("当前节点名：" + node.getNodeName());
      //如果当前节点是元素节点 Node和Element应该是父子包含关系 节点有多种类型：其中就有元素节点Element
      if (node.getNodeType() == Node.ELEMENT_NODE) {
        Element element = (Element) node;
        //节点自身的attribute
        System.out.println("学生roll no：" + element.getAttribute("rollno"));

        //节点之下的子节点
        System.out.println("=======子节点部分======");
        System.out.println("firstname:"
          + element.getElementsByTagName("firstname").item(0).getTextContent());
        System.out.println("lastname:"
          + element.getElementsByTagName("lastname").item(0).getTextContent());
        System.out.println("nickname:"
          + element.getElementsByTagName("nickname").item(0).getTextContent());
        System.out.println("marks:"
          + element.getElementsByTagName("marks").item(0).getTextContent());
      }
      //test

    }
  }






}
