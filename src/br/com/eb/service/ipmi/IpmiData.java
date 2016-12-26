package br.com.eb.service.ipmi;

public class IpmiData {
	String nome;
	String valor;
	
	public IpmiData() {
		super();
	}

	public IpmiData(String nome, String valor) {
		super();
		this.nome = nome;
		this.valor = valor;
	}

	public String getNome() {
		return nome;
	}

	public void setNome(String nome) {
		this.nome = nome;
	}

	public String getValor() {
		return valor;
	}

	public void setValor(String valor) {
		this.valor = valor;
	}

}
