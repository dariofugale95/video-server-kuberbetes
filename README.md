# Video Server Homework
## Distributed Systems and Big Data

### Studenti:
* Fugale Dario - Mat. O55000394
* Castagnolo Giulia - Mat. O55000389
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


### Quick Start

1) Lanciare minikube (preferibilmente) con le seguenti risorse:

```bash
minikube start --vm-driver=kvm2 --memory=8120 --cpus=4 
eval $(minikube docker-env)
```

2) Effettuare i build delle immagini. I dockerfiles dei componenti si trovano all'interno delle relative directory
```bash
docker build -t apigateway:v1 . -f Dockerfile-prod
docker build -t videomanagementservice:v1 . -f Dockerfile-prod
docker build -t videoprocessingservice:v1 . -f Dockerfile-prod
docker build -t spark-hadoop:v1 . -f Dockerfile-prod
```

3) Creare i deployments, pods e services. Dalla main directory del progetto video-server-kubernetes, lanciare:
```bash
kubectl apply -f ./k8s
cd spark/
bash create.sh
```

4) Creare un account *admin* all'interno del pod di mongodb 
```bash
kubectl exec -it <NOMEPOD_MONGODB> mongo
use video-server
db.createUser({user:"admin",pwd:"admin",roles:[{role:"userAdminAnyDatabase",db:"admin"}]})
```

Adesso tutto è pronto per utizzare il sistmea.
