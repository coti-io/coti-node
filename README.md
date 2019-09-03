Coti Node
=============

[![GitHub Stars](https://img.shields.io/github/stars/coti-io/coti-node.svg)](https://github.com/coti-io/coti-node/stargazers) [![GitHub Issues](https://img.shields.io/github/issues/coti-io/coti-node.svg)](https://github.com/coti-io/coti-node/issues) [![Current Version](https://img.shields.io/badge/version-1.0.2SNAPSHOT-yellow.svg)](https://github.com/coti-io/coti-node/)

---
## About

All Coti Nodes

---
## Table of Contents

- [Requirements](#requirements)
- [Build](#build-process)
- [Support](#support)
- [License](#License)
---
## Requirements
* java jdk version: ^1.8
* mvn version: ^3.5.3
---
## Build
```
mvn initialize && mvn clean compile && mvn package -DskipTests
```
### run localy

```
java -jar fullnode/target/fullnode-1.0.2-SNAPSHOT.jar --spring.config.additional-location=fullnode1.properties
```
### using docker
There is a dockerfile and docker compose in this folder.
to build:
```
docker-compose build
```

to start:

```
docker-compose up -d
```
---
## Support

Don't hesitate to reach out to us at one of the following places:

- Email: <a href="https://coti.io/" target="_blank">`contact@coti.io`</a>
- Website: <a href="https://coti.io/" target="_blank">`https://coti.io/`</a>
- Twitter: <a href="https://twitter.com/COTInetwork" target="_blank">`@COTInetwork`</a>
- Medium: <a href="https://medium.com/cotinetwork" target="_blank">`https://medium.com/cotinetwork`</a>
- Reddit: <a href="https://www.reddit.com/r/cotinetwork/" target="_blank">`https://www.reddit.com/r/cotinetwork/`</a>
- Telegram: <a href="https://t.me/COTInetwork" target="_blank">`COTInetwork`</a>

---
## License
[![License: GPL v3](https://img.shields.io/badge/License-GPLv3-blue.svg)](https://www.gnu.org/licenses/gpl-3.0)
This project is licensed under the terms of the **GNU General Public License v3.0** license.