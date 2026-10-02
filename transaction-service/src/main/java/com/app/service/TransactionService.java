package com.app.service;

import com.app.constants.Constants;
import com.app.dto.TransactionDTO;
import com.app.dto.UserResponseDTO;
import com.app.enums.TransactionStatus;
import com.app.models.Transaction;
import com.app.repository.TransactionRepository;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.json.simple.JSONObject;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.UUID;
@Slf4j
@Service
public class TransactionService {

    @Autowired
    TransactionRepository transactionRepository;

    @Autowired
    KafkaTemplate kafkaTemplate;

    @Autowired
    @Qualifier("transactionObjectMapper")
    ObjectMapper objectMapper;

    public String transact(@Valid TransactionDTO transactionDTO, List<UserResponseDTO> users) {

        UserResponseDTO sender = users.stream()
                .filter(u -> u.getPhoneNumber().equals(transactionDTO.getSender()))
                .findFirst()
                .orElse(null);

        UserResponseDTO receiver = users.stream()
                .filter(u -> u.getPhoneNumber().equals(transactionDTO.getReceiver()))
                .findFirst()
                .orElse(null);

        if (sender == null) {
            return "Sender not found";
        }
        if (receiver == null)
            return "Receiver Does Not Exists";

        Transaction newTransaction = Transaction.builder()
                .sender(transactionDTO.getSender())
                .receiver(transactionDTO.getReceiver())
                .amount(transactionDTO.getAmount())
                .reason(transactionDTO.getReason() == null ? "" : transactionDTO.getReason())
                .transactionId(String.valueOf(UUID.randomUUID()))
                .transactionStatus(TransactionStatus.PENDING)
                .build();

        Transaction savedTransaction = transactionRepository.save(newTransaction);
        String senderEmail = sender.getEmail();
        String receiverEmail = receiver.getEmail();
        log.info("senderEmail = {}", senderEmail);
        log.info("receiverEmail = {}", receiverEmail);

        JSONObject jsonObject = new JSONObject();
        jsonObject.put(Constants.TRANSACTION_CREATED_TOPIC_SENDER, savedTransaction.getSender());
        jsonObject.put(Constants.TRANSACTION_CREATED_TOPIC_RECEIVER, savedTransaction.getReceiver());
        jsonObject.put(Constants.TRANSACTION_CREATED_TOPIC_AMOUNT, savedTransaction.getAmount());
        jsonObject.put(Constants.TRANSACTION_CREATED_TOPIC_TRANSACTION_ID, savedTransaction.getTransactionId());
        jsonObject.put(Constants.SENDER_EMAIL,senderEmail);
        jsonObject.put(Constants.RECEIVER_EMAIL,receiverEmail);

        kafkaTemplate.send(Constants.TRANSACTION_CREATED_TOPIC, objectMapper.writeValueAsString(jsonObject));

        return savedTransaction.getTransactionId();
    }

    @KafkaListener(topics = Constants.WALLET_UPDATED_TOPIC, groupId = "e-wallet_group")
    public void updateTransactionStatus(String message) throws ParseException {
        JSONObject data = (JSONObject) new JSONParser().parse(message);

        String transactionId = data.get(Constants.TRANSACTION_CREATED_TOPIC_TRANSACTION_ID).toString();
        String transactionStatus = data.get(Constants.WALLET_UPDATED_TOPIC_STATUS).toString();
        String senderEmail = data.get(Constants.SENDER_EMAIL).toString();
        String receiverEmail = data.get(Constants.RECEIVER_EMAIL).toString();
        String sender = data.get(Constants.TRANSACTION_CREATED_TOPIC_SENDER).toString();
        Transaction transaction = transactionRepository.findByTransactionId(transactionId);

        if (transactionStatus.equalsIgnoreCase(TransactionStatus.SUCCESSFUL.toString())) {
            log.info("in if block");
            transaction.setTransactionStatus(TransactionStatus.SUCCESSFUL);
            transactionRepository.save(transaction);
            String emailMessage ="Hi, You Have Received An Amount :  "+ transaction.getAmount() +" From " + sender + "In Your Wallet!";
            JSONObject jsonObject = new JSONObject();
            jsonObject.put(Constants.EMAIL,receiverEmail);
            jsonObject.put(Constants.EMAIL_MESSAGE,emailMessage);
            log.info("EMAIL MESSAGE BEING SENT TO KAFKA: {}", emailMessage);
            kafkaTemplate.send(Constants.SEND_EMAIL_TOPIC,objectMapper.writeValueAsString(jsonObject));
        } else {
            log.info("in else block");
            transaction.setTransactionStatus(TransactionStatus.FAILED);
            transactionRepository.save(transaction);
            String emailMessage ="Hi, Your Transaction With Transaction Id : " + transactionId +"Was : " + " " + transactionStatus ;
            JSONObject jsonObject = new JSONObject();
            jsonObject.put(Constants.EMAIL,senderEmail);
            jsonObject.put(Constants.EMAIL_MESSAGE,emailMessage);
            log.info("EMAIL MESSAGE BEING SENT TO KAFKA: {}", emailMessage);
            log.info("transactionStatus = '{}'", transactionStatus);
            kafkaTemplate.send(Constants.SEND_EMAIL_TOPIC,objectMapper.writeValueAsString(jsonObject));
        }
    }

    public TransactionStatus getTransactionStatus(String transactionId) {
        return transactionRepository.findByTransactionId(transactionId).getTransactionStatus();
    }
}
