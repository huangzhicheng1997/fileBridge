# 项目构建
建议使用linux进行编译<br>
以centos7为例,安装好maven以及graalvm17<br>
graalvm下载：https://www.graalvm.org/downloads/ <br>
graalvm nativeImage安装教程：https://www.graalvm.org/latest/reference-manual/native-image/ <br>
(有条件的建议将上述环境安装在docker容器下，编译完成后进行docker commit，将编译环境保存下来)

进入fileBridge目录执行如下命令

```shell
mvn install #默认使用jvm模式进行打包,要求jdk17及以上
mvn install -Pnative #使用graalvm打包，要求graalvm jdk17及以上的版本
```

打包完成后，/bin/jar 目录下存放的jar包
，/bin/native下存放的是可直接执行的nativeImage
