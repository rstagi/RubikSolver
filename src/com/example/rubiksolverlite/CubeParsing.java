package rubiksolverlite;

import org.kociemba.twophase.PruneTableLoader;
import org.kociemba.twophase.Search;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class CubeParsing {
    private static final int RED = 0, WHITE = 1, BLUE = 2, GREEN = 3, YELLOW = 4, ORANGE = 5, BLACK = 6, UNDEFINED = 7;
	private MainActivity callingActivity;
    private int parsedIndex;
    private int[][] facelet;
    private Hand bluetoothHand;
	
	
	public CubeParsing(MainActivity cA){
		callingActivity = cA;
		
		parsedIndex=0;
        
        facelet = new int[6][9];
        for(int i=0; i<6; i++)
        	for(int j=0; j<9; j++)
        		facelet[i][j] = UNDEFINED;
        
        Toast.makeText(callingActivity, "Caricamento tabelle di pruning.", Toast.LENGTH_SHORT).show();

        new LoadingTables().execute(callingActivity);
        
        bluetoothHand = new Hand(this, callingActivity);
	}
	
	public boolean connectToHand(){
		return bluetoothHand.connect();
	}
	
	
	public int colourOf(int pixel){
	    float[] hsv = new float[3];
	    float h, s, l;
	    Color.colorToHSV(pixel, hsv); //conversione a HSV
	    //conversione da HSV a HSL
	    h = hsv[0];
	    l = (2-hsv[1])*hsv[2];
	    s = hsv[1]*hsv[2];
	    s /= (l<=1) ? l : (2-l);
	    l/=2;
	    l*=100;
	    s*=100;
	    //rilevazione colori
	    if(s<50)
	    	if(l<20) return BLACK;
	    	else if(l>70) return WHITE;
	    if(l<15) return BLACK;
	    if(l>80) return WHITE;
	    if(45<=h&&h<=72) return YELLOW;
	    if(73<=h&&h<=190) return GREEN;
	    if(200<=h&&h<=260) return BLUE;
	    if(17<=h&&h<=45) return ORANGE;
	    if(h<=16||h>=338) return RED;
	    return UNDEFINED;
	}	
	
	
	protected void parseImage(Bitmap img){
		//valori di coordinate da cui iniziano le caselle
		int[] inizio = {450, 1050, 1650};
		
		//rilevazione dei colori delle 9 caselle
		for(int i=0, inizioX, inizioY; i<9; i++){
			int[] cont = new int[8];
			inizioX = inizio[(i%3)];
			inizioY = inizio[(i/3)];
			for(int j=0; j<8; j++)
				cont[j] = 0;
			
			//Rilevazione statistica
			//Il colore rilevato più frequentemente in un'area di 10000px
			//sarà il colore della casella
			for(int x=0; x<100; x++)
				for(int y=0; y<100; y++)
					cont[colourOf(img.getPixel(inizioX+x, inizioY+y))]++;
			facelet[parsedIndex][i] = MainActivity.massimo(cont);
		}
		parsedIndex++; //incremento l'indice della faccia
		if(parsedIndex<6){
			//incremento progresso
			ProgressBar bar = (ProgressBar)callingActivity.findViewById(R.id.progressBar);
			TextView text = (TextView)callingActivity.findViewById(R.id.loading);
			bar.setProgress(parsedIndex*10);
			text.setText("Sto risolvendo il cubo... Acquisizione faccia "+(parsedIndex+1)+".");
			//chiamata alla funzione per ruotare il cubo alla prossima faccia
			bluetoothHand.moveHand(parsedIndex);
		}else
			parseCube(); //se sono acquisite tutte, analizzo il cubo in generale
	}

	public void nextPicture(){
		callingActivity.nextPicture();
	}
	
	protected void parseCube(){
		StringBuffer s = new StringBuffer(54);
		ProgressBar bar = (ProgressBar)callingActivity.findViewById(R.id.progressBar);
		TextView text = (TextView)callingActivity.findViewById(R.id.loading);
		bar.setProgress(60);
		text.setText("Sto risolvendo il cubo... Calcolo procedimento risolutivo.");
		for (int i = 0; i < 54; i++)
			s.insert(i, 'B');
		for(int i=0; i<6; i++)
			for(int j=0; j<9; j++)
				if (facelet[i][j] == facelet[0][4])
					s.setCharAt(9 * i + j, 'U');
				else if (facelet[i][j] == facelet[1][4])
					s.setCharAt(9 * i + j, 'R');
				else if (facelet[i][j] == facelet[2][4])
					s.setCharAt(9 * i + j, 'F');
				else if (facelet[i][j] == facelet[3][4])
					s.setCharAt(9 * i + j, 'D');
				else if (facelet[i][j] == facelet[4][4])
					s.setCharAt(9 * i + j, 'L');
				else if (facelet[i][j] == facelet[5][4])
					s.setCharAt(9 * i + j, 'B');
		String 	cube = s.toString(),
		result = Search.solution(cube, 25, 5, false);
		if (result.contains("Error")){
			bar.setProgress(0);
			text.setText(displayError(result));
		}else{
			bar.setProgress(80);
			text.setText("Sto risolvendo il cubo... Applicazione mosse.");
			bluetoothHand.notationToCommand(result);
			bluetoothHand.closeConnections();
			bar.setProgress(100);
			text.setText("Cubo risolto! Pronto per un'altra combinazione da risolvere.");
		}
		parsedIndex=0;
		((Button)callingActivity.findViewById(R.id.start)).setVisibility(View.VISIBLE);
	}
	
	public String displayError(String result){
		switch (result.charAt(result.length() - 1)) {
		case '1':
			result = "Non ci sono esattamente 9 caselle dello stesso colore.";
			break;
		case '2':
			result = "Non tutti i 12 spigoli esistono una volta sola.";
			break;
		case '3':
			result = "Uno spigolo dev'essere invertito.";
			break;
		case '4':
			result = "Non tutti gli 8 angoli esistono una volta sola.";
			break;
		case '5':
			result = "Un angolo dev'essere ruotato.";
			break;
		case '6':
			result = "Due angoli o due spigoli devono essere scambiati.";
			break;
		case '7':
			result = "Non esiste soluzione per il massimo numero di mosse richiesto.";
			break;
		case '8':
			result = "Timeout!";
			break;
		}
		result += " Riprovare.";
		return result;
	}
}


