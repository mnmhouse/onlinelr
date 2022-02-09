# onlinelr

Flink是采用java语言进行开发的，目前调研Flink除了有基于JPython的python语言支持外，没有针对其它语言的开发，除了在Flink中有些公司为了性能等会使用 C++ 和 LLVM 实现的高性能 Flink Native 执行引擎等方式进行优化，通过java调用底层c++之类的实现，这里主要是参考Alink进行了实现
# Alink介绍

Alink 是阿里巴巴基于实时计算引擎 Flink 研发的新一代机器学习算法平台，
是业界首个同时支持批式算法、流式算法的机器学习平台，Alink 中提供了在线学习算法FTRL在Alink中的实现，这里主要就是去实现FTRL

# 打包jar包
拷贝工程到本地 ，执行mvn install打包安装包，也可以直接使用target目录中的FlinkOnlineProject-1.0-SNAPSHOT.jar包

# 部署方式

* 本地安装启动flink集群
wget https://archive.apache.org/dist/flink/flink-1.13.0/flink-1.13.0-bin-scala_2.11.tgz
tar -xf flink-1.13.0-bin-scala_2.11.tgz && cd flink-1.13.0
./bin/start-cluster.sh

* 提交基于flink的任务 

./bin/flink run -p 1 -c org.example.FTRLExample  FlinkOnlineProject-1.0-SNAPSHOT.jar  


其中 FlinkOnlineProject-1.0-SNAPSHOT.jar 位于target中编译好的jar包


# 查看任务运行 ：默认flink 集群web ui启动在8081端口
http://localhost:8081/#/job/running 
