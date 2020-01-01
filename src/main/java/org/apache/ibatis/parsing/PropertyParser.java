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

  public static final String KEY_ENABLE_DEFAULT_VALUE = KEY_PREFIX + "enable-default-value";


  public static final String KEY_DEFAULT_VALUE_SEPARATOR = KEY_PREFIX + "default-value-separator";

  /**
   * 是否开启默认值功能（默认不开启）
   */
  private static final String ENABLE_DEFAULT_VALUE = "false";

  /**
   * 默认值的分隔符（默认为“:”）
   */
  private static final String DEFAULT_VALUE_SEPARATOR = ":";

  /**
   * PropertyParser的使用更像静态工厂类；不提供构造方法，而由上一层GenericTokenParser进行调用
   * 不被人调用，声明的原因大概是为了避免默认构造函数被人使用吧
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
    //1.创建VariableTokenHandler对象----真实的动态参数handler对象（静态内部类）
    VariableTokenHandler handler = new VariableTokenHandler(variables);
    //2.创建parser解析对象 ----再上一层次的封装【通用的解析器】 important：这里说明，参数动态替换必须要使用${}
    GenericTokenParser parser = new GenericTokenParser("${", "}", handler);
    //3.解析
    return parser.parse(string);
  }

  /**
   * 动态参数解析器的私有静态内部类——主要用于真实的解析token参数
   * 静态内部类可以访问外部类的静态成员属性
   */
  private static class VariableTokenHandler implements TokenHandler {
    /**
     * 表示一组持久的属性——真实的动态值
     */
    private final Properties variables;

    /**
     * 是否开启默认值 不开启
     */
    private final boolean enableDefaultValue;

    /**
     * 默认分隔符 :
     */
    private final String defaultValueSeparator;

    /**
     * 私有构造器---只能被父类所调用
     */
    private VariableTokenHandler(Properties variables) {
      this.variables = variables;
      //默认 false
      this.enableDefaultValue = Boolean.parseBoolean(getPropertyValue(KEY_ENABLE_DEFAULT_VALUE, ENABLE_DEFAULT_VALUE));
      //默认 :
      this.defaultValueSeparator = getPropertyValue(KEY_DEFAULT_VALUE_SEPARATOR, DEFAULT_VALUE_SEPARATOR);
    }

    /**
     * 获取PropertyValue的值
     * 如果调用者传入的Properties对象为空，则使用的是默认的值
     * 如果不为空，则从Properties中取得
     * @param key
     * @param defaultValue
     * @return String key对应的value值
     */
    private String getPropertyValue(String key, String defaultValue) {
      return (variables == null) ? defaultValue : variables.getProperty(key, defaultValue);
    }


    /**
     * 处理 token
     * 逻辑：
     *  1.判断variables（动态值）是否为空；空则无法替换，直接包装成原来的样子${content}返回；不为空则进入2
     *  2.判断是否启动默认值方式，若不启动，则直接从variables中的key中拿取（如果variables中没有则走1），若启动进入3
     *  3.获取到分隔符（在构造器中确保了如果variables有则取variables的，否则使用默认:）,获取到${key:value}的value部分，
     *  与variables进行比较，如果variables中有则替换，否则直接使用value
     * @param content token字符串
     * @return String 动态替换后的结果
     */
    @Override
    public String handleToken(String content) {
      //判断Properties 是否为空 不为空则将${content}进行替换(从Properties中取动态值)
      if (variables != null) {
        String key = content;
        //是否开启默认值形式  开启则优先使用默认值--使用默认的分隔符
        if (enableDefaultValue) {
          //默认的分割符 : ，也可以是Properties传入动态的分隔符（自己约定） indexOf如果不符合则返回-1
          final int separatorIndex = content.indexOf(defaultValueSeparator);
          String defaultValue = null;
          //获取分割符前面的部分 key:value 也就是key
          if (separatorIndex >= 0) {
            //说明有 key 分隔符 value的默认值
            key = content.substring(0, separatorIndex);
            //默认值就是：xml中编写的 key:value的value  这个做法好处:如果没有动态值，那么就用我自己写好的key:value中的value
            defaultValue = content.substring(separatorIndex + defaultValueSeparator.length());
          }
          //默认值不为空 则进行动态的比较，若存在动态值则使用动态值
          if (defaultValue != null) {
            //从variables中取key的值，如果为空 则返回defaultValue【相当于是多一层保障吧】
            return variables.getProperty(key, defaultValue);
          }
        }
        //不开启默认值形式【默认是不开启的】，直接判断Properties中是否含有该key，直接返回结果----最简单的一种
        if (variables.containsKey(key)) {
          return variables.getProperty(key);
        }
      }

      //1.Properties对象 variables为空，无法进行动态值替换 返回${content}
      //2.或者Properties对象 没有对应的key的时候
      return "${" + content + "}";
    }
  }

}
