/* Muhammet Emre Cebeci 1705950
 * 2021 - December
 * TODO: Slider ile guc cubugu ekle , container ile altta olabilir belki. printer orneginde vardi veya guc cubugu cizersin dikdortgenden asagi ve yukari onun gucunu ayarlar
 * TODO: Dusman ( hatta dusmanlar) custom shape ekle iyi puan olur, shape icinde clipping stroke vs
 * TODO: Dusmana vurunca ayri olunca ayri image process li animasyon
 * TODO: Arkaplan ekle 
 * TODO: Splash screen hazirla  * TODO:  Splash Screen de diffucult olsun
 * TODO: Firlatma acisini ve sekil niye cannon iken platforma degemiyor ogren
 * TODO: Kalp sekli yerine top sekli olabilir veya ayrintili bir sey hmm
 * TODO: Timer ekle ki sure ile yarisip dusmani oldursun -- bence gerek yok
 * TODO: Score kisminda texture hatasini duzelt
 * */

package project;

import java.awt.*;
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
import javax.swing.event.MenuKeyListener;

import java.awt.print.*;

// konudan emin ol!
public class CannonGame extends JFrame implements ActionListener {
	
	static GamePanel gamePanel = new GamePanel(1200,400,50);
	static JFrame splashFrame;
	PrinterJob pj;

	public static void main(String[] args) {

		splashFrame = new JFrame();
		splashFrame.setUndecorated(true); 

		splashFrame.setLayout(new BorderLayout()); 

		JPanel panel = new SplashPanel(); 
											
		splashFrame.add(panel, BorderLayout.CENTER);

		JButton btn = new JButton("Start the Game");
		splashFrame.add(btn, BorderLayout.SOUTH);

		btn.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				createApplicationFrame();
			}
		});

		panel.addMouseListener(new MouseListener() {

			@Override
			public void mouseReleased(MouseEvent e) {
			}

			@Override
			public void mousePressed(MouseEvent e) {
				createApplicationFrame();
			}

			@Override
			public void mouseExited(MouseEvent e) {
			}

			@Override
			public void mouseEntered(MouseEvent e) {
			}

			@Override
			public void mouseClicked(MouseEvent e) {
			}
		});

		splashFrame.pack();
		splashFrame.setLocationRelativeTo(null); // Set location to the center of screen

		splashFrame.setVisible(true); // Set visibility
		
		}
	
	public static void createApplicationFrame() {
		splashFrame.dispose(); // dispose the splash screen frame

		JFrame frame = new CannonGame(); // To Drawing MenuBar
		frame.setTitle("Cannon Game TEST");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // constant class tan hmm
		JPanel panel = gamePanel;
		
		frame.getContentPane().add(panel);
		frame.pack(); // fits frame to panel object dimension size
		frame.setLocationRelativeTo(null); // center position of screen
		
		URL iconPath = CannonGame.class.getResource("/resources/icon.png");
		try {
			frame.setIconImage(ImageIO.read(iconPath)); // Set icon for game frame
			} catch (IOException ex) {
			     ex.printStackTrace();
		}	
		
		frame.setVisible(true);	
	}
	
	public CannonGame() {
		JMenuBar mb = new JMenuBar();
		setJMenuBar(mb);

		JMenu menu = new JMenu("Menu");
		JMenuItem mi = new JMenuItem("Reset Game");
		mi.addActionListener(this);
		menu.add(mi);
		menu.addSeparator();
		mi = new JMenuItem("Print Game");
		mi.addActionListener(this);
		menu.add(mi);
		menu.addSeparator();
		mi = new JMenuItem("Exit");
		mi.addActionListener(this);
		menu.add(mi);
		mb.add(menu);

		menu = new JMenu("Game Settings");
		mi = new JMenuItem("Select Diffuculty");
		mi.setEnabled(false);
		mi.addActionListener(this);
		menu.add(mi);
		menu.addSeparator();
		mi = new JMenuItem("Easy");
		mi.addActionListener(this);
		menu.add(mi);
		mi = new JMenuItem("Medium");
		mi.addActionListener(this);
		menu.add(mi);
		mi = new JMenuItem("Hard");
		mi.addActionListener(this);
		menu.add(mi);
		mb.add(menu);
		
		menu = new JMenu("Graphic Settings");
		mi = new  JCheckBoxMenuItem ("Anti-Aliasing", true); // selected
		mi.addActionListener(this);
		menu.add(mi);
	    mb.add(menu);
	    
	    // Set up for printing
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
		case "Anti-Aliasing":
			if(gamePanel.AntiAliasing)
				gamePanel.AntiAliasing = false;
			else
				gamePanel.AntiAliasing = true;
			break;
		}

    }  
	

}

class GamePanel extends JPanel implements  Runnable, KeyListener, MouseListener, MouseMotionListener, Printable { 
	// == Graphic Render == 
	boolean AntiAliasing = true;
	
	// === Area of the plot ===
	double width;
	double height;
	double platformX = 0;
	double platformY; 
	// double platformWidth;
	double platformHeight;
	BufferedImage backgroundImage = null;

	// === Shapes === 
	Shape player = null;
	Shape cannonBall = null;
	Shape platform = null;
	Shape enemy = null;
	
	// == Cannon == 
	double cannonX;
	double cannonY;
	double cannonWidth = 70;
	double cannonHeight = 50;
	double cannonAngle = 0;
	AffineTransform atCannon = new AffineTransform();
	boolean keyPressed = false;
	
	// == To Calculate Angle of Cannon == 
	int x0;
	int y0;
	Point p1;
	Point p = null;
	
	// == Enemy ==
	double enemyX;
	double enemyY;
	double enemyWidth = 200;
	double enemyHeight = 200;
	double minEnemyX;
	double maxEnemyX;
	
	double enemyWidthGame;
	double enemyHeightGame;
	
	BufferedImage enemyTexture = null;
	BufferedImage enemyTextureGame = null;
	
	
	// == Enemy Animation ==
	boolean isEnemyShooted;
	int shootedIteration = 0;
	
	// == Firing ==
	int sample = 25; // draw sample 
	int firingIteration = 0;
	List<Line2D> firingLineList = new ArrayList<>(); // Drawing Lines of firingLine
	double fireHeight = 200; // TODO: Must be setting by slider or sth like that
	double fireHeightInterval;
	double fireWidth; // it is calculated with firingHeight and angle of cannon (cotangent)
	double fireWidthInterval;

	// == Cannon Ball ==
	boolean isCannonBallFired;
	boolean isCannonBallVisible = false;
	double cannonBallX = 0;
	double cannonBallY = 0;
	double cannonBallXTemp;
	double cannonBallYTemp;
	double cannonBallWidth = 20;
	double cannonBallHeight = 20;
	
	// == Platform == 
	BufferedImage brickImage = null;
	
	// == Game Options ==
	double gameDiffucultyFactor = 0.25;
	double enemyScaleFactor = 1; // (0,1] (for Enemy Size)
	
	// == Live and Score ==
	int score = 0;
	int lives = 5;
	List<Shape> liveShape = new ArrayList<>();
	int heartWidth = 50;
	int heartHeight = 50;


	public GamePanel(int width, int height, int platformHeight) {
		setPreferredSize(new Dimension(width, height)); 
		setBackground(Color.LIGHT_GRAY);	
		this.width = width;
		this.height = height;
		this.platformHeight = platformHeight;
		platformY = height-platformHeight;
		
		cannonX = cannonWidth;
		cannonY = platformY-cannonHeight;
		
		URL urlBackground = getClass().getClassLoader().getResource("resources/postapocalypse1.png");
		try {
		   backgroundImage = ImageIO.read(urlBackground);
		} catch (IOException ex) {
		     ex.printStackTrace();
		}	
		
		URL urlBrick = getClass().getClassLoader().getResource("resources/brick.png");
		try {
		   brickImage = ImageIO.read(urlBrick);
		} catch (IOException ex) {
		     ex.printStackTrace();
		}	
		
		URL urlEnemyTexture = getClass().getClassLoader().getResource("resources/texture.jpg");
		try {
		   enemyTexture = ImageIO.read(urlEnemyTexture);
		} catch (IOException ex) {
		     ex.printStackTrace();
		}	
		enemyTextureGame = enemyTexture;
		
		// Create lives location
		for(int i=0; i<lives; i++) {
			// Heart yerine yildiz yap!?
			liveShape.add(new Heart((i)*50,0,heartWidth,heartHeight)); // i+1 de olabilir
		}
		
		enemyWidthGame = enemyWidth; 
		enemyHeightGame = enemyHeight;

		// Spawn first enemy randomly in a position range
		enemyY= platformY-enemyHeight;
		minEnemyX = width / 2;
		maxEnemyX = width - enemyWidth;
		spawnEnemy();
		
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
		
		if(AntiAliasing) {
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON); // Enable Anti Aliasing
		}
		
		gameDraw(g2);
	}

	private void gameDraw(Graphics2D g2) {	
	 	// Draw Background
		TexturePaint tp = new TexturePaint(backgroundImage, new Rectangle2D.Double(0, 0, width, height));
		//g2.setPaint(tp);
		//platform = new Rectangle2D.Double(0, 0, width, height);
		//g2.fill(platform); 
		
		// Draw Platform with Painting Brick Texture
		tp = new TexturePaint(brickImage, new Rectangle2D.Double(0, platformY, 25, 25));
		g2.setPaint(tp);
	    platform = new Rectangle2D.Double(platformX, platformY, width, platformHeight);
	    g2.fill(platform);
		
		// Draw Cannon Ball and Tracking Fire Lines
		if(isCannonBallVisible) {
			cannonBall = new Ellipse2D.Double(cannonBallX - cannonBallWidth/2, cannonBallY - cannonBallHeight/2, cannonBallWidth, cannonBallHeight);
			GradientPaint ballPaint = new GradientPaint(100,300, Color.BLACK, (int) width /2 , (int) height /2 , Color.RED);
			g2.setPaint(ballPaint);
			g2.fill(cannonBall);
		}
		
		float[] dashArray = {10};
		int dashPhase = 0;
		BasicStroke stroke = new BasicStroke(3, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 0,
		  dashArray, dashPhase);
		  g2.setStroke(stroke);
		for(int i = 0 ; i< firingLineList.size() - 1 ;i++) { // -1 for delete last line
		    g2.draw(firingLineList.get(i));
		}
		
		// Drawing Score Label (Responsively for number digit changes with frc)
		FontRenderContext frc;
		Font scoreLabelFont = new Font("Serif",Font.BOLD,24);	
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
		
		// TODO: Can inince oyunu durdurup ekran cikar
        if(lives < 1) {
        	//score = 0;
            lives = 5;
            spawnEnemy();
        }
        
        // Draw Enemy
        //g2.setColor(Color.BLACK);
        tp = new TexturePaint(enemyTextureGame, new Rectangle2D.Double(enemyX, enemyY, enemyWidthGame, enemyHeightGame));
        g2.setPaint(tp);
        
        enemy = new Rectangle2D.Double(enemyX,enemyY,enemyWidthGame,enemyHeightGame);
        
        g2.fill(enemy);
        
		// Draw Player
	    g2.setColor(Color.BLACK);
		player = new Cannon(cannonX,cannonY,cannonWidth,cannonHeight); // To draw, Create as Base Class (Shape not Cannon)
		if(!keyPressed ) { //  fixes that movement rotate glitch	
        	atCannon.setToRotation(cannonAngle);
			player = atCannon.createTransformedShape(player);
		}
		g2.fill(player);
	    
	}
	
	// === Animation and Checking Collision Thread ===
	@Override
	public void run() {
		while(true) {
			checkBallFiring();		
			checkCollision();
			checkPanelLimits();
			checkShootedEnemyAnimation(); // for shooted enemy animation with image processing
			
			repaint();
			
			try {
				Thread.sleep(20); // speed
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
		}
	}
	
	// Shooted enemy animation
	private void checkShootedEnemyAnimation() {
		if(isEnemyShooted) {
			if(shootedIteration < 5) {
				// Image in ile same size ve same color type
				BufferedImage imageOut = new BufferedImage(enemyTextureGame.getWidth(),enemyTextureGame.getHeight(),enemyTextureGame.getType());
				// recommended fastest one, raster image matrix'i alip mudahale ederek hizli write ozelligi saglar, bufferedimage ile mudahale yavastir!
				WritableRaster rasterImgIn = enemyTextureGame.getRaster();
				WritableRaster rasterImgOut = imageOut.getRaster();
			
				int[] rgba = new int[4]; // a alpha channel dir, transparency'i gosterir, bu ornekte sadece rgb mudahalesi olacak. alpha channel kalacak
			
				for(int y=0; y<enemyTextureGame.getHeight(); y++) { // piksel satir satir 
					for(int x=0; x<enemyTextureGame.getWidth(); x++) {
					
						// piksele gore cikis, raster hizlilik saglar buffered image ile tek tek deseydik yavas olurdu
						rasterImgIn.getPixel(x, y, rgba); // rgba ya yazilir
						// pixel = r+g+b/3
						int gray = (int) (rgba[0] + rgba[1] + rgba[2] / 5f);
						rgba [0] = rgba[1]= rgba[2] = gray;
						rasterImgOut.setPixel(x, y, rgba);
					}
					enemyTextureGame = imageOut;
				}
			
				shootedIteration++;
			} else {
					isEnemyShooted = false;
					shootedIteration = 0;
					enemyTextureGame = enemyTexture; // revert texture to normal
		
			//spawnEnemy();
			}
			
		}
			
	}
	
	//checkPanelLimits for player
	private void checkPanelLimits() {
		if(cannonX < 0) {  // en kosedeyken merkezden r cikarirsan sinir olur hmm! cizdim
			cannonX = 0; // sinira geri goturur
		}
		if(cannonX + cannonWidth > width) {
			cannonX = width - cannonWidth; // sinira geri goturur
		}
		

	}

	// Shooting animation
	private void checkBallFiring() {	
		if(isCannonBallFired) { 
			if(firingIteration == 0) {
				firingLineList.clear();
				isCannonBallVisible = true;
				
				//For first iterator
				cannonBallX = cannonX + cannonWidth;
				cannonBallY = cannonY + cannonHeight/2;
				
				// Important!
				// English : r is found by taking the cotangent according to the height. So rotate angle and h and r are found.
				// Turkish: Yukseklige gore kotanjant alinarak r bulunur. Yani rotate acisi  ve h ile r bulunur
				fireWidth = fireHeight * Math.atan2(1, -(cannonAngle*-90)) ; // cot(cannonAngle)=tan(1/-cannonAngle), defterde cizdin
				
				// fireHeight= // TODO: Setting power
				fireWidthInterval = fireWidth / sample ;
				fireHeightInterval = fireHeight / sample;
				//System.out.println("Fire Activated "+" fireWidth "+ fireWidth+" fireWidthInterval "+fireWidthInterval);
				//System.out.println("fireHeight "+ fireHeight+" fireWidthInterval "+fireHeightInterval);
				firingIteration++;
			}
			if(firingIteration < sample) { // kinetic shoot with symmetrical rising and falling
				
				cannonBallXTemp = cannonBallX + fireWidthInterval;
				
				if(firingIteration<sample/2) { // Rising
					cannonBallYTemp = cannonBallY - fireHeightInterval;
				} else { // Falling 
					cannonBallYTemp = cannonBallY + fireHeightInterval;
				}
				
				firingLineList.add(new Line2D.Double(cannonBallX,cannonBallY,cannonBallXTemp,cannonBallYTemp));
				
				//System.out.println("Line 2D "+cannonBallX+" "+cannonBallY+" "+cannonBallXTemp+" "+cannonBallYTemp+" "+fireHeightInterval);
				cannonBallX = cannonBallXTemp;
				cannonBallY = cannonBallYTemp;	
				firingIteration++;
				
			} else { //  if symmetrical shoot ended, the ball must continue to fall down platform. (it loops until collapsed)
				cannonBallXTemp = cannonBallX + fireWidthInterval;
				cannonBallYTemp = cannonBallY + fireHeightInterval;
				firingLineList.add(new Line2D.Double(cannonBallX,cannonBallY,cannonBallXTemp,cannonBallYTemp));
				//System.out.println("Line 2D "+cannonBallX+" "+cannonBallY+" "+cannonBallXTemp+" "+cannonBallYTemp+" "+fireHeightInterval);
				cannonBallX = cannonBallXTemp;
				cannonBallY = cannonBallYTemp;	
			} 
		}
	}

	private void checkCollision() {
		if(isCannonBallFired) {
			Point2D p = new Point2D.Double(cannonBallX,cannonBallY);
			
			//Enemy and ball collision
			if(enemy.contains(p)) {
				score++;
				isCannonBallFired = false;
				firingIteration = 0;
				isEnemyShooted = true;
				
				System.out.println("Hit");
				return;
			}
			
			// CannonBall and Platform collision
			if(platform.contains(p)) {
				cannonBallY = platformY - cannonBallHeight /2;
				isCannonBallFired = false; // stop if the collision happens
				firingIteration = 0;
				lives--;
				//System.out.println("Collision");
				return;
			}
			
			// If ball goes outside of area 
			if(cannonBallX + cannonBallWidth > width) {
				
				isCannonBallFired = false; // stop if the collision happens
				firingIteration = 0;
				lives--;
				System.out.println("Out of zone");
				return;
			}
			
		}
	}
	
	private void spawnEnemy() {
		enemyX = minEnemyX + (Math.random() * (maxEnemyX - minEnemyX)); // Math.random takes a number [0,1) range.
		// min +  1 ( max - min) = max
		// min + 0 (max - min) = min

		// Update Enemy factor for drawing on as Scale
		// if the score rises , the game goes to hard.
		if(score>1) { // prevent 1/0 problem
			enemyScaleFactor = 1 / (score * gameDiffucultyFactor); // results must be range of (0,1]
	        if(enemyScaleFactor<1) { // Checking scale factor in (0,1] range, but if enemyScaleFactor=1 there is no need for translation
	        	enemyWidthGame = enemyWidth * enemyScaleFactor;
	        	enemyHeightGame = enemyWidth * enemyScaleFactor; // it updates
	        }
		}
		
		// Updates Enemy Y after change size of enemyHeightGame
		enemyY= platformY-enemyHeightGame;
		
		// Resetting Enemy Texture
		enemyTextureGame = enemyTexture;
		
		System.out.println(enemyX+" "+enemyScaleFactor);
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
		case KeyEvent.VK_A:
			//score++;
			cannonX -= 5;
			cannonAngle = 0;
			keyPressed = true;
			break;
		case KeyEvent.VK_RIGHT:
			 cannonX += 5;
			 cannonAngle = 0;
			 keyPressed = true;
			break;
		case KeyEvent.VK_D:
			 cannonX += 5;
			 cannonAngle = 0;
			 keyPressed = true;
			break;
		case 32:
			if(!isCannonBallFired) {
				isCannonBallFired = true;
			}
			
		}
	}

	@Override
	public void keyReleased(KeyEvent e) {
		keyPressed = false;
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		// The cannon angle is changed by MouseDrag event
		if(!keyPressed) { // check for cannon movement activity
			p1 = e.getPoint();
			//System.out.println("Angle "+cannonAngle+" cannonBallX= "+cannonBallX+" cannonBallY= "+cannonBallY);
			cannonAngle = (Math.atan2(p1.y-y0,p1.x-x0) - Math.atan2(p.y-y0,p1.x-x0))/3; // TODO : Check that!
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
		cannonAngle = 0;
	
		isCannonBallVisible = false;
		cannonBallX = 0;
		cannonBallY = 0;
		
		firingLineList.clear();
		
		score = 0;
		lives = 5;
		
		spawnEnemy();
		
		repaint();	 
		//System.out.println("Game is restarted!");
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

class SplashPanel extends JPanel {

	public SplashPanel() {
		setPreferredSize(new Dimension(600, 600));
		setBackground(Color.MAGENTA);
	}

	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D g2 = (Graphics2D) g;

	}
}
