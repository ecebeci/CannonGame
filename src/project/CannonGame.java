/* Muhammet Emre Cebeci 1705950
 * 2021 - December
 * TODO:  shape icinde clipping stroke vs
 * TODO: yýlan sekli xor lu cisimler ile yap
 * 
 * TODO: Splash screen hazirla  * TODO:  Splash Screen de diffucult olsun
 * TODO: Splash Screen de oyun nasil oynanilir belirt fare ile sey space ile vs
 * 
 * TODO: Kalp sekli yerine top sekli olabilir veya ayrintili bir sey hmm

 Base ve Splash Screen hazirla biter oyun

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
import shapes.Enemy;

import java.awt.geom.*;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.event.MenuKeyListener;

import java.awt.print.*;

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
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); 
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
		mi.setMnemonic(KeyEvent.VK_R);
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
		case "Easy":
			gamePanel.gameDiffucultyFactor = 0.25;
			break;
		case "Medium":
			gamePanel.gameDiffucultyFactor = 0.50;
			break;
		case "Hard":
			gamePanel.gameDiffucultyFactor = 0.75;
			break;
		}
    }  
	
}

class GamePanel extends JPanel implements  Runnable, KeyListener, MouseListener, MouseMotionListener, Printable { 
	// == Game Settings == 
	boolean AntiAliasing = true;
	
	// == Game Situations ==
	boolean isPlayerLost = false;
	boolean isPlayerLostMessageSent = false;
	
	// === Area of the plot ===
	double width;
	double height;
	double platformX = 0;
	double platformY; 
	// double platformWidth;
	double platformHeight;
	
	BufferedImage backgroundImage = null;
	BufferedImage backgroundImageGame = null;
	
	// === Shapes === 
	Shape player = null;
	Shape cannonBall = null;
	Shape platform = null;
	Shape enemy = null;
	Shape powerBar = null;
	Shape castle = null;
	
	// == Cannon == 
	double cannonX;
	double cannonY;
	double cannonWidth = 70; // TODO : geometrik oranla!
	double cannonHeight = 50; // TODO : geometrik oranla!
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
	double enemyWidth = 300; // TODO : geometrik oranla!
	double enemyHeight = 300; // TODO : geometrik oranla!
	double minEnemyX;
	double maxEnemyX;
	
	double enemyWidthGame;
	double enemyHeightGame;
	
	double enemyLives;
	double enemyLivesGame;
	
	double enemySpeed;
	boolean ifEnemyMovingRight = true;
	
	BufferedImage enemyHeadTexture = null; // head
	BufferedImage enemyHeadTextureGame = null;
	
	BufferedImage enemyBodyTexture = null; // middle
	BufferedImage enemyBodyTextureGame = null;
	
			
	// == Enemy Animation ==
	boolean isEnemyShooted;
	int shootedIteration = 0;
	
	// == Power Bar ==
	boolean isSpacePressed = false;
	boolean isPowerBarRises = true;
	double powerBarX = 0;
	double powerBarY = 0;
	double powerBarWidth; // default max width
	double powerBarHeight; // default max width
	//double powerBarXGame;
	double powerBarYGame;
	// double powerBarWidthGame; do not need (width wont change when animation)
	double powerBarHeightGame = 150; // when space is pressed, The size is determined by ratio the current power
	
	// == Firing ==
	int sample = 25; // draw sample 
	int firingIteration = 0;
	List<Line2D> firingLineList = new ArrayList<>(); // Drawing Lines of firingLine
	double fireHeightMax; 
	double fireHeight; // IMPORTANT: it is changed by power bar!
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
	
	// == Castle ==
	BufferedImage castleImage = null;
	
	// == Platform == 
	BufferedImage brickImage = null;
	
	// == Game Options ==
	double gameDiffucultyFactor = 0.5; //easy  0.25 medium 0.5 hard 0.75
	
	double enemyScaleFactor = 1; // (0,1] (for Enemy Size Scale)
	
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
		
		// == Cannon ==
		cannonX = cannonWidth;
		cannonY = platformY-cannonHeight;
		
		// == Background ==
		URL urlBackground = getClass().getClassLoader().getResource("resources/sky.png");
		try {
		   backgroundImage = ImageIO.read(urlBackground);
		} catch (IOException ex) {
		     ex.printStackTrace();
		}	
		backgroundImageGame = backgroundImage; 
		
		URL urlBrick = getClass().getClassLoader().getResource("resources/brick.png");
		try {
		   brickImage = ImageIO.read(urlBrick);
		} catch (IOException ex) {
		     ex.printStackTrace();
		}	
		
		
		URL urlEnemyHeadTexture = getClass().getClassLoader().getResource("resources/enemyHead.png");
		try {
			enemyHeadTexture = ImageIO.read(urlEnemyHeadTexture);
		} catch (IOException ex) {
		     ex.printStackTrace();
		}	
		enemyHeadTextureGame = enemyHeadTexture;
		
		URL urlEnemyBodyTexture = getClass().getClassLoader().getResource("resources/enemyBodyTexture.png");
		try {
		   enemyBodyTexture = ImageIO.read(urlEnemyBodyTexture);
		} catch (IOException ex) {
		     ex.printStackTrace();
		}	
		enemyBodyTextureGame = enemyBodyTexture;
		
		
		URL urlCastleTexture = getClass().getClassLoader().getResource("resources/castle.png");
		try {
		   castleImage = ImageIO.read(urlCastleTexture);
		} catch (IOException ex) {
		     ex.printStackTrace();
		}	

		// Create lives location
		for(int i=0; i<lives; i++) {
			// TODO: Heart yerine cannon yap!?
			liveShape.add(new Heart((i)*50,0,heartWidth,heartHeight)); // i+1 de olabilir
		}
		
		// == Enemy ==
		enemyWidthGame = enemyWidth; 
		enemyHeightGame = enemyHeight;
		
		minEnemyX = width/4; // after width/4
		maxEnemyX = width - enemyWidth;
		
		enemyLivesGame = enemyLives;
		
		// == Firing ==
		fireHeightMax = 3 * height / 4; // TODO: tekrar bak
		
		// == Power Bar Construct ==
		powerBarWidth = width / 25;
		powerBarHeight = height - platformHeight - heartHeight;
		powerBarX = width / 100;
		powerBarY = heartHeight;

		// Spawn first enemy randomly in a position range
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
		
		if(isPlayerLost) {
			if(isPlayerLostMessageSent == false) {
			    isPlayerLostMessageSent = true;
				JOptionPane.showMessageDialog(null, "Your Score: " + score, "You Lost", JOptionPane.INFORMATION_MESSAGE);	
				// isPlayerLostMessageSent = true; infinite loop
			}
		}
		
	}

	private void gameDraw(Graphics2D g2) {
		// == Check Life Situation ==
        if(lives < 1) {
        	//score = 0;
            //lives = 5;
        	 //spawnEnemy();
            enemyScaleFactor = 1;
            isPlayerLost = true;
            backgroundImageGame = imageGrayScale(backgroundImage);
        }
        
	 	// == Draw Background == 
		TexturePaint tp = new TexturePaint(backgroundImageGame, new Rectangle2D.Double(0, 0, width, height));
		g2.setPaint(tp);
		platform = new Rectangle2D.Double(0, 0, width, height);
		g2.fill(platform); 
	
		// == Draw Platform with Painting Brick Texture ==
		tp = new TexturePaint(brickImage, new Rectangle2D.Double(0, platformY, 25, 25));
		g2.setPaint(tp);
	    platform = new Rectangle2D.Double(platformX, platformY, width, platformHeight);
	    g2.fill(platform);
	    
	    // == Draw Castle ==
		tp = new TexturePaint(castleImage, new Rectangle2D.Double(width/15, 2*height/5 -1, width/5, height - 2*height/5 - platformHeight));
		g2.setPaint(tp);
		castle = new Rectangle2D.Double(width/15, 2*height/5, width/5, height - 2*height/5 - platformHeight);
		g2.fill(castle); 
	    
		// == Draw Cannon Ball and Tracking Fire Lines ==
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
		
		// == Drawing Score Label == (Responsively for number digit changes with frc)
		FontRenderContext frc;
		Font scoreLabelFont = new Font("Trebuchet",Font.PLAIN,24);	
		String scoreLabel = "Score: " + String.valueOf(score);
		g2.setPaint(Color.BLUE);
		g2.setFont(scoreLabelFont);
		frc = g2.getFontRenderContext();
		double scoreLabelWidth = scoreLabelFont.getStringBounds(scoreLabel,frc).getWidth();
		double scoreLabelHeight =  scoreLabelFont.getStringBounds(scoreLabel,frc).getHeight();
		double scoreLabelX = width - scoreLabelWidth - 5; // -5 for margin
		double scoreLabelY = scoreLabelHeight;
		g2.drawString(scoreLabel,(int) scoreLabelX, (int)scoreLabelY);
		
		// == Draw Lives Shapes ==
		for(int i = 0; i< lives ;i++) {
			g2.setColor(Color.RED);
			g2.fill(liveShape.get(i));
		}
		
		// == Draw Difficulty Label ==
		Font difficultyLabelFont = new Font("Trebuchet",Font.PLAIN,18);	
		String difficultyLabel = "";
		if(gameDiffucultyFactor ==  0.75) {
			difficultyLabel = "Difficulty: Hard";
		} else if(gameDiffucultyFactor == 0.5) {
			difficultyLabel = "Difficulty: Medium";
		} else {
			difficultyLabel = "Difficulty: Easy";
		}
		
		g2.setPaint(Color.BLUE);
		g2.setFont(difficultyLabelFont);
		double difficultyLabelWidth = difficultyLabelFont.getStringBounds(difficultyLabel,frc).getWidth();
		double difficultyLabelHeight =  difficultyLabelFont.getStringBounds(difficultyLabel,frc).getHeight();
		double difficultyLabelX = scoreLabelX - difficultyLabelWidth - 10; 
		double difficultyLabelY = scoreLabelHeight;
		g2.drawString(difficultyLabel,(int) difficultyLabelX, (int)difficultyLabelY);
        
		// == Drawing Power Bar ==
	    if(isSpacePressed) {
	        GradientPaint powerBarPaint = new GradientPaint((int) powerBarX + (int)(powerBarWidth/2), (int) powerBarY 
	        		, Color.red, (int) powerBarX + (int)(powerBarWidth/2),
	        		(int) powerBarY + (int) powerBarHeight, Color.green, true);
	        g2.setPaint(powerBarPaint);
	    	powerBar = new Rectangle2D.Double(powerBarX,powerBarYGame,powerBarWidth,powerBarHeightGame);
	    	g2.fill(powerBar);
	    }  
        
        // == Draw Enemy ==
        
        float enemyTransparency = 1f;
        if(enemyLivesGame>0) {
        	enemyTransparency =  (float) ((enemyLivesGame)/enemyLives);
        	if(enemyTransparency + 0.6 < 1) {
        		enemyTransparency += 0.6;
        	}
        }
        
        enemy = new Enemy(g2,enemyX,enemyY,enemyWidthGame, enemyLivesGame > 0 ? enemyTransparency : 1f, enemyHeadTextureGame,enemyBodyTextureGame,enemyBodyTextureGame); 
        // enemyHeightGame is not required
        // enemyLivesGame = 1 - 1/lives transparency levels 0 to 1
        
       // g2.fill(enemy); // do not need. Enemy constructor draws shapes itself. We don't need to draw area (it causes duplicated)
        g2.setPaint(Color.BLACK);
        
		// == Draw Player ==
	    g2.setColor(Color.BLACK);
		
		if(!keyPressed ) { //  fixes that movement rotate glitch	
			player = new Cannon(cannonX,cannonY,cannonWidth,cannonHeight, cannonAngle, true); 
		} else {
			player = new Cannon(cannonX,cannonY,cannonWidth,cannonHeight, cannonAngle, false); // false for rotating
		}
		g2.fill(player);
	    
	}
	
	// === Animation and Checking Collision Thread ===
	@Override
	public void run() {
		while(true) {
			checkEnemyMovingAnimation();
			checkPowerBar();
			checkBallFiring();		
			checkCollision();
			checkPanelLimits();
			checkShootedEnemyAnimationAndSpawnEnemy(); // for shooted enemy animation with image processing
			
			repaint(); // calls paintComponent!
			
			try {
				Thread.sleep(20); // speed
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
		}
	}
	
	private void checkEnemyMovingAnimation() {
		if(!isPlayerLost) {
			if(!isEnemyShooted) {
				
				if(gameDiffucultyFactor >= 0.5) { // Medium and Hard
					if(ifEnemyMovingRight) {
						if(enemyX + enemyHeight + enemySpeed < width) {
						enemyX += enemySpeed;
						} else {
						ifEnemyMovingRight = false;
						}
					} else {
						if(enemyX - enemySpeed > minEnemyX) {
							enemyX -= enemySpeed;
						} else {
							ifEnemyMovingRight = true;
						}
					}
				}
			}
			
		}
	}


	private void checkPowerBar() {
		if(isSpacePressed) {
			if(isPowerBarRises) { // bar is rising up
				if(powerBarHeightGame<=powerBarHeight) {
					powerBarHeightGame += 10;
				} else {
					isPowerBarRises = false; // if it is on max position
					powerBarHeightGame -= 10;
				}
			} else { // bar is falling down
				if(powerBarHeightGame>10) { // not to 10 because under 10 is very slow
					powerBarHeightGame -= 10;
				} else {
					isPowerBarRises = true; // if it is on min position
					powerBarHeightGame += 10;
				}
			}
			powerBarYGame = powerBarY + powerBarHeight - powerBarHeightGame;
	
			fireHeight =  fireHeightMax * (powerBarHeightGame / powerBarHeight); // IMPORTANT: ratio 
		}
	}


	// Shooted enemy animation and Spawn Enemy
	private void checkShootedEnemyAnimationAndSpawnEnemy() {
		if(isEnemyShooted) {
			firingLineList.clear(); // clear firing Line List
			
			if(shootedIteration < 5) {
				enemyBodyTextureGame = imageGrayScale(enemyBodyTextureGame); // change image
			
				shootedIteration++;
				return;
				
			} else { // if enemy shooted animation iteration ends
				isCannonBallVisible = false;
				firingIteration = 0;
				
				// enemyHeadTextureGame = enemyHeadTexture; // 
				enemyBodyTextureGame = enemyBodyTexture;
				
				if(score>3) { 
					if((enemyLivesGame-1)==0) {
						enemySpeed += enemySpeed/3 ;
						enemyHeadTextureGame = imageGrayScale(enemyHeadTextureGame);
					}
				}
				
				isEnemyShooted = false;
				shootedIteration = 0;
			
			}
			
			if(enemyLivesGame < 1) {  // if enemy lives is 0
				isCannonBallVisible = false;
				firingIteration = 0;
				
				isEnemyShooted = false;
				shootedIteration = 0;
				
				backgroundImageGame = imageRGBFilter(backgroundImageGame,0,score * +10); // red filter
				backgroundImageGame = imageRGBFilter(backgroundImageGame,2,score * -5); // blue filter
			
				score += 1;
				lives = 5;
			
				spawnEnemy(); // Spawn Enemy
			}	
			
		}
		
	}
	
	//checkPanelLimits for player
	private void checkPanelLimits() {
		if(cannonX < 0) {  // en kosedeyken merkezden r cikarirsan sinir olur hmm! cizdim
			cannonX = 0; // sinira geri goturur
		}
		/* if(cannonX + cannonWidth > width) {
			cannonX = width - cannonWidth; // sinira geri goturur
		} */
		
		// width/4 limit
		if(cannonX + cannonWidth > width/4) {
			cannonX  =  width/4 - cannonWidth; 
		}

	}

	// Shooting animation
	private void checkBallFiring() {	
		if(isCannonBallFired) { 
			if(firingIteration == 0) { // first firing iteration
				firingLineList.clear();
				isCannonBallVisible = true;
				
				//For first iterator
				cannonBallX = cannonX + cannonWidth;
				cannonBallY = cannonY + cannonHeight/3; // cannonHeight proportion
				
				// fireHeight is calculated by power bar
				// Important!
				// English : r is found by taking the cotangent according to the height. So rotate angle and h and r are found.
				// Turkish: Yukseklige gore kotanjant alinarak r bulunur. Yani rotate acisi  ve h ile r bulunur, , defterde cizdin
				fireWidth = fireHeight * Math.atan2(1, -(cannonAngle*-90)) ; // cot(cannonAngle)=tan(1/-cannonAngle)
				
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
				isCannonBallFired = false;
				firingIteration = 0;
				isEnemyShooted = true;
				enemyLivesGame--;
				//System.out.println("Hit");
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
				//System.out.println("Out of zone");
				return;
			}
			
		}
	}
	
	private BufferedImage imageRGBFilter(BufferedImage img, int selectedRGB, int value) {
		BufferedImage imageOut = new BufferedImage(img.getWidth(),img.getHeight(),img.getType());
		WritableRaster rasterImgIn = img.getRaster();
		WritableRaster rasterImgOut = imageOut.getRaster();
		
		int[] rgba = new int[4]; 
		for(int y=0; y<img.getHeight(); y++) { //
			for(int x=0; x<img.getWidth(); x++) {
				rasterImgIn.getPixel(x, y, rgba); 
				
				switch(selectedRGB) {
				case 0:
					rgba[0] += value;
					if(rgba[0]>255) { // if the value is exceeded 255
						rgba[0] = 255;
					}
					if(rgba[0]<0) { 
						rgba[0] = 0;
					}
					break;
				case 1:
					rgba[1] += value;
					if(rgba[1]>255) { // if the value is exceeded 255
						rgba[1] = 255;
					}
					if(rgba[1]<0) { 
						rgba[1] = 0;
					}
					break;
				case 2:
					rgba[2] += value;
					if(rgba[1]>255) { // if the value is exceeded 255
						rgba[1] = 255;
					}
					if(rgba[1]<0) { // if the value is below 0
						rgba[1] = 0;
					}
					break;
				}
				
				rasterImgOut.setPixel(x, y, rgba);
			}
		}
		
		return imageOut;
	}

	private BufferedImage imageGrayScale(BufferedImage img) {
		BufferedImage imageOut = new BufferedImage(img.getWidth(),img.getHeight(),img.getType());
		WritableRaster rasterImgIn = img.getRaster();
		WritableRaster rasterImgOut = imageOut.getRaster();
	
		int[] rgba = new int[4]; 
		for(int y=0; y<img.getHeight(); y++) { 
			for(int x=0; x<img.getWidth(); x++) {
				rasterImgIn.getPixel(x, y, rgba); 
				int gray = (int) (rgba[0] + rgba[1] + rgba[2] / 10f);
				rgba [0] = rgba[1]= rgba[2] = gray;
				rasterImgOut.setPixel(x, y, rgba);
			}
		}
		
		return imageOut;
	}

	private void spawnEnemy() {
		
		// min +  1 ( max - min) = max
		// min + 0 (max - min) = min
		enemyX = minEnemyX + (Math.random() * (maxEnemyX - minEnemyX)); // Math.random takes a number [0,1) range.
		enemyY= platformY-enemyHeight;
		

		// Update Enemy factor for drawing on as Scale
		// if the score rises , the game goes to hard.
		if(score>1) { // prevent 1 divide by 0 problem
			enemyScaleFactor = 1 / (score * (gameDiffucultyFactor-0.2)) ; // results must be range of (0,1]
			if(enemyScaleFactor>1)
				enemyScaleFactor = 1;
		} 
		
	     // Checking scale factor in (0,1] range, but if enemyScaleFactor=1 there is no need for translation
	    enemyWidthGame = enemyWidth * enemyScaleFactor;
	    enemyHeightGame = enemyWidth * enemyScaleFactor; // it updates
	     
		// Updates Enemy Y after change size of enemyHeightGame
		enemyY= platformY-enemyHeightGame;
		
		// Next enemy Lives
		// TODO: Score will increased by score and diffuculty factor
		// score *  gameDiffucultyFactor 
		enemyLives = 3 + (score/2) * gameDiffucultyFactor;
		enemyLivesGame = enemyLives;
		
		// Enemy Speed
		enemySpeed = 3 + ( score * gameDiffucultyFactor) +  (enemyLives-enemyLivesGame+1);
				
		// Resetting Enemy Texture
		enemyBodyTextureGame = enemyBodyTexture;
		enemyHeadTextureGame = enemyHeadTexture;
		
		System.out.println("Enemy Spawned X:"+enemyX+" Scaling factor:"+enemyScaleFactor);
	}

	@Override
	public void keyTyped(KeyEvent e) {
		// TODO Auto-generated method stub
	}

	@Override
	public void keyPressed(KeyEvent e) {
		if(!isPlayerLost) {
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
		case KeyEvent.VK_SPACE:
			if(!isCannonBallFired) {
				if(!isSpacePressed) { // checking the power bar is active
					isSpacePressed = true;	
				}
			}
		}
		}
	}

	@Override
	public void keyReleased(KeyEvent e) {
		if(!isPlayerLost) {
		keyPressed = false;
		switch(e.getKeyCode()) {
			case KeyEvent.VK_SPACE: // Power bar is released
				isSpacePressed = false; // stopping the bar animation and hide the bar
				isCannonBallFired = true; // fire the ball
				
				powerBarHeightGame = 150; // set default power bar to 150
				isPowerBarRises = true;
				break;
		}
		}
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		if(!isPlayerLost) {
		// The cannon angle is changed by MouseDrag event
		if(!keyPressed) { // check for cannon movement activity
			p1 = e.getPoint();
			//System.out.println("Angle "+cannonAngle+" cannonBallX= "+cannonBallX+" cannonBallY= "+cannonBallY);
			cannonAngle = (Math.atan2(p1.y-y0,p1.x-x0) - Math.atan2(p.y-y0,p1.x-x0))/3; // TODO : Check that!
		}
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
		// == Player Situation == 
		isPlayerLost = false;
		isPlayerLostMessageSent = false;
		
		// == Background ==
		backgroundImageGame = backgroundImage; // resetting image
		
		// == Power Bar == 
		isSpacePressed = false;
		
		// == Cannon ==
		cannonX = cannonWidth;
		cannonY = platformY-cannonHeight;
		cannonAngle = 0;
		
		// == Cannon Ball and Firing List == 
		isCannonBallVisible = false;
		isCannonBallFired = false;
		
		cannonBallX = 0;
		cannonBallY = 0;
		firingIteration = 0;
		firingLineList.clear();
		
		// == Enemy ==
		enemyScaleFactor = 1;
		
		// == Power Bar ==
		isSpacePressed = false; 
		isPowerBarRises = true;
		
		// == Score and Lives ==
		score = 0;
		lives = 5;
		
		// == Re-Spawn Enemy == 
		spawnEnemy();
		
		// == Repaint ==
		repaint();	 
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

class SplashPanel extends JPanel implements Runnable {

	BufferedImage textImage = null;
	BufferedImage instruction1 = null;
    BufferedImage instruction2 = null;
    
	double backgroundX = 0;
	
	public SplashPanel() {
		setPreferredSize(new Dimension(600, 600));
		
		setBackground(Color.MAGENTA);
		
		URL url = getClass().getClassLoader().getResource("resources/creditsTexture.png");
		try {
			textImage = ImageIO.read(url);
		} catch (IOException ex) {
		     ex.printStackTrace();
		}
		
		url = getClass().getClassLoader().getResource("resources/instr1.png");
		try {
			instruction1 = ImageIO.read(url);
		} catch (IOException ex) {
		     ex.printStackTrace();
		}
		
		url = getClass().getClassLoader().getResource("resources/instr2.png");
		try {
			instruction2 = ImageIO.read(url);
		} catch (IOException ex) {
		     ex.printStackTrace();
		}
	}

	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D g2 = (Graphics2D) g;
		
	 	// == Draw Background == 
	
		// == Instructions == 
		g2.drawImage (instruction1,30,50,null);
		g2.drawImage (instruction2,400,250,null);
		
		// == Credits ==
		TexturePaint tp = new TexturePaint(textImage, new Rectangle2D.Double(-backgroundX, 0, 2000, 600));
		g2.setPaint(tp); 
		FontRenderContext frc;
		Font headerLabelFont = new Font("Trebuchet",Font.PLAIN,18);	
		String headerLabel = "Created by M. Emre Cebeci";
		g2.setFont(headerLabelFont);
		frc = g2.getFontRenderContext();
		double textWidth = headerLabelFont.getStringBounds(headerLabel,frc).getWidth();
		double textHeight =  600 - headerLabelFont.getStringBounds(headerLabel,frc).getHeight();
		g2.drawString(headerLabel,(int) (300 - textWidth/2), (int) textHeight);
		
		//String headerLabel = "Easy de degisiklik";
		
		Thread thread = new Thread(this);
		thread.start();
		
	}

	@Override
	public void run() {
		while(true) {
			checkBackgroundAnimation();
			
			repaint(); // calls paintComponent!
			
			try {
				Thread.sleep(2000); // speed
			} catch (InterruptedException e) {
				e.printStackTrace();
			}	
		}
	}

	private void checkBackgroundAnimation() {
		if(backgroundX+1<2000)
		backgroundX += 0.1;
		else 
			backgroundX = 0;
		
	}
}
