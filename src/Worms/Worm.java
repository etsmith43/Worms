package Worms;

import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.awt.geom.AffineTransform;
import java.io.*;

public class Worm extends PhysicsObject
{
	private boolean jump = false;
	private boolean left = true;
	private BufferedImage img;
	private float shootAngle = 0.0f;
	private float shootAngleX = 0.0f;
	private float shootAngleY = 0.0f;
	private boolean isActivePlayer = false;
	private float power = 0.0f;
	private float health = 100.0f;
	private Trail trail;
	
	public Worm(float x, float y)
	{
		super(x,y);
		try
		{
			img = ImageIO.read(new File("resources/art/worm1.png"));
			radius = img.getWidth()/2;
		}
		catch (IOException e) {System.out.println("Could not load img");}
		friction = 0.5f;
	}
	public void onColision(){}
	public void setTrail(Trail t)
	{
		trail = t;
	}
	
	public boolean isActivePlayer()
	{
		return isActivePlayer;
	}
	
	public void setActivePlayer(boolean b)
	{
		isActivePlayer = b;
	}
	
	public void takeDamage(float percent)
	{
		float min = 5;
		float max = 50;
		
		health = health - ((1.0f - percent) * (max - min) + min);
		System.out.println("health: " + health);
	}
	
	public float getHealth()
	{
		return health;
	}
	
	public void draw(Graphics g)
	{
		shootAngleX = (radius*2.0f) * (float)Math.cos(shootAngle) + posX;
		shootAngleY = (radius*2.0f) * (float)Math.sin(shootAngle) + posY;
		
		int wormX = (int)posX-(int)radius;
		int wormY = (int)posY-(int)radius;
		
		if(trail != null)
			trail.draw(g);
		
		if(power > 0)
		{
			int green = 1;
			if(power > 10) power = 10;
			if(power > 1) green = (int)power;
			
			g.setColor(new Color(25*(int)power,255/green,0));
			g.fillRect(wormX-3, wormY-4,green*2,3);
		}
		
		//g.drawLine((int)wormX,(int)wormY,(int)wormX,(int)wormY);
		
		if(left)
			g.drawImage(img, wormX, wormY, img.getWidth(),img.getHeight(),null);
		else
			g.drawImage(img, (int)posX+(int)radius, wormY,-img.getWidth(),img.getHeight(),null);
		
		//Border
		g.setColor(Color.BLACK);
		g.drawRect(wormX-(int)radius-1, wormY-10,img.getWidth()*2+1,5);
		//HealthBar
		g.setColor(Color.GREEN);
		float drawnHealth = health*0.01f * (img.getWidth()*2);
		g.fillRect(wormX-(int)radius, wormY-9,(int)drawnHealth,4);
		
		if(isActivePlayer)
		{
			g.setColor(Color.WHITE);
			g.drawRect((int)shootAngleX,(int)shootAngleY,1,2);
		}
	}
	
	public void firing(float p)
	{
		power = p;
	}
	
	public void update(int movement)
	{
		if(velX == 0 && velY == 0)  jump = false;
		switch (movement)
		{
			case 1: //Left
				if(stable || jump)
				{
					left = true;
					velX = -2;
					if(jump == false)
						velY = -1.5f;
					stable = false;
				}
				break;
			case 2: //Right
				if(stable || jump)
				{
					left = false;
					velX = 2;
					if(jump == false)
						velY = -1.5f;
					stable = false;
				}
				break;
			case 3: //Jump
				if(stable)
				{
					jump = true;
					velY = 0;
					velY = -3;
					stable = false;
				}
				break;
			case 4:
				if(stable)//E
				{
					shootAngle += pi/50.0f;
					if(shootAngle > 0)
						shootAngle = 0;
				}
				break;
			case 5:
				if(stable)//Q
				{
					shootAngle -= pi/50.0f;
					if(shootAngle < -pi)
						shootAngle = -pi;
				}
				break;
		}
	}
	
	public float getShootAngleX()
	{
		return shootAngleX;
	}
	
	public float getShootAngleY()
	{
		return shootAngleY;
	}
	
	public Bomb shoot(int selection)
	{
		Bomb bomb;
		switch (selection)
		{
			case 2: //ClusterBomb
				bomb = new ClusterBomb(getShootAngleX(),getShootAngleY(), 5.0f);
				break;
			case 3: //Grenade
				bomb = new Grenade(getShootAngleX(),getShootAngleY(), 7.0f);
				break;
			case 4: //HomingMissle
				bomb = new HomingMissle(getShootAngleX(),getShootAngleY(), 6.0f);
				break;
			default: //Bomb
				bomb = new Bomb(getShootAngleX(),getShootAngleY(), 8.0f);
				break;
		}

		bomb.setVel(power*(float)Math.cos(shootAngle),power*(float)Math.sin(shootAngle));
		power = 0.0f;
		return bomb;
	}
	
}