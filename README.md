# Money Transfer PoC

### Requirements
Java 8
Maven 3

### Build
mvn clean install

### Run
mvn exec:java
or
java -jar target\money-transfer-poc-1.0-SNAPSHOT-jar-with-dependencies.jar

### REST API
| Method | Path | Description | Sample Body | Sample Response |
| -------| ---- | ----------- | ----------- | --------------- |
| POST   | /api/v1/accounts | Create new account | | AccountResponse: {"status":"OK", "balance":"0", "id":1} |
| GET    | /api/v1/accounts/{id} | Get account by id | | AccountResponse: {"status":"OK", "balance":"0", "id":1} |
| POST   | /api/v1/accounts/{id}/money | Send/Withdraw money | {"amount":"100"} | AccountResponse: {"status":"OK", "balance":"100", "id":1} |
| POST   | /api/v1/transfers | Transfer money | {"fromAccountId":1, "toAccountId":2, "amount":"50"} | Status: {"status":"OK"} |
