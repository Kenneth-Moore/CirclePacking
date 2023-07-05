package graphbits;

public class Hexdot {

	public static final int mathcircrad = 300;
	
	private double radius = 10;
	
	private double pendingrad = -1;
	
	private final int i;
	private final int j;
	
	private double xpos = -1000;
	private double ypos = -1000;
	
	// Order is (-1,-1), (-1, 0), (0, 1), (1,1), (1,0), (0,-1)
	//      6
	//   1     5 
	//   2     4
	//      3
	public final Hexdot[] neighbors;


	public Hexdot(final int numneigh, final int i, final int j) {
		this.i = i;
		this.j = j;
		neighbors = new Hexdot[numneigh];
	}
	
	
	// This method attempts to set up the nighbors, but it might find that the configuration is 
	// invalid. which happens if removing a single dot can create two disjoing components.
	public boolean createneighbors(final boolean[][] blob, final Hexdot[][] solblob) {
		
    	if (neighbors.length <	 2) return false;
		
		final boolean neighslist[] = {blob[i-1][j-1], blob[i-1][j], blob[i][j+1],
                blob[i+1][j+1], blob[i+1][j], blob[i][j-1]};
		final Hexdot neighstruelist[] = {solblob[i-1][j-1], solblob[i-1][j], solblob[i][j+1],
				solblob[i+1][j+1], solblob[i+1][j], solblob[i][j-1]};
		

		// now we find the correct starting position.
		int startpos = 0;
		if (this.isBoundary()) {
			// There is at least one false in this case, so we move until we find it. Then we 
			// move until we find the first true. That is the correct starting point.
			int safety = 0;
			while (neighslist[startpos]) {
				startpos = addmod6(startpos, 1);
				safety += 1;
				if (safety >= 7) {
					System.out.println("Safety was called in loop 1");
					break;
				}
			}
			while (!neighslist[startpos]) {
				startpos = addmod6(startpos, 1);
				safety += 1;
				if (safety >= 14) {
					System.out.println("Safety was called in loop 2");
					break;
				}
			}
		}
		
		for (int neigh = 0; neigh < neighbors.length; neigh++) {
			
			neighbors[neigh] = neighstruelist[addmod6(startpos, neigh)];
		}
		
		try {
			this.printneighborhood();
			return true;
		} catch (NullPointerException e) {
			System.out.println("Hexdot line 77: Invalid graph: " + i + ", " + j);
			return false;
		}
		
	}
	
	// here we compute the xpos and ypos. 
	// If none of the neighbors have been placed, this is necessarily the first dot
	// we are placing, and it also is a boundary point. We thus just put it at (300-r, 0)
	// The next one placed must also be a boundary point.
	public boolean place() {
		
		final double r1;;
		final double x1;
		final double y1;
		
		final double r2;
		final double x2;
		final double y2;
		
		final int placers[] = {-1, -1};
		
		int pos = 0;
		
		boolean mod = false;
		
		for (int i = 0; i < neighbors.length; i++) {
			if (pos >= 2) break;
			if (neighbors[i].isplaced()) {
				placers[pos] = i;
				pos++;
				if (this.isBoundary()) break;
			}
		}
		
		if (pos == 2) { // There were two positioned neighbors
			r1 = neighbors[placers[0]].getRadius() + this.radius;
			x1 = neighbors[placers[0]].xpos;
			y1 = neighbors[placers[0]].ypos;
			
			r2 = neighbors[placers[1]].getRadius() + this.radius;
			x2 = neighbors[placers[1]].xpos;
			y2 = neighbors[placers[1]].ypos;
		}
		
		else if (pos == 1) { // Only one positioned nieghbor. This must be a boundary point. 
			
			r1 = mathcircrad - this.radius;
			x1 = 0;
			y1 = 0;
			
			r2 = neighbors[placers[0]].getRadius() + this.radius;
			x2 = neighbors[placers[0]].xpos;
			y2 = neighbors[placers[0]].ypos;
			
			mod = placers[0] == 0;
			
		}
		
		else {
			xpos =  this.radius - mathcircrad ;
			ypos = 0;
			
			//System.out.println(this.printcoords() + " was base placed at " + this.xpos + ", " + this.ypos);
			return true;
		}
		
//		final double coef1 = ((r1*r1) - (r2*r2))/(2*R*R);
//		final double coef2 = 0.5 * Math.sqrt( 
//					(2*(((r1*r1) + (r2*r2))/(R*R))) 
//					
//					- 
//					
//					(((((r1*r1) - (r2*r2))*((r1*r1) - (r2*r2)))/(R*R*R*R))) 
//					
//					- 
//					
//					1
//				);
//		
//		this.xpos = (0.5*(x1+x2)) + coef1*(x2-x1) + coef2*(y2-y1);
//		
//		this.ypos = (0.5*(y1+y2)) + coef1*(y2-y1) + coef2*(x1-x2);
		
		double centerdx = x1 - x2;
		double centerdy = y1 - y2;
		double R = Math.sqrt(centerdx * centerdx + centerdy * centerdy);
		if (!(Math.abs(r1 - r2) <= R && R <= r1 + r2)) { // no intersection
			return false; // empty list of results
		}
		  // intersection(s) should exist
		
		double R2 = R*R;
		double R4 = R2*R2;
		double a = (r1*r1 - r2*r2) / (2 * R2);
		double r2r2 = (r1*r1 - r2*r2);
		double c = Math.sqrt(2 * (r1*r1 + r2*r2) / R2 - (r2r2 * r2r2) / R4 - 1);
		
		double fx = (x1+x2) / 2 + a * (x2 - x1);
		double gx = c * (y2 - y1) / 2;
		double ix1 = fx + gx;
		double ix2 = fx - gx;
		
		double fy = (y1+y2) / 2 + a * (y2 - y1);
		double gy = c * (x1 - x2) / 2;
		double iy1 = fy + gy;
		double iy2 = fy - gy;
		
		if (mod) {
			this.xpos = ix2;
			this.ypos = iy2;
		}
		else {
			this.xpos = ix1;
			this.ypos = iy1;
		}

		//System.out.println(this.printcoords() + " was placed at " + this.xpos + ", " + this.ypos);
		return true;
	}
	
	public boolean isplaced() {
		return xpos > -900;
	}
	
	public static int addmod6(final int startpos, final int toadd) {
		
		int ans = startpos + toadd;
		while (ans >= 6) ans -= 6;
		return ans;
	}
	
	public double getRadius() {
		return radius;
	}

	public void updateRadius() {
		if (pendingrad > 0 && 155 > pendingrad) {
			radius = pendingrad;
			pendingrad = -1;
		} else if (155 > pendingrad) {
			System.out.println("Called updaterad in (" + i + ", " + j + ") but it was bad: " + pendingrad);
		} else {
			System.out.println("Called updaterad in (" + i + ", " + j + ") but it was too big:" + pendingrad);
		}
	}
	
	public void setPendingRadius(double radius) {
		pendingrad = radius;
	}
	
	public double getPendingRadius() {
		return pendingrad;
	}
	
	
	public double getXpos() {
		return xpos;
	}


	public double getYpos() {
		return ypos;
	}
	

	public boolean isBoundary() {
		return neighbors.length != 6;
	}
	   
    public String printcoords() {
    	return "(" + i + ", " + j + ")" ;
    }
    
    public String printneighborhood() {
    	if (neighbors.length==0) return i + ", " + j;
    	
    	String ans = i + ", " + j + ": " + neighbors[0].printcoords(); 
    	
    	for (int ind = 1; ind < neighbors.length; ind++) {
    		ans += ", " + neighbors[ind].printcoords();
    	}
    	return ans;
    }
}
