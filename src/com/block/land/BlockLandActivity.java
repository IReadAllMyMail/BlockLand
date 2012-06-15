package com.block.land;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class BlockLandActivity extends Activity {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        Intent myIntent = new Intent(this,CreateMode.class);
        startActivity(myIntent);
    }
    
    public void restartGame(View view){
    	Intent myIntent = new Intent(this,CreateMode.class);
        startActivity(myIntent);
    }
}