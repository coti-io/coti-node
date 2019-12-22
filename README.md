
<p align="center"><img src="/basenode/resources/logo-slogan-300x200.jpg"></p>

COTI Node
=============

[![GitHub Stars](https://img.shields.io/github/stars/coti-io/coti-node.svg)](https://github.com/coti-io/coti-node/stargazers)
[![GitHub Issues](https://img.shields.io/github/issues/coti-io/coti-node.svg)](https://github.com/coti-io/coti-node/issues)
[![Current Version](https://img.shields.io/badge/version-1.0.2.SNAPSHOT-yellow.svg)](https://github.com/coti-io/coti-node/)
[![Discord Server](https://img.shields.io/discord/386571547508473876.svg)](https://discord.me/coti)

---
## What is COTI?


[COTI](https://coti.io/) is a fully encompassing “finance on the blockchain” ecosystem that is designed specifically to
meet the challenges of traditional finance (fees, latency, global inclusion and risk) by introducing a new type of DAG
 based base protocol and infrastructure that is scalable, fast, private, inclusive, low cost and is optimized for real 
time payments.  
  
The ecosystem includes a  
- [DAG based Blockchain](https://www.youtube.com/watch?v=kSdRxqHDKe8) 
- [Proof of Trust Consensus Algorithm](https://coti.io/files/COTI-technical-whitepaper.pdf)
- [multiDAG](https://medium.com/cotinetwork/introducing-the-coti-multidag-b353793cf582)
- [Global Trust System](https://medium.com/cotinetwork/introducing-cotis-global-trust-system-gts-an-advanced-layer-of-trust-for-any-blockchain-7e44587b8bda)
- [Universal Payment Solution](https://medium.com/cotinetwork/coti-universal-payment-system-ups-8614e149ee76)
- [Payment Gateway](https://medium.com/cotinetwork/announcing-the-first-release-of-the-coti-payment-gateway-4a9f3e515b86)
- [consumer COTI Pay applications](https://coti.io/coti-pay)
- [merchant COTI Pay Business](https://gateway.coti.io/dashboard)  

Find out more about COTI in our [Medium](https://medium.com/cotinetwork).

## About this repository
```coti node``` is the development repository for COTI's DAG-based distributed ledger. It is comprised of a basenode 
(which provides the base functionality for all COTI nodes) and all other COTI nodes.
  
  :star: Star this repository to show your support!


---
## Table of Contents

- [Requirements](#requirements)
- [Build](#Build)
- [Support](#support)
- [License](#License)
---
## Requirements

#### Software requirements:

* java jdk version: ^1.8
* mvn version: ^3.5.3

#### Node requirements:

* properties file for each node in the project root directory (Differs for each node type. A skeleton will be added later to the README of each node repository)
* clusterstamp file for each node in the project root directory (Differs for each node type. Will be downloaded automatically in future releases)
* lift the nodes in the following order:  
      nodemanager :arrow_right: zerospend :arrow_right: trustscore :arrow_right: financial :arrow_right: storage :arrow_right: history :arrow_right: dsp :arrow_right: fullnode 

---
## Build & Run

#### Locally
build:
```
mvn initialize && mvn clean compile && mvn package -DskipTests
java -jar fullnode/target/fullnode-1.0.2-SNAPSHOT.jar --spring.config.additional-location=fullnode1.properties
```
#### Docker container
There is a dockerfile and docker compose in this folder.
to build:
```
docker-compose build
docker-compose up -d
```
---
## Support

Don't hesitate to reach out to us at one of the following places:

- Email: <a href="https://coti.io/" target="_blank">`contact@coti.io`</a>
- Discord: <a href="https://discord.me/coti" target="_blank">`COTI Discord`</a>
- Telegram: <a href="https://t.me/COTInetwork" target="_blank">`COTInetwork`</a>

---
## License
[![License: GPL v3](https://img.shields.io/badge/License-GPLv3-blue.svg)](https://www.gnu.org/licenses/gpl-3.0)
This project is licensed under the terms of the **GNU General Public License v3.0** license.