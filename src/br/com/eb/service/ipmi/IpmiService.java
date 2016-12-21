package br.com.eb.service.ipmi;

import java.net.InetAddress;

import com.veraxsystems.vxipmi.api.async.ConnectionHandle;
import com.veraxsystems.vxipmi.api.sync.IpmiConnector;
import com.veraxsystems.vxipmi.coding.commands.PrivilegeLevel;

public interface IpmiService {
	
	/**
	 * Fornece Porta (UDP), IP, Cipher Suite, prvilégio, usuario, senha e 'BMC key' (opcional) para conexão
	 * */
	public void openConnection(int port, String address, PrivilegeLevel privilegeLevel, String username, String password, byte[] bmcKey) throws Exception;

	/**
	 * Envia comando IPMI e recebe o resultado
	 * */
	public void sendMessageChassis() throws Exception;

	public void sendMessageSensors() throws Exception;
	
//	public void sendMessagesModules();
	
//	public void sendMessagesEventos();
	
	/**
	 * Encerra sessão, conexão e conector criados
	 * */
	public void closeConnection() throws Exception;
}