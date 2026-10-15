# ==============================================================================
# Étape 1 : Construction du projet avec Maven et Eclipse Temurin JDK 21
# ==============================================================================
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /build

# Copie des fichiers Maven wrapper et descripteur pour mise en cache
COPY pom.xml mvnw ./
COPY .mvn .mvn/
RUN chmod +x mvnw

# Téléchargement des dépendances en cache Docker
RUN ./mvnw dependency:go-offline -B || true

# Copie des sources
COPY src ./src

# Compilation et empaquetage du fichier JAR exécutable
RUN ./mvnw clean package -DskipTests -B

# ==============================================================================
# Étape 2 : Image d'exécution minimale avec Eclipse Temurin JRE 21
# ==============================================================================
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Création d'un groupe et d'un utilisateur non-root pour la sécurité
RUN addgroup -S ladafura && adduser -S ladafura -G ladafura

# Dossier d'upload pour les images et documents
RUN mkdir -p uploads && chown -R ladafura:ladafura /app

# Récupération du JAR produit à l'étape 1
COPY --from=builder /build/target/*.jar app.jar
RUN chown ladafura:ladafura app.jar

USER ladafura

# Configuration mémoire JVM adaptée aux conteneurs cloud (Render 512MB RAM)
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -XX:+ExitOnOutOfMemoryError -Djava.security.egd=file:/dev/./urandom"

EXPOSE 8080

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
