This is a Root backend project bootstrapped with [Spring Initializr](https://start.spring.io/). 

## Getting Started
First, configure your database and environment variables:
- Create a MySQL database named root_db in your local environment.
- Inject your database credentials as environment variables (DB_USERNAME, DB_PASSWORD) in your IDE.

Then run the development server:
```bash
# macOS / Linux
./gradlew bootRun

# Windows
gradlew.bat bootRun
```
(Or Press the Run button directly in RootApplication.java in the IDE)

Open http://localhost:8080 with your browser or API Client (like Postman) to see the result.

You can start editing the application by modifying files in `src/main/java/com/root/root/`. The server will apply your changes when you restart it.

This project uses [Spring Data JPA](https://spring.io/projects/spring-data-jpa) to automatically manage database schemas and optimize querues.

## Learn More
To learn more about Spring Boot and the tools we use, take a look at the following resources:
- [Spring Boot Reference Documentation](https://docs.spring.io/spring-boot/documentation.html) - learn about Spring Boot features and API.
- [Spring Data JPA reference](https://docs.spring.io/spring-data/jpa/reference/) - learn about database mapping and repository interfaces.
- [Lombok Features](https://projectlombok.org/features/) - an interactive tutorial on how to reduce boilerplate code.

You can check out [the Spring Boot Github repository](https://github.com/spring-projects/spring-boot) - your feedback and contributions are welcome!

## Deploy on Cloud
The easiest way to deploy your Spring Boot app is to use cloud platforms like AWS(EC2/Elastic Beanstalk), Google Cloud or by containerizing with Docker.

Check out the [Spring Boot deployment documentation](https://docs.spring.io/spring-boot/how-to/deployment/index.html) for more details. 
