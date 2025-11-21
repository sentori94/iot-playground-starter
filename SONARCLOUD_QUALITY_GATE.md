# Configuration Quality Gate SonarCloud

## Seuils Recommandés pour iot-playground-starter

### 📊 Quality Gate "Raisonnable" pour un Projet IoT/DevOps

Ces seuils sont adaptés pour un projet en développement actif, avec un bon équilibre entre qualité et pragmatisme.

---

## 🎯 Conditions sur le Nouveau Code (New Code)

### 1. **Coverage** (Couverture de tests)
- **Métrique** : `Coverage on New Code`
- **Opérateur** : `is less than`
- **Valeur** : **70%**
- ✅ **Raisonnable** : 70% est un bon équilibre (80% serait idéal, mais 70% reste acceptable)

### 2. **Duplications** (Code dupliqué)
- **Métrique** : `Duplicated Lines (%) on New Code`
- **Opérateur** : `is greater than`
- **Valeur** : **3%**
- ✅ **Raisonnable** : Tolère un peu de duplication, mais reste strict

### 3. **Maintainability Rating** (Dette technique)
- **Métrique** : `Maintainability Rating on New Code`
- **Opérateur** : `is worse than`
- **Valeur** : **A**
- ✅ **Strict mais réaliste** : Force une bonne qualité de code dès le début

### 4. **Reliability Rating** (Bugs)
- **Métrique** : `Reliability Rating on New Code`
- **Opérateur** : `is worse than`
- **Valeur** : **A**
- ✅ **Strict** : Aucun bug critique/majeur toléré dans le nouveau code

### 5. **Security Rating** (Vulnérabilités)
- **Métrique** : `Security Rating on New Code`
- **Opérateur** : `is worse than`
- **Valeur** : **A**
- ✅ **Strict** : Sécurité primordiale pour un projet IoT

### 6. **Security Hotspots Reviewed** (Revue des points sensibles)
- **Métrique** : `Security Hotspots Reviewed on New Code`
- **Opérateur** : `is less than`
- **Valeur** : **100%**
- ✅ **Strict** : Tous les hotspots doivent être revus

---

## 📈 Conditions sur le Code Global (Overall Code) - Optionnel

Ces conditions sont plus permissives pour le code existant (legacy) :

### 7. **Coverage Globale** (Optionnel)
- **Métrique** : `Coverage`
- **Opérateur** : `is less than`
- **Valeur** : **60%**
- 💡 Plus tolérant pour le code existant

### 8. **Code Smells** (Optionnel mais recommandé)
- **Métrique** : `Code Smells`
- **Opérateur** : `is greater than`
- **Valeur** : **50**
- 💡 Limite le nombre total de problèmes mineurs

---

## 🚀 Comment Configurer dans SonarCloud

### ⚠️ IMPORTANT : Configuration Initiale Requise

**Si vous voyez "Quality Gate: Not computed"**, suivez ces étapes obligatoires :

#### Étape 1 : Assigner un Quality Gate au projet
1. Connectez-vous à [SonarCloud](https://sonarcloud.io)
2. Allez sur votre projet `iot-playground-starter`
3. **Project Settings** (engrenage) → **Quality Gate**
4. Sélectionnez **"Sonar way"** (recommandé pour commencer)
5. Cliquez sur **Save**

#### Étape 2 : Définir le "New Code" (CRUCIAL)
1. **Project Settings** → **New Code**
2. Sélectionnez **"Previous version"** (recommandé)
   - Ou **"Number of days"** : 30 jours
3. Cliquez sur **Save**

#### Étape 3 : Relancer une analyse
- Relancez votre workflow GitHub Actions
- Le Quality Gate sera maintenant calculé correctement

---

### Option 1 : Utiliser le Quality Gate par Défaut (Recommandé pour commencer)

1. Connectez-vous à [SonarCloud](https://sonarcloud.io)
2. Allez sur votre projet `iot-playground-starter`
3. **Project Settings** → **Quality Gate**
4. Sélectionnez **"Sonar way"** (le Quality Gate par défaut)
   - Il inclut déjà des seuils similaires à ceux recommandés ci-dessus

### Option 2 : Créer un Quality Gate Personnalisé

1. Dans SonarCloud, menu principal → **Quality Gates**
2. Cliquez sur **Create**
3. Nommez-le : `IoT-Playground-Gate`
4. Cliquez sur **Add Condition** pour chaque métrique ci-dessus
5. Configurez les valeurs recommandées
6. **Project Settings** → **Quality Gate** → Sélectionnez votre gate personnalisé

---

## 📊 Résumé des Seuils Recommandés

| Métrique | Nouveau Code | Code Global |
|----------|--------------|-------------|
| **Coverage** | ≥ 70% | ≥ 60% |
| **Duplications** | ≤ 3% | ≤ 5% |
| **Maintainability** | A | B acceptable |
| **Reliability** | A (0 bugs critiques) | A |
| **Security** | A (0 vulnérabilités) | A |
| **Hotspots Reviewed** | 100% | 80% |

---

## 💡 Conseils

### Pour un Projet en Production
- Gardez les seuils **stricts** (A partout)
- Coverage minimum : **80%**

### Pour un Projet en Développement (votre cas)
- Seuils **raisonnables** comme ci-dessus
- Coverage minimum : **70%**
- Focus sur le **nouveau code** plutôt que le code legacy

### Si le Quality Gate Bloque Trop Souvent
- Augmentez temporairement les seuils (ex: coverage à 60%)
- Mais gardez toujours les critères de **sécurité stricts** (A)

---

## 🔍 Vérifier le Quality Gate dans GitHub Actions

Votre workflow `.github/workflows/ecr.yml` vérifie déjà automatiquement le Quality Gate :

```yaml
- name: SonarQube Quality Gate
  uses: sonarsource/sonarqube-quality-gate-action@v1.1.0
  with:
    scanMetadataReportFile: target/sonar/report-task.txt
```

Si le Quality Gate **échoue** (FAILED), le workflow s'arrêtera et l'image Docker ne sera **pas pushée** sur ECR.

---

## 📚 Ressources

- [Documentation SonarCloud Quality Gates](https://docs.sonarcloud.io/improving/quality-gates/)
- [Métriques SonarQube](https://docs.sonarqube.org/latest/user-guide/metric-definitions/)
- [Best Practices Clean Code](https://docs.sonarcloud.io/improving/clean-as-you-code/)

---

**Dernière mise à jour** : 2025-01-21
