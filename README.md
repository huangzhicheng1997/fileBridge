# 一、简介

---

一款用java编写的日志采集组件，具有以下的特点：

1. 集成了graalvm的静态编译能力，可脱离jvm运行，aot后的可执行文件将拥有更快的启动速度以及更低的内存cpu占用。
2. 采用grpc通信协议，跨语言平台，主流编程语言编写的server都可以作为fileBride的消费端
3. 支持lua脚本对日志进行处理，例如文本转换、脱敏、解析日志生成kv结构的结构化数据。
4. 支持offset提交从而支持断点续传
5. 支持基于"水位线"的控流，不会因为突发的大量日志采集导致内存暴涨 oom。


# 二、运行

默认的运行环境是centos7，其他环境下可能会缺库或者版本不适配，例如libc，所以需要在对应的环境下进行编译<br>
切换到"/bin/native"文件夹下，直接执行可执行文件，建议按需用-Xmx选项限制内存。


```shell
cd ${dir}/fileBridge/bin/native
./fileBridge -Xmx20m &
```


# 三、文档

文档在 /doc目录下

```text
config_guide.md - 配置说明
build_guide.md  - 编译说明
design.md       - 设计文档
lua_guide.md    - lua脚本使用说明
protocol.md     - 通信协议说明
```
# 四、注意点
1. 需要业务应用合理的配置日志滚动策略，不要间隔太少的时间就滚动新的日志文件，同时一条日志中必须包含时间字段
2. 不同应用的日志应该在各自应用的存储日志文件的目录下，不要混在一起，否则可能导致offset文件冲突

# 五、部署方式
建议采用sidecar模式进行部署，一个后端应用对应一个sidecar，内存上激进的话限制到20m，保守的话限制到100m是没有问题。