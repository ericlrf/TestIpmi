package br.com.eb.service.ipmi.dados;

import java.util.List;

public abstract class MetricaIpmi {

	List<String> valoresOperacionais;
	
	Double valorMin;
	Double valorMax;
	
	/**
	 * Verifica se valor recebido pelo host 
	 * */
	public void validarValorOperacional(String valorOperacional) {
		for (String vo : valoresOperacionais) {
			if (vo == valorOperacional) {
				
			}
		}
	}
}
