package at.ac.ase.inso.group02.messaging.rabbitmq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import io.quarkiverse.rabbitmqclient.RabbitMQClient;
import jakarta.enterprise.context.ApplicationScoped;

import java.io.IOException;
import java.io.UncheckedIOException;

@ApplicationScoped
public class RabbitMQInitializer {

    private RabbitMQClient rabbitMQClient;

    private static final String CHAT_EXCHANGE_NAME = "chat-exchange";
    private static final String CHAT_INACTIVITY_EXCHANGE_NAME = "chat-inactivity-exchange";
    private static final String STALE_CHAT_EXCHANGE_NAME = "stale-chat-exchange";

    private static final String STALE_CHAT_QUEUE_NAME = "stale-chat-queue";

    public RabbitMQInitializer(RabbitMQClient rabbitMQClient) {
        this.rabbitMQClient = rabbitMQClient;
    }

    public void initRabbitMQExchanges() {
        try (Connection connection = rabbitMQClient.connect()){
            // create a channel
            Channel channel = connection.createChannel();

            channel.exchangeDeclare(CHAT_EXCHANGE_NAME, "topic", true, false, null);
            channel.exchangeDeclare(CHAT_INACTIVITY_EXCHANGE_NAME, "topic", true, false, null);
            channel.exchangeDeclare(STALE_CHAT_EXCHANGE_NAME, "topic", true, false, null);

            channel.queueDeclare(STALE_CHAT_QUEUE_NAME, true, false, false, null);
            channel.queueBind(STALE_CHAT_QUEUE_NAME, STALE_CHAT_EXCHANGE_NAME, "#");

        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
