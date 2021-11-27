/* Muhammet Emre Cebeci 1705950
 * 2021 - December
 * TODO: Slider ile guc cubugu ekle , container ile altta olabilir belki. printer orneginde vardi
 * TODO: Dusman ( hatta dusmanlar) custom shape ekle iyi puan olur, shape icinde clipping stroke vs
 * TODO: Dusman olunce image process li animasyon
 * TODO: Arkaplan ekle 
 * TODO: Cannon sekil kontrolu et
 * TODO: Fazla variableleri sil
 * */

package project;

import java.awt.*;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Shape;
import java.awt.TexturePaint;
import java.awt.color.ColorSpace;
import java.awt.event.*;
import java.awt.font.FontRenderContext;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ColorConvertOp;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.awt.image.RescaleOp;
import java.awt.image.WritableRaster;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import shapes.Heart;
import shapes.Cannon;

import java.awt.geom.*;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.event.MenuKeyListener;

import java.awt.print.*;

// konudan emin ol!
public class CannonGame extends JFrame implements ActionListener {
	
	static GamePanel gamePanel = new GamePanel(0,0,1200,400,50);
	PrinterJob pj;

	
	public static void main(String[] args) {
		JFrame frame = new CannonGame(); // Drawing Menubar
		frame.setTitle("Cannon Game TEST");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // constant class tan hmm
		
		JPanel panel = gamePanel;
		frame.getContentPane().add(panel);
		frame.pack(); // fits frame to panel object dimension size
		frame.setVisible(true);	
		}
	
	public CannonGame() {
		JMenuBar mb = new JMenuBar();
		setJMenuBar(mb);

		JMenu menu = new JMenu("Menu");
		JMenuItem mi = new JMenuItem("Reset Game");
		mi.addActionListener(this);
		menu.add(mi);
		mi = new JMenuItem("Print Game");
		mi.addActionListener(this);
		menu.add(mi);
		menu.addSeparator();
		mi = new JMenuItem("Exit");
		mi.addActionListener(this);
		menu.add(mi);
		mb.add(menu);

		menu = new JMenu("Developer Menu");
	    mb.add(menu);
	    
	    pj = PrinterJob.getPrinterJob();
	    pj.setPrintable(gamePanel);
	   
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		String cmd = e.getActionCommand();	
		switch(cmd) {
		case "Reset Game":
			gamePanel.restartGame();
			break;
		case "Print Game":
			try {
		        pj.print();
		      } catch (PrinterException ex) {
		        ex.printStackTrace();
		      }
			break;
		case "Exit":
			System.exit(0);
			break;
		}
	}

}

class GamePanel extends JPanel implements  Runnable, KeyListener, MouseListener, MouseMotionListener, Printable { // inheritance
	// === Area of the plot ===
	int x0;
	int y0;
	double width;
	double height;
	double platformX = 0;
	double platformY; // inside
	// int platformWidth;
	double platformHeight;

	
	Shape ball = null;
	Shape player = null;
	Shape platform = null;
	
	double cannonX;
	double cannonY;
	double cannonWidth = 70;
	double cannonHeight = 50;
	double cannonAngle = 0;
	AffineTransform at = new AffineTransform();
	boolean keyPressed = false;
	
	int sample = 500; // draw sample 
	double shootHeight = 200; // power!
	double shootHeightInterval;
	double shootWidth;
	double shootWidthInterval;
	double cannonBallX = -50;
	double cannonBallY = -50;
	double cannonBallXTemp;
	double cannonBallYTemp;
	double cannonBallWidth = 20;
	double cannonBallHeight = 20;
	
	int i = 0;
	int r = 100;

	int x1 = (int) (r * Math.cos(0));
	int y1 = (int) (r * Math.sin(0));
	int x2 = 0;
	int y2 = 0;
	
	boolean isCannonBallShooted;
	
	List<Line2D> ballLineList = new ArrayList<>();
	
	BufferedImage brickImage = null;
	
	int score = 0;
	int lives = 5;
	
	List<Shape> liveShape = new ArrayList<>();
	int heartWidth = 50;
	int heartHeight = 50;
	
	
	Point p1;
	Point p = null;


	public GamePanel(int x, int y, int width, int height, int platformHeight) {
		setPreferredSize(new Dimension(width, height)); 
		setBackground(Color.LIGHT_GRAY);	
		this.x0 = x;
		this.y0 = y;
		this.width = width;
		this.height = height;
		this.platformHeight = platformHeight;
		platformY = height-platformHeight;
		
		cannonX = cannonWidth;
		cannonY = platformY-cannonHeight;
		
		URL urlBrick = getClass().getClassLoader().getResource("assets/brick.png");
		try {
		   brickImage = ImageIO.read(urlBrick);
		} catch (IOException ex) {
		     ex.printStackTrace();
		}		
		
		// Create lives location
		for(int i=0; i<lives; i++) {
			// Heart yerine yildiz yap!?
			liveShape.add(new Heart((i)*50,0,heartWidth,heartHeight)); // i+1 de olabilir
		}
		
		addMouseListener(this);
		addMouseMotionListener(this);
		
		addKeyListener(this);
		setFocusable(true); 
		
		Thread thread = new Thread(this);
		thread.start();
	}

	public void paintComponent(Graphics g) { 
		super.paintComponent(g); 
		Graphics2D g2 = (Graphics2D) g; // to usage Shape (Rectangle2D etc) classes
		gameDraw(g2);
		
	}

	private void gameDraw(Graphics2D g2) {		
		FontRenderContext frc;
		Font scoreLabelFont = new Font("Serif",Font.BOLD,24);	
		
		player = new Cannon(cannonX,cannonY,cannonWidth,cannonHeight); // To draw, Create as Base Class (Shape not Cannon)
		//player = new Rectangle2D.Double(cannonX,cannonY,cannonWidth,cannonHeight);
		
		if(!keyPressed ) { //  fixes that movement rotate glitch
			at.setToRotation(cannonAngle);	// rotate cannon angle	
			player = at.createTransformedShape(player);
		}
		
		g2.draw(player.getBounds());
		g2.fill(player);
		
		
		// Draw Cannon Ball and Line
		ball = new Ellipse2D.Double(cannonBallX - cannonBallWidth/2, cannonBallY - cannonBallHeight/2, cannonBallWidth, cannonBallHeight);
		g2.fill(ball);
		g2.draw(ball.getBounds());
		
		for(int i = 0 ; i< ballLineList.size() ;i++) {
		    g2.draw(ballLineList.get(i));
		}
		
		// Drawing Score Label (Responsively for number digit changes with frc)
		String scoreLabel = "Score: " + String.valueOf(score);
		g2.setFont(scoreLabelFont);
		frc = g2.getFontRenderContext();
		double textWidth = scoreLabelFont.getStringBounds(scoreLabel,frc).getWidth();
		double textHeight =  scoreLabelFont.getStringBounds(scoreLabel,frc).getHeight();
		double scoreLabelX = width - textWidth - 5; // -5 for margin
		double scoreLabelY = textHeight;
		g2.drawString(scoreLabel,(int) scoreLabelX, (int)scoreLabelY);
		
		// Draw Lives Shapes
		for(int i = 0; i< lives ;i++) {
			g2.setColor(Color.RED);
			g2.fill(liveShape.get(i));
		}
		
        if(lives < 1) {
        	score = 0;
            lives = 5;
        }
		
		// Draw Brick Platform
		TexturePaint tp = new TexturePaint(brickImage, new Rectangle2D.Double(0, platformY, 25, 25));
		g2.setPaint(tp);
		
	    platform = new Rectangle2D.Double(platformX, platformY, width, platformHeight);
	    g2.fill(platform);
	    

	    
	}
	
	@Override
	public void run() {
		while(true) {
			checkBallShooting();
			checkCollision();
			checkPanelLimits();
			repaint();
			
			try {
				Thread.sleep(1); // speed
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
		}
	}
	
	private void checkPanelLimits() {
		if(cannonX < 0) {  // en kosedeyken merkezden r cikarirsan sinir olur hmm! cizdim
			cannonX = 0; // sinira geri goturur
		}
		if(cannonX + cannonWidth > width) {
			cannonX = width - cannonWidth; // sinira geri goturur
		}
	}

	// Shooting Animation
	private void checkBallShooting() {	
		if(isCannonBallShooted) { 
			if(i == 0) {
				ballLineList.clear();
				
				//For first iterator
				cannonBallX = cannonX + cannonWidth;
				cannonBallY = cannonY + cannonHeight/2;
				
				// Important!
				// Yukseklige gore kotanjant alinarak r bulunur. Yani rotate acisi  ve h ile r bulunur
				shootWidth = shootHeight * Math.atan2(1, -(cannonAngle*-90)) ; // cot(cannonAngle)=tan(1/-cannonAngle), defterde cizdin
				
				// shootHeight= // TODO: Setting power
				shootWidthInterval = shootWidth / sample ;
				shootHeightInterval = shootHeight / sample;
				System.out.println("Shoot Activated"+" shootWidth"+ shootWidth+" shootWidthInterval "+shootWidthInterval);
				System.out.println("Shoot Activated"+" shootHeight"+ shootHeight+" shootWidthInterval "+shootHeightInterval);
				i++;
			}
			
			if(i < sample) { // kinetic shoot with symmetrical rising and falling
				
				cannonBallXTemp = cannonBallX + shootWidthInterval;
				
				if(i<sample/2) { // Rising
					cannonBallYTemp = cannonBallY - shootHeightInterval;
				} else { // Falling 
					cannonBallYTemp = cannonBallY + shootHeightInterval;
				}
				
				ballLineList.add(new Line2D.Double(cannonBallX,cannonBallY,cannonBallXTemp,cannonBallYTemp));
				
				//System.out.println("Line 2D "+cannonBallX+" "+cannonBallY+" "+cannonBallXTemp+" "+cannonBallYTemp+" "+shootHeightInterval);
				cannonBallX = cannonBallXTemp;
				cannonBallY = cannonBallYTemp;	
				i++;
				
			} else { //  if symmetrical shoot ended, the ball must continue to fall down platform. (it loops)
				cannonBallXTemp = cannonBallX + shootWidthInterval;
				cannonBallYTemp = cannonBallY + shootHeightInterval;
				ballLineList.add(new Line2D.Double(cannonBallX,cannonBallY,cannonBallXTemp,cannonBallYTemp));
				//System.out.println("Line 2D "+cannonBallX+" "+cannonBallY+" "+cannonBallXTemp+" "+cannonBallYTemp+" "+shootHeightInterval);
				cannonBallX = cannonBallXTemp;
				cannonBallY = cannonBallYTemp;	
			} 
			
		}
		
	}

	private void checkCollision() {
		if(isCannonBallShooted) {
			Point2D p = new Point2D.Double(cannonBallX,cannonBallY);
			if(platform.contains(p)) {
				cannonBallY = platformY - cannonBallHeight /2;
				isCannonBallShooted = false; // stop if the collision happens
				i = 0;
				lives--;
				System.out.println("Collision");
			}
		}
	
	}

	@Override
	public void keyTyped(KeyEvent e) {
		// TODO Auto-generated method stub
	}

	@Override
	public void keyPressed(KeyEvent e) {
		switch(e.getKeyCode()) {
		case KeyEvent.VK_LEFT:
			//lives--;
			cannonX -= 5;
			cannonAngle = 0;
			keyPressed = true;
			break;
		case KeyEvent.VK_RIGHT:
			 cannonX += 5;
			 cannonAngle = 0;
			 keyPressed = true;
			break;
		case 32:
			if(!isCannonBallShooted) {
				isCannonBallShooted = true;
			}
			
		}
	}

	@Override
	public void keyReleased(KeyEvent e) {
		keyPressed = false;
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		
		if(!keyPressed) {
		p1 = e.getPoint();
		System.out.println("Angle "+cannonAngle+" cannonBallX= "+cannonBallX+" cannonBallY= "+cannonBallY+" r= "+r);
		cannonAngle = (Math.atan2(p1.y-y0,p1.x-x0) - Math.atan2(p.y-y0,p1.x-x0))/3;
		}
		
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		
	    
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		// TODO Auto-generated method stub
	}

	@Override
	public void mousePressed(MouseEvent e) {
		p = e.getPoint();
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}
	
	public void restartGame() {
		cannonX = cannonWidth;
		cannonY = platformY-cannonHeight;
		score = 0;
		lives = 5;
		cannonBallX = 0;
		cannonBallY = 0;
		cannonAngle = 0;
		repaint();	 
		System.out.println("Game is restarted!");
	}
	
	  public int print(Graphics g, PageFormat pf, int pageIndex) {
		    switch (pageIndex) {
		      case 0:
		    	paintComponent(g);
		        break;
		      case 1:
		        g.translate(-(int)pf.getImageableWidth(), 0);
		        paintComponent(g);
		        break;
		      default:
		        return NO_SUCH_PAGE;
		    }
		    return PAGE_EXISTS;
	  }
}
