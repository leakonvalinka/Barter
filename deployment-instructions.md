# Build

## Backend
```bash
cd backend
mvn install -Dquarkus.container-image.build=true -DskipTests
```

## Frontend
```bash
docker build -t registry.reset.inso-w.at/2024ws-ase-pr-group/24ws-ase-pr-inso-02/frontend -f frontend/Dockerfile frontend
```


# Deploy
- Assuming you have a local cluster with ingress setup
- Ingress need to be setup so that we can access by just using localhost[:80]
- also assumes you have loaded the images into the local registry

```bash
kubectl apply -f deployment/config
kubectl apply -f deployment/
```

## Extra: Setting up kind cluster with ingress
- Install kind

```bash
kind create cluster --config deployment/local/kind-ingress-config.yaml
kind load docker-image registry.reset.inso-w.at/2024ws-ase-pr-group/24ws-ase-pr-inso-02/backend:latest
kind load docker-image registry.reset.inso-w.at/2024ws-ase-pr-group/24ws-ase-pr-inso-02/frontend:latest
kubectl wait --namespace ingress-nginx \
  --for=condition=ready pod \
  --selector=app.kubernetes.io/component=controller \
  --timeout=90s
```
