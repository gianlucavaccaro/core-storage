package com.storage.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.storage.exception.ResourceNotFoundException;
import com.storage.model.Magazzino;
import com.storage.model.MagazzinoPK;
import com.storage.repository.StorageRepository;

@Service
@Transactional
public class StorageService{

	@Autowired 
	StorageRepository storageRepository;
	
	/*READ ALL*/
	public List<Magazzino> retrieveAllStorages(){
		return storageRepository.findAll();
	}
	
	/*READ BYID*/
	public Magazzino retrieveById(Long idStorage, Long idProdotto) throws ResourceNotFoundException {
		MagazzinoPK id= new MagazzinoPK(idStorage, idProdotto);
		return storageRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Resource not found"));
	}
	
	/*CREATE*/
	public Magazzino createStorage(Long idStorage, Long idProdotto, boolean disponibilità, int numeroPezzi) {
		MagazzinoPK id=new MagazzinoPK(idStorage,idProdotto);
		Magazzino storage;
		if(storageRepository.findById(id).isPresent()) {
			storage=storageRepository.findById(id).get();
		}
		else {
			//check idprodotto esiste e/o già presente + gestione errori
			storage= new Magazzino(id, disponibilità, numeroPezzi);
		}
		return storageRepository.save(storage);
	}
	
	/*UPDATE*/
	public Magazzino updateMagazzino(Long id, Long idProdotto, boolean disponibilità, int numeroPezzi) throws ResourceNotFoundException {
		MagazzinoPK idMag= new MagazzinoPK(id, idProdotto);
		Magazzino storage= storageRepository.findById(idMag).orElseThrow(() -> new ResourceNotFoundException("Resource not found"));
		//check su id prodotto
		storage.setDisponibilità(disponibilità);
		storage.setNumeroPezzi(numeroPezzi);
		return storageRepository.save(storage);
	}
	
	/*DELETE*/
	public void deleteMagazzino(MagazzinoPK id) throws ResourceNotFoundException {
		Magazzino storage= storageRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Resource not found"));
		storageRepository.delete(storage);
	}
	
	public boolean updateNumeroPezzi(MagazzinoPK id, int numeroPezzi) {
		Magazzino storage=storageRepository.findById(id).get();
		if(storage.getNumeroPezzi() >= numeroPezzi) {
			storage.setNumeroPezzi(storage.getNumeroPezzi() - numeroPezzi);
			storage.setDisponibilità(!(storage.getNumeroPezzi() == 0));
			return true;
		} else {
			return false;
		}
	}
	
	public void addStorageNumeroPezzi(MagazzinoPK id, int numeroPezzi) {
		Magazzino storage=storageRepository.findById(id).get();
		storage.setNumeroPezzi(storage.getNumeroPezzi() + numeroPezzi);
	}
}
