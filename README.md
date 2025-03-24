## flink-over-zio-http
The application is an example of a ZIO HTTP wrapper over Flink\
It provides an ability to run SQL queries using an HTTP interface and encapsulate complexity related to sessions' / operations' management

In the result of the query the full metadata is returned in response to future analysis

It uses Flink and Flink SQL Gateway under the hood

### Local Run
* Run the Flink inside the container using the following command: `docker compose up`
* Run the application locally with `sbt run` command
* Open http://localhost:8080/app/index.html
* Run query example
```sql
SELECT 1 AS id, 'test' AS name;
```

### Run inside the container
* Run Flink inside the container using the following command: `docker compose up`
* Run the application image using the following command: ` docker run -p 8080:8080 --network host  $(docker build -q .)`
* Open http://localhost:8080/app/index.html
* Run query example
```sql
SELECT 1 AS id, 'test' AS name;
```

### Implementation Details
- _sbt-tpolecat_ plugin is used as a simple compile-time code issues detector
- _sbt-scalafmt_ plugin for code formatting


- Use `application.conf` file to configure application
- run `sbt fmt` command to format code\
- run `sbt test` to run test

Application has in-memory persistence layer, so data will be lost after the restart

Project was implemented using **JDK 17**

You can use dockerfile in project root in order to create application image