package graphbits;

import java.util.ArrayList;

import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

public class Hexblob {
	
	public static final int mathcircrad = 300;

	public final static int blobradius = 100;
	public final static double boundary3constant = Math.pow(1.39, 65/14);
	
//	private final static int newtoniters = 5;
	
	public final boolean blob[][] = new boolean[2*blobradius][2*blobradius];
	public final Hexdot solblob[][] = new Hexdot[2*blobradius][2*blobradius];
	
	public Hexblob() {
		for (int i = 0; i < 2*blobradius; i++) {
			for (int j = 0; j < 2*blobradius; j++) {
				blob[i][j] = false;
				solblob[i][j] = null;
			}
		}
	}
	
	
	public void clear() {
		for (int i = 0; i < 2*blobradius; i++) {
			for (int j = 0; j < 2*blobradius; j++) {
				blob[i][j] = false;
			}
		}
	}
	
	public boolean get(final int i, final int j) {
		
		try {
			return blob[i + blobradius][j + blobradius];
		} catch (IndexOutOfBoundsException e) {
			System.out.println("Went out of bounds in Hexblob get: " + i + ", " + j);
			return false;
		}
	}
	
	public ArrayList<Hexdot> getdots() {
		final ArrayList<Hexdot> result = new ArrayList<>();
		
		for (int i=0; i < 2*blobradius; i++) {
			for (int j=0; j < 2*blobradius; j++) {
				if (blob[i][j]) result.add(solblob[i][j]);
			}
		}
		
		return result;
	}
	
	public boolean isplaced(final int i, final int j) {
		
		try {
			return solblob[i + blobradius][j + blobradius].isplaced();
		} catch (IndexOutOfBoundsException e) {
			System.out.println("Went out of bounds in Hexblob isplaced: " + i + ", " + j);
			return false;
		} catch (NullPointerException e) {
			System.out.println("Tried to check isplaced but there was no dot: " + i + ", " + j);
			return false;
		}
	}
	
	
	public void set(final double pixi, final double pixj, final double scale, final boolean set) {
		int newx = (int) Math.round(tohexX(pixi, scale));
		int newy = (int) Math.round(tohexY(pixi, pixj, scale));
		
		try {
			blob[newx + blobradius][newy + blobradius] = set;
		} catch (IndexOutOfBoundsException e) {
			System.out.println("Went out of bounds in Hexblob set: " + newx + ", " + newy);
		}
	}
	
	public void hexset(final int i, final int j, final boolean set) {
		blob[i + blobradius][j + blobradius] = set;
	}
	
	// this returns true if the operation was successful.
	public boolean makeDots(final int iters) {
		int count = 0;
		for (int i = 1; i < 2*blobradius-1; i++) {
			for (int j = 1; j < 2*blobradius-1; j++) {
				if (!blob[i][j]) continue; // no dot was placed here, so skip
				count++;
				int neighs = 0;
				boolean neighslist[] = {blob[i-1][j-1], blob[i-1][j], blob[i][j+1],
		                blob[i+1][j+1], blob[i+1][j], blob[i][j-1]};
				
				for (int around = 0; around < 6; around++) {
					if (neighslist[around]) neighs += 1;
				}
				
				final Hexdot dot = new Hexdot(neighs, i, j);
				solblob[i][j] = dot;
			}
		}
		if (count < 3) {
			System.out.println("Not enough dots to make a graph");
			return false;
		} else {
			System.out.println("Total input: " + count);
		}
			
		
		
		boolean valid = true;
		for (int i = 1; i < 2*blobradius-1; i++) {
			for (int j = 1; j < 2*blobradius-1; j++) {
				if (!blob[i][j]) continue; 
				// There should be a dot here now
				valid = solblob[i][j].createneighbors(blob, solblob);
				
			}
		}
		if (!valid) {
			System.out.println("Wasn't valid, returning in Hexblob line 94");
			return false;
		}
				
		for (int iter = 0; iter < iters; iter++) {
			
			// Choose an internal vertex v of the input graph
			for (int i = 1; i < 2*blobradius-1; i++) {
				for (int j = 1; j < 2*blobradius-1; j++) {
					if (!blob[i][j]) continue;
					// There should be a dot here
					
					try {
						if (solblob[i][j].isBoundary()) solblob[i][j].setPendingRadius(boundaryprob(i, j));
						else                            solblob[i][j].setPendingRadius(interiorprob(i, j));
					} catch (NullPointerException e) {
						System.out.println("Somehow skipped the valid check (hexblob line 115)");
						return false;
					} catch (IndexOutOfBoundsException e) {
						System.out.println("Out of bounds in Hexblob 118: " + e.getMessage());
						return false;
					}
				}
			}
			
			for (int i = 1; i < 2*blobradius-1; i++) {
				for (int j = 1; j < 2*blobradius-1; j++) {
					if (!blob[i][j]) continue;
					// There should be a dot here
					solblob[i][j].updateRadius();
				}
			}
			
			
			// I now think that my algorithm must work without this mulitplier somehow. The shrinking
			// every iteration indicates something is wrong. But how to do it...
			double multiplier = exteriorprob();
			
			for (int i = 1; i < 2*blobradius-1; i++) {
				for (int j = 1; j < 2*blobradius-1; j++) {
					if (!blob[i][j]) continue;
					// There should be a dot here
					
					solblob[i][j].setPendingRadius(solblob[i][j].getRadius()*multiplier);
					// the last modification to the rad has finished.
					solblob[i][j].updateRadius();
				}
			}
		}
		
		Hexdot currentdot = null;
		for (int n = 0; n < (2*blobradius-1)*(2*blobradius-1); n++) {
			int i = n % (2*blobradius-1);
			int j = n / (2*blobradius-1);

			if (!blob[i][j]) continue;
			// found the starting one
			currentdot = solblob[i][j];
			break;
		}
		int safety = 0;

		while (!currentdot.isplaced()) {
			// place the boundary dots
			safety++;
			currentdot.place();
			currentdot = currentdot.neighbors[0];
			if (safety > 2000) {
				System.out.println("Safety was called in placingloop, Hexblob 166");
				return false;
			}
		}
		
		for (int i = 0; i < 2*blobradius-1; i++) {
			for (int j = 0; j < 2*blobradius-1; j++) {
				if (!blob[i][j]) continue;
				if (solblob[i][j].isplaced()) continue;
				
				solblob[i][j].place();
			}
		}
		
		return true;
	}
	
	
	// we find how big the radius of the exterior circle should be, and instead of setting it to that,
	// we multiply everything else by 1 / that.
	private double exteriorprob() {
		// Calculate the total angle θ that its k neighboring circles would cover around the 
		//   circle for v, if the neighbors were placed tangent to each other and to the central 
		//   circle using their tentative radii.
		double theta = 0;
		int count = 0;
		for (int i = 1; i < 2*blobradius-1; i++) {
			for (int j = 1; j < 2*blobradius-1; j++) {
				if (!blob[i][j]) continue;
				// There should be a dot here
				Hexdot blob1 = solblob[i][j];
				if (blob1.isBoundary()) {
					Hexdot blob2 = solblob[i][j].neighbors[0];
					count += 1;
					
					theta += cosinelaw2(blob2.getRadius() + blob1.getRadius(),
							  mathcircrad - blob2.getRadius(),
							  mathcircrad - blob1.getRadius());
				}
			}
		}

		// Determine a representative radius r for the neighboring circles, such that k circles 
		//   of radius r would give the same covering angle θ as the neighbors of v give.
		final double reprad = findintrep(theta/count);
		
		// Set the new radius for v to be the value for which k circles of radius r would give 
		//   a covering angle of exactly 2pi.
		final double newrad = newboundaryintrad(2*Math.PI/count, reprad);
		
		//System.out.println("count: " + count + ", reprad: " + reprad + ", newrad: " + newrad + ", theta: " + theta);
		
		return mathcircrad/newrad;
	}
	 
	private double boundaryprob(final int i, final int j) {
		// Calculate the total angle θ that its k neighboring circles would cover around the 
		//   circle for v, if the neighbors were placed tangent to each other and to the central 
		//   circle using their tentative radii.
		Hexdot center = solblob[i][j];
		double theta = 0;

		final int k = center.neighbors.length;
		for (int neigh = 0; neigh < k-1; neigh++) {
			// add to theta for each pair of interior neighbors.
			theta += cosinelaw(center.getRadius(), 
							   center.neighbors[neigh].getRadius(), 
					           center.neighbors[Hexdot.addmod6(neigh,1)].getRadius());
		}
		
		theta += Math.PI - cosinelaw2(mathcircrad - center.neighbors[0].getRadius(),
				  mathcircrad - center.getRadius(), 
				  center.neighbors[0].getRadius() + center.getRadius());
		theta += Math.PI - cosinelaw2(mathcircrad - center.neighbors[k-1].getRadius(),
				  mathcircrad - center.getRadius(), 
				  center.neighbors[k-1].getRadius() + center.getRadius());
		
		// Determine a representative radius r for the neighboring circles, such that k circles 
		//   of radius r would give the same covering angle θ as the neighbors of v give.
		final double repradius = findrep(center.getRadius(), theta/(k + 1.0));
		
//		String msg = i + ", " + j + " centerad: " + center.getRadius() + ", reprad: " + repradius + ", k: " + k + ", theta: " + theta
		//		 + " new: " + newboundaryoutrad(k, repradius);
//		for (int n = 0; n < center.neighbors.length; n++) {
//			msg = msg + ", rad_" + n + ": " + center.neighbors[n].getRadius();
//		}
//		
//		System.out.println(msg);
		
		// Set the new radius for v to be the value for which k+1 circles of radius reprad 
		// along with the boundary would give a covering angle of exactly 2pi.
		return newboundaryoutrad(k, repradius);
	}
	
	
//	private double invertrad(final double x, final double y, final double r) {
//		
//		final double f = Math.sqrt((x*x) + (y*y));
//		
//		return (GameScreen.mathcircrad * GameScreen.mathcircrad) / r ;
//	}
	
	
	// The following was a very tricky thing that didn't work in the end.
//	private double newtonboundaryrad(final int k,  final double r) {
//		
//		final double initialguess;
//		
//		if (k == 2) {
//			initialguess = 2;
//		}
//		
//		else if (k == 3) {
//			initialguess = (r/1.9671) + Math.pow(1.39,(r+65)/14) - boundary3constant + Math.pow(5.3, (r-90)/10);
//		}
//		
//		else if (k == 4) {
//			initialguess = (101.3*Math.asin(r/85)) - (6*Math.sin(r/85)) - (2*Math.sin(r/22));
//		}
//		
//		else if (k == 5) {
//			initialguess = (108*Math.asin(r/71.5)) - (1.6*Math.sin(r/17.9));
//		}
//		else {
//			System.out.println("ERROR: had an invalid k in newboundaryrad");
//			return 10;
//		}
//		//System.out.println("initialguess, r = " + r + " k = " + k + " -> " + initialguess);
//		return newtonsmethod(initialguess, r, k);
//	}
//	
//	private static double newtonsmethod(final double init, final double r, final int k) {
//		
//		double guess = init;
//		for (int i = 0; i < newtoniters; i++) {
//			//System.out.println(" > NEWTON " + k + ": " + init + " -> " + guess + "  eff: " + eff(guess, r, k) + "  deff: " + deff(guess, r, k));
//			guess = guess - (eff(guess, r, k)/deff(guess, r, k));
//		}
//		
//		return guess;
//	}
//	
//	private static double eff(final double x, final double r, final int k) {
//		final double R = GameScreen.mathcircrad;
//		
//		return Math.acos((((x+r)*(x+r))-(2*r*r))/((x+r)*(x+r))) 
//				
//			   - 
//			   
//			   (2.0/(k-1.0)) * 
//			   Math.acos((((R-x)*(R-x))+((x+r)*(x+r))-((R-r)*(R-r)))/(2*(R-x)*(x+r)));
//	}
//	
//	private static double deff(double x, final double r, final int k) {
//		final double R = GameScreen.mathcircrad;
//
//		final double a = r+x;
//		final double b = R-r;
//		final double c = R-x;
//		
//		return (2*
//				((a - c)/(a*c) 
//				+ ((-b*b) + (a*a) + (c*c))/(2*a*c*c) 
//				- ((-b*b) + (a*a) + (c*c))/(2*a*a*c)))
//				/
//				((k - 1) * 
//				    Math.sqrt(
//					    1 - ((((-b*b) + (a*a) + (c*c))*((-b*b) + (a*a) + (c*c))) / (4*a*a*c*c))
//				    )
//			    ) 
//				
//				- 
//				
//				((((2*a*a) - (4*r*r))/(-(a*a*a))) + (2/a))
//				/
//				Math.sqrt(1 - ((((2*a*a) - (4*r*r))*((2*a*a) - (4*r*r)))/(4*a*a*a*a)));
//		
//	}
	
	private double newboundaryintrad(final double theta, final double repradius) {
		
		final double d = Math.sqrt(1-Math.cos(theta));
		
		return repradius * ((Math.sqrt(2)/d) + 1.0);
	}
	
	private double newboundaryoutrad(final int k, final double repradius) {
		
		final double d = Math.sqrt(1-Math.cos((2*Math.PI)/(k+1.0)));
		
		return (repradius * ((Math.sqrt(2)/d) - 1.0));
	}
	
	private double interiorprob(final int i, final int j) {
		// Calculate the total angle θ that its 6 neighboring circles would cover around the 
		//   circle for v, if the neighbors were placed tangent to each other and to the central 
		//   circle using their tentative radii.
		
		Hexdot center = solblob[i][j];
		double theta = 0;
		
		for (int neigh = 0; neigh < 6; neigh++) {
			// add to theta for each pair of neighbors.
			theta += cosinelaw(center.getRadius(), 
							   center.neighbors[neigh].getRadius(), 
					           center.neighbors[Hexdot.addmod6(neigh,1)].getRadius());
		}

		// Determine a representative radius r for the neighboring circles, such that 6 circles 
		//   of radius r would give the same covering angle θ as the neighbors of v give.
		final double repradius = findrep(center.getRadius(), theta/6);
		
		// Set the new radius for v to be the value for which 6 circles of radius r would give 
		//   a covering angle of exactly 2π.
		//   NOTE: since there are always 6 in this case, the answer is also just r.
		
		return repradius;
	}
	
	
	private double findrep(final double rad, final double theta) {
		
		final double d = Math.sqrt(1 - Math.cos(theta));
		
		return d*rad / (Math.sqrt(2)-d);
	}
	
	private double findintrep(final double theta) {
		
		final double d = Math.sqrt(1 - Math.cos(theta));
		
		return d*mathcircrad / (Math.sqrt(2)+d);
	}
	
	
	
	
	private double cosinelaw(final double x, final double y, final double z) {
		final double a = y+z; // the distance between the two neighbors 
		
		final double b = x+y; 
		final double c = x+z;
		
		//System.out.println("x: " + x + " y: " + y + " z: " + z);
		
		return Math.acos(((b*b)+(c*c)-(a*a)) / (2*b*c));
		
	}
	
	private double cosinelaw2(final double a, final double b, final double c) {
		return Math.acos(((b*b)+(c*c)-(a*a)) / (2*b*c));
	}
	
	
	public Circle visu(final int i, final int j, final double scale) {
		Circle vis = new Circle(topixX(i, scale), topixY(i, j, scale), 10*scale);
		try {
			if (solblob[i+blobradius][j+blobradius].isBoundary()) vis.setFill(Color.NAVY);
			
		} catch (IndexOutOfBoundsException e) {
			System.out.println("Went out of bounds in Hexblob visu: " + i + ", " + j);
		} catch (NullPointerException e) {
			System.out.println("Made it all the way to visu but there was no dot " + i + ", " + j);
		}
		return vis;
	}
	
	public Circle solvisu(final int i, final int j) {
		try {
			Circle vis =  new Circle(solblob[i+blobradius][j+blobradius].getXpos(), 
					solblob[i+blobradius][j+blobradius].getYpos(), solblob[i+blobradius][j+blobradius].getRadius());
			vis.setFill(Color.TRANSPARENT);
			vis.setStroke(Color.BLACK);
			
			if (vis.getCenterX() > 305 || vis.getCenterX() < - 305 || vis.getCenterY() < - 305 || vis.getCenterY() > 305)
				vis = new Circle(0, 0, 10, Color.DARKRED);
			return vis;
		} catch (NullPointerException e) {
			// probably wasn't a valid graph.
			Circle vis = new Circle(0,0,10);
			vis.setFill(Color.DARKRED);
			return vis;

		}

	}
	
	public static double tohexX(final double x, final double scale) {
		return x/(Math.sqrt(3)*scale*10);
	}
	
	public static double tohexY(final double x, final double y, final double scale) {
		return y/(2*scale*10)+x/(2*Math.sqrt(3)*scale*10);
	}
	
	public static double topixX(final double x, final double scale) {
		return Math.sqrt(3)*scale*10*x;
	}
	
	public static double topixY(final double x, final double y, final double scale) {
		return (2*scale*10*y) - (topixX(x,scale)/ Math.sqrt(3));
	}
	
}
