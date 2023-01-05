#一、简介
一款用java编写的日志采集组件，具有以下的特点：
1. 集成了graalvm的静态编译能力，aot后的可执行文件，系统资源占用低。
2. 采用grpc通信协议，跨语言平台，主流语言编写的server都可以作为fileBride的消费端
3. 支持lua脚本对日志进行处理，例如脱敏。

#二、配置介绍

```yaml
output:
  app1: #对应的outputName
    dir: /app1/logs
    pattern: access[0-9]{1}.log
    logPattern: '^([0-9]{4}-[0-9]{2}-[0-9]{2} [0-9]{2}:[0-9]{2}:[0-9]{2})([\s\S]*)'
    readStrategy: latest  #latest  offset  fromHead
    addresses: 192.168.2.229:9991
    invalidateTime: 36000 #seconds
    scriptName: xx.lua
    waterMark: 512-1024
    transTimeout: 500  #milliseconds
  app2:
    dir: /app2/logs
    pattern: access[0-9]{1}.log
    logPattern: '^([0-9]{4}-[0-9]{2}-[0-9]{2} [0-9]{2}:[0-9]{2}:[0-9]{2})([\s\S]*)'
    readStrategy: offset  #latest  offset  fromHead
    addresses: 192.168.2.229:9991
    invalidateTime: 36000 #seconds
    scriptName: xx.lua
    waterMark: 512-1024
    transTimeout: 500  #milliseconds
```
<p>
按照配置文件中的结构可知，output选项下可以配置多个output，例如"app1"、"app2"，对应被采集的应用。
</p>

1. dir
```text
当前应用的日志目录
```
2. pattern
```text
用于匹配日志文件的正则表达式
```
3. logPattern
```text
用来确认一行文本是否是一个标准日志的正则表达式，主要用来做多行日志合并，
比如java应用的异常堆栈，并不是每一行数据都是一个日志，而是所有的堆栈信息合并后才是一个完整的日志。
以rocketmq的日志为例：
2020-11-08 21:21:07 INFO main - namesrvAddr=
则如下的正则返回的结果就是true
^([0-9]{4}-[0-9]{2}-[0-9]{2} [0-9]{2}:[0-9]{2}:[0-9]{2})([\s\S]*)
如果是一个异常堆栈
2020-12-20 16:19:14 ERROR NettyServerNIOSelector_3_2 - processRequestWrapper response to /127.0.0.1:65066 failed
java.nio.channels.ClosedChannelException: null
	at io.netty.channel.AbstractChannel$AbstractUnsafe.write(...)(Unknown Source) ~[netty-all-4.0.42.Final.jar:4.0.42.Final]
通过正则可知第二行数据和第三行数据，不是一个标准日志是需要合并到第一行的

```
4. readStrategy
```text
fromHead 表示每次都从文件最开始进行读取
offset 表示每次输出日志时会提交offset，那么即使重启后也会根据offset继续向下读取
latest 表示每次都从文件末尾进行读取，忽略之前的所有日志，同时此配置项也是默认值
```
5. addresses
```text
表示输出的地址
集群可以用","隔开
```
6. invalidateTime
```text
表示一个文件监控的失效时间（单位秒）。
例如配置了3600秒，表示如果文件过了一个小时后还没有修改过则停止监控此文件，
同时启动的过程中也会通过 "pattern"和"invalidateTime"配置项一起判断文件是否应该被采集
```

7. scriptName
```text
配置lua脚本的文件名
```
8. waterMark
```text
日志堆积的水位线配置，
例如512-1024，则低水位线为512条日志，高水位线为1024条日志
在低水位线以下时正常采集日志，在高低水位线之间时，放缓日志采集速率，当超过高水位线是则是完全暂停日志的采集
```
9. transTimeout
```text
传输的超时时间，毫秒
```


#三、lua执行器
