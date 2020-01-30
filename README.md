# Video Server Homework
Homework Distributed Systems and Big Data

____________________________________

## Homework #1 - video-server-docker 


### Quick Start 
1) creare i file .jar delle applicazioni spring relativi ai componenti dell'architettura.

2) Spostarsi nella cartella "deploy" e digitare il seguente comando:

```bash
sudo docker-compose build
sudo docker-compose up
```

3) Creazione account admin sul container di mongodb (da utilizzare solo nella fase di start up del sistema) 
```bash
sudo docker exec -it mongo-container mongo --authenticationDatabase admin

use admin
db.auth("admin","admin")
db.createUser({user:"admin",pwd:"admin",roles:[{role:"readWrite",db:"video-server"},"clusterAdmin"]})
use video-server
db.createUser({user:"admin",pwd:"admin",roles:["readWrite"]})
```
___________________________________

## Homework #2 - video-server-kubernetes
