# --- TAHAP 1: build JAR (profil production agar frontend Vaadin ikut dibundel) ---
FROM eclipse-temurin:25-jdk-jammy AS build
WORKDIR /app

COPY pom.xml .
COPY .mvn .mvn
COPY mvnw .
COPY src ./src

RUN chmod +x mvnw && ./mvnw -B clean package -Pproduction -DskipTests

# --- TAHAP 2: runtime ringkas ---
FROM eclipse-temurin:25-jre-jammy
WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

# Foto profil dan bukti transaksi (UPLOAD_DIR=uploads) sebaiknya dipasang sebagai volume.
RUN mkdir -p /app/uploads
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
