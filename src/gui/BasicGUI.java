package gui;

import java.awt.BorderLayout;

public class BasicGUI extends CenterFrame
{

   public BasicGUI(int width, int height, String title, Drawable d, String icon_file_name)
   {      
      super (width, height, title);

      setLayout(new BorderLayout());
      setSize(width, height);
      setResizable(false);

      DrawPanel draw = new DrawPanel();
      draw.setDrawable(d);
      add(draw, BorderLayout.CENTER);
	  
	  javax.swing.ImageIcon img_icon = new javax.swing.ImageIcon(icon_file_name);
	  setIconImage(img_icon.getImage());

      setVisible(true);
   }
   
}
