package Worms;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.util.Random;
import java.lang.Math;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.*;

public class MiniBomb extends Bomb
{
	private BufferedImage missle;
	
	public MiniBomb(float x, float y, float r)
	{
		super(x,y,r);
		radius = r;
		
		try
		{
			missle = ImageIO.read(new File("resources/art/miniMissle.png"));
		}
		catch (IOException e) {System.out.println("Could not load img");}
	}
	
	public void draw(Graphics g)
	{
		Graphics2D g2d = (Graphics2D) g.create();
		
		if(!stable)
			g2d.rotate(angle, posX, posY);
		
		g2d.drawImage(missle, (int)posX-(int)radius, (int)posY-missle.getHeight()/2, missle.getWidth(), missle.getHeight(),null);
	}
}