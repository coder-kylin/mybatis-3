/**
 *    Copyright 2009-2016 the original author or authors.
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

import java.util.Properties;

/**
 * 动态属性解析器
 * 定义动态属性解析的规则
 * @author Clinton Begin
 * @author Kazuki Shimizu
 */
public class PropertyParser {

  private static final String KEY_PREFIX = "org.apache.ibatis.parsing.PropertyParser.";
  /**
   * The special property key that indicate whether enable a default value on placeholder.
   * <p>
   *   The default value is {@code false} (indicate disable a default value on placeholder)
   *   If you specify the {@code true}, you can specify key and default value on placeholder (e.g. {@code ${db.username:postgres}}).
   * </p>
   * @since 3.4.2
   */
  public static final String KEY_ENABLE_DEFAULT_VALUE = KEY_PREFIX + "enable-default-value";

  /**
   * The special property key that specify a separator for key and default value on placeholder.
   * <p>
   *   The default separator is {@code ":"}.
   * </p>
   * @since 3.4.2
   */
  public static final String KEY_DEFAULT_VALUE_SEPARATOR = KEY_PREFIX + "default-value-separator";

  private static final String ENABLE_DEFAULT_VALUE = "false";
  private static final String DEFAULT_VALUE_SEPARATOR = ":";

  /**
   * 静态工厂类 不提供构造方法
   */
  private PropertyParser() {
    // Prevent Instantiation
  }

  /**
   * 基于variables替换动态值（如果result为动态值）
   * 这就是mybatis如何替换XML中动态值的实现方式
   * 将variables替换从xml获取的死板的值
   * @param string
   * @param variables
   * @return String
   */
  public static String parse(String string, Properties variables) {
    //1.创建VariableTokenHandler对象【todo 存在的意义】
    VariableTokenHandler handler = new VariableTokenHandler(variables);
    //2.创建parser解析对象
    GenericTokenParser parser = new GenericTokenParser("${", "}", handler);
    //3.解析
    return parser.parse(string);
  }
  /**
   * 测试结果： res = kylin
   * 很明显 按照规则---将动态值kylin替换${zhuoqilin}（从XML获取的固定值）
   */
  public static void main(String []args){
    Properties variables = new Properties();
    variables.setProperty("username", "kylin");
    String res = parse("${zhuoqilin}", variables);
    System.out.println(res);
  }


  /**
   * 私有静态内部类
   */
  private static class VariableTokenHandler implements TokenHandler {
    private final Properties variables;
    private final boolean enableDefaultValue;
    private final String defaultValueSeparator;

    private VariableTokenHandler(Properties variables) {
      this.variables = variables;
      this.enableDefaultValue = Boolean.parseBoolean(getPropertyValue(KEY_ENABLE_DEFAULT_VALUE, ENABLE_DEFAULT_VALUE));
      this.defaultValueSeparator = getPropertyValue(KEY_DEFAULT_VALUE_SEPARATOR, DEFAULT_VALUE_SEPARATOR);
    }

    private String getPropertyValue(String key, String defaultValue) {
      return (variables == null) ? defaultValue : variables.getProperty(key, defaultValue);
    }


    /**
     * todo 这个方法是用来干嘛的？
     * @param
     * @return
     */
    @Override
    public String handleToken(String content) {
      if (variables != null) {
        String key = content;
        if (enableDefaultValue) {
          final int separatorIndex = content.indexOf(defaultValueSeparator);
          String defaultValue = null;
          if (separatorIndex >= 0) {
            key = content.substring(0, separatorIndex);
            defaultValue = content.substring(separatorIndex + defaultValueSeparator.length());
          }
          if (defaultValue != null) {
            return variables.getProperty(key, defaultValue);
          }
        }
        if (variables.containsKey(key)) {
          return variables.getProperty(key);
        }
      }
      return "${" + content + "}";
    }
  }

}
