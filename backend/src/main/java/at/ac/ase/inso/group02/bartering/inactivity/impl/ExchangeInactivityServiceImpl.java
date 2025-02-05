package at.ac.ase.inso.group02.bartering.inactivity.impl;

import at.ac.ase.inso.group02.bartering.inactivity.ExchangeInactivityService;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import io.quarkiverse.rabbitmqclient.RabbitMQClient;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Map;
import java.util.UUID;

@ApplicationScoped
public class ExchangeInactivityServiceImpl implements ExchangeInactivityService {

    private RabbitMQClient rabbitMQClient;

    public ExchangeInactivityServiceImpl(RabbitMQClient rabbitMQClient) {
        this.rabbitMQClient = rabbitMQClient;
    }

    private Channel channel;

    @ConfigProperty(name = "exchange.rating.allow-after-inactivity", defaultValue = "259200")
    private Long chatInactivityTimeSeconds;

    private Long chatInactivityTimeMilliSeconds;

    // demo setup
    @PostConstruct
    private void setupRabbitMQ() {
        chatInactivityTimeMilliSeconds = chatInactivityTimeSeconds * 1000;

        try {
            Connection connection = rabbitMQClient.connect();
            // create a channel
            channel = connection.createChannel();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @PreDestroy
    private void teardownRabbitMQ() {
        try {
            if (channel.getConnection().isOpen()) {
                channel.getConnection().close();
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }


    @Override
    public void declareChatQueue(UUID exchangeUUID) throws IOException {
        String queueName = "chat-queue-" + exchangeUUID.toString();
        channel.queueDeclare(queueName, true, false, false, Map.of(
                "x-message-ttl", chatInactivityTimeMilliSeconds,
                "x-dead-letter-exchange", "stale-chat-exchange"
//                "x-dead-letter-routing-key", "#"
        ));

        channel.queueBind(queueName, "chat-inactivity-exchange", exchangeUUID.toString());
    }

    @Override
    public void onNewMessageForExchangeChat(UUID exchangeUUID) throws IOException {

        // ensure the required queue exist
        this.declareChatQueue(exchangeUUID);

        String queueName = "chat-queue-" + exchangeUUID.toString();
        channel.queuePurge(queueName);

        channel.basicPublish(
                "chat-inactivity-exchange",
                exchangeUUID.toString(),
                new AMQP.BasicProperties.Builder()
                        .expiration(String.valueOf(chatInactivityTimeMilliSeconds)) // New TTL
                        .build(),
                new byte[0]
        );
    }

}
