package Worms;

import java.awt.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.*;

public class HomingMissle extends Bomb
{
	private BufferedImage HomingMissle;
	private HomingTarget ht;
	
	public HomingMissle(float x, float y, float r)
	{
		super(x,y,r);
		radius = r;
		try
		{
			HomingMissle = ImageIO.read(new File("resources/art/missle.png"));
		}
		catch (IOException e) {System.out.println("Could not load img");}
	}
	
	public void setHomingTarget(HomingTarget h)
	{
		ht = h;
	}
	
	public HomingTarget getHomingTarget()
	{
		return ht;
	}
	
	public void draw(Graphics g)
	{
		Graphics2D g2d = (Graphics2D) g.create();
		
		if(!stable)
			g2d.rotate(angle, posX, posY);
		
		//drawTrail(g);
		g2d.drawImage(HomingMissle, (int)posX-(int)radius, (int)posY-HomingMissle.getHeight()/2, HomingMissle.getWidth(), HomingMissle.getHeight(),null);
	}
	
	public void mouseAngle(int mouseX, int mouseY)
	{
		float angle = (float) Math.atan2((double) (mouseY-posY),(double)(mouseX-posX));
		setVel(5*(float)Math.cos(angle),5*(float)Math.sin(angle));
	}
}