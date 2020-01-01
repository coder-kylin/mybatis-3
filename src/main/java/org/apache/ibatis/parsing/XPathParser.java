/**
 *    Copyright 2009-2019 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package org.apache.ibatis.parsing;

import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.apache.ibatis.builder.BuilderException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * XPath解析，对java自带的Xpath进行更进一层次的封装
 * 主要用于解析：mybatis-config.xml和 **Mapper.xml文件
 *
 * @author Clinton Begin
 * @author Kazuki Shimizu
 */
public class XPathParser {


  /**
   * XML Document对象
   * XML被解析后 生成的org.w3c.dom.document对象
   */
  private final Document document;


  /**
   * 是否校验XML，一般情况下默认为true
   * 校验规则就是XML文档开始位置指定的DTD文件或者XSD文件
   */
  private boolean validation;

  /**
   * XML实体处理器
   * org.xml.sax.EntityResolver对象（非mybatis编写的接口）
   * 默认情况下，对XML进行校验时会基于 XML 文档开始位置指定的 DTD 文件或 XSD 文件。
   * 例如说，解析 mybatis-config.xml 配置文件时，会加载 http://mybatis.org/dtd/mybatis-3-config.dtd 这个 DTD 文件。
   * 但是，如果每个应用启动都从网络加载该 DTD 文件，势必在弱网络下体验非常下，甚至说应用部署在无网络的环境下，
   * 还会导致下载不下来，那么就会出现 XML 校验失败的情况。
   * 所以，在实际场景下，MyBatis 自定义了 EntityResolver 的实现，达到使用本地 DTD 文件，从而避免下载网络 DTD 文件的效果
   *
   * important： 解析是在xpath中做了，这个类是org自带的接口，mybatis自定义XMLMapperEntityResolver类实现EntityResolver
   * 主要用途就是：加载本地mybatis-3-config.dtd 和 mybatis-3-mapper.dtd 这两个 DTD 文件（校验文件）
   */
  private EntityResolver entityResolver;

  /**
   * 变量Properties对象 用来替换需要动态配置的属性值
   * <dataSource type="POOLED">
   *   <property name="driver" value="${driver}"/>
   *   <property name="url" value="${url}"/>
   *   <property name="username" value="dev_user"/>
   *   <property name="password" value="${password}"/>
   * </dataSource>
   * 这里就可以替换${username}的值 而不是硬编码写在配置文件中---不安全
   * variables来源：可以是写在配置文件中的硬编码（上面的dev_user），也可以是动态属性${password}获取
   * 动态属性的获取：参考PropertyParser#parse(String string, Properties variables)
   */
  private Properties variables;

  /**
   * Java java.xml.xpath.Xpath对象
   * 用于查询XML中的节点和元素
   * 对于Xpath不理解的请翻阅 org.apach.ibatis.pasring.XpathLearn
   */
  private XPath xpath;

  public XPathParser(String xml) {
    commonConstructor(false, null, null);
    this.document = createDocument(new InputSource(new StringReader(xml)));
  }

  public XPathParser(Reader reader) {
    commonConstructor(false, null, null);
    this.document = createDocument(new InputSource(reader));
  }

  public XPathParser(InputStream inputStream) {
    commonConstructor(false, null, null);
    this.document = createDocument(new InputSource(inputStream));
  }

  public XPathParser(Document document) {
    commonConstructor(false, null, null);
    this.document = document;
  }

  public XPathParser(String xml, boolean validation) {
    commonConstructor(validation, null, null);
    this.document = createDocument(new InputSource(new StringReader(xml)));
  }

  public XPathParser(Reader reader, boolean validation) {
    commonConstructor(validation, null, null);
    this.document = createDocument(new InputSource(reader));
  }

  public XPathParser(InputStream inputStream, boolean validation) {
    commonConstructor(validation, null, null);
    this.document = createDocument(new InputSource(inputStream));
  }

  public XPathParser(Document document, boolean validation) {
    commonConstructor(validation, null, null);
    this.document = document;
  }

  public XPathParser(String xml, boolean validation, Properties variables) {
    commonConstructor(validation, variables, null);
    this.document = createDocument(new InputSource(new StringReader(xml)));
  }

  public XPathParser(Reader reader, boolean validation, Properties variables) {
    commonConstructor(validation, variables, null);
    this.document = createDocument(new InputSource(reader));
  }

  public XPathParser(InputStream inputStream, boolean validation, Properties variables) {
    commonConstructor(validation, variables, null);
    this.document = createDocument(new InputSource(inputStream));
  }

  public XPathParser(Document document, boolean validation, Properties variables) {
    commonConstructor(validation, variables, null);
    this.document = document;
  }

  /**
   * 构造器
   * @param xml xml文件实体
   * @param validation 是否校验
   * @param variables 参数对象
   * @param entityResolver 解析器实体
   * @return XPathParser
   */
  public XPathParser(String xml, boolean validation, Properties variables, EntityResolver entityResolver) {
    commonConstructor(validation, variables, entityResolver);
    this.document = createDocument(new InputSource(new StringReader(xml)));
  }

  public XPathParser(Reader reader, boolean validation, Properties variables, EntityResolver entityResolver) {
    commonConstructor(validation, variables, entityResolver);
    this.document = createDocument(new InputSource(reader));
  }

  public XPathParser(InputStream inputStream, boolean validation, Properties variables, EntityResolver entityResolver) {
    commonConstructor(validation, variables, entityResolver);
    this.document = createDocument(new InputSource(inputStream));
  }

  public XPathParser(Document document, boolean validation, Properties variables, EntityResolver entityResolver) {
    commonConstructor(validation, variables, entityResolver);
    this.document = document;
  }

  public void setVariables(Properties variables) {
    this.variables = variables;
  }

  /**
   * eval族方法:用于获取 String Boolean Integer Long Float Double Short类型的元素（Element）的值
   */
  public String evalString(String expression) {
    return evalString(document, expression);
  }

  /**
   *
   * @param root document的节点数
   * @param expression 解析路径 如：/class/student
   * @return String 值
   */
  public String evalString(Object root, String expression) {
    //1.获取值 returnType 传入XPathConstants.STRING 表示返回的结果是String类型的值
    String result = (String) evaluate(expression, root, XPathConstants.STRING);
    //2.基于variables替换动态值，如果result为动态值（里面有判断${}这样的就代表是动态值）
    result = PropertyParser.parse(result, variables);
    return result;
  }

  public Boolean evalBoolean(String expression) {
    return evalBoolean(document, expression);
  }

  public Boolean evalBoolean(Object root, String expression) {
    return (Boolean) evaluate(expression, root, XPathConstants.BOOLEAN);
  }

  /**
   * 其他类型就从evalString中获取后再使用 引用类型进行类型转换即可
   */
  public Short evalShort(String expression) {
    return evalShort(document, expression);
  }

  public Short evalShort(Object root, String expression) {
    return Short.valueOf(evalString(root, expression));
  }

  public Integer evalInteger(String expression) {
    return evalInteger(document, expression);
  }

  public Integer evalInteger(Object root, String expression) {
    return Integer.valueOf(evalString(root, expression));
  }

  public Long evalLong(String expression) {
    return evalLong(document, expression);
  }

  public Long evalLong(Object root, String expression) {
    return Long.valueOf(evalString(root, expression));
  }

  public Float evalFloat(String expression) {
    return evalFloat(document, expression);
  }

  public Float evalFloat(Object root, String expression) {
    return Float.valueOf(evalString(root, expression));
  }

  public Double evalDouble(String expression) {
    return evalDouble(document, expression);
  }

  public Double evalDouble(Object root, String expression) {
    return (Double) evaluate(expression, root, XPathConstants.NUMBER);
  }

  /**
   * 根据解析路径获取所有的节点
   * @param expression 解析路径 如 /class/student
   * @return List<XNode>
   */
  public List<XNode> evalNodes(String expression) {
    return evalNodes(document, expression);
  }

  public List<XNode> evalNodes(Object root, String expression) {
    List<XNode> xnodes = new ArrayList<>();
    //获取XML配置文件中的节点和值
    NodeList nodes = (NodeList) evaluate(expression, root, XPathConstants.NODESET);
    for (int i = 0; i < nodes.getLength(); i++) {
      //替换动态值（替换被标记为${}的值，换上真实的值）
      xnodes.add(new XNode(this, nodes.item(i), variables));
    }
    return xnodes;
  }

  public XNode evalNode(String expression) {
    return evalNode(document, expression);
  }

  public XNode evalNode(Object root, String expression) {
    Node node = (Node) evaluate(expression, root, XPathConstants.NODE);
    if (node == null) {
      return null;
    }
    //替换动态值
    return new XNode(this, node, variables);
  }


  /**
   * 获取指定元素或节点的值
   * @param expression
   * @param root
   * @param returnType
   * @return Object
   */
  private Object evaluate(String expression, Object root, QName returnType) {
    try {
      //获得指定元素或节点的值
      return xpath.evaluate(expression, root, returnType);
    } catch (Exception e) {
      throw new BuilderException("Error evaluating XPath.  Cause: " + e, e);
    }
  }

  /**
   * 创建Document对象---将XML实体转换成Document
   * @param inputSource
   * @return Document
   */
  private Document createDocument(InputSource inputSource) {
    // important: this must only be called AFTER common constructor
    //重要：这个必须在 common constructor后调用
    try {
      //1.构建DocumentBuilderFactory对象 以及一些基本的配置
      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
      //是否对XML进行检验 使用的是成员属性的值
      factory.setValidating(validation);
      //工厂的基本配置 无关核心内容
      factory.setNamespaceAware(false);
      factory.setIgnoringComments(true);
      factory.setIgnoringElementContentWhitespace(false);
      factory.setCoalescing(false);
      factory.setExpandEntityReferences(true);

      //2.构建DocumentBuilder对象
      DocumentBuilder builder = factory.newDocumentBuilder();
      // 设置解析器实体 使用成员属性的值
      builder.setEntityResolver(entityResolver);
      //设置异常处理器 发生异常后的处理  无关核心
      builder.setErrorHandler(new ErrorHandler() {
        @Override
        public void error(SAXParseException exception) throws SAXException {
          throw exception;
        }

        @Override
        public void fatalError(SAXParseException exception) throws SAXException {
          throw exception;
        }

        @Override
        public void warning(SAXParseException exception) throws SAXException {
          // NOP
        }
      });
      //3.创建Document对象
      return builder.parse(inputSource);
    } catch (Exception e) {
      throw new BuilderException("Error creating document instance.  Cause: " + e, e);
    }
  }

  /**
   * 通用构造器--16个构造器都需要用到的，抽离出来公用
   * @param validation
   * @param variables
   * @param entityResolver
   */
  private void commonConstructor(boolean validation, Properties variables, EntityResolver entityResolver) {
    this.validation = validation;
    this.entityResolver = entityResolver;
    this.variables = variables;
    //构建Xpath对象
    XPathFactory factory = XPathFactory.newInstance();
    this.xpath = factory.newXPath();
  }

}
