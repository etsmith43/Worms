package Worms;

import java.awt.*;
import java.util.Vector;
import java.util.ArrayList;
import util.Pair;

public class PhysicsObject
{
	private static int scale = 2;
	public static int width = 720;
	public static int height = 720/scale;
	public float posX = 0.0f;
	public float posY = 0.0f;
	public float velX = 0.0f;
	public float velY = 0.0f;
	public float accX = 0.0f;
	public float accY = 0.0f;
	public int numBounces = 0;
	public boolean die = false;
	public float friction = 0.7f;
	public float angle = 0.0f;
	public float pi = 3.14159f;
	public float radius = 4.0f;
	public boolean stable = false;
	public boolean selected = false;
	
	public PhysicsObject(float x, float y)
	{
		posX = x;
		posY = y;
	}
	
	public void draw(Graphics g)
	{
		
	}
	
	public void onColision()
	{
		
	}
	
	public boolean isSelected()
	{
		return selected;
	}
	
	public void incNumBounces()
	{
		numBounces++;
	}
	
	public int getNumBounces()
	{
		return numBounces;
	}
	
	public void setSelected(boolean s)
	{
		selected = s;
	}
	
	public boolean isDead()
	{
		return die;
	}
	
	public void setAngle(float a)
	{
		angle = a;
	}
	
	public float getFriction()
	{
		return friction;
	}
	
	public float getRadius()
	{
		return radius;
	}
	
 	public void setVel(float x, float y)
	{
		velX = x;
		velY = y;
	}
	
	public void setAcc(float x, float y)
	{
		accX = x;
		accY = y;
	}
	
	public void setPos(float x, float y)
	{
		posX = x ;
		posY = y ;
	}
	
	public float getvelX()
	{
		return velX;
	}
	
	public float getvelY()
	{
		return velY;
	}
	
	public float getposX()
	{
		return posX;
	}
	
	public float getposY()
	{
		return posY;
	}
	
	public float getaccX()
	{
		return accX;
	}
	
	public float getaccY()
	{
		return accY;
	}
	
	public void setStable(boolean s)
	{
		stable = s;
	}
	
	public boolean isStable()
	{
		return stable;
	}
}
