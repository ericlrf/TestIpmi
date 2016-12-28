package br.com.eb.service.ipmi;

/**
 * Classe responsável por tipo de objeto com dados IPMI.
 * 
 * @author Eric Rodrigues (eric@usto.re)
 * @since 22-Dezembro-2016
 */
public class IpmiData {
	String nome;
	String valor;
	String grupo;

	public IpmiData() {
		super();
	}

	public IpmiData(String nome, String valor) {
		super();
		this.nome = nome;
		this.valor = valor;
	}

	public IpmiData(String nome, String valor, String grupo) {
		super();
		this.nome = nome;
		this.valor = valor;
		this.grupo = grupo;
	}

	public String getNome() {
		return nome;
	}

	public void setNome(String nome) {
		this.nome = nome;
	}

	public String getValor() {
		if (valor == null || valor == "") {
			return "not_present";
		}
		return valor;
	}

	public void setValor(String valor) {
		this.valor = valor;
	}

	public String getGrupo() {
		return grupo;
	}

	public void setGrupo(String grupo) {
		this.grupo = grupo;
	}

}
