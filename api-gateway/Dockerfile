FROM eclipse-temurin:17-jdk-jammy
WORKDIR /app
COPY . .
RUN chmod +x mvnw || true
RUN ./mvnw clean package -DskipTests || mvn clean package -DskipTests
EXPOSE 8080
CMD ["sh", "-c", "java -jar target/*.jar"]