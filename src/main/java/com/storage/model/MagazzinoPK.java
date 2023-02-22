package com.storage.model;

import java.io.Serializable;
import java.util.Objects;

import jakarta.persistence.Embeddable;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

@Embeddable
public class MagazzinoPK implements Serializable{

	private static final long serialVersionUID = 1L;
	private Long idMagazzino;
	/*@ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_prodotto")*/
	private Long idProdotto;
	
	public MagazzinoPK() {
		super();
	}
	public Long getIdMagazzino() {
		return idMagazzino;
	}
	public void setIdMagazzino(Long idMagazzino) {
		this.idMagazzino = idMagazzino;
	}
	public Long getIdProdotto() {
		return idProdotto;
	}
	public void setIdProdotto(Long idProdotto) {
		this.idProdotto = idProdotto;
	}
	public MagazzinoPK(Long idMagazzino, Long idProdotto) {
		super();
		this.idMagazzino = idMagazzino;
		this.idProdotto = idProdotto;
	}
	@Override
	public int hashCode() {
		return Objects.hash(idMagazzino, idProdotto);
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		MagazzinoPK other = (MagazzinoPK) obj;
		return Objects.equals(idMagazzino, other.idMagazzino) && Objects.equals(idProdotto, other.idProdotto);
	}
	
	
	
}
