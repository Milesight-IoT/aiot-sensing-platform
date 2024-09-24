# AIoT Sensing Platform

## Project Development Description

### Prerequisites

- Frontend Environment：
    - node (v16.x)
    - pnpm
- Java JDK11
- Database (postgresql, Cassandra)
- Maven，3.6.0+ (Optional, some IDEs equipped)
- Docker
- MQTT Client Tool

<br/>

### Project Structure

```
./msaiotsensingplatform/
├── application # Main application module, all functional moduels in a monolithic architecture
│   ├── src/main/conf   # Configuration files
├── dao           # Implementation classes for database query interfaces
├── img           # Logo image
├── msa             # Modules implementing microservice architecture
│   ├── tb          # Docker packaging files
│   ├── tb-node     # Docker implementation for horizontal scaling of ThingBoard nodes
│   ├── transport   # Docker running server for various protocols
├── netty-mqtt      # MQTT client implemented with Netty, referenced by rule-engine module
├── packaging   # Project build resources
├── common      # Common modules
│   ├── actor   # Cusstom actor system
│   ├── dao-api # Database query interfaces
│   ├── data    # Domain models (Java classes corresponding to database tables)
│   ├── message # System messaging mechanism implementation
│   ├── queue   # Message queue
│   ├── stats   # Statistics
│   ├── transport # Server for receiving device messages
│   │   ├── coap  
│   │   ├── http  
│   │   ├── mqtt  
│   │   └── transport-api
│   └── util      # Utilities
├── rest-client      # Java API client which can call the same interfaces as webpages(login, query devices, etc.)
├── rule-engine      # Rule engine
│   ├── rule-engine-api
│   └── rule-engine-components
├── transport        # Independent Java processes for various protocol servers, code references common/transport
└── web              # Webpages implemented with Vue.js
```

<br/>

### Installation-1. Compile the Project

#### Fork the Repository

```
https://github.com/Milesight-IoT/aiot-sensing-platform.git
```

#### Go to Project Directory

```
cd aiot-sensing-platform
```

#### Frontend Compile

##### 1. Install pnpm

```
# Install once only
npm install -g pnpm
```

##### 2. Install dependencies

```
pnpm install
```

##### 3.  Build frontend files

```
pnpm build
```

Check if the files are complied correctly:

```
ls application\src\main\resources\static
```

#### Backend Compile

##### 1. Execute compile file

```
 mvn clean install -DskipTests
```

##### 2. Compile file location

`msaiotsensingplatform.deb`  file is under *application* folder.

If parts of dependencies are not able to fetch during compile execution, please refer to below example to modify the repository configuration in `maven/setting.xml`file:

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

### Installation-2. Install Compile File Locally

#### Prerequisites

##### **Hardware**

- RAM: 1 GB  for AIoT Sensing Platform and PostgreSQL, or 4-8 GB  for AIoT Sensing Platform, PostgreSQL and Cassandra

- Other requirements depends on the database and device amounts

##### **Operating System**

- Ubuntu Kinetic 22.10
- Ubuntu Jammy 22.04 (LTS)
- Ubuntu Focal 20.04 (LTS)
- Ubuntu Bionic 18.04 (LTS)

<br/>

#### Install AIoT Sensing Platform to Ubuntu Server

##### 1. Install JAVA 11 (OpenJDK)

```
sudo apt update
sudo apt install openjdk-11-jdk
```

Check installation status:

```
java -version
```

Result of install success:

```
openjdk version "11.0.xx"
OpenJDK Runtime Environment (...)
OpenJDK 64-Bit Server VM (build ...)
```

##### 2. Install Deb Package

```
sudo dpkg -i msaiotsensingplatform.deb
```

##### 3. Configure the Database

Add configurations of PostgreSQL database and Cassandra database as required.

##### 4. Modify Configuration File

```
# Configure JAVA_OPTS parameter as environment
# Change the path of configuration file (The source file is under application), for example
export LOADER_PATH=${pkg.installFolder}/conf,${pkg.installFolder}/extensions
export SQL_DATA_FOLDER=${pkg.installFolder}/data/sql
```

Example：

```
# Merge into one line if JAVA_OPTS is not able to execute
export JAVA_OPTS="$JAVA_OPTS -Dplatform=@pkg.platform@ -Dinstall.data_dir=@pkg.installFolder@/data"
export JAVA_OPTS="$JAVA_OPTS -Xlog:gc*,heap*,age*,safepoint=debug:file=@pkg.logFolder@/gc.log:time,uptime,level,tags:filecount=10,filesize=10M"
export JAVA_OPTS="$JAVA_OPTS -XX:+IgnoreUnrecognizedVMOptions -XX:+HeapDumpOnOutOfMemoryError"
export JAVA_OPTS="$JAVA_OPTS -XX:-UseBiasedLocking -XX:+UseTLAB -XX:+ResizeTLAB -XX:+PerfDisableSharedMem -XX:+UseCondCardMark"
export JAVA_OPTS="$JAVA_OPTS -XX:+UseG1GC -XX:MaxGCPauseMillis=500 -XX:+UseStringDeduplication -XX:+ParallelRefProcEnabled -XX:MaxTenuringThreshold=10"
# Project runtime variables
export LOG_FILENAME=msaiotsensingplatform.out
export LOADER_PATH=/usr/share/msaiotsensingplatform/conf,/usr/share/msaiotsensingplatform/extensions
export SQL_DATA_FOLDER=/usr/share/msaiotsensingplatform/data/sql

# See configurations on msaiotsensingplatform.yml
# POSTGRESQL configuration
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

##### 5. Run install script

```
sudo /usr/share/msaiotsensingplatform/bin/install/install.sh --loadDemo
```

##### 6. Start the service

Execute the command to start the AIoT Sensing Platform:

```
sudo service msaiotsensingplatform start
```

Open the Web GUI of AIoT Sensing platform:

```
http://localhost:8080/
```

<br/>

#### Install AIoT Sensing Platform to Docker

##### 1. Go to the Directory of File Package

```
cd msa/tb/docker-iot
```

##### 2. Put deb Package

Copy the deb package after complied to directory *msa/tb/docker-iot*.

##### 3. Build Docker Image

```
# Build image
docker build -t msaiotsensingplatform:test . 
# Package the image as tar
docker save msaiotsensingplatform:test -o msaiotsensingplatform.tar
```

##### 4. Push Image

Load docker image:

```
# Install docker image
docker load < ~/msaiotsensingplatform.tar
```

Create docker compose file:

```
#Create docker compose file
nano docker-compose.yml
```

Add below contents to docker-compose.yml file：

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

**Parameter introduction:**

- *8080:9090* - connect local port 5220 to exposed internal HTTP port 9090, and both of them should not be changed, otherwise the platform may not work well.
- *1883:1883* - connect local port 1883 to exposed internal MQTT port 1883. The local port will be used on SC series camera configurations.
- *7070:7070* - connect local port 7070 to exposed internal Edge RPC port 7070.
- *5683-5688:5683-5688/udp* - connect local UDP ports 5683-5688 to exposed internal COAP and LwM2M ports.
- *mysp* - friendly local name of this machine
- *restart：always* - automatically start AIoT Sensing platform in case of system reboot and restart in case of failure.
- *image：msaiotsensingplatform:test* - image name
- */var/mysp-data:/data* - mounts the host’s dir /var/mysp-data to platform DataBase data directory
- */var/mysp-logs:/var/log/msaiotsensingplatform* - mounts the host’s dir /var/mysp-logs to platform logs directory


##### 5. Create User Permissions for New Folders

```
sudo useradd -m msaiotsensingplatform
sudo groupadd msaiotsensingplatform //ignore the exist error
sudo usermod -aG msaiotsensingplatform msaiotsensingplatform
mkdir -p /var/mysp-data && sudo chown -R msaiotsensingplatform:msaiotsensingplatform /var/mysp-data
chmod -R 777 /var/mysp-data
mkdir -p /var/mysp-logs && sudo chown -R msaiotsensingplatform:msaiotsensingplatform /var/mysp-logs
chmod -R 777 /var/mysp-logs
```

##### 6. Run the image

Start the image:

```
docker compose up -d
```

Open the Web GUI of AIoT Sensing platform:

```
http://localhost:8080/
```


## More Use

### Reference

Milesight documentation:
- [Milesight AIoT Sensing Platform](https://resource.milesight.com/milesight/iot/document/aiot-sensing-platform-user-guide.pdf "Sensing Platform")
- [Milesight AIoT Inference Platform](https://resource.milesight.com/milesight/iot/document/aiot-inference-platform-user-guide-en.pdf)

ThingsBoard documentation:

- [thingsboard.io](https://thingsboard.io/docs)

<br/>

### Hot Issues

#### How to Connect Devices to Platform?

Please follow below steps to configure devices to connect to AIoT Sensing Platform:

1. Ensure the device has connected to the network and is able to access to the platform;
2. Select the data report platform as Sensing Platform and configure settings as below:

```
Post Type: MQTT
Host：IP address of AIoT Sensing Platform
MQTT Port：1883
Client ID: Device SN
Username：Device SN
Password：(leave blank)
Topic: v1/devices/me/telemetry
```

<br/>

#### Telemetry Data Content

The devices will report the picture information in json format to platform.

Data Example：

```
{
    ts:1725904500258, //capture timestamp, unit: ms
    data:{
        "image":"data:image/jpeg;base64,/9j/4AAQSkZJRgABAQA...(Image code)",
        // Other properties....
        
    }
}
```

<br/>

#### Rule Engine Introduction

AIoT Sensing Platform supports to select below trigger conditions to send data to third-party recipients via MQTT/HTTP protocols, or show the results on the dashboard.

- Once data received：Once the platform receive the data of sensing objects, it will send the picture data in json format to MQTT/HTTP recipients.
- Low battery：Once the battery level of device is lower than the threshold value, the platform will send the low battery alarm to MQTT/HTTP recipients or show it on the widgets of dashboard.

```
# Alarm report format
{
  "threshold":10 //Battery level
}
```

- Devices become inactive：Once the device becomes inactive, the platform will send the device offline alarm to MQTT/HTTP recipients or show it on the widgets of dashboard.
- Once result recognized: Once the sensing platform receives the recognized results of sensing objects on the pictures from AIoT Inference Platform, it will send the results in json format to MQTT/HTTP recipients.

<br/>

## Contributing

Welcome to contribute to this project following below steps:

1. Fork this repository
2. Create a branch to work on (git checkout -b feature/AmazingFeature)
3. Make and commit your changes (git commit -m 'Add some AmazingFeature')
4. Push your changes to branch (git push origin feature/AmazingFeature)
5. Make a pull request

## Community

Welcome to join the community to get involved in this project to report bugs, share the experiences, make discussions:

- [Discord](https://discord.gg/vNFxbwfErm "Discord")
- [Github](https://github.com/Milesight-IoT "GitHub")

## About Milesight

- [Linkedin](https://www.linkedin.com/company/milesightiot "Linkedin")
- [Youtube](https://www.youtube.com/c/MilesightIoT "Youtube")
- [Facebook](https://www.facebook.com/MilesightIoT "Facebook")
- [Instagram](https://www.instagram.com/milesightiot/ "Instagram")
- [Twitter](https://twitter.com/MilesightIoT "Twitter")
- [Milesight-Evie](https://www.linkedin.com/in/milesight-evie/ "Milesight-Evie")

## License

This project is released under the MIT license. See also [LICENSE](../LICENSE).
