package at.ac.ase.inso.group02.bartering.inactivity.impl;

import at.ac.ase.inso.group02.bartering.ExchangeService;
import at.ac.ase.inso.group02.bartering.inactivity.ExchangeExpirationListener;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import io.quarkiverse.rabbitmqclient.RabbitMQClient;
import io.quarkus.logging.Log;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.UUID;

@ApplicationScoped
public class ExchangeExpirationListenerImpl implements ExchangeExpirationListener {

    private RabbitMQClient rabbitMQClient;
    private ExchangeService exchangeService;

    public ExchangeExpirationListenerImpl(RabbitMQClient rabbitMQClient, ExchangeService exchangeService) {
        this.rabbitMQClient = rabbitMQClient;
        this.exchangeService = exchangeService;
    }

    private Channel channel;

    // demo setup
    @PostConstruct
    private void setupRabbitMQAndListenToStaleQueue() {

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
    public void setupExpirationListener() throws IOException {
        String queueName = "stale-chat-queue";
        channel.basicConsume(queueName, false, (consumerTag, message) -> {
            try {
                // Extract exchangeUUID from the message routing key
                String exchangeUUID = message.getEnvelope().getRoutingKey();

                // Process the stale chat message
                Log.infov("Processing stale message for ExchangeChat: {0}", exchangeUUID);

                exchangeService.onExchangeChatExpiry(UUID.fromString(exchangeUUID));

                // Acknowledge the message
                channel.basicAck(message.getEnvelope().getDeliveryTag(), false);
            } catch (Exception e) {
                // Handle processing error and reject the message without requeuing
                channel.basicNack(message.getEnvelope().getDeliveryTag(), false, false);
            }
        }, consumerTag -> {
            // Consumer canceled
        });

        Log.infov("Listening to stale chats");
    }
}
