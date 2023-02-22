package com.storage.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

import com.storage.model.Magazzino;
import com.storage.model.MagazzinoPK;

@Service
public interface StorageRepository extends JpaRepository<Magazzino,MagazzinoPK>{

	/*Magazzino findByMagazzinoId(MagazzinoPK magazzinoId);
	List<Magazzino> findByMagazzinoPk_idMagazzino(Long idMagazzino);
	List<Magazzino> findByMagazzinoPk_idProdotto(Long idProdotto);*/
}
