package com.example.rubiksolverlite;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.widget.Toast;

public class Hand {
	private CubeParsing cube;
	private MainActivity callingActivity;
	private BluetoothAdapter mBluetoothAdapter;
	private BluetoothSocket btSocket;
	private BluetoothDevice mDevice;
	private OutputStream output;
	private InputStream input;
	
	public Hand(CubeParsing c, MainActivity cA){
		cube = c;
		callingActivity = cA;
		
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
	}

	public boolean connect() {
		boolean b=true;
		if(!mBluetoothAdapter.enable())
			return false;
		Set<BluetoothDevice> devices = mBluetoothAdapter.getBondedDevices();
        if(devices.size() > 0)
        {
            for(BluetoothDevice device : devices)
            {
                if(device.getName().equals("RubikSolverBT"))
                {
                    mDevice = device;
                    break;
                }
            }
        }
        if(mDevice==null){
        	Toast.makeText(callingActivity, "Dispositivo non trovato.", Toast.LENGTH_LONG).show();
        	return false;
        }
        	
		UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb"); //Porta seriale standard
		try {
            btSocket = mDevice.createRfcommSocketToServiceRecord(uuid);
			btSocket.connect();
			output = btSocket.getOutputStream();
			input = btSocket.getInputStream();
		} catch (IOException e) {
			try {
				btSocket.close();
			} catch (IOException e2) {
				Toast.makeText(callingActivity, "Impossibile terminare la connessione Bluetooth.", Toast.LENGTH_SHORT).show();
			}
			Toast.makeText(callingActivity, "Creazione Socket fallita.", Toast.LENGTH_SHORT).show();
			b=false;
		}
		return b;
	}
	
	public void moveHand(int parsedIndex){
		/*TELL Arduino what to do via Bluetooth by checking parsedIndex*/
		switch(parsedIndex){
		case 1:
			sendToArduino(Convertions.get("UR"));
			break;
		case 2:
			sendToArduino(Convertions.get("RF"));
			break;
		case 3:
			sendToArduino(Convertions.get("FD"));
			break;
		case 4:
			sendToArduino(Convertions.get("DL"));
			break;
		case 5:
			sendToArduino(Convertions.get("LB"));
			break;
		}
		
		//Take next picture
		cube.nextPicture();
	}
	
	public void sendToArduino(String command){
		/*Code to send the string "command" to Arduino via Bluetooth*/
		command+=".";
		byte[] message = command.getBytes();
		try {
			output.write(message);
		} catch (IOException e) {
			Toast.makeText(callingActivity, "Errore nell'invio dei dati via Bluetooth.", Toast.LENGTH_LONG).show();
		}
		receiveOkFromArduino();
	}
	
	//svuota il buffer appena è disponibile
	public String receiveFromArduino(){
		String result = "";
		try{
			while(input.available()==0);
			if(input.available()>0)
				while(input.available()>0){
					result += (char)input.read();
					Thread.sleep(10);
				}
		}catch(Exception e){ }
		return result;
	}
	
	public void receiveOkFromArduino(){
		/*Code to receive the "OK" from Arduino via Bluetooth*/
		String ok="";
		while(!ok.contains("OK"))
			ok+=receiveFromArduino();
	}
	
	public void notationToCommand(String notation){
		String[] parts = notation.split(" ");
		sendToArduino(Convertions.get("BU")); //Comando iniziale: Back->Up
		
		for(int i=0; i<parts.length; i++)
			sendToArduino(Convertions.get(parts[i]));
		
		closeConnections();
		
		callingActivity.stopTimer(parts.length);
	}
	
	public void closeConnections(){
		try{
			input.close();
			output.close();
		}catch(IOException e){
		}
	}
}
