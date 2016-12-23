package br.com.eb.service.ipmi;

import java.util.List;
import com.veraxsystems.vxipmi.coding.commands.IpmiCommandCoder;
import com.veraxsystems.vxipmi.coding.commands.PrivilegeLevel;
import com.veraxsystems.vxipmi.coding.commands.ResponseData;
import com.veraxsystems.vxipmi.coding.security.CipherSuite;

/**
 * Interface responsável por conectar, autenticar, enviar e receber dados via
 * IPMI de hosts remotos.
 * 
 * @author Eric Rodrigues (eric@usto.re)
 * @since 22-Dezembro-2016
 */
public interface IpmiService {
	/**
	 * Criar canal de comunicação para um host no protocolo UDP
	 */
	public void createConnection(int port, String address) throws Exception;

	/**
	 * Provê lista de suítes de algoritmos (autenticação, confidencialidade e
	 * integridade) para a conexão
	 */
	public void encryptConnection() throws Exception;

	/**
	 * Autenticar canal de comunicação e inicia conexão com host remoto
	 */
	public void openConnection(String username, String password, byte[] bmcKey) throws Exception;

	/**
	 * Envia comando IPMI e recebe o resultado
	 */
	public ResponseData sendMessage(IpmiCommandCoder commandCoder) throws Exception;

	/**
	 * Encerra sessão na conexão
	 */
	public void closeConnection() throws Exception;

	/**
	 * Encerra canal de comunicação para o host
	 */
	public void releaseConnection() throws Exception;
}