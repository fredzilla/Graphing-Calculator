import java.awt.*;
import java.util.ArrayList;
import java.util.Random;

import javax.swing.*;

public class EquationParser extends JApplet{
	
	int width, height = 800;
	String formula;
	double xmin,xmax,ymin,ymax,inc;
	
	public void init(){
		//set window properties
		
		//get min
		xmin = Integer.parseInt(JOptionPane.showInputDialog("X Min: ", "-10"));
		//get max
		xmax = Integer.parseInt(JOptionPane.showInputDialog("X Max: ", "10"));
		
		//get min
		ymin = Integer.parseInt(JOptionPane.showInputDialog("Y Min: ", "-10"));
		//get max
		ymax = Integer.parseInt(JOptionPane.showInputDialog("Y Max: ", "10"));
		
		//get inc
		inc = Math.abs(Double.parseDouble(JOptionPane.showInputDialog("Inc: ", ".1")));	
		
		repaint();
	}
	
	public void paint(Graphics g){
		//paint the axes
		g.setColor(Color.red);
		g.drawLine(0, this.getWidth()/2, this.getHeight(), this.getWidth()/2); //x axis
		g.drawLine(this.getWidth()/2, 0, this.getWidth()/2, this.getHeight()); //y axis
		//draw minimum/maximum labels
		g.drawString("" + xmax, this.getWidth()-14, this.getHeight()/2 - 3);
		g.drawString("" + xmin, 0, this.getHeight()/2 - 3);
		g.drawString("" + ymax, this.getWidth()/2, 10);
		g.drawString("" + ymin, this.getWidth()/2, this.getHeight()-10);
		
		
		String func = JOptionPane.showInputDialog("Enter the function.\ny[x]=");
		if(func != null){
			formula = func;
			//graph function
			Equation eqn = new Equation(formula);
			int yfrom, yto = 0, xfrom, xto = 0;
			g.setColor(Color.blue);
			boolean first = true;
			for(double i = xmin; i < xmax; i += inc){
				xfrom = xto;
				yfrom = yto;
				yto = mapY(eqn.findOutput(i));
				xto = mapX(i);
				if(!first)
					g.drawLine(xfrom, yfrom, xto, yto);
				else 
					first = false;
			}
			repaint();
		}
	}
	public int mapY(double num){
		//System.out.println("Output: " + num);
		int height;
		if(num >= 0)
			height = (int)ymax;
		else
			height = (int)ymin;
		double oneInPixels = (this.getHeight()/2)/Math.abs(height);
		//if the answer is even on the map
		return (-1)*(int)(num * oneInPixels) + this.getHeight()/2;
	}
	public int mapX(double num){
		int size;
		if(num >= 0)
			size = (int)xmax;
		else
			size = (int)xmin;
		double oneInPixels = (this.getWidth()/2)/Math.abs(size);
		return (int)(oneInPixels * num) + this.getWidth()/2;
	}
}
