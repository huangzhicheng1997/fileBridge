# 一、简介

---

一款用java编写的日志采集组件，具有以下的特点：

1. 集成了graalvm的静态编译能力，aot后的可执行文件将拥有更快的启动速度以及更低的内存cpu占用。
2. 采用grpc通信协议，跨语言平台，主流编程语言编写的server都可以作为fileBride的消费端
3. 支持lua脚本对日志进行处理。

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
