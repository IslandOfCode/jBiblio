package it.islandofcode.jbiblio.artefact;

public class Client {
	private int ID;
	private String nome;
	private String cognome;
	private int classe;
	private String sezione;
	private int removed;
	
	
	
	public Client(int iD, String nome, String cognome, int classe, String sezione, int removed) {
		ID = iD;
		this.nome = nome;
		this.cognome = cognome;
		this.classe = classe;
		this.sezione = sezione;
		this.removed = removed;
	}
	
	public Client() {
		super();
	}

	public int getID() {
		return ID;
	}

	public void setID(int iD) {
		ID = iD;
	}

	public String getNome() {
		return nome;
	}

	public void setNome(String nome) {
		this.nome = nome;
	}

	public String getCognome() {
		return cognome;
	}

	public void setCognome(String cognome) {
		this.cognome = cognome;
	}

	public int getClasse() {
		return classe;
	}

	public void setClasse(int classe) {
		this.classe = classe;
	}

	public String getSezione() {
		return sezione;
	}

	public void setSezione(String sezione) {
		this.sezione = sezione;
	}
	
	public int getRemoved() {
		return removed;
	}

	public void setRemoved(int removed) {
		this.removed = removed;
	}
	
	public boolean isRemoved() {
		return this.removed==1;
	}

	@Override
	public String toString() {
		return "Client [ID=" + ID + ", nome=" + nome + ", cognome=" + cognome + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ID;
		result = prime * result + classe;
		result = prime * result + ((cognome == null) ? 0 : cognome.hashCode());
		result = prime * result + ((nome == null) ? 0 : nome.hashCode());
		result = prime * result + ((sezione == null) ? 0 : sezione.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Client other = (Client) obj;
		if (ID != other.ID)
			return false;
		if (classe != other.classe)
			return false;
		if (cognome == null) {
			if (other.cognome != null)
				return false;
		} else if (!cognome.equals(other.cognome))
			return false;
		if (nome == null) {
			if (other.nome != null)
				return false;
		} else if (!nome.equals(other.nome))
			return false;
		if (sezione == null) {
			if (other.sezione != null)
				return false;
		} else if (!sezione.equals(other.sezione))
			return false;
		return true;
	}

}
