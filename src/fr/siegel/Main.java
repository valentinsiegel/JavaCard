package fr.siegel;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.Socket;

import com.sun.javacard.apduio.Apdu;
import com.sun.javacard.apduio.CadT1Client;
import com.sun.javacard.apduio.CadTransportException;

public class Main {
	
	public static final byte[] APPLET_AID = { 0x01, 0x02, 0x03, 0x04, 0x05, 0x06,0x07, 0x08, 0x09, 0x00, 0x00 };
	
	private static final byte FIDELITE_CLA = (byte) 0xB0;
	private static final byte INS_OBTENIR_SOLDE = (byte) 0x01;
	private static final byte INS_AJOUTER_SOLDE = (byte) 0x02;
	private static final byte INS_AJOUTER_SOLDE_AVEC_SOLDE = (byte) 0x03;
	
	public static void main(String[] args) {
		try {
			
			Socket sckClient=new Socket("localhost",9025); 
			sckClient.setTcpNoDelay(true); 
			BufferedInputStream input;
			input = new 	BufferedInputStream(sckClient.getInputStream());
			BufferedOutputStream output=new BufferedOutputStream(sckClient.getOutputStream());
			
			//Create card
			CadT1Client cad= new CadT1Client(input,output);
			
			//Card power up
			cad.powerUp();
			System.out.println("Card powered up"); 	
			
			selectAppletInstaller(cad);
			installApplet(cad);
			selectApplet(cad);
			getAmount(cad);
			addFundsAndGetNewAmount(cad, 10);
			addFundsAndGetNewAmount(cad, 15);
			
			cad.powerDown();
			

		} catch (IOException e) {
			
			// TODO Auto-generated catch block
			e.printStackTrace();
			
		} catch (CadTransportException e) { 
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	}


	public static void selectAppletInstaller(CadT1Client cad){
		
		// Select the installer applet
		Apdu apdu = new Apdu(); 
		apdu.command[Apdu.CLA] = 0x00; 
		apdu.command[Apdu.INS] = (byte) 0xA4; 
		apdu.command[Apdu.P1] = 0x04; 
		apdu.command[Apdu.P2] = 0x00;
		
		byte[] data = { (byte) 0xa0, 0x00, 0x00, 0x00, 0x62, 0x03, 0x01, 0x08, 0x01 };
		apdu.setDataIn(data);
		
		// Sends data 
		try {
			cad.exchangeApdu(apdu);
			System.out.println("Applet installer selected"); 	
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (CadTransportException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 	
		
		// Error check
		if (apdu.getStatus() != 0x9000) { 
			System.out.println("Error selecting applet installer"); 	
			System.exit(1); 
		}
	}
	
	public static void installApplet(CadT1Client cad){
		// Install the applet
		Apdu apdu = new Apdu(); 
		apdu.command[Apdu.CLA] = (byte) 0x80; 
		apdu.command[Apdu.INS] = (byte) 0xB8; 
		apdu.command[Apdu.P1] = 0x00; 
		apdu.command[Apdu.P2] = 0x00;
		
		byte[] data = { 0xb, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09, 0x00, 0x00, 0x00 };
		apdu.setDataIn(data);
		
		// Sends data 
		try {
			cad.exchangeApdu(apdu);
			System.out.println("Applet installed"); 	
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (CadTransportException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		
		if (apdu.getStatus() != 0x9000) { 
			System.out.println("Error Installing applet"); 	
			System.exit(1); 
		}
	}
	
	public static void selectApplet(CadT1Client cad){
		// Select applet
		Apdu apdu = new Apdu(); 
		apdu.command[Apdu.CLA] = 0x00; 
		apdu.command[Apdu.INS] = (byte) 0xA4; 
		apdu.command[Apdu.P1] = 0x04; 
		apdu.command[Apdu.P2] = 0x00;
		
		byte[] data = APPLET_AID;
		apdu.setDataIn(data);
		
		// Sends data 
		try {
			cad.exchangeApdu(apdu);
			System.out.println("Applet selected"); 	
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (CadTransportException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		
		if (apdu.getStatus() != 0x9000) { 
			System.out.println("Error selecting applet"); 	
			System.exit(1); 
		}
	}
	
	public static void getAmount(CadT1Client cad){
		
		// return amount
		Apdu apdu = new Apdu(); 
		apdu.command[Apdu.CLA] = FIDELITE_CLA; 
		apdu.command[Apdu.INS] = INS_OBTENIR_SOLDE; 
		apdu.command[Apdu.P1] = 0x00; 
		apdu.command[Apdu.P2] = 0x00;
		
		// lenght of answer
		apdu.setLe(0x02);
		// Sends data 
		try {
			cad.exchangeApdu(apdu);
			System.out.println("Request to get amount"); 	
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (CadTransportException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		
		if (apdu.getStatus() != 0x9000) { 
			System.out.println("Errror requesting amount"); 	
			System.exit(1); 
		} else {
			System.out.println("Valeur : " +apdu.dataOut[0]);
		}
	}
	
	
	private static void addFundsAndGetNewAmount(CadT1Client cad, int i) {

		// Adds funds then return amount
		Apdu apdu = new Apdu(); 
		apdu.command[Apdu.CLA] = FIDELITE_CLA; 
		apdu.command[Apdu.INS] = INS_AJOUTER_SOLDE_AVEC_SOLDE; 
		apdu.command[Apdu.P1] = 0x00; 
		apdu.command[Apdu.P2] = 0x00;
		
		// lenght of answer
		apdu.setLe(0x02);
		byte[] data = { 0x00, (byte) i };
		apdu.setDataIn(data);
		
		// Sends data 
		try {
			cad.exchangeApdu(apdu);
			System.out.println("Request to get amount"); 	
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (CadTransportException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		
		if (apdu.getStatus() != 0x9000) { 
			System.out.println("Errror requesting amount"); 	
			System.exit(1); 
		} else {
			System.out.println("Valeur : " +apdu.dataOut[1]);
		}
	}
}
