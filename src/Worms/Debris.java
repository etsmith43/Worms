package Worms;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.util.Random;
import java.lang.Math;

public class Debris extends PhysicsObject
{

	public Debris(float x, float y)
	{
		super(x,y);
		radius = 1.0f;
		Random rand = new Random();
		velX = 3.0f * (float)Math.cos(rand.nextFloat() * 2.0f * pi);
		velY = 3.0f * (float)Math.sin(rand.nextFloat() * 2.0f * pi);
	}
	
	public void draw(Graphics g)
	{
		Graphics2D g2d = (Graphics2D) g.create();
		Rectangle2D rect = new Rectangle2D.Float(posX-radius, posY-radius, radius*2, radius);
		Rectangle2D rect2 = new Rectangle2D.Float(posX-radius, posY, radius, radius);
		
		g2d.setColor(new Color(0,128,0));
		g2d.rotate(angle, posX, posY);
		g2d.fill(rect);
		g2d.fill(rect2);
	}
	
	public void setStable(boolean s)
	{
		stable = s;
	}
	
	public void onColision()
	{
		incNumBounces();
		if(numBounces > 5) die = true;
	}
	

}