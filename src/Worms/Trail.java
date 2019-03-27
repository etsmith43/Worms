package Worms;

import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.util.ArrayList;

public class Trail
{
	private ArrayList<Float> trailX;
	private ArrayList<Float> trailY;
	private int size;
	
	public Trail(ArrayList<Float> x, ArrayList<Float> y, int s)
	{
		trailX = x;
		trailY = y;
		size = s;
	}
	
	public void draw(Graphics g)
	{	
		Graphics2D g2 = (Graphics2D) g.create();
		g2.setColor(Color.BLACK);
		int trailRadius = 1;
		for(int i = 0; i < size; i++)
		{
			Shape trail = new Ellipse2D.Float(trailX.get(i), trailY.get(i), trailRadius, trailRadius);
			g2.draw(trail);
			//g.drawOval(trailX.get(i).intValue(), trailY.get(i).intValue(), trailRadius, trailRadius);
		}
	}
}