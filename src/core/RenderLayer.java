package core;

import java.util.*;
import java.awt.*;
import java.awt.image.*;

public class RenderLayer {
	
	public final int n,w,h;
	public final BufferedImage[] images;
	public final Graphics2D[] graphics;
	
	public RenderLayer(int n,int w,int h){
		this.n=n;
		this.w=w;
		this.h=h;
		images = new BufferedImage[n];
		graphics = new Graphics2D[n];
		for(int i=0;i<n;i++){
			BufferedImage img = images[i] = new BufferedImage(w,h,BufferedImage.TYPE_INT_ARGB);
			Graphics2D g = graphics[i] = img.createGraphics();
			g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		}
	}
	
	public void renderTo(Graphics2D g){
		for(BufferedImage img:images){
			g.drawImage(img, 0, 0, null);
		}
	}
	
	public void translate(double x,double y){
		for(int i=0;i<n;i++){
			graphics[i].translate(x, y);
		}
	}
	
	public void rotate(double r){
		for(int i=0;i<n;i++){
			graphics[i].rotate(r);
		}
	}
	
	public void scale(double x,double y){
		for(int i=0;i<n;i++){
			graphics[i].scale(x, y);
		}
	}

}
