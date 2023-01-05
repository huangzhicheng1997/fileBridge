package com.github.fileBridge.common;

import java.util.Map;

/**
 * @author ZhiCheng
 * @date 2022/10/31 16:03
 */
public record Event(String absPath,String content,Map<String, String> mapping,String output,long offset,String id) {

}
