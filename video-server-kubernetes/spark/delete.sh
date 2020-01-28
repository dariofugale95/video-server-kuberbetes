#!/bin/bash

kubectl delete -f ./kubernetes/spark-deployment.yaml
kubectl delete -f ./kubernetes/spark-service.yaml
#kubectl delete -f ./kubernetes/minikube-ingress.yaml
