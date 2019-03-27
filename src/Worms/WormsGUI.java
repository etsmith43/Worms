package Worms;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.io.*;
import javax.swing.JPanel;
import javax.swing.JFrame;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.ArrayList;
import java.util.Random;
import java.awt.Rectangle;
import javax.imageio.ImageIO;
import java.lang.Math;
import util.QueueLinked;

public class WormsGUI extends Canvas implements ActionListener, KeyListener, Runnable, MouseMotionListener, MouseListener, MouseWheelListener
{
	private static int windowScale = 2;
	private static float scaleX = 1;
	private static float scaleY = 1;
	private static int width = 720;
	private static int height = 720/windowScale;
	private static int windowWidth = 1080;
	private static int windowHeight = 720;
	private int screenHeight = height;
	private int screenWidth = width;	
	private boolean running;
	private Thread thread;
	private JFrame jf;
	
	private int offsetX = 0;
	private int offsetY = 0;
	private int mouseX = 0;
	private int mouseY = 0;
	private float startPanX = 0.0f;
	private float startPanY = 0.0f;
	private int targetOffsetX = 0;
	private boolean mousePressed = false;
	private boolean mouseReleased = false;
	private boolean mouseClicked = false;
	private int selection = 1;
	
	private float gravity = 0.2f;
	private float pi = 3.14159f;
	private int movement = 0;
	private boolean shoot = false;
	private float power = 0.0f;
	private boolean inc = true;
	private int count = 0;
	private boolean startGame = false;

	private BufferedImage missle;
	private BufferedImage cluster;
	private BufferedImage img_1;
	private BufferedImage img_2;
	private BufferedImage img_3;
	private BufferedImage grenade;
	private BufferedImage homing;
	private BufferedImage img = new BufferedImage(width,height,BufferedImage.TYPE_INT_RGB);
	private BufferedImage oldImg = new BufferedImage(width,height,BufferedImage.TYPE_INT_RGB);
	private Graphics g = img.createGraphics();
	private Worm activeWorm;
	private Trail trail;
	private HomingTarget ht;
	
	private List<PhysicsObject> objects = new CopyOnWriteArrayList<PhysicsObject>();
	private int[] map = new int[width*height];
	private float[] initialSurface = new float[width];
	private float[] surface = new float[width];
	private int[] pixels = ((DataBufferInt)img.getRaster().getDataBuffer()).getData();
	private int[] oldPixels = ((DataBufferInt)oldImg.getRaster().getDataBuffer()).getData();
	private QueueLinked<Worm> wormList = new QueueLinked<Worm>();
	
	public WormsGUI()
	{
		super();
		try
		{
			missle = ImageIO.read(new File("resources/art/missle_button.png"));
			cluster = ImageIO.read(new File("resources/art/cluster_button.png"));
			img_1 = ImageIO.read(new File("resources/art/1_button.png"));
			img_2 = ImageIO.read(new File("resources/art/2_button.png"));
			img_3 = ImageIO.read(new File("resources/art/3_button.png"));
			grenade = ImageIO.read(new File("resources/art/grenade_button.png"));
			homing = ImageIO.read(new File("resources/art/homingMissle_button.png"));
		}
		catch (IOException e) {System.out.println("Could not load img");}
		
		setPreferredSize(new Dimension(windowWidth,windowHeight));
		setFocusable(true);
		requestFocus();
		setBackground(Color.BLACK);
		jf = new JFrame();
		
		createMap();
		redrawMap();
	}
	
	public synchronized void start()
	{
		thread = new Thread(this);
		thread.start();
	}
	
	public static void main(String[] args)
	{
		WormsGUI w = new WormsGUI();
		w.jf.setResizable(false);
		w.jf.setTitle("Worms");
		w.jf.add(w);
		w.jf.pack();
		w.jf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		w.jf.setVisible(true);

		w.start();
	}
	
	public void addNotify()
	{
		super.addNotify();

		addKeyListener(this);
		addMouseMotionListener(this);
		addMouseListener(this);
		addMouseWheelListener(this);
	}
	
	public void run()
	{
		running = true;

		long lastTime = System.nanoTime();
		long timer = System.currentTimeMillis();
		final double ns = 1000000000.0 / 60.0;
		double delta = 0;
		int frames = 0;
		int updates = 0;
		long now = 0;
		
		while(running)
		{
			mouseX=(MouseInfo.getPointerInfo().getLocation().x-this.getLocationOnScreen().x)/windowScale;
			mouseY=(MouseInfo.getPointerInfo().getLocation().y-this.getLocationOnScreen().y)/windowScale;
			
			now = System.nanoTime();
			delta += (now - lastTime) / ns;
			lastTime = now;
			while(delta >= 1)
			{
				gameUpdate();
				//gameRender();
				updates++;
				delta--;
			}
			gameRender();
			gameDraw();
			frames++;
				
			if(System.currentTimeMillis() - timer > 1000)
			{
				timer += 1000;
				jf.setTitle("Worms - " + "UPS: " + updates + " , FPS: " + frames + " , Objects: " + objects.size() + " , Worms: " + wormList.size());
				
				if(startGame) gameStart();
				
				updates = 0;
				frames = 0;
			}

		}
		
	}
	
	private void gameUpdate()
	{
		boolean collision = false;
		float responseX = 0.0f;
		float responseY = 0.0f;
		
		//Update Physics for all objects
		for(PhysicsObject obj : objects)
		{	
			collision = false;
			
			//Apply gravity
			obj.setAcc(0.0f,gravity);
			
			//Update velocity
			float velX = obj.getvelX() + obj.getaccX();
			float velY = obj.getvelY() + obj.getaccY();
			obj.setVel(velX, velY);
			
			//Update position
			float potentialposX = obj.getposX() + obj.getvelX();
			float potentialposY = obj.getposY() + obj.getvelY();

			//Reset Accel
			obj.setAcc(0.0f,0.0f);
			obj.setStable(false);		
			
			//Check for collision, first get the angle with respect to both velocities
			float angle = (float) Math.atan2((double) velY,(double) velX);
			obj.setAngle(angle);
			
			if(obj instanceof HomingMissle)
			{
				HomingMissle hm = (HomingMissle)obj;
				HomingTarget ht = hm.getHomingTarget();
				if(ht.getTimeCreated() < count){System.out.println("ht: " + ht.getTimeCreated());
				System.out.println("count: " + count);
				((HomingMissle)obj).mouseAngle(ht.getTargetX(),ht.getTargetY());}
			}
			
			//Calculate points around object to check for collision (unit circle)
			//pi/2 because only need to check half
			//pi/8 to make 8 points on the circle
			for(float r = angle - pi / 2.0f; r <= angle + pi / 2.0f; r += pi / 9.0f)
			{
				//Get the points around the circle
				float cirPointsX = obj.getRadius() * (float)Math.cos(r) + potentialposX;
				float cirPointsY = obj.getRadius() * (float)Math.sin(r) + potentialposY;
				
				//g.setColor(Color.BLACK);
				//g.drawLine((int)cirPointsX,(int)cirPointsY,(int)cirPointsX,(int)cirPointsY);
				
				//Don't get points out of bounds
				if (cirPointsX >= width) cirPointsX = width - 1;
				if (cirPointsY >= height) cirPointsY = height - 1;
				if (cirPointsX < 0) cirPointsX = 0;
				if (cirPointsY < 0) cirPointsY = 0;
				
				//Check the position for ground collision
				if (map[(int)cirPointsX + (int)cirPointsY * width] != 0)
				{
					responseX += potentialposX - cirPointsX;
					responseY += potentialposY - cirPointsY;
					collision = true;
				}
			}
			
			//Add up all the response points
			float sqrtThis = responseX*responseX + responseY*responseY;
			float magResponse = (float) Math.sqrt((double) sqrtThis);
			
			sqrtThis = velX*velX + velY*velY;
			float velMag = (float) Math.sqrt((double) sqrtThis);
			
			if(collision)
			{
				obj.setStable(true);
				
				//Calculating response angle (reflection equation)
				float dot = velX * (responseX / magResponse) + velY * (responseY / magResponse);
				velX = obj.getFriction() * (-2.0f * dot * (responseX / magResponse) + velX);
				velY = obj.getFriction() * (-2.0f * dot * (responseY / magResponse) + velY);
				
				obj.setVel(velX, velY);
				
				obj.onColision();
				
				if(obj instanceof Bomb)
				{
					int bounces = obj.getNumBounces();
					if(obj instanceof Grenade){}
					else map = ((Bomb)obj).destroy((int)potentialposX,(int)potentialposY,map,objects);
					for(int i = 0; i < 50; i++)
					{
						Debris debris = new Debris(potentialposX,potentialposY);
						objects.add(debris);
					}
					if(obj instanceof MiniBomb || obj instanceof HomingMissle){}
					else trail = new Trail(((Bomb)obj).getTrailX(),((Bomb)obj).getTrailY(),((Bomb)obj).getTrailSize());
				}
				
				if(obj instanceof ClusterBomb)
				{
					for(int i = 0; i < 10; i++)
					{
						Bomb bomb = new MiniBomb(potentialposX,potentialposY,3.0f);
						Random rand = new Random();
						bomb.velX = 4.0f * (float)Math.cos(rand.nextFloat() * 2.0f * pi);
						bomb.velY = 4.0f * (float)Math.sin(rand.nextFloat() * 2.0f * pi);
						objects.add(bomb);
					}
				}
				
				if(obj instanceof Grenade)
				{
					if(obj.isDead())
						map = ((Bomb)obj).destroy((int)potentialposX,(int)potentialposY,map,objects);
				}
				if(obj.isDead()) remove(obj);
			}
			else
			{
				obj.setPos(potentialposX, potentialposY);
				
				//Delete objects off the screen
				if(potentialposY < 0)
				{
					if(obj instanceof Bomb){}
					else remove(obj);
				}
				else if(potentialposX >= width)
				{
					if(obj instanceof Worm) obj.setVel(-1.0f,gravity);
					else remove(obj);
				}
				else if(potentialposX < 0)
				{
					if(obj instanceof Worm) obj.setVel(1.0f,gravity);
					else remove(obj);
				}
				else if(potentialposY >= height)
				{
					remove(obj);
				}
			}
			//Stop objects that are moving too slow
 			if(velMag < gravity)
			{
				obj.setVel(0.0f, 0.0f);
			} 
		}
		
		//Need to get mouse in screen space for buttons
		mouseX=(MouseInfo.getPointerInfo().getLocation().x-this.getLocationOnScreen().x);
		mouseY=(MouseInfo.getPointerInfo().getLocation().y-this.getLocationOnScreen().y);
		if(mouseClicked)
		{
			//Mouse selects preset zoom
			if(mouseY >= windowHeight-40 && mouseY <= windowHeight-40+30)
			{
				if(mouseX >= windowWidth-120 && mouseX <= windowWidth-120+30)
				{
					screenWidth = width;
					screenHeight = height;
					offsetX = 0;
					offsetY = 0;
					System.out.println("scale 1");
				}
				else if(mouseX >= windowWidth-80 && mouseX <= windowWidth-80+30)
				{
					screenWidth = width*2;
					screenHeight = height*2;
					System.out.println("scale 2");
				}
				else if(mouseX >= windowWidth-40 && mouseX <= windowWidth-40+30)
				{
					screenWidth = width*3;
					screenHeight = height*3;
					System.out.println("scale 3");
				}
				else if(mouseX >= 10 && mouseX <= 40) //Mouse selects a bomb
				{
					System.out.println("Missle selected");
					selection = 1;
				}
				else if(mouseX >= 50 && mouseX <= 80)
				{
					System.out.println("Cluster Bomb selected");
					selection = 2;
				}
				else if(mouseX >= 90 && mouseX <= 120)
				{
					System.out.println("Grenade selected");
					selection = 3;
				}
				else if(mouseX >= 130 && mouseX <= 160)
				{
					System.out.println("HomingMissle selected");
					selection = 4;
				}
			}
			mouseClicked = false;
		}		
		scaleX = (float)width/(float)screenWidth;
		scaleY = (float)height/(float)screenHeight;
		
		//Put mouse back into world space
		mouseX=(MouseInfo.getPointerInfo().getLocation().x-this.getLocationOnScreen().x)/windowScale;
		mouseY=(MouseInfo.getPointerInfo().getLocation().y-this.getLocationOnScreen().y)/windowScale;
	}
	
	private void gameRender()
	{
		//Render all objects
		//pixels = oldPixels.clone();
		//img = oldImg;
		//g = img.createGraphics();
		redrawMap();
		for(PhysicsObject obj : objects)
		{
			obj.draw(g);
			if(obj instanceof Worm)
			{
				float posX = obj.getposX()-offsetX/2;
				float posY = obj.getposY();
				float radius = obj.getRadius();
				if(mouseX >= posX-radius && mouseX <= posX+radius && mouseY >= posY-radius && mouseY <= posY+radius)
				{
					g.setColor(Color.WHITE);
					drawCenteredString(g,Integer.toString((int)((Worm)obj).getHealth()),new Rectangle((int)(posX-radius*2),(int)(posY-radius-19),(int)radius*4,8),new Font("", Font.PLAIN, 12));
				}
			}
		}
		
		if(activeWorm != null)
		{
			float posX = activeWorm.getposX()-offsetX/2;
			float posY = activeWorm.getposY();
			float radius = activeWorm.getRadius();
			if(mouseX >= posX-radius && mouseX <= posX+radius && mouseY >= posY-radius && mouseY <= posY+radius)
			{
				g.setColor(Color.WHITE);
				drawCenteredString(g,Integer.toString((int)activeWorm.getHealth()),new Rectangle((int)(posX-radius*2),(int)(posY-radius-19),(int)radius*4,8),new Font("", Font.PLAIN, 12));
			}
		}
		
		if(mouseClicked && selection == 4)
		{
			ht = new HomingTarget(mouseX+offsetX/2,mouseY,count);
		}
		
		if(ht != null)
			ht.draw(g);
		
		if(activeWorm == null && shoot) shoot = false;
		if(activeWorm != null)
		{
			Worm worm = getActiveWorm();
			if(worm.getHealth() > 0)
			{
				worm.update(movement);
				movement=0;
				worm.setActivePlayer(true);
				if(shoot)
				{
					Bomb bomb = worm.shoot(selection);
					if(selection == 4)
					{
						HomingTarget finalht = new HomingTarget(ht.getTargetX(),ht.getTargetY(),count);
						((HomingMissle)bomb).setHomingTarget(finalht);
					}
					objects.add(bomb);
					worm.setActivePlayer(false);
					activeWorm = null;
					power = 0.0f;
					shoot = false;
				}
			}
			else
			{
				g.setColor(Color.BLACK);
				drawCenteredString(g,"GAME OVER",new Rectangle(0,0,windowWidth/2,windowHeight/2),new Font("", Font.BOLD, 16));
			}
		}
		
		else if(activeWorm == null && objects.size() > 0)
		{
			if(!wormList.isEmpty())
			{
				Worm worm = wormList.dequeue();
				wormList.enqueue(worm);
				activeWorm = worm;
			}

		}
		
		if(trail != null && activeWorm != null)
		{
			activeWorm.setTrail(trail);
			trail = null;
		}

	}
	
	private void gameDraw()
	{
		BufferStrategy bs = getBufferStrategy();
		if(bs == null)
		{
			createBufferStrategy(3);
			return;
		}

 		int tempmouseX = mouseX*windowScale;
		int tempmouseY = mouseY*windowScale;
		
		if(mousePressed)
		{
			offsetX -= (tempmouseX - startPanX);
			offsetY -= (tempmouseY - startPanY);
			if(offsetX <= 0) offsetX = 0;
			if(offsetY <= 0) offsetY = 0;
			if(offsetX > width/2) offsetX += (tempmouseX - startPanX);
			if(offsetY > (screenHeight-height)*windowScale) offsetY += (tempmouseY - startPanY);
			if(mouseReleased)
			{
				mousePressed = false;
				mouseReleased = false;
			}
		}
		startPanX = tempmouseX;
		startPanY = tempmouseY;

		float movement; 
		//g.drawString("test",540/2+offsetX/2,screenHeight/2);
		g.setColor(Color.BLACK);
		if(count == 1)
		{
			g.drawString("3",540/2+offsetX/2,screenHeight/2);
		}
		if(count == 2)
		{	
			g.setColor(Color.BLACK);
			targetOffsetX = width/2;
			movement = (targetOffsetX - offsetX) * 0.01f;
			if(movement < 1) movement = 1;
			offsetX += movement;
			g.drawString("2",540/2+offsetX/2,screenHeight/2);
		}
		else if(count == 3)
		{
			g.drawString("1",540/2+offsetX/2,screenHeight/2);
		}
		else if(count == 4)
		{	
			targetOffsetX = 0;
			movement = (targetOffsetX - offsetX) * 0.01f;
			offsetX += movement;
			//System.out.println("movment: " + (targetOffsetX - offsetX) * 0.01f);
			g.drawString("go",540/2+offsetX/2,screenHeight/2);
		}
		if(offsetX > width/2) offsetX = width/2;
		if(offsetX <= 0) offsetX = 0;
			
		
		Graphics g = bs.getDrawGraphics();
		g.drawImage(img,-offsetX,-offsetY,screenWidth*windowScale,screenHeight*windowScale,null);

/* 		g.drawImage(img_1,windowWidth-120, windowHeight-40, 30, 30,null);
		g.drawImage(img_2,windowWidth-80, windowHeight-40, 30, 30,null);
		g.drawImage(img_3,windowWidth-40, windowHeight-40, 30, 30,null);  */

		
		g.drawImage(missle,10,windowHeight-40,30,30,null);
		g.drawImage(cluster,50,windowHeight-40,30,30,null);
		g.drawImage(grenade,90,windowHeight-40,30,30,null);
		g.drawImage(homing,130,windowHeight-40,30,30,null);

		g.dispose();
		bs.show();
	}
	
	private void gameStart()
	{
		count++;
		
		if(count == 1)
		{
			Worm worm = new Worm(0+50, height/2);
			objects.add(worm);
			wormList.enqueue(worm);
			activeWorm = worm;
			worm.setActivePlayer(true);
			
		}
		else if(count == 3)
		{
			Worm worm2 = new Worm(width-50, height/2);
			objects.add(worm2);
			wormList.enqueue(worm2);
			
		}
	}
	
	private void remove(PhysicsObject obj)
	{
		if(obj instanceof Worm)
		{
			wormList.dequeue();
			if(((Worm)obj).isActivePlayer()) activeWorm = null;
		}
		objects.remove(obj);
	}
	
	public void drawCenteredString(Graphics g, String text, Rectangle rect, Font font)
	{
		// Get the FontMetrics
		FontMetrics metrics = g.getFontMetrics(font);
		// Determine the X coordinate for the text
		int x = rect.x + (rect.width - metrics.stringWidth(text)) / 2;
		// Determine the Y coordinate for the text (note we add the ascent, as in java 2d 0 is top of the screen)
		int y = rect.y + ((rect.height - metrics.getHeight()) / 2) + metrics.getAscent();
		// Set the font
		g.setFont(font);
		// Draw the String
		g.drawString(text, x+offsetX/2, y);
	}
	
	public Worm getActiveWorm()
	{
		return wormList.peek();
	}
	
	public float WorldXToScreenX(int worldX)
	{
		float screenX;
		return screenX = ((float)(worldX + offsetX))*scaleX;
	}
	
	public float WorldYToScreenY(int worldY)
	{
		float screenY;
		return screenY = ((float)(worldY + offsetY))*scaleY;
	}
	
	public float ScreenXToWorldX(float screenX)
	{
		float worldX;
		return worldX = (float)((screenX - offsetX)/scaleX);
	}
	
	public float ScreenYToWorldY(float screenY)
	{
		float worldY;
		return worldY = (float)((screenY - offsetY)/scaleY);
	}
	
	public void createMap()
	{
		float[] seed = new float[width];
		int interations = 7;
		float smoothness = 2.0f; //Higher is more smooth
		Random rand = new Random();
		
		//fill the array with random noise
		for(int i = 0; i < width; i++)
			seed[i] = rand.nextFloat();
		
		//set to 0.5 so the map starts in the middle of the screen and not at the top
		seed[0] = 0.6f;
		surface = PerlinNoise1D(width, seed, interations, smoothness);

		//1 for surface 
		//0 for sky
		float temp = 0;

 		for(int i = 0; i < width; i++)
		{
			temp = surface[i];
			for(int j = 0; j < height; j++)
			{
				if(j >= (temp * height))
				{
					map[i+j*width] = 1;
				}
				else 
				{
					map[i+j*width] = 0;
				}
			}
		}
	}
	
	public void redrawMap()
	{
		int colorSky = 25855; // hex for 0x008000 which is green
		//int colorGround = 
		int colorLimit = 10000000;
		int colorStart = colorSky;
		int temp = 0;
     	for(int i = 0; i < width; i++)//x
		{
			for(int j = 0; j < height; j++)//y
			{
				if(temp == 0)
					if(colorSky <= colorLimit) colorSky += 65792;
				temp++;
				if(temp == 2) temp = 0;
				if(i+j*width >= width*height) break;
				
				if(map[i+j*width] == 1)
				{
					pixels[i+j*width] = 0x008000;
					oldPixels[i+j*width] = 0x008000;
				}
				else
				{
					pixels[i+j*width] = colorSky;
					oldPixels[i+j*width] = colorSky;
				}
				
			}
			colorSky = colorStart;
		}  
	}
	
 	public float[] PerlinNoise1D(int nCount, float[] fSeed, int nOctaves, float fBias)
	{
		// Used 1D Perlin Noise
		float[] fOutput = new float[width];
		for (int x = 0; x < nCount; x++)
		{
			float fNoise = 0.0f;
			float fScaleAcc = 0.0f;
			float fScale = 1.0f;

			for (int o = 0; o < nOctaves; o++)
			{
				int nPitch = nCount >> o;
				int nSample1 = (x / nPitch) * nPitch;
				int nSample2 = (nSample1 + nPitch) % nCount;

				float fBlend = (float)(x - nSample1) / (float)nPitch;

				float fSample = (1.0f - fBlend) * fSeed[nSample1] + fBlend * fSeed[nSample2];

				fScaleAcc += fScale;
				fNoise += fSample * fScale;
				fScale = fScale / fBias;
			}

			// Scale to seed range
			fOutput[x] = fNoise / fScaleAcc;
		}
		return fOutput;
	}
	
	public void keyReleased(KeyEvent k)
	{
		if (k.getKeyCode() == KeyEvent.VK_F)
			shoot = true;
	}
	
	public void keyPressed(KeyEvent k)
	{
		float winMouseX = WorldXToScreenX(mouseX)-offsetX/2;
		float winMouseY = WorldYToScreenY(mouseY);
		
		switch (k.getKeyCode())
		{
			case KeyEvent.VK_B: //Spawn bomb
				Bomb bomb = new Bomb(winMouseX,winMouseY,4.0f);
				objects.add(bomb);
				break;
				
			case KeyEvent.VK_R: //Remove all objects
				System.out.println(winMouseX);
				System.out.println(winMouseY);
				for(PhysicsObject obj : objects)
				{
					remove(obj);
				}
				break;
			
			case KeyEvent.VK_UP: //Recreatemap
				createMap();
				break;
			
			case KeyEvent.VK_SPACE: //Spawn worm
				Worm worm = new Worm(winMouseX,winMouseY);
				objects.add(worm);
				wormList.enqueue(worm);
				if(activeWorm == null)
				{
					activeWorm = worm;
					worm.setActivePlayer(true);
				}
				break;
			
			case KeyEvent.VK_S: //Start
				startGame = true;
				break;
			
			case KeyEvent.VK_A: //Left
				movement = 1;
				break;
			
			case KeyEvent.VK_D: //Right
				movement = 2;
				break;
			
			case KeyEvent.VK_W: //Jump
				movement = 3;
				break;
			
			case KeyEvent.VK_E: //Angle right
				movement = 4;
				break;
			
			case KeyEvent.VK_Q: //Angle left
				movement = 5;
				break;
			
			case KeyEvent.VK_F: //Fire
				if(inc)
				{
					power += 0.4f;
					if(power >= 10) inc = false;
				}
				else if(!inc)
				{
					power -= 0.2f;
					if(power <= 0) inc = true;
				}
				if(getActiveWorm() != null) getActiveWorm().firing(power);
				break;
		}

	}
	
	public void mouseMoved(MouseEvent e)
	{
		//mouseX = (float)e.getX();
		//mouseY = (float)e.getY();
	}
	
	public void mouseDragged(MouseEvent e)
	{
		//mouseX = (float)e.getX();
		//mouseY = (float)e.getY();
	}

	public void mousePressed(MouseEvent e)
	{
		mousePressed = true;
	}

	public void mouseReleased(MouseEvent e)
	{
		mouseReleased = true;
	}
	
	public void mouseWheelMoved(MouseWheelEvent e)
	{
		//Get value of initial mouse position
/* 		float mouseWorldXBeforeZoom = WorldXToScreenX(mouseX*windowScale);
		float mouseWorldYBeforeZoom = WorldYToScreenY(mouseY*windowScale);
		
		if(e.getWheelRotation() < 0 && screenWidth >= width) //zoom in
		{
			screenWidth += 32;
			screenHeight += 18;
		}
		else if(screenWidth > width) //zoom out
		{
			screenWidth -= 32;
			screenHeight -= 18;
		}
		
		//Reset scales after screen width & height have changed
		scaleX = (float)width/(float)screenWidth;
		scaleY = (float)height/(float)screenHeight;
		
		//Get value of mouse after zoom
		float mouseWorldXAfterZoom = WorldXToScreenX(mouseX*windowScale);
		float mouseWorldYAfterZoom = WorldYToScreenY(mouseY*windowScale);
		
		//Substact after from before in realtion to the scale and adjust that difference to the offset
		offsetX += (mouseWorldXBeforeZoom - mouseWorldXAfterZoom) / scaleX;
		offsetY += (mouseWorldYBeforeZoom - mouseWorldYAfterZoom) / scaleY;
		
		//Keep in bounds
		if(offsetX <= 0) offsetX = 0;
		if(offsetY <= 0) offsetY = 0;
		if((screenWidth*scaleX+offsetX)-screenWidth > 0)
			offsetX = offsetX - (int)((screenWidth*scaleX+offsetX)-screenWidth);
		if((screenHeight*scaleY+offsetY)-screenHeight > 0)
			offsetY = offsetY - (int)((screenHeight*scaleY+offsetY)-screenHeight); */
	}
	
	public void mouseClicked(MouseEvent e)
	{
		mouseClicked = true;
	}
	
	public void actionPerformed(ActionEvent evt) {} 
	public void keyTyped(KeyEvent k) {}
	public void mouseEntered(MouseEvent e) {}
	public void mouseExited(MouseEvent e) {}
}