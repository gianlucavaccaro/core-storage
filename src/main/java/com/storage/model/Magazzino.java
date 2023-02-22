package com.storage.model;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name="magazzino")
public class Magazzino {

	@EmbeddedId
	private MagazzinoPK magazzinoPk;
	private boolean disponibilità;
	private int numeroPezzi;
	
	public Magazzino() {
		super();
	}

	public Magazzino(Long idProdotto, boolean disponibilità, int numeroPezzi) {
		super();
		this.disponibilità = disponibilità;
		this.numeroPezzi = numeroPezzi;
	}

	public boolean isDisponibilità() {
		return disponibilità;
	}

	public void setDisponibilità(boolean disponibilità) {
		this.disponibilità = disponibilità;
	}

	public int getNumeroPezzi() {
		return numeroPezzi;
	}

	public void setNumeroPezzi(int numeroPezzi) {
		this.numeroPezzi = numeroPezzi;
	}

	public MagazzinoPK getMagazzinoPk() {
		return magazzinoPk;
	}

	public void setMagazzinoPk(MagazzinoPK magazzinoPk) {
		this.magazzinoPk = magazzinoPk;
	}

	public Magazzino(MagazzinoPK magazzinoPk, boolean disponibilità, int numeroPezzi) {
		super();
		this.magazzinoPk = magazzinoPk;
		this.disponibilità = disponibilità;
		this.numeroPezzi = numeroPezzi;
	}
	
	
	
}
