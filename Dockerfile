# Use uma imagem base oficial do OpenJDK 17
FROM openjdk:17-jdk-slim

# Defina o diretório de trabalho dentro do container
WORKDIR /app

# Copie o arquivo JAR compilado da aplicação para o diretório de trabalho
# O nome do JAR é baseado no artifactId e version do pom.xml
COPY target/associaGEST-0.0.1-SNAPSHOT.jar app.jar

# Exponha a porta que a aplicação Spring Boot usa (padrão 8080)
EXPOSE 8080

# Defina o comando padrão para executar a aplicação quando o container iniciar
ENTRYPOINT ["java", "-jar", "app.jar"]
