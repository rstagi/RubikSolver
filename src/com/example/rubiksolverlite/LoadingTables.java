package com.example.rubiksolverlite;

import org.kociemba.twophase.PruneTableLoader;

import android.os.AsyncTask;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

public class LoadingTables extends AsyncTask<MainActivity, MainActivity, MainActivity> {
	
	@Override
	protected MainActivity doInBackground(MainActivity... values){
        PruneTableLoader prune = new PruneTableLoader();
        while(!prune.loadingFinished()){
        	prune.loadNext();
        	publishProgress(values[0]);
        }
		return values[0];
	}
	
	@Override
	protected void onProgressUpdate(MainActivity... values){
		super.onProgressUpdate(values);
		ProgressBar bar = (ProgressBar)values[0].findViewById(R.id.progressBar);
		TextView text = (TextView)values[0].findViewById(R.id.loading);
		int progress = bar.getProgress()+9;
		if(progress>100) progress=100;
		bar.setProgress(progress);
		text.setText("Stato caricamento tabelle di pruning: "+progress+"%");
	}
	
	@Override
	protected void onPostExecute(MainActivity result){
		super.onPostExecute(result);
		ProgressBar bar = (ProgressBar)result.findViewById(R.id.progressBar);
		Button but = (Button)result.findViewById(R.id.start);
		TextView text = (TextView)result.findViewById(R.id.loading);
		but.setVisibility(View.VISIBLE);
		bar.setVisibility(View.INVISIBLE);
		text.setVisibility(View.INVISIBLE);
	}

}
