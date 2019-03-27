package gui;

import java.awt.Graphics;

public class NullDrawable implements Drawable
{
   public void draw(Graphics g, int w, int h) {}
   public void mouseClicked(int x, int y) {}
   public void keyPressed(char key) {}
}