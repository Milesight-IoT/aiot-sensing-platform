# AIoT感知平台

## 项目研发说明

### 先决条件

#### 环境

- 前端环境：
    - node (v16.x)
    - pnpm
- Java jdk11
- 数据库（postgresql、Cassandra）
- Maven，3.6.0+，不强制需要，某些IDE也自带
- Docker容器引擎
- Mqtt客户端

<br/>

### 项目结构

```
./msaiotsensingplatform/
├── application # 项目主程序模块，单体架构时包含所有功能模块于一体
│   ├── src/main/conf   # 配置文件信息
├── dao           # 数据库查询接口的实现类
├── img           # 放logo的
├── msa             # 实现微服务架构的模块
│   ├── tb          # 用docker打包文件
│   ├── tb-node     # 用docker实现横向扩展ThingBoard节点
│   ├── transport   # 用docker跑多种协议的服务端
├── netty-mqtt      # netty实现的mqtt客户端，被rule-engine模块引用
├── packaging   # 项目构建资源
├── common      # 公共模块
│   ├── actor   # 自己实现的actor系统
│   ├── dao-api # 数据库查询接口
│   ├── data    # 域模型（数据库表对应的Java类）
│   ├── message # 实现系统的消息机制
│   ├── queue   # 消息队列
│   ├── stats   # 统计
│   ├── transport # 接收设备消息的服务端
│   │   ├── coap  
│   │   ├── http  
│   │   ├── mqtt  
│   │   └── transport-api
│   └── util      # 工具
├── rest-client      # Java版的api客户端，可以调用页面上同样的接口（登录、查询设备...）
├── rule-engine      # 规则引擎
│   ├── rule-engine-api
│   └── rule-engine-components
├── transport        # 多种协议的服务端做成独立的Java进程，实现代码都是引用common/transport
└── web              # Vue.js 实现的前端页面
```

<br/>

### 项目安装-1. 编译项目

#### 克隆仓库

```

```

#### 进入项目目录

```
cd aiot-sensing-platform
```

#### 前端编译

##### 1.安装pnpm

```
# 安装一次即可
npm install -g pnpm
```

##### 2.安装依赖项

```
pnpm install
```

##### 3.构建前端文件

```
pnpm build
```

查看文件是否正确编译

```
ls application\src\main\resources\static
```

#### 后端编译

##### 1.执行编译命令

```
 mvn clean install -DskipTests
```

##### 2.编译包所在位置：

`msaiotsensingplatform.deb`文件包在application文件夹下。

如果执行编译时部分依赖无法拉取，可参考如下示例修改`maven/setting.xml`文件：

```
  <profiles>
    <profile>
        <id>nexus</id>
        <!--Enable snapshots for the built in central repo to direct -->
        <!--all requests to nexus via the mirror -->
        <repositories>
            <repository>
                <id>central1</id>
                <url>http://120.25.59.85:8081/nexus/content/groups/public</url>
                <releases>
                    <enabled>true</enabled>
                </releases>
                <snapshots>
                    <enabled>true</enabled>
                </snapshots>
            </repository>
            <repository>
                <id>central</id>
                <url>http://maven.aliyun.com/nexus/content/groups/public/</url>
                <name>aliyun</name>
            </repository>
        </repositories>

        <pluginRepositories>
            <pluginRepository>
                <id>central</id>
                <url>http://maven.aliyun.com/nexus/content/groups/public/</url> 
                <releases>
                    <enabled>true</enabled>
                </releases>
                <snapshots>
                    <enabled>true</enabled>
                </snapshots>
            </pluginRepository>
        </pluginRepositories>
    </profile> 

    <profile>
       <id>sonar</id>
       <activation>
          <activeByDefault>true</activeByDefault>
       </activation>
       <properties>
          <sonar.jdbc.url>jdbc:postgresql://120.25.59.85:5433/sonar</sonar.jdbc.url>
          <sonar.jdbc.driver>org.postgresql.Driver</sonar.jdbc.driver>
          <sonar.jdbc.username>postgres</sonar.jdbc.username>
          <sonar.jdbc.password>postgres</sonar.jdbc.password>
          <!-- SERVER ON A REMOTE HOST -->
          <sonar.host.url>http://120.76.241.24:9990</sonar.host.url>
          <sonar.scm.disabled>true</sonar.scm.disabled>
       </properties>
    </profile>
  </profiles> 

  <activeProfiles>
    <!--make the profile active all the time -->
    <activeProfile>nexus</activeProfile>
  </activeProfiles> 

```

### 项目安装-2.本地安装编译文件

#### 先决条件

##### 硬件要求

- RAM: 1 GB用于运行感知平台和PostgreSQL，或者4-8GB用于运行感知平台、PostgreSQL和Cassandra
- 其它要求取决于选择的数据库和连接到系统的设备数量

##### 操作系统要求

- Ubuntu Kinetic 22.10
- Ubuntu Jammy 22.04 (LTS)
- Ubuntu Focal 20.04 (LTS)
- Ubuntu Bionic 18.04 (LTS)

<br/>

#### Ubuntu安装

##### 1.安装JAVA 11 (OpenJDK)

```
sudo apt update
sudo apt install openjdk-11-jdk
```

可以使用以下命令检查安装:

```
java -version
```

命令输出结果：

```
openjdk version "11.0.xx"
OpenJDK Runtime Environment (...)
OpenJDK 64-Bit Server VM (build ...)
```

##### 2.安装服务

```
sudo dpkg -i msaiotsensingplatform.deb
```

##### 3.配置数据库

> 根据数据库配置按需添加PostgreSQL数据库、Cassandra数据库配置

##### 4.修改配置文件

```
# JAVA_OPTS JVM 参数根据环境配置
# 修改对应配置文件位置（源文件在application下）例如
export LOADER_PATH=${pkg.installFolder}/conf,${pkg.installFolder}/extensions
export SQL_DATA_FOLDER=${pkg.installFolder}/data/sql
```

示例：

```
# 如果无法执行JAVA_OPTS请整合为一行
export JAVA_OPTS="$JAVA_OPTS -Dplatform=@pkg.platform@ -Dinstall.data_dir=@pkg.installFolder@/data"
export JAVA_OPTS="$JAVA_OPTS -Xlog:gc*,heap*,age*,safepoint=debug:file=@pkg.logFolder@/gc.log:time,uptime,level,tags:filecount=10,filesize=10M"
export JAVA_OPTS="$JAVA_OPTS -XX:+IgnoreUnrecognizedVMOptions -XX:+HeapDumpOnOutOfMemoryError"
export JAVA_OPTS="$JAVA_OPTS -XX:-UseBiasedLocking -XX:+UseTLAB -XX:+ResizeTLAB -XX:+PerfDisableSharedMem -XX:+UseCondCardMark"
export JAVA_OPTS="$JAVA_OPTS -XX:+UseG1GC -XX:MaxGCPauseMillis=500 -XX:+UseStringDeduplication -XX:+ParallelRefProcEnabled -XX:MaxTenuringThreshold=10"
# 项目运行变量
export LOG_FILENAME=msaiotsensingplatform.out
export LOADER_PATH=/usr/share/msaiotsensingplatform/conf,/usr/share/msaiotsensingplatform/extensions
export SQL_DATA_FOLDER=/usr/share/msaiotsensingplatform/data/sql

# 具体配置查看msaiotsensingplatform.yml配置文件
# POSTGRESQL配置
export SPRING_DRIVER_CLASS_NAME=org.postgresql.Driver
export SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/msaiotsensingplatform
export SPRING_DATASOURCE_USERNAME=msaiotsensingplatform
export SPRING_DATASOURCE_PASSWORD=password
# CASSANDRA
export CASSANDRA_KEYSPACE_NAME=msaiotsensingplatform
export CASSANDRA_HOME=/opt/cassandra
export CASSANDRA_URL=localhost:9042 
export CASSANDRA_USERNAME=
export CASSANDRA_PASSWORD=
```

##### 5.运行安装脚本

```
sudo /usr/share/msaiotsensingplatform/bin/install/install.sh --loadDemo
```

##### 6.启动服务

执行以下命令以启动感知平台：

```
sudo service msaiotsensingplatform start
```

启动后使用以下链接打开平台网页：

```
http://localhost:8080/
```

<br/>

#### Docker安装

##### 1. 进入打包文件位置

```
cd msa/tb/docker-iot
```

##### 2. 复制deb包到当前位置

将编译好的deb包复制到msa/tb/docker-iot下

##### 3. 构建Docker镜像

```
# 构建镜像
docker build -t msaiotsensingplatform:test . 
# 镜像打包为tar
docker save msaiotsensingplatform:test -o msaiotsensingplatform.tar
```

##### 4. docker准备

加载镜像：

```
# 安装docker image
docker load < ~/msaiotsensingplatform.tar
```

创建docker compose文件：

```
#创建docker执行文件
nano docker-compose.yml
```

将下列文本内容加入到docker-compose.yml文件中：

```
version: '3.0'
services:
  mysp:
    restart: always
    image: "msaiotsensingplatform:test"
    ports:
      - "8080:9090"
      - "1883:1883"
      - "7070:7070"
      - "5683-5688:5683-5688/udp"
    environment:
      TB_QUEUE_TYPE: in-memory 
    volumes:
      - /var/mysp-data:/data
      - /var/mysp-logs:/var/log/msaiotsensingplatform
```

**命令参数介绍**

- 8080:9090 - 将本地端口8080连接到公开的内部HTTP端口9090（请勿改变此配置，否则导致部分功能无法使用）
- 1883:1883 - 将本地端口1883连接到公开的内部MQTT端口1883
- 7070:7070 - 将本地端口7070连接到公开的内部RPC端口7070
- 5683-5688:5683-5688/udp - 将本地UDP端口5683-5688连接到公开的COAP和LwM2M端口
- mysp - 将系统进行命名
- restart：always - 在Ubuntu重启时自启动，并会在发生故障时重新启动
- image：msaiotsensingplatform:test - 镜像名
- /var/mysp-data:/data - 将平台的数据目录挂载到本地数据目录/var/mysp-data
- /var/mysp-logs:/var/log/msaiotsensingplatform - 将平台的目录挂载到本地日志目录/var/mysp-logs


##### 5. 为新建的文件夹创建用户权限

```
sudo useradd -m msaiotsensingplatform
sudo groupadd msaiotsensingplatform（提示已存在忽略）
sudo usermod -aG msaiotsensingplatform msaiotsensingplatform
mkdir -p /var/mysp-data && sudo chown -R msaiotsensingplatform:msaiotsensingplatform /var/mysp-data
chmod -R 777 /var/mysp-data
mkdir -p /var/mysp-logs && sudo chown -R msaiotsensingplatform:msaiotsensingplatform /var/mysp-logs
chmod -R 777 /var/mysp-logs
```

##### 6. 在docker配置文件对应目录下运行镜像

启动镜像：

```
docker compose up -d
```

启动后使用以下链接打开平台网页：

```
http://localhost:8080/
```


## 进一步使用

### 参考文档

星纵智能平台文档:
- [Milesight AIoT Sensing Platform](https://resource.milesight.com/milesight/iot/document/aiot-sensing-platform-user-guide.pdf "Sensing Platform")
- [Milesight AIoT Inference Platform](https://resource.milesight.com/milesight/iot/document/aiot-inference-platform-user-guide-en.pdf)

ThingsBoard文档:
- [thingsboard.io](https://thingsboard.io/docs)

<br/>

### 可能遇到的问题

#### 如何连接设备到平台？

请参照以下步骤配置设备连接平台：

1. 确保设备联网且可达感知平台服务器；
2. 选择数据上报平台为Sensing Platform并配置如下参数：

```
协议：MQTT
主机：已搭建感知平台IP地址
默认MQTT端口：1883
客户端ID: 设备SN
用户名：设备SN
密码：（为空不填）
主题：v1/devices/me/telemetry
```

<br/>

#### 遥测数据内容说明

设备支持以JSON格式向感知平台推送图片数据。

数据示例：

```
{
    ts:1725904500258, //时间戳，单位：ms
    data:{
        "image":"data:image/jpeg;base64,/9j/4AAQSkZJRgABAQA...(Image code)",
        // 其他属性...
        
    }
}
```

<br/>

#### 规则引擎说明

感知平台支持配置如下规则引擎：

- 一旦收到数据：一旦所选感知对象的通道接收到数据，则发送JSON格式的图片数据到配置的MQTT/HTTP接收方
- 低电量：一旦所选设备推送的电量信息低于所配置的阈值，则推送低电量告警给接收方或展示在仪表盘组件上

```
# 低电量告警上报内容
{
  "threshold":10 //电量
}
```

- 设备不活跃：一旦所选设备从活跃状态变为不活跃，则推送低电量告警给接收方或展示在仪表盘组件上
- 一旦识别到结果：一旦感知平台从推理平台获取到所选感知对象的通道的识别结果，则发送JSON格式的识别结果到配置的MQTT/HTTP接收方

<br/>

## 贡献指南

欢迎任何形式的贡献！请遵循以下步骤提交您的贡献：

1. Fork 本仓库
2. 创建您的特性分支 (git checkout -b feature/AmazingFeature)
3. 提交您的更改 (git commit -m 'Add some AmazingFeature')
4. 推送到分支 (git push origin feature/AmazingFeature)
5. 打开一个 Pull Request

## 社区

加入我们的社区，获取帮助、分享经验、讨论项目相关内容：

- [Discord](https://discord.gg/vNFxbwfErm "Discord")
- [Github](https://github.com/Milesight-IoT "GitHub")

## 关注Milesight

- [Linkedin](https://www.linkedin.com/company/milesightiot "Linkedin")
- [Youtube](https://www.youtube.com/c/MilesightIoT "Youtube")
- [Facebook](https://www.facebook.com/MilesightIoT "Facebook")
- [Instagram](https://www.instagram.com/milesightiot/ "Instagram")
- [Twitter](https://twitter.com/MilesightIoT "Twitter")
- [Milesight-Evie](https://www.linkedin.com/in/milesight-evie/ "Milesight-Evie")

## 许可证

此存储库在 [MIT](../LICENSE)下可用。
