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


/**
 * 通用的 Token 解析器 备注：这里的token不是代表登录时候获取的token，似乎是${}这种东西
 * @author Clinton Begin
 */
public class GenericTokenParser {


  /**
   * 前缀
   */
  private final String openToken;

  /**
   * 后缀
   */
  private final String closeToken;

  /**
   * Token的真实解析器（因为这个类【通用解析器】是更高一层次的封装，所以成员变量里面带真实的解析处理器）
   */
  private final TokenHandler handler;


  /**
   * 构造器
   */
  public GenericTokenParser(String openToken, String closeToken, TokenHandler handler) {
    this.openToken = openToken;
    this.closeToken = closeToken;
    this.handler = handler;
  }

  /**
   * 解析 text 获取需要的内容
   * 方法的用途；
   *  更抽象一层次的封装，比如参数非空判断，前缀后缀是否符合规范等， 内部调用了各个真实解析器的解析方法
   * @param text
   * @return String 解析后的结果
   */
  public String parse(String text) {
    //0.非空判断
    if (text == null || text.isEmpty()) {
      return "";
    }
    //1.判断是否以符合条件的前缀开始，如果不是 则直接返回文本--不解析
    int start = text.indexOf(openToken);
    if (start == -1) {
      return text;
    }
    //2.真实解析开始 转换成char数组 一个一个字符看
    char[] src = text.toCharArray();
    int offset = 0; //？作何使用
    final StringBuilder builder = new StringBuilder();
    StringBuilder expression = null;
    while (start > -1) {
      if (start > 0 && src[start - 1] == '\\') {
        // this open token is escaped. remove the backslash and continue.
        builder.append(src, offset, start - offset - 1).append(openToken);
        offset = start + openToken.length();
      } else {
        // found open token. let's search close token.
        if (expression == null) {
          expression = new StringBuilder();
        } else {
          expression.setLength(0);
        }
        builder.append(src, offset, start - offset);
        offset = start + openToken.length();
        int end = text.indexOf(closeToken, offset);
        while (end > -1) {
          if (end > offset && src[end - 1] == '\\') {
            // this close token is escaped. remove the backslash and continue.
            expression.append(src, offset, end - offset - 1).append(closeToken);
            offset = end + closeToken.length();
            end = text.indexOf(closeToken, offset);
          } else {
            expression.append(src, offset, end - offset);
            break;
          }
        }
        if (end == -1) {
          // close token was not found.
          builder.append(src, start, src.length - start);
          offset = src.length;
        } else {
          builder.append(handler.handleToken(expression.toString()));
          offset = end + closeToken.length();
        }
      }
      start = text.indexOf(openToken, offset);
    }
    if (offset < src.length) {
      builder.append(src, offset, src.length - offset);
    }
    return builder.toString();
  }
}
