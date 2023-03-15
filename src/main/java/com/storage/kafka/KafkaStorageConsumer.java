package com.storage.kafka;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.storage.exception.ResourceNotFoundException;
import com.storage.model.MagazzinoPK;
import com.storage.service.StorageService;

import jakarta.annotation.PostConstruct;

@Component
public class KafkaStorageConsumer {

	@Autowired
	private StorageService service;
	@Autowired
	private KafkaStorageProducer producer;
	
	private ObjectMapper objectMapper;
	
	private static final Logger logger = LogManager.getLogger(KafkaStorageConsumer.class);
	
	@PostConstruct
	public void init() {
		objectMapper=new ObjectMapper();
		objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
	}
	
	@KafkaListener(topics="TOPIC_STORAGE_IN",groupId="CORE-PRODUCT_KAFKA_TOPIC_STORAGE_IN")
	public void consume(String message) throws JsonProcessingException {
		OrderEvent event=objectMapper.readValue(message, OrderEvent.class);
		TrackingEvent storageRecord=new TrackingEvent();
		logger.info("Received an OrderEvent in core-storage from "+ event.getLastTracking().getServiceName()+ " with status: "+event.getLastTracking().getStatus()+".");
		
		try {
			storageRecord=event.getLastTracking();
			service.retrieveById(event.getIdMagazzino(),event.getIdProdotto());
			logger.info("Product present in storage with id: "+event.getIdMagazzino());
			MagazzinoPK id=new MagazzinoPK(event.getIdMagazzino(),event.getIdProdotto());
			storageRecord.setServiceName("core-storage");
			if(service.updateNumeroPezzi(id, event.getNumeroPezzi()))
				storageRecord.setStatus("OK");
			else {
				//non posso decrementare, la giacenza è < della richiesta
				storageRecord.setStatus("REJECTED");
				logger.info("Not enough stock in storage with id: "+event.getIdMagazzino());
			}
			event.getTracking().add(storageRecord);
			logger.info("Sending OrderEvent back from core-storage with status" +event.getLastTracking().getStatus());
			producer.sendAckStorageOrder(event);
		} catch (Exception e) {
			e.printStackTrace();
			storageRecord.setStatus("KO");
			storageRecord.setServiceName("core-storage");
			if(e instanceof ResourceNotFoundException)
				logger.info("Storage with id "+event.getIdMagazzino()+" not found.");
			event.getTracking().add(storageRecord);
			logger.error("KO. Sending OrderEvent back from core-storage to orchestrator.");
			producer.sendAckStorageOrder(event);
		}
	}
	
	/*
	 * Transazione di compensazione se core-order fallisce - incremento di numeroPezzi
	 * */
	@KafkaListener(topics="TOPIC_STORAGE_ROLLBACK",groupId="CORE-PRODUCT_KAFKA_TOPIC_STORAGE_IN")
	public void consumeRollback(String message) throws JsonProcessingException {
		OrderEvent event=objectMapper.readValue(message, OrderEvent.class);
		logger.info("Rollback Event. Received message from "+ event.getLastTracking().getServiceName()+" to compensate product decrease.");
		TrackingEvent storageRecord=new TrackingEvent();
		
		try {
			storageRecord=event.getLastTracking();
			service.retrieveById(event.getIdMagazzino(),event.getIdProdotto());
			MagazzinoPK id=new MagazzinoPK(event.getIdMagazzino(),event.getIdProdotto());
			service.addStorageNumeroPezzi(id, event.getNumeroPezzi());
			logger.info("Order product correctly added in storage.");
			storageRecord.setServiceName("core-storage");
			storageRecord.setStatus("ROLLBACK");
			event.getTracking().add(storageRecord);
			logger.info("Sending OrderEvent back to orchestrator after compensation transaction.");
			producer.sendAckStorageOrder(event);
		} catch (Exception e) {
			e.printStackTrace();
			if(e instanceof ResourceNotFoundException) {
				logger.info("Storage with id "+ event.getIdMagazzino()+" not found.");
				storageRecord.setStatus("KO");
			} else {
				storageRecord.setStatus("ROLLBACK");
			}
			storageRecord.setServiceName("core-storage");
			event.getTracking().add(storageRecord);
			logger.error("KO. Sending OrderEvent Rollback back from core-storage to orchestrator.");
			producer.sendAckStorageOrder(event);
		}
	}
}
