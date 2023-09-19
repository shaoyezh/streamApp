# streamApp
1. Install project Java dependency

    ```mvn install```
2. Start kafka server and create two topics
    ```
    bin/kafka-topics.sh --create --topic stream-app-input --bootstrap-server localhost:9092
    bin/kafka-topics.sh --create --topic stream-app-output --bootstrap-server localhost:9092
    ```
3. Start a Producer in console 1, and a Consumer in console 2
    ```
    bin/kafka-console-producer.sh --topic stream-app-input --from-beginning --bootstrap-server localhost:9092 
    bin/kafka-console-consumer.sh --topic stream-app-output --from-beginning --bootstrap-server localhost:9092
    ```
4. Run program 
    ```
    mvn -q compile exec:Java
    ```