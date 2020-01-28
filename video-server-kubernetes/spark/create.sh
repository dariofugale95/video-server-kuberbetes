#!/bin/bash

kubectl create -f ./kubernetes/spark-deployment.yaml
kubectl create -f ./kubernetes/spark-service.yaml
#kubectl create -f ./kubernetes/minikube-ingress.yaml



