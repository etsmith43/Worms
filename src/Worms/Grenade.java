package Worms;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Ellipse2D;
import java.util.Random;
import java.lang.Math;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.*;

public class Grenade extends Bomb
{
	private BufferedImage grenade;
	
	public Grenade(float x, float y, float r)
	{
		super(x,y,r);
		radius = r;
		friction = 0.6f;
		try
		{
			grenade = ImageIO.read(new File("resources/art/grenade.png"));
		}
		catch (IOException e) {System.out.println("Could not load img");}
	}
	
	public void draw(Graphics g)
	{
		Graphics2D g2d = (Graphics2D) g.create();
		if(!stable)
			g2d.rotate(angle, posX, posY);
		
		drawTrail(g);
		g2d.drawImage(grenade, (int)posX-(int)radius, (int)posY-grenade.getHeight()/2, grenade.getWidth(), grenade.getHeight(),null);
	}
	
	public void drawTrail(Graphics g)
	{
		Graphics2D g2 = (Graphics2D) g.create();
		
		if(numBounces == 0)
		{
			trailX.add(index, posX);
			trailY.add(index, posY);
			index++;
		}
		g2.setColor(Color.BLACK);
		int trailRadius = 1;
		for(int i = 0; i < index; i++)
		{
			Shape trail = new Ellipse2D.Float(trailX.get(i), trailY.get(i), trailRadius, trailRadius);
			g2.draw(trail);
			//g.drawOval(trailX.get(i).intValue(), trailY.get(i).intValue(), trailRadius, trailRadius);
		}
	}
	
	public void onColision()
	{
		incNumBounces();
		if(numBounces > 3) die = true;
	}
}