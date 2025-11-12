# AWS X-Ray - Guide de déploiement

## 📋 Configuration

L'application est configurée avec AWS X-Ray de manière **optionnelle** :

- **Développement local** : X-Ray est **désactivé** par défaut (pas besoin de daemon)
- **Production AWS** : X-Ray s'active automatiquement avec le profil `aws`

## 🚀 Déploiement sur AWS

### Option 1 : AWS Elastic Beanstalk

1. **Créer l'application Elastic Beanstalk** :
```bash
eb init -p docker iot-playground --region eu-west-1
eb create iot-playground-prod
```

2. **Activer X-Ray dans Elastic Beanstalk** :
   - Console AWS > Elastic Beanstalk > Configuration > Software
   - Cocher "AWS X-Ray daemon"
   - Sauvegarder et appliquer

3. **Déployer** :
```bash
eb deploy
```

### Option 2 : AWS ECS/Fargate

1. **Créer une tâche ECS avec 2 conteneurs** :

**Conteneur 1 : Application Spring Boot**
```json
{
  "name": "iot-playground-app",
  "image": "votre-ecr-repo/iot-playground:latest",
  "portMappings": [{"containerPort": 8080, "protocol": "tcp"}],
  "environment": [
    {"name": "SPRING_PROFILES_ACTIVE", "value": "aws"},
    {"name": "AWS_XRAY_DAEMON_ADDRESS", "value": "localhost:2000"}
  ]
}
```

**Conteneur 2 : X-Ray Daemon (sidecar)**
```json
{
  "name": "xray-daemon",
  "image": "amazon/aws-xray-daemon",
  "portMappings": [{"containerPort": 2000, "protocol": "udp"}],
  "command": ["-o"]
}
```

2. **Déployer le service ECS** :
```bash
aws ecs create-service \
  --cluster iot-cluster \
  --service-name iot-playground \
  --task-definition iot-playground-task \
  --desired-count 1 \
  --launch-type FARGATE
```

### Option 3 : AWS App Runner

1. **Créer le service App Runner** :
```bash
aws apprunner create-service \
  --service-name iot-playground \
  --source-configuration "ImageRepository={ImageIdentifier=votre-ecr-repo/iot-playground:latest,ImageConfiguration={Port=8080,RuntimeEnvironmentVariables={SPRING_PROFILES_ACTIVE=aws}}}" \
  --instance-configuration "Cpu=1024,Memory=2048" \
  --observability-configuration "XrayEnabled=true"
```

## 🔧 Configuration AWS (IAM)

L'application a besoin des permissions suivantes :

```json
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "Action": [
        "xray:PutTraceSegments",
        "xray:PutTelemetryRecords"
      ],
      "Resource": "*"
    }
  ]
}
```

## 🏠 Développement local (sans X-Ray)

```bash
# Lancer Docker Compose (PostgreSQL, Prometheus, Grafana)
docker-compose up -d

# Lancer l'application avec le profil local
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

X-Ray est désactivé → pas besoin de daemon local !

## 🧪 Tester X-Ray localement (optionnel)

Si tu veux tester X-Ray en local :

1. **Lancer le daemon X-Ray manuellement** :
```bash
docker run -d -p 2000:2000/udp --name xray-daemon \
  -e AWS_ACCESS_KEY_ID=your_key \
  -e AWS_SECRET_ACCESS_KEY=your_secret \
  -e AWS_REGION=eu-west-1 \
  amazon/aws-xray-daemon -o
```

2. **Lancer l'application avec X-Ray activé** :
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=aws
```

3. **Vérifier dans AWS X-Ray Console** :
   - https://console.aws.amazon.com/xray/home?region=eu-west-1

## 📊 Visualiser les traces

- **AWS X-Ray Console** : Service Map + Traces détaillées
- **CloudWatch ServiceLens** : Vue intégrée avec les métriques et logs

## 🔍 Variables d'environnement importantes

| Variable | Description | Défaut |
|----------|-------------|--------|
| `SPRING_PROFILES_ACTIVE` | Profil Spring (local/aws) | local |
| `AWS_XRAY_DAEMON_ADDRESS` | Adresse du daemon X-Ray | 127.0.0.1:2000 |
| `AWS_REGION` | Région AWS | eu-west-1 |

