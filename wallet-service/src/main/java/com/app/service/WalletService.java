package com.app.service;

import com.app.constants.Constants;
import com.app.constants.Constants.*;
import com.app.enums.UserIdentifier;
import com.app.models.Wallet;
import com.app.repository.WalletRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import tools.jackson.databind.ObjectMapper;

@Service
@Slf4j
public class WalletService {

    @Autowired
    WalletRepository walletRepository;

    @Autowired
    KafkaTemplate kafkaTemplate;

    @Autowired
    ObjectMapper objectMapper;

    @KafkaListener(topics = Constants.USER_CREATED_TOPIC,groupId = "e-wallet_group")
    public void createWallet(String message) throws ParseException {
       log.info("In Crete Wallet Method of Wallet Service ");

        JSONObject data = (JSONObject) new JSONParser().parse(message);

        Wallet wallet = Wallet.builder()
                .userId((Long) data.get(Constants.USER_CREATED_TOPIC_USER_ID))
                .email((String) data.get(Constants.USER_CREATED_TOPIC_EMAIL))
                .phoneNumber((String) data.get(Constants.USER_CREATED_TOPIC_PHONE_NUMBER))
                .userIdentifier(UserIdentifier.valueOf((String)data.get(Constants.USER_CREATED_TOPIC_IDENTIFIER_KEY)))
                .identifierValue((String) data.get(Constants.USER_CREATED_TOPIC_IDENTIFIER_VALUE))
                .balance(100)
                .build();


        Wallet savedWallet=walletRepository.save(wallet);
        log.info("wallet created for username: {}", savedWallet.getPhoneNumber());
    }

    @Transactional
    @KafkaListener(topics = Constants.TRANSACTION_CREATED_TOPIC,groupId = "e-wallet_group")
    public void updateWallet(String message) throws ParseException {
        log.info("In update Wallet Method of Wallet Service ");

        JSONObject data = (JSONObject) new JSONParser().parse(message);

        String sender = data.get(Constants.TRANSACTION_CREATED_TOPIC_SENDER).toString();
        String receiver = data.get(Constants.TRANSACTION_CREATED_TOPIC_RECEIVER).toString();
        Double amount =(Double) data.get(Constants.TRANSACTION_CREATED_TOPIC_AMOUNT);
        String transactionId= data.get(Constants.TRANSACTION_CREATED_TOPIC_TRANSACTION_ID).toString();
        String senderEmail = data.get(Constants.SENDER_EMAIL).toString();
        String receiverEmail = data.get(Constants.RECEIVER_EMAIL).toString();

        Wallet senderWallet = walletRepository.findByPhoneNumber(sender);
        Wallet receiverWallet = walletRepository.findByPhoneNumber(receiver);

        log.info("Sender: {}, Receiver: {}, Amount: {}", sender, receiver, amount);
        log.info("Sender Wallet: {}", senderWallet);
        log.info("Receiver Wallet: {}", receiverWallet);

        JSONObject jsonObject = new JSONObject();

        jsonObject.put(Constants.TRANSACTION_CREATED_TOPIC_SENDER,sender);
        jsonObject.put(Constants.TRANSACTION_CREATED_TOPIC_RECEIVER,receiver);
        jsonObject.put(Constants.TRANSACTION_CREATED_TOPIC_AMOUNT,amount);
        jsonObject.put(Constants.TRANSACTION_CREATED_TOPIC_TRANSACTION_ID,transactionId);
        jsonObject.put(Constants.SENDER_EMAIL,senderEmail);
        jsonObject.put(Constants.RECEIVER_EMAIL,receiverEmail);

        if(senderWallet==null || receiverWallet == null || senderWallet.getBalance()< amount ){
            jsonObject.put(Constants.WALLET_UPDATED_TOPIC_STATUS,Constants.WALLET_UPDATED_TOPIC_FAILED);
        }else {
            senderWallet.setBalance( (senderWallet.getBalance() - amount));
            receiverWallet.setBalance( (receiverWallet.getBalance()  + amount));

            walletRepository.save(senderWallet);
            walletRepository.save(receiverWallet);

            jsonObject.put(Constants.WALLET_UPDATED_TOPIC_STATUS,Constants.WALLET_UPDATED_TOPIC_SUCCESS);
        }

        kafkaTemplate.send(Constants.WALLET_UPDATED_TOPIC,objectMapper.writeValueAsString(jsonObject));
    }

    public Wallet getWalletByPhoneNumber(String phoneNumber) {
        return walletRepository.findByPhoneNumber(phoneNumber);
    }

    public Double getBalance(String phoneNumber) {
        Wallet wallet = walletRepository.findByPhoneNumber(phoneNumber);

        if (wallet == null) {
            return null;
        }

        return wallet.getBalance();
    }

    public Wallet addMoney(String phoneNumber, Double amount) {
        Wallet wallet = walletRepository.findByPhoneNumber(phoneNumber);

        if (wallet == null || amount == null || amount <= 0) {
            return null;
        }

        wallet.setBalance(wallet.getBalance() + amount);

        return walletRepository.save(wallet);
    }


}
