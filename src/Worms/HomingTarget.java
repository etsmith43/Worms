package Worms;

import java.awt.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.*;

public class HomingTarget
{
	//private BufferedImage HomingMissle;
	
	private int targetX, targetY, timeCreated;
	private int size = 5;
	
	public HomingTarget(int x, int y, int tc)
	{
		targetX = x;
		targetY = y;
		timeCreated = tc;
/* 		try
		{
			HomingMissle = ImageIO.read(new File("resources/art/missle.png"));
		}
		catch (IOException e) {System.out.println("Could not load img");} */
	}
	
	public void draw(Graphics g)
	{
		//Graphics2D g2d = (Graphics2D) g.create();
		
		g.setColor(Color.BLACK);
		g.drawRect(targetX-size/2,targetY-size/2,size,size);
		
		//g2d.drawImage(HomingMissle, (int)posX-(int)radius, (int)posY-HomingMissle.getHeight()/2, HomingMissle.getWidth(), HomingMissle.getHeight(),null);
	}
	
	public int getTimeCreated()
	{
		return timeCreated;
	}
	
	public int getTargetX()
	{
		return targetX;
	}
	
	public int getTargetY()
	{
		return targetY;
	}

}