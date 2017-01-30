import java.awt.Canvas;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.text.DecimalFormat;

import java.awt.Rectangle;

public class GamePane extends Canvas implements MouseListener, Runnable {

	static boolean running = false;
	private Thread thread;
	private int tickCount;
	private int frameCount;

	static Rectangle drawRegion;

	static long timeElapsed = 0; // In milliseconds
	private long timeToAdd = 0;
	static long tickCounter = 0;

	int width;
	int height;
	int startingCarnivores, startingHerbivores;
	int startMinSpeed, startMaxSpeed; 
	int startMinRad, startMaxRad;
	int startMinEgg, startMaxEgg;
	double startMinEnergy, startMaxEnergy;
	double startMinMetabolism, startMaxMetabolism;
	double startMinFood, startMaxFood;
	private DrawArea drawArea;

	public GamePane(int width, int height, 
			int startingCarnivores, int startingHerbivores, 
			int startMinSpeed, int startMaxSpeed, 
			int startMinRad, int startMaxRad, 
			int startMinEgg, int startMaxEgg, 
			double startMinEnergy, double startMaxEnergy, 
			double startMinMetabolism, double startMaxMetabolism, 
			double startMinFood, double startMaxFood) {
		this.startingCarnivores = startingCarnivores;
		this.startingHerbivores = startingHerbivores;
		
		this.startMinSpeed = startMinSpeed;
		this.startMaxSpeed = startMaxSpeed;
		
		this.startMinRad = startMinRad;
		this.startMaxRad = startMaxRad;
		
		this.startMinEgg = startMinEgg;
		this.startMaxEgg = startMaxEgg;
		
		this.startMinEnergy = startMinEnergy;
		this.startMaxEnergy = startMaxEnergy;
		
		this.startMinMetabolism = startMinMetabolism;
		this.startMaxMetabolism = startMaxMetabolism;
		
		this.startMinFood = startMinFood;
		this.startMaxFood = startMaxFood;
		
		drawArea = new DrawArea(startingCarnivores, startingHerbivores, 
				startMinSpeed, startMaxSpeed, 
				startMinRad, startMaxRad, 
				startMinEgg, startMaxEgg, 
				startMinEnergy, startMaxEnergy, 
				startMinMetabolism, startMaxMetabolism, 
				startMinFood, startMaxFood);
		
	//public GamePane(int width, int height) {
		//drawArea = new DrawArea();
		this.width = width;
		this.height = height;

		this.addMouseListener(this);
		this.setPreferredSize(new Dimension(width, height)); // size

	}

	public synchronized void start() {
		if (running)
			return;

		running = true;
		thread = new Thread(this);
		thread.start();
	}

	public synchronized void stop() {
		if (!running)
			return;
		timeToAdd = timeElapsed;
		running = false;
		try {
			thread.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void run() {
		long lastTime = System.nanoTime();
		long timer = System.currentTimeMillis();
		long start = System.currentTimeMillis();
		double delta = 0.0;
		int ticksPerSecond = 30;
		double ns = 1000000000.0 / ticksPerSecond;
		int frames = 0;
		int ticks = 0;
		while (running) {
			long now = System.nanoTime();
			delta += (now - lastTime) / ns;
			lastTime = now;
			while (delta >= 1) {
				tick();
				ticks++;
				tickCounter++;
				delta--;
				timeElapsed = timeToAdd + System.currentTimeMillis() - start;
			}
			render();
			frames++;
			if (System.currentTimeMillis() - timer > 1000) {
				timer += 1000;
				frameCount = frames;
				tickCount = ticks;
				frames = 0;
				ticks = 0;
			}
		}
	}

	public void tick() { // Per tick
		drawArea.updatePositions();
		drawArea.decayFood();
		drawArea.eat();
		drawArea.spawnFood();
		drawArea.layEggs();
		drawArea.hatchEggs();
		drawArea.energyCheck();
		drawArea.eatCheck();
		Main.statsPanel.updateStats();
	}

	public void render() {
		BufferStrategy bs = getBufferStrategy();
		if (bs == null) {
			createBufferStrategy(2);
			return;
		}
		drawArea.updateImage();
		Graphics g = bs.getDrawGraphics();
		g.drawImage(drawArea.getSubimage(drawRegion.x, drawRegion.y, drawRegion.width, drawRegion.height), 0, 0,
				drawRegion.width / 2, drawRegion.height / 2, null);
		g.drawString("FPS: " + frameCount + " | Ticks: " + tickCount + " | Time Elapsed: "
				+ new DecimalFormat("#.###").format(timeElapsed / 1000.0) + "s", 5, 15);
		g.dispose();
		bs.show();
	}

	public void paint(Graphics g) {
		render();
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		boolean orgFound = false;
		boolean eggFound = false;
		boolean foodFound = false;
		int x = 2 * e.getX() + drawRegion.x;
		int y = 2 * e.getY() + drawRegion.y;
		System.out.println(x + " " + y);
		for (int i = 0; i < DrawArea.carnivores.size() && !orgFound; i++) {
			if (DrawArea.carnivores.get(i).hitbox.contains(x, y)) {
				StatsPanel.selectedOrg = DrawArea.carnivores.get(i);
				orgFound = true;
			}
		}
		for (int i = 0; i < DrawArea.herbivores.size() && !orgFound; i++) {
			if (DrawArea.herbivores.get(i).hitbox.contains(x, y)) {
				StatsPanel.selectedOrg = DrawArea.herbivores.get(i);
				orgFound = true;
			}
		}
		for (int i = 0; i < DrawArea.eggs.size() && !eggFound; i++) {
			if (DrawArea.eggs.get(i).hitbox.contains(x, y)) {
				StatsPanel.selectedEgg = DrawArea.eggs.get(i);
				eggFound = true;
			}
		}
		for (int i = 0; i < DrawArea.food.size() && !foodFound; i++) {
			if (DrawArea.food.get(i).hitbox.contains(x, y)) {
				StatsPanel.selectedFood = DrawArea.food.get(i);
				foodFound = true;
			}
		}
		
		if (orgFound) {
			StatsPanel.selectedEgg = null;
			StatsPanel.selectedFood = null;
		}
		else if (eggFound){
			StatsPanel.selectedOrg = null;
			StatsPanel.selectedFood = null;
		}
		else if (foodFound){
			StatsPanel.selectedOrg = null;
			StatsPanel.selectedEgg = null;
		}
		else{
			StatsPanel.selectedOrg = null;
			StatsPanel.selectedEgg = null;
			StatsPanel.selectedFood = null;
		}
		Main.statsPanel.updateStats();
		render();
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mousePressed(MouseEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseReleased(MouseEvent e) {
		// TODO Auto-generated method stub

	}
}
