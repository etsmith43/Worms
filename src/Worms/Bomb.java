package Worms;

import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.*;
import java.lang.Math;
import java.util.ArrayList;
import java.util.List;

public class Bomb extends PhysicsObject
{
	private BufferedImage missle;
	
	public Bomb(float x, float y, float r)
	{
		super(x,y);
		radius = r;
		try
		{
			missle = ImageIO.read(new File("resources/art/missle.png"));
		}
		catch (IOException e) {System.out.println("Could not load img");}
	}
	
	public ArrayList<Float> trailX = new ArrayList<Float>();
	public ArrayList<Float> trailY = new ArrayList<Float>();
	public int index = 0;
	
	public void draw(Graphics g)
	{
		Graphics2D g2d = (Graphics2D) g.create();
		
		if(!stable)
			g2d.rotate(angle, posX, posY);
		
		drawTrail(g);
		g2d.drawImage(missle, (int)posX-(int)radius, (int)posY-missle.getHeight()/2, missle.getWidth(), missle.getHeight(),null);
	}
	
	public void drawTrail(Graphics g)
	{
		Graphics2D g2 = (Graphics2D) g.create();
		
		trailX.add(index, posX);
		trailY.add(index, posY);
		index++;
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
		die = true;
	}
	
	public void setStable(boolean s)
	{
		stable = s;
	}
	
	public ArrayList<Float> getTrailX()
	{
		return trailX;
	}
	
	public ArrayList<Float> getTrailY()
	{
		return trailY;
	}
	
	public int getTrailSize()
	{
		return index;
	}
	
	public int[] destroy(int xc, int yc, int[] map, List<PhysicsObject> objects)
	{
		die = true;
		int radiusOfExplosion = (int)(radius * 2.5f);
		for(PhysicsObject obj : objects)
		{
			if(obj instanceof Worm)
			{
				float posX = obj.getposX() - xc;
				float posY = obj.getposY() - yc;
				float sqrtThis = posX*posX + posY*posY;
				float distance = (float) Math.sqrt((double) sqrtThis);
				
				if(distance < 0.0001f) distance = 0.0001f;
				if(distance < radiusOfExplosion)
				{
					((Worm)obj).takeDamage(distance/radiusOfExplosion);
					obj.setVel((posX/distance)*radiusOfExplosion/4,(posY/distance)*radiusOfExplosion/4);
					obj.setStable(false);
				}
			}
		}
		
		int point = 0;
		int origin = 0;
		
 		//Brute fource approach of checking all points in a circular area
		//Start from negative points because need to go around the origin
		//Then check if those points squared and added are less than the radius^2 and added
		//Then its inside the circle and now we need where the explosion happened and if it was legal or not
		for(int y = -radiusOfExplosion; y <= radiusOfExplosion; y++)
			for(int x = -radiusOfExplosion; x <= radiusOfExplosion; x++)
				if(x * x + y * y < radiusOfExplosion * radiusOfExplosion + radiusOfExplosion)
				{
					//origin of circle
					origin = (xc+yc*width) % width;
					//current point being checked
					point = ((xc+x)+(yc+y)*width) % width;
					
					//If the point if out of bounds break out
					if((xc+x)+(yc+y)*width >= width*height) break;
					
					//If origin and point are similar numbers, both near 300 not 300 and 10, then its ok to change the map
					if((origin <= width/2+20 && point <= width/2+20) || (origin > width/2-20 && point > width/2-20))
						if(map[(xc+x)+(yc+y)*width] == 1) 
						{
							map[(xc+x)+(yc+y)*width] = 0;
						}
				}
		
		/* int x = radiusOfExplosion-1;
		int y = 0;
		int dx = 1;
		int dy = 1;
		int err = dx - (radiusOfExplosion << 1);
		while (x >= y)
		{
			if(map[(xc+x)+(yc+y)*width] == 1)
				map[(xc+x)+(yc+y)*width] = 0;
			if(map[(xc+y)+(yc+x)*width] == 1)
				map[(xc+y)+(yc+x)*width] = 0;
			if(map[(xc-y)+(yc+x)*width] == 1)
				map[(xc-y)+(yc+x)*width] = 0;
			if(map[(xc-x)+(yc+y)*width] == 1)
				map[(xc-x)+(yc+y)*width] = 0;
			if(map[(xc-x)+(yc-y)*width] == 1)
				map[(xc-x)+(yc-y)*width] = 0;
			if(map[(xc-y)+(yc-x)*width] == 1)
				map[(xc-y)+(yc-x)*width] = 0;
			if(map[(xc+y)+(yc-x)*width] == 1)
				map[(xc+y)+(yc-x)*width] = 0;
			if(map[(xc+x)+(yc-y)*width] == 1)
				map[(xc+x)+(yc-y)*width] = 0;

			if (err <= 0)
			{
				y++;
				err += dy;
				dy += 2;
			}
			
			if (err > 0)
			{
				x--;
				dx += 2;
				err += dx - (radiusOfExplosion << 1);
			}
		} */
		

		return map;
	}
}