# Shopping Cart

### How-to guide
For further reference, please consider the following sections:

* git clone project repo (e.g. https://github.com/Mintas/task-shopping-cart.git )
* run './gradlew letsShop -x test'  (there are integration tests that can take time)
* it will build project (skipping tests), pack image and start docker-compose
* open [shoppingCartApi](http://localhost:8080/swagger-ui/index.html) in your browser
* have fun
* note: ordered cart messages are in "CART_SUBMITTED" topic, try command below:
* docker exec -ti shopping-cart-project_kafka_1 /bin/kafka-console-consumer --bootstrap-server localhost:9092 --topic CART_SUBMITTED --from-beginning


### Troubleshooting
In case of any problems with build and run contact me.
* known cases: user registration via email is required for OIDC
* verify topics are present with command below:
* docker exec -ti shopping-cart-project_kafka_1 /bin/kafka-topics --list --zookeeper zookeeper:22181


### TODO List:
Ordered by implementation priority
1. describe errors in rest specs
2. add more javadocs
3. implement MockOrderManagement - CART_SUBMITTED consumer that will free items from reserved state
4. add static code analisys: pmd, checkstyle; add jacoco
5. implemet GUI (ReactJS  + node plugin)
6. split to microservices with common gateway (as resource server) such as:
 BookApplication, CartApplication, UiApplication, OrderManagementApplication, GatewayApplication
