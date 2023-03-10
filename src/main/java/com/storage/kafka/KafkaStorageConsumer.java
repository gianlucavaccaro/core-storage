package com.storage.kafka;

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
	
	@PostConstruct
	public void init() {
		objectMapper=new ObjectMapper();
		objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
	}
	
	@KafkaListener(topics="TOPIC_STORAGE_IN",groupId="CORE-PRODUCT_KAFKA_TOPIC_STORAGE_IN")
	public void consume(String message) throws JsonProcessingException {
		OrderEvent event=objectMapper.readValue(message, OrderEvent.class);
		TrackingEvent storageRecord=new TrackingEvent();
		System.out.println("Messaggio ricevuto in storage:" + event.getLastTracking().toString());
		
		try {
			storageRecord=event.getLastTracking();
			service.retrieveById(event.getIdMagazzino(),event.getIdProdotto());
			MagazzinoPK id=new MagazzinoPK(event.getIdMagazzino(),event.getIdProdotto());
			storageRecord.setServiceName("core-storage");
			if(service.updateNumeroPezzi(id, event.getNumeroPezzi()))
				storageRecord.setStatus("OK");
			else {
				//non posso decrementare, la giacenza è < della richiesta
				storageRecord.setStatus("REJECTED");
				storageRecord.setFailureReason("Product not available in stock.");
			}
			event.getTracking().add(storageRecord);
			producer.sendAckStorageOrder(event);
		} catch (Exception e) {
			e.printStackTrace();
			storageRecord.setStatus("KO");
			storageRecord.setServiceName("core-storage");
			if(e instanceof ResourceNotFoundException)
				storageRecord.setFailureReason("Product not found.");
			event.getTracking().add(storageRecord);
			producer.sendAckStorageOrder(event);
		}
	}
	
	/*
	 * Transazione di compensazione se core-order fallisce - incremento di numeroPezzi
	 * */
	@KafkaListener(topics="TOPIC_STORAGE_ROLLBACK",groupId="CORE-PRODUCT_KAFKA_TOPIC_STORAGE_IN")
	public void consumeRollback(String message) throws JsonProcessingException {
		OrderEvent event=objectMapper.readValue(message, OrderEvent.class);
		TrackingEvent storageRecord=new TrackingEvent();
		System.out.println("Messaggio di callback ricevuto");
		
		try {
			storageRecord=event.getLastTracking();
			service.retrieveById(event.getIdMagazzino(),event.getIdProdotto());
			MagazzinoPK id=new MagazzinoPK(event.getIdMagazzino(),event.getIdProdotto());
			service.addStorageNumeroPezzi(id, event.getNumeroPezzi());
			storageRecord.setServiceName("core-storage");
			storageRecord.setStatus("ROLLBACK");
			storageRecord.setFailureReason("Order deleted.");
			event.getTracking().add(storageRecord);
			producer.sendAckStorageOrder(event);
		} catch (Exception e) {
			e.printStackTrace();
			//gestire eccezioni e path
		}
	}
}
