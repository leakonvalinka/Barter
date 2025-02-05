package at.ac.ase.inso.group02.messaging.worker;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import io.quarkiverse.rabbitmqclient.RabbitMQClient;
import io.quarkus.logging.Log;
import io.quarkus.websockets.next.WebSocketConnection;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class ChatRabbitMQWorker {
    private final RabbitMQClient rabbitMQClient;
    private final Long userID;
    private final WebSocketConnection connection;

    private Channel channel;
    private String queueID;


    public ChatRabbitMQWorker(RabbitMQClient rabbitMQClient, Long userID, WebSocketConnection connection) {
        this.rabbitMQClient = rabbitMQClient;
        this.userID = userID;
        this.connection = connection;
    }

    public void startWorker() throws IOException {
        Connection rabbitMQConnection = rabbitMQClient.connect();
        // create a channel
        channel = rabbitMQConnection.createChannel();

        // create queue, use the WebSocket connection-ID as queue-ID, should be unique
        queueID = "worker-queue-" + connection.id();
        channel.queueDeclare(queueID, false, true, true, null);
        channel.queueBind(queueID, "chat-exchange", userID.toString());

        channel.basicConsume(queueID, true, (consumerTag, delivery) -> {
            String message = new String(delivery.getBody());
            Log.infov("New message: {0}", message);

            if (connection.isOpen()) {
                connection.sendTextAndAwait(message);
            } else {
                Log.warn("Could not send message because WebSocket connection is no longer open!");
            }

        }, consumerTag -> {
            Log.infov("Consumer {0} canceled", consumerTag);
        });
    }

    public void stopWorker() throws IOException, TimeoutException {
        if (channel.isOpen()) {
            channel.close();
        }
    }
}
