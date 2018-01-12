package com.example.rubiksolverlite;

import java.util.Calendar;
import java.util.Date;

import org.kociemba.twophase.PruneTableLoader;
import org.kociemba.twophase.Search;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
    private CubeParsing cube;    
    private Camera mCamera;
    private ProgressBar bar;
    private int progress;
    
    private long start;
    
    private SQLiteDatabase db;
    
    private PictureCallback mPicture = new PictureCallback() {

        public void onPictureTaken(byte[] data, Camera camera) {
    		Bitmap img = BitmapFactory.decodeByteArray(data , 0, data.length);
        	cube.parseImage(img);
        	img.recycle();
        }
    };

	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.activity_main);
        
        cube = new CubeParsing(this);

        mCamera = null;
        
        db = null;
        
        try{
        	db = this.openOrCreateDatabase("RubikRecord", MODE_PRIVATE, null);
        	db.execSQL("create table if not exists record "
        			+ "(_id integer primary key autoincrement, "
        			+ "nMosse integer not null, "
        			+ "tempo integer not null, "
        			+ "data text not null);");
        }catch(Exception e){}
        
        updateListaRecord();
	}
	
	public void progress(){
		progress+=9;
		if(progress>100) progress=100;
		bar.setProgress(progress);
	}
	
	public void startTimer(){
		start = SystemClock.uptimeMillis(); //assegno il valore in millisecondi a start
	}
	public void stopTimer(int nMosse){
		long tempo = SystemClock.uptimeMillis() - start; //calcolo del tempo in millisecondi
		Calendar c = Calendar.getInstance(); //istanza del calendario
		//creazione della data sotto forma di stringa
		String data = c.get(Calendar.DATE)+"/"+(c.get(Calendar.MONTH)+1)+"/"+c.get(Calendar.YEAR);
		//esecuzione della query
		db.execSQL("INSERT INTO record VALUES (null, "+nMosse+", "+tempo+", '"+data+"');");
		updateListaRecord(); //aggiornamento a video dei record
	}
	public void updateListaRecord(){
		TextView listaTempi = (TextView)findViewById(R.id.listaTempi); //istanza del TextView
		//query per selezionare i primi 5 record
		Cursor result = db.rawQuery("SELECT * FROM record ORDER BY tempo LIMIT 5", null);
		String testo = "\n\nRecord precedenti:\n\n";
		int cont=1;
		while(result.moveToNext()){ //scandisco il risultato della query e concateno i valori al testo
			testo += cont+". ";
			testo += result.getString(result.getColumnIndex("data"));
			testo += "; mosse="+result.getString(result.getColumnIndex("nMosse"));
			long tempo = result.getLong(result.getColumnIndex("tempo"));
			int sec = (int)tempo/1000;
			int min = sec/60;
			sec = sec%60;
			int ms = (int)tempo%1000;
			testo +="; tempo = "+min+"min : "+sec+"sec : "+ms+"ms;\n\n";
			cont++;
		}
		result.close(); //chiudo il risultato
		listaTempi.setText(testo); //setto il testo alla TextView
	}
	
	public void nextPicture(){
		mCamera.takePicture(null, null, mPicture);
	}
	
	public void start(View view){
		if(cube.connectToHand()){
			try{
				mCamera = Camera.open();
				mCamera.startPreview();
			}catch(Exception e){
				if(mCamera==null)
					Toast.makeText(this, "Fotocamera non disponibile.", Toast.LENGTH_LONG).show();
			}
			if(mCamera!=null){
				((Button)findViewById(R.id.start)).setVisibility(View.INVISIBLE);
				ProgressBar bar = (ProgressBar)findViewById(R.id.progressBar);
				TextView text = (TextView)findViewById(R.id.loading);
				bar.setProgress(0);
				bar.setVisibility(View.VISIBLE);
				text.setText("Sto risolvendo il cubo... Acquisizione faccia 1.");
				text.setVisibility(View.VISIBLE);
				nextPicture();
			}
		}else{
			if(view==null)
				Toast.makeText(this, "Problemi nella connessione Bluetooth.", Toast.LENGTH_LONG).show();
			else{
				if(!BluetoothAdapter.getDefaultAdapter().enable()){
					Intent enableBT = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
					startActivity(enableBT);
				}
			}
		}
	}
	
	public void onActivityResult(int a, int b, Intent c){
		start(null);
	}
	
	public static int massimo(int[] vet){
		int m = vet[0], pos=0;
		for(int i=1; i<vet.length; i++)
			if(vet[i]>m) m=vet[pos=i];
		return pos;
	}
	
	public void startTimer(View v){
		startTimer();
		((Button)findViewById(R.id.startTimer)).setVisibility(View.INVISIBLE);
		((Button)findViewById(R.id.stopTimer)).setVisibility(View.VISIBLE);
	}
	
	public void stopTimer(View v){
		stopTimer(15);
		((Button)findViewById(R.id.stopTimer)).setVisibility(View.INVISIBLE);
		((Button)findViewById(R.id.startTimer)).setVisibility(View.VISIBLE);
	}
}
