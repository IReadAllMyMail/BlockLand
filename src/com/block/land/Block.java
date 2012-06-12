package com.block.land;

public class Block {

	public float x;
	public float y;
	public boolean gravity;
	public int textureIndex;
	public float friction;
	
	//
	//Constructors
	//
	public Block(float x,float y, boolean gravity,int textureIndex,float friction){
		
	}
	
	//
	//Getters and Setters
	//
	
	public void setX(float x){
		this.x = x;
	}
	
	public float getX(){
		return this.x;
	}
	
	public void setY(float y){
		this.y= y;
	}
	
	public float getY(){
		return this.y;
	}
	
}
