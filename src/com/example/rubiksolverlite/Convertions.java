package rubiksolverlite;

import java.util.HashMap;

public abstract class Convertions {
	private static HashMap<String,String> convertions = new HashMap<String,String>();
	private static void setMap(){
		//Conversioni in mosse per acquisire le facce
		convertions.put("UR", "BpLaPlbOpLBP");	 	//Up -> Right
		convertions.put("RF", "LoPlbOpLBaPlbp"); 	//Right -> Front
		convertions.put("FD", "lOpLBP"); 			//Front -> Down
		convertions.put("DL", "LoPlbOpLBaPlbp"); 		//Down -> Left
		convertions.put("LB", "LoPlbOpLBaPlbp"); 	//Left -> Back
		
		//Ritorno alla posizione standard
		convertions.put("BU", "LoPlbpLoPlbOpLBP"); 	//Back -> Up
		
		/* Conversioni per la notazione.
		 * Right e Down sono le rotazioni standard che non comportano
		 * una rotazione completa del cubo. Queste verranno quindi usate
		 * anche per le altre 4 facce.
		 */
		//Right (base)
		convertions.put("R", "OLBP");				//orario
		convertions.put("R'", "ALBP");				//antiorario
		convertions.put("R2", get("R")+get("R"));	//180°
		//Down (base)
		convertions.put("D","olbp");				//orario
		convertions.put("D'", "albp");				//antiorario
		convertions.put("D2", get("D")+get("D"));	//180°
		
		//Inizio mosse non base
		String inizio="", fine=""; //mosse iniziali e finali
		//Front (spostamenti iniziali per averla al posto di Down)
		inizio = "lApLBP"; fine = "lOpLBP";
		convertions.put("F", inizio+get("D")+fine);		//orario
		convertions.put("F'", inizio+get("D'")+fine);	//antiorario
		convertions.put("F2", inizio+get("D2")+fine);	//180°
		//Left (spostamenti iniziali per averla al posto di Right)
		inizio = "LoPlbpLoPlbp"; fine = "LaPlbpLaPlbp";
		convertions.put("L", inizio+get("R")+fine); 	//orario
		convertions.put("L'", inizio+get("R'")+fine); 	//antiorario
		convertions.put("L2", inizio+get("R2")+fine); 	//180°
		//Back (spostamenti iniziali per averla al posto di Right)
		inizio = "LaPlbp"; fine = "LoPlbp";
		convertions.put("B", inizio+get("R")+fine); 	//orario
		convertions.put("B'", inizio+get("R'")+fine); 	//antiorario
		convertions.put("B2", inizio+get("R2")+fine); 	//180°
		//Up (spostamenti iniziali per averla al posto di Down)
		inizio = "lApLBPlApLBP"; fine = "lOpLBPlOpLBP";
		convertions.put("U", inizio+get("D")+fine);		//orario
		convertions.put("U'", inizio+get("D'")+fine);	//antiorario
		convertions.put("U2", inizio+get("D2")+fine);	//180°
		
	}
	
	
	public static String get(String key){
		if(convertions.size()==0)
			setMap();
		return convertions.get(key);
	}
}
