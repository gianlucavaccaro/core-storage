package com.storage.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.storage.model.Magazzino;
import com.storage.exception.ResourceNotFoundException;
import com.storage.model.MagazzinoPK;
import com.storage.service.StorageService;

@RestController
@RequestMapping("/magazzino")
public class StorageController {

	@Autowired
	StorageService storageService;
	
	@GetMapping("/storage")
	public ResponseEntity<List<Magazzino>> getAllStorages(){
		List<Magazzino> listStorages=storageService.retrieveAllStorages();
		if(listStorages.isEmpty() || listStorages.size()==0)
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);
		return new ResponseEntity<>(listStorages,HttpStatus.OK);
	}
	
	@GetMapping("/storageById")
	public ResponseEntity<Magazzino> getStorageById(@RequestParam(required=true) Long idMagazzino, @RequestParam(required=true) Long idProdotto) {
		try {
			Magazzino storage=storageService.retrieveById(idMagazzino, idProdotto);
			return new ResponseEntity<Magazzino>(storage,HttpStatus.OK);
		} catch(ResourceNotFoundException e) {
			e.printStackTrace();
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
	}
	
	/*CREATE*/
	@PutMapping("/storage")
	public ResponseEntity<Magazzino> createStorage(@RequestParam(required=true) Long idStorage,@RequestParam(required=true) Long idProdotto,@RequestParam(required=true) boolean disponibilità,@RequestParam(required=true) int numeroPezzi) {
		try {
			Magazzino storage=storageService.createStorage(idStorage,idProdotto,disponibilità,numeroPezzi);
			return new ResponseEntity<Magazzino>(storage,HttpStatus.OK);
		} catch (DataIntegrityViolationException e) {
			e.printStackTrace();
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		} catch (Exception e) {
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	/*UPDATE*/
	@PostMapping("/storage")
	public ResponseEntity<Magazzino> updateStorage(@RequestParam(required=true) Long idStorage, @RequestParam(required=true) Long idProdotto, @RequestParam(required=true) boolean disponibilità, @RequestParam(required=true) int numeroPezzi) throws ResourceNotFoundException {
		try {
			Magazzino storage= storageService.updateMagazzino(idStorage, idProdotto, disponibilità, numeroPezzi);
			return new ResponseEntity<Magazzino>(storage,HttpStatus.OK);
		} catch (ResourceNotFoundException e) {
			e.printStackTrace();
			return new ResponseEntity<>(null,HttpStatus.NOT_FOUND);
		}
	}
	
	/*delete*/
	@DeleteMapping("/storage")
	public ResponseEntity<String> deleteStorage(@RequestParam(required=true) Long idStorage,@RequestParam Long idProdotto) {
		try {
			MagazzinoPK idMag=new MagazzinoPK(idStorage,idProdotto);
			storageService.deleteMagazzino(idMag);
			return new ResponseEntity<String>("Deleted.",HttpStatus.OK);
		} catch (ResourceNotFoundException e) {
			e.printStackTrace();
			return new ResponseEntity<String>("Resource not found",HttpStatus.OK);
		}
		
	}
	
}
