rd /s /q target
call mvn clean compile
call mvn package -DskipTests
java -jar target/api-server-1.0.4.jar
java -jar target/api-server-1.0.4.jar