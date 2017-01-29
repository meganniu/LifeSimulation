import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.text.DecimalFormat;
import java.util.ArrayList;

public class Egg {
	BufferedImage img;

	Point pos;

	Rectangle hitbox;
	
	double angle;

	int speed;// ticks/pixel

	protected int detectRadius;

	int eggCycle;

	long timeBorn;

	double metabolism;
	
	int carnivorePoints;
	
	int accumulatedPoints;
	long chaseLength;
	/**
	 * Constructor for objects of class Egg
	 */
	public Egg(Point pos, double angle, int speed, int detectRadius, int eggCycle, int carnivorePoints, double metabolism, long chaseLength) {
		
		this.speed = speed;
		this.angle = angle;
		this.pos = pos;
		this.detectRadius = detectRadius;
		this.eggCycle = eggCycle;
		//this.accumulatedPoints = accumulatedPoints;
		this.timeBorn= GamePane.timeElapsed;
		this.carnivorePoints = carnivorePoints;
		this.metabolism = metabolism;
		this.chaseLength = chaseLength;
		mutate();
		img = DrawArea.eImg;
		hitbox = new Rectangle(pos.x - 8, pos.y - 8, 16, 16);
	}
	
	public void mutate(){
		while(Math.random()>.5){
			speed++;
		}
		while(Math.random()>.5){
			speed--;
		}
		while(Math.random()>.2){
			detectRadius ++;
		}
		while(Math.random()>.2){
			detectRadius --;
		}
		while(Math.random()>.2){
			metabolism ++;
		}
		while(Math.random()>.2){
			metabolism --;
		}
		while(Math.random()>.6){
			carnivorePoints++;
		}
		while(Math.random()>.6){
			carnivorePoints--;
		}
		while(Math.random()>.2){
			chaseLength += 10;
		}
		while(Math.random()>.2){
			chaseLength -= 10;
		}
		while(Math.random()>.2){
			eggCycle += 50;
		}
		while(Math.random()>.2){
			eggCycle -= 50;
		}
		if (speed >= 10)
			detectRadius =  detectRadius / 3;
		if (speed < 2)
			speed = 2;
		if (detectRadius < 50)
			detectRadius = 50;
		if (eggCycle < 25000)
			eggCycle = 25000;
		if (carnivorePoints < 2)
			carnivorePoints = 2;
		if (metabolism < 60.0)
			metabolism = 60.0;
		if (chaseLength < 3500)
			chaseLength = 3500;
	}
	
	public BufferedImage getImage() {
		return img;
	}

	public Point getPoint() {
		return pos;
	}

	public double getAngle() {
		return angle;
	}

	public boolean hatch() {
		if (timeBorn + 10000 <= GamePane.timeElapsed) {
			if (carnivorePoints >= 10) {
				DrawArea.carnivores.add(new Carnivore(new Point(pos), angle, speed, detectRadius, eggCycle, carnivorePoints, 4000, metabolism, chaseLength));
			} else {
				DrawArea.herbivores.add(new Herbivore(new Point(pos), angle, speed, detectRadius, eggCycle, carnivorePoints, 4000, metabolism, 5000));
			}
			System.out.println("Egg hatched at" + GamePane.timeElapsed / 1000.0);
			return true;
		}
		return false;
	}
	
	public ArrayList<String> getStats() {
		ArrayList<String> stats = new ArrayList<String>();
		stats.add("<html><pre><span style=\"font-family: arial\">Egg\t\t");
		stats.add("<html><pre><span style=\"font-family: arial\">Position\t\t(" + pos.x + ", " + pos.y + ")</span></pre></html>");
		stats.add("<html><pre><span style=\"font-family: arial\">Angle\t\t" + (int) angle + " deg</span></pre></html>");
		stats.add("<html><pre><span style=\"font-family: arial\">Speed\t\t" + speed + "</span></pre></html>");
		stats.add("<html><pre><span style=\"font-family: arial\">R. Detection\t" +  detectRadius + "</span></pre></html>");
		stats.add("<html><pre><span style=\"font-family: arial\">Egg Counter\t" + eggCycle + "</span></pre></html>");
		stats.add("<html><pre><span style=\"font-family: arial\">Carnivorism\t" + carnivorePoints + "</span></pre></html>");
		stats.add("<html><pre><span style=\"font-family: arial\">Metabolism\t" + new DecimalFormat("#.##").format(metabolism) + "</span></pre></html>");
		stats.add("<html><pre><span style=\"font-family: arial\">Chase Length\t" + chaseLength + "</span></pre></html>");
		stats.add("<html><pre><span style=\"font-family: arial\">Time Until Hatch\t" + (timeBorn + 10000 - GamePane.timeElapsed) + "</span></pre></html>");
		return stats;
	}
}
