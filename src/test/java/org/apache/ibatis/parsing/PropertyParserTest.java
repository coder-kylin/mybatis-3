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

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Properties;
/**
 * 动态属性解析器测试
 */
class PropertyParserTest {

  @Test
  void replaceToVariableValue() {
    //Properties是java的util类中的对象 设置参数（key-value形式  线程安全）
    Properties props = new Properties();
    //使用使用默认值 传入true
    props.setProperty(PropertyParser.KEY_ENABLE_DEFAULT_VALUE, "true");
    props.setProperty("key", "value");
    props.setProperty("tableName", "members");
    props.setProperty("orderColumn", "member_id");
    props.setProperty("a:b", "c");
    Assertions.assertThat(PropertyParser.parse("${key}", props)).isEqualTo("value");
    System.out.println(PropertyParser.parse("{key}", props)); //结果是{key}  因为不符合动态参数替换${}的规则
    Assertions.assertThat(PropertyParser.parse("${key:aaaa}", props)).isEqualTo("value");  //value
    Assertions.assertThat(PropertyParser.parse("SELECT * FROM ${tableName:users} ORDER BY ${orderColumn:id}", props))
      .isEqualTo("SELECT * FROM members ORDER BY member_id"); //true

    //不使用默认值
    props.setProperty(PropertyParser.KEY_ENABLE_DEFAULT_VALUE, "false");
    //true
    Assertions.assertThat(PropertyParser.parse("${a:b}", props)).isEqualTo("c");

    //移除这个值---则说明使用的是默认的false 返回true
    props.remove(PropertyParser.KEY_ENABLE_DEFAULT_VALUE);
    String flag = PropertyParser.parse("${a:b}", props);
    System.out.println(flag);
    Assertions.assertThat(PropertyParser.parse("${a:b}", props)).isEqualTo("c");

  }

  @Test
  void notReplace() {
    Properties props = new Properties();
    props.setProperty(PropertyParser.KEY_ENABLE_DEFAULT_VALUE, "true");
    System.out.println(PropertyParser.parse("${key}", props)); //${key} props中无值可替代
    Assertions.assertThat(PropertyParser.parse("${key}", props)).isEqualTo("${key}"); //true
    Assertions.assertThat(PropertyParser.parse("${key}", null)).isEqualTo("${key}"); //true

    props.setProperty(PropertyParser.KEY_ENABLE_DEFAULT_VALUE, "false"); //指明不使用默认值
    Assertions.assertThat(PropertyParser.parse("${a:b}", props)).isEqualTo("${a:b}"); //true
    System.out.println(PropertyParser.parse("${a:b}", props));

    props.remove(PropertyParser.KEY_ENABLE_DEFAULT_VALUE);//移除之后 使用的是自带的false
    Assertions.assertThat(PropertyParser.parse("${a:b}", props)).isEqualTo("${a:b}"); //true
    System.out.println(PropertyParser.parse("${a:b}", props));

  }

  @Test
  void applyDefaultValue() {
    Properties props = new Properties();
    props.setProperty(PropertyParser.KEY_ENABLE_DEFAULT_VALUE, "true");
    Assertions.assertThat(PropertyParser.parse("${key:default}", props)).isEqualTo("default"); //true
    Assertions.assertThat(PropertyParser.parse("SELECT * FROM ${tableName:users} ORDER BY ${orderColumn:id}", props))
      .isEqualTo("SELECT * FROM users ORDER BY id"); //true
    Assertions.assertThat(PropertyParser.parse("${key:}", props)).isEmpty(); //true
    Assertions.assertThat(PropertyParser.parse("${key: }", props)).isEqualTo(" "); //true
    Assertions.assertThat(PropertyParser.parse("${key::}", props)).isEqualTo(":");//true
  }

  @Test
  void applyCustomSeparator() {
    Properties props = new Properties();
    props.setProperty(PropertyParser.KEY_ENABLE_DEFAULT_VALUE, "true");
    props.setProperty(PropertyParser.KEY_DEFAULT_VALUE_SEPARATOR, "?:"); //分隔符  ?:
    Assertions.assertThat(PropertyParser.parse("${key?:default}", props)).isEqualTo("default"); //true
    //一个key中只替换第一个
    Assertions.assertThat(PropertyParser.parse(
      "SELECT * FROM ${schema?:prod}.${tableName == null ? 'users' : tableName} ORDER BY ${orderColumn}", props))
      .isEqualTo("SELECT * FROM prod.${tableName == null ? 'users' : tableName} ORDER BY ${orderColumn}");//true
    Assertions.assertThat(PropertyParser.parse("${key?:}", props)).isEmpty(); //true
    Assertions.assertThat(PropertyParser.parse("${key?: }", props)).isEqualTo(" "); //true
    Assertions.assertThat(PropertyParser.parse("${key?::}", props)).isEqualTo(":");//true
  }

}
