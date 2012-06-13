package com.block.land;

import org.andengine.entity.sprite.Sprite;
import org.andengine.opengl.texture.region.ITextureRegion;
import org.andengine.opengl.vbo.VertexBufferObjectManager;

public class Block extends Sprite {

	public Block(float pX, float pY, ITextureRegion pTextureRegion,
			VertexBufferObjectManager pVertexBufferObjectManager) {
		super(pX, pY, pTextureRegion, pVertexBufferObjectManager);
		
		
	}
	

	public float x;
	public float y;
	public boolean gravity;
	public int textureIndex;
	public float friction;
	
	//
	//Constructors
	//
	
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
