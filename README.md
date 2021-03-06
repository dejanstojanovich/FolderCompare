![Logo of the project](./readme/logo.png)

# Folder Compare
![Spring Boot 2.3.2](https://img.shields.io/badge/Spring%20Boot-2.3.2-brightgreen) ![JDK 11](https://img.shields.io/badge/JDK-11-brightgreen) [![GitHub license](https://img.shields.io/badge/license-GPL%203.0-blue)](https://www.gnu.org/licenses/gpl-3.0-standalone.html)


This is the Spring Boog MVC (Thymeleaf) application used to display and compare files and folders on the file system. It can be used to compare different copies of the source code or other textual files especially on the file system without the GUI.
This is the main reason I created it and I also wanted to easily clear all those copied files when changing the computer.

WARNING: SINCE THE APPLICATION EXPOSES THE FILES ON THE FILE SYSTEM USE IT WITH CAUTION AND FOLLOW THE SECURITY GUIDES BELOW IN THE [SECURITY](#security) SECTION.

## Installing / Getting started

Build the application in IDE of your choice and start it as any other Java application from `Terminal` or `Command Prompt`:
```shell
java -jar FolderCompare.jar
```
Above command works if Java executable is on the system path and the command is executed within the parent folder of the application. If necessary specify absolute path for the application.
The application works in the browser, the default port is `2222` and it can be changed in the `application.properties` before building the application.
It can be accessed on the same computer by specifying the port:
```shell
http://localhost:2222/
```
or from another computer by specifying the IP address and same port. 
The port might be blocked on the system by the firewall. In that case, the firewall rule is required to allow traffic through the used port or another open port can be selected.

## Developing

### Built With
All the dependencies are declared in `pom.xml` and will be downloaded by Maven on Build process.
The [`NetBeans Modules Diff`](https://mvnrepository.com/artifact/org.netbeans.api/org-netbeans-modules-diff) library is used to determine the differences when comparing file contents. It provides the same experience as "Diff To..." feature within the NetBeans.

### Prerequisites
No specific prerequisites, only JDK is required. The code is built with OpenJDK 11.

### Building
I used NetBeans IDE for application build, but any IDE will work just fine.  It is important to update the `application.properties` file and set approptiate parameters prior to making the executable. Please see the [Configuration](##-configuration) section.

## Configuration

The main parameters are specified in the `application.properties` file prior to the build. They are commented for easier understanding. Security parameters are described in separate section [Security](#security).
```shell
#target port for the application
server.port=2222

#define the list of extensions as CSV without the dot (.), comment out to allow all extensions
diff.extensions=txt,java,css,html,php,log,js

#define maximum size of the file to be displayed and compared, 3MB is default
#acceptable units are B, KB, MB, GB, TB
diff.allowedDiffSize=3MB

#define the time zone for formatting the last modified date of the file
diff.timezone=Europe/Belgrade
```
Besides application port, there is an option to specify the file extensions for the files whose contents can be displayed and compared. This serves as a security feature and also there is no reason to try to display binary file contents. Commenting out this parameter will allow any file extension.
Since the file contents are displayed in the browser, there is a top size limit which can be set also.

## Security

As mentioned above, the application lists files and folders on the computer where the application is running. Those files should not be seen by any unauthorised user. So use it with care and pay special attention to this section.
If `diff.authenticate` parameter is true, then basic authentication is in place to prevent unauthorised access. You also need to specify the user and password.
Otherwise, IP address filter is in place which allows access only from the list of comma separated IP addresses (localhost by default).

```shell
#set to true to ask for the authentication or false to use IP address filter
diff.authenticate=false

#set the user/password credentials for application login
#this applies if the diff.authenticate=true. IP address filter is not active here
diff.user=user
diff.pass=pass

#define the list of IP addresses as CSV, which are permitted to access the application
#this applies if the diff.authenticate=false and no login is required
diff.allowedClients=127.0.0.1,192.168.130.60
```

# Usage
Please see Wiki pages.
# Licensing

This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or  (at your option) any later version.
You should have received a copy of the GNU General Public License along with this program.  If not, see <http://www.gnu.org/licenses/>.
