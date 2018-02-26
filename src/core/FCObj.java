package core;

import java.util.*;

/**
 * Represents a game object
 * <br>
 * If its hash is used anywhere, it should be treated as immutable
 * 
 * @author EPICI
 * @version 1.0
 */
public class FCObj {
	
	/**
	 * Comparator using the object z value, useful for sorting
	 */
	public static final Comparator<FCObj> Z_COMPARE = new Comparator<FCObj>(){

		@Override
		public int compare(FCObj a, FCObj b) {
			return Integer.compare(a.z, b.z);
		}
		
	};
	
	/**
	 * Translates an object name to a standard type
	 */
	public static final HashMap<String,Integer> nameToType = new HashMap<>();
	/**
	 * Translates a standard type to an object name
	 */
	public static final HashMap<Integer,String> typeToName = new HashMap<>();
	/**
	 * Use this bit to test if a type is a circle or a rectangle
	 */
	public static final int TYPE_CIRCLE = 0;
	/**
	 * Use this bit to test if a type cannot be rotated (only areas are not rotatable)
	 */
	public static final int TYPE_UNROTATABLE = 1;
	/**
	 * Use this bit to test if a type can move or not
	 */
	public static final int TYPE_MOVABLE = 2;
	/**
	 * Use this bit to test if a type is a design piece or level piece
	 */
	public static final int TYPE_DESIGN = 3;
	/**
	 * Use this bit to test if a type can be jointed
	 */
	public static final int TYPE_JOINTABLE = 4;
	/**
	 * Use this bit to test if a type collides with everything or not everything
	 */
	public static final int TYPE_COLLIDES = 5;
	/**
	 * Use this bit to test if a type is a goal area/piece or not
	 */
	public static final int TYPE_GOAL = 6;
	/**
	 * Use this bit to test if a type is powered
	 */
	public static final int TYPE_POWERED = 7;
	/**
	 * Use this bit to test if a type spins clockwise
	 */
	public static final int TYPE_CLOCKWISE = 8;
	/**
	 * Use this bit to test if a type collides with other of itself or not
	 */
	public static final int TYPE_NOSELFCOLLIDE = 9;
	
	/**
	 * Quickly add a pair
	 * 
	 * @param name
	 * @param type
	 */
	public static void addNameType(String name,int type){
		nameToType.put(name, type);
		typeToName.put(type, name);
	}
	/**
	 * Quickly add a pair
	 * 
	 * @param name
	 * @param baseType original type
	 * @param ts flags to toggle
	 */
	public static void addNameTypeBits(String name,int baseType,int...ts){
		for(int i:ts){
			baseType^=(1<<i);
		}
		addNameType(name,baseType);
	}
	/**
	 * Add other names which are identical to some name
	 * 
	 * @param to the reference name
	 * @param from
	 */
	public static void addAlias(String to,String...from){
		int typeData = nameToType.get(to);
		for(String v:from){
			nameToType.put(v, typeData);
		}
	}
	
	/**
	 * Add both rectangle and circle for type
	 * 
	 * @param name
	 * @param baseType
	 * @param ts
	 */
	public static void addNameTypeBitsPair(String name,int baseType,int...ts){
		for(int i:ts){
			baseType^=(1<<i);
		}
		addNameType(name+"Rect",baseType);
		baseType^=(1<<TYPE_CIRCLE);
		addNameType(name+"Circle",baseType);
	}
	
	static{
		// Add special
		addNameTypeBits("Empty",0);
		// Add nonstandard and standalone aliases first
		addNameTypeBits("UnpowGoalCircle",0,TYPE_MOVABLE,TYPE_GOAL,TYPE_DESIGN,TYPE_JOINTABLE,TYPE_CIRCLE);
		addNameTypeBits("CWGoalCircle",0,TYPE_MOVABLE,TYPE_GOAL,TYPE_DESIGN,TYPE_JOINTABLE,TYPE_CIRCLE,TYPE_POWERED,TYPE_CLOCKWISE);
		addNameTypeBits("CCWGoalCircle",0,TYPE_MOVABLE,TYPE_GOAL,TYPE_DESIGN,TYPE_JOINTABLE,TYPE_CIRCLE,TYPE_POWERED);
		// Add standard types next
		addNameTypeBits("BuildArea",0,TYPE_UNROTATABLE);
		addNameTypeBits("GoalArea",0,TYPE_UNROTATABLE,TYPE_GOAL);
		addNameTypeBitsPair("Static",0,TYPE_COLLIDES);
		addNameTypeBitsPair("Dynamic",0,TYPE_COLLIDES,TYPE_MOVABLE);
		addNameTypeBitsPair("Goal",0,TYPE_MOVABLE,TYPE_GOAL,TYPE_DESIGN,TYPE_JOINTABLE);
		addNameTypeBits("UnpowWheel",0,TYPE_MOVABLE,TYPE_DESIGN,TYPE_JOINTABLE,TYPE_CIRCLE);
		addNameTypeBits("CWWheel",0,TYPE_MOVABLE,TYPE_DESIGN,TYPE_JOINTABLE,TYPE_CIRCLE,TYPE_POWERED,TYPE_CLOCKWISE);
		addNameTypeBits("CCWWheel",0,TYPE_MOVABLE,TYPE_DESIGN,TYPE_JOINTABLE,TYPE_CIRCLE,TYPE_POWERED);
		addNameTypeBits("WaterRod",0,TYPE_MOVABLE,TYPE_DESIGN,TYPE_JOINTABLE,TYPE_NOSELFCOLLIDE);
		addNameTypeBits("WoodRod",0,TYPE_MOVABLE,TYPE_DESIGN,TYPE_JOINTABLE);
		// Add separate aliases after
		addAlias("BuildArea","BA","Build");
		addAlias("GoalArea","GA","Goal");
		addAlias("StaticRect","SR");
		addAlias("StaticCircle","SC");
		addAlias("GoalRect","GR");
		addAlias("GoalCircle","GC","GP","UG");
		addAlias("UnpowWheel","UW","Unpowered");
		addAlias("CWWheel","WW","Clockwise");
		addAlias("CCWWheel","CW","Counterclockwise");
		addAlias("CWGoalCircle","WG");
		addAlias("CCWGoalCircle","CG");
		addAlias("WaterRod","BR","Water");
		addAlias("WoodRod","WR","Wood");
	}
	
	public int getTypeData(){
		return nameToType.get(type);
	}
	
	/**
	 * For level pieces, is in [-2,-1] and determines render order
	 * <br>
	 * For design pieces, is 0 or higher, is index as well
	 */
	public int z=-1;
	/**
	 * Base type (name)
	 */
	public String type;
	/**
	 * Center (x,y), dimensions (w,h), rotation degrees (r)
	 */
	public double x,y,w,h,r;
	/**
	 * If another object's z value is in this list, then they are connected
	 */
	public ArrayList<Integer> joints = new ArrayList<>();
	
	/**
	 * Default constructor, does nothing
	 */
	public FCObj(){
		
	}
	
	/**
	 * Copy constructor
	 * 
	 * @param source object to copy
	 */
	public FCObj(FCObj source){
		if(source==null)throw new NullPointerException("Can't copy a null object");
		z=source.z;
		type=source.type;
		x=source.x;
		y=source.y;
		w=source.w;
		h=source.h;
		r=source.r;
		joints=new ArrayList<>(source.joints);
	}
	
	/**
	 * Parse the text using a given format
	 * 
	 * @param text text containing a single FC object
	 * @param format string to indicate the format used
	 */
	public FCObj(String text,String format){
		switch(format){
		case "fcml":{
			String[] tokens = text.split("[\\s(),\\[\\]]+");
			int ntokens = tokens.length;
			String[] typeBits = tokens[0].split("#");
			type = typeBits[0];
			if(!nameToType.containsKey(type))throw new IllegalArgumentException(text+"\nFC object type \""+type+"\" not recognized");
			int typeData = nameToType.get(type);
			if(Bits.readBit(typeData, TYPE_UNROTATABLE))z=-2;
			if(Bits.readBit(typeData, TYPE_DESIGN))z=0;
			if(typeBits.length==2){
				if(!Bits.readBit(typeData,TYPE_DESIGN))throw new IllegalArgumentException(text+"\nOnly designs can have an index");
				z = Integer.parseInt(typeBits[1]);
				if(z<0)throw new IllegalArgumentException(text+"\nIndex cannot be negative");
			}
			if(typeBits.length>2)throw new IllegalArgumentException(text+"\nCan only have one index.");
			x = Double.parseDouble(tokens[1]);
			y = Double.parseDouble(tokens[2]);
			w = Double.parseDouble(tokens[3]);
			h = ntokens>4?Double.parseDouble(tokens[4]):w;
			if(Bits.readBit(typeData,TYPE_CIRCLE)&&!Floats.isNear(w, h))throw new IllegalArgumentException(text+"\nThis type is a circle, so the width and height (diameter) must be the same");
			r = ntokens>5?Double.parseDouble(tokens[5]):0;
			if(Bits.readBit(typeData,TYPE_UNROTATABLE)&&!Floats.isNear(r, 0))throw new IllegalArgumentException(text+"\nThis type cannot be rotated");
			for(int i=6;i<ntokens;i++){
				String tk = tokens[i].trim();
				if(tk.length()>0){
					joints.add(Integer.parseInt(tk));
				}
			}
			if(!Bits.readBit(typeData,TYPE_DESIGN)&&joints.size()>0)throw new IllegalArgumentException(text+"\nThis type is not a design piece, so it cannot be jointed");
			break;
		}
		default:{
			throw new IllegalArgumentException("FC object format \""+format+"\" not known");
		}
		}
	}
	
	public boolean equals(Object o){
		if(o==null)return false;
		if(o==this)return true;
		if(!(o instanceof FCObj))return false;
		FCObj fo = (FCObj) o;
		return Objects.equals(type, fo.type)
				&&Floats.isNear(x, fo.x)
				&&Floats.isNear(y, fo.y)
				&&Floats.isNear(w, fo.w)
				&&Floats.isNear(h, fo.h)
				&&Floats.isNearMod(r, fo.r, 180);
	}
	
	public int hashCode(){
		final long p=0x2f107d62abd44853L;
		long r=p;
		r=r*p+Objects.hashCode(type);
		r=r*p+(Double.doubleToLongBits(x)>>>32);
		r=r*p+(Double.doubleToLongBits(y)>>>32);
		r=r*p+(Double.doubleToLongBits(w)>>>32);
		r=r*p+(Double.doubleToLongBits(h)>>>32);
		r=r*p+Math.floorMod(Math.round(r), 180);
		return Long.hashCode(r);
	}
	
	public String toString(){
		return toString("fcml");
	}
	
	/**
	 * Export this object in a canonical format
	 * 
	 * @param format format type
	 * @return
	 */
	public String toString(String format){
		switch(format){
		case "fcml":{
			StringBuilder sb = new StringBuilder();
			sb.append(type);
			if(z>=0){
				sb.append('#');
				sb.append(z);
			}
			sb.append(" (");
			sb.append(x);
			sb.append(", ");
			sb.append(y);
			sb.append("), (");
			sb.append(w);
			sb.append(", ");
			sb.append(h);
			sb.append("), ");
			sb.append(r);
			int jn = joints.size();
			if(jn>0){
				sb.append(", [");
				sb.append(joints.get(0));
				for(int i=1;i<jn;i++){
					sb.append(", ");
					sb.append(joints.get(i));
				}
				sb.append(']');
			}
		}
		default:{
			throw new IllegalArgumentException("Unrecognized format \""+format+"\"");
		}
		}
	}
	
	/**
	 * Return coordinates of the corners
	 * <br>
	 * Even if it is not a rectangle, it is treated as such
	 * 
	 * @return
	 */
	public double[][] corners(){
		final double x = this.x,
				y = this.y,
				rx = this.w*0.5,
				ry = this.h*0.5,
				r = Math.toRadians(this.r),
				cr = Math.cos(r),
				sr = Math.sin(r),
				rcx = rx*cr,
				rcy = ry*cr,
				rsx = rx*sr,
				rsy = ry*sr,
				ix1 = rcx-rsy,
				iy1 = rsx+rcy,
				ix2 = rcx+rsy,
				iy2 = rsx-rcy;
		final double[][] result = {
				{x+ix1,y+iy1},
				{x+ix2,y+iy2},
				{x-ix1,y-iy1},
				{x-ix2,y-iy2}
				};
		return result;
	}
	
	/**
	 * Is it contained in this area?
	 * <br>
	 * Negative area is allowed
	 * 
	 * @param px
	 * @param py
	 * @return
	 */
	public boolean contains(double px,double py){
		boolean iscircle = Bits.readBit(getTypeData(), TYPE_CIRCLE);
		if(iscircle){
			return Math.hypot(x-px, y-py)<=Math.abs(w*0.5);
		}else{
			final double x = this.x,
					y = this.y,
					rx = Math.abs(this.w*0.5),
					ry = Math.abs(this.h*0.5),
					r = -Math.toRadians(this.r),// Invert rotation
					cr = Math.cos(r),
					sr = Math.sin(r);
			px -= x;
			py -= y;
			final double nx = px*cr-py*sr;
			py = px*sr+py*cr;
			px = nx;
			return -rx<=px&&px<=rx && -ry<=py&&py<=ry;
		}
	}
	
	/**
	 * Do these two objects' shapes intersect?
	 * 
	 * @param other
	 * @return
	 */
	public boolean intersects(FCObj other){
		boolean iscircle = Bits.readBit(getTypeData(), TYPE_CIRCLE);
		boolean oiscircle = Bits.readBit(other.getTypeData(), TYPE_CIRCLE);
		if(iscircle){
			if(oiscircle){
				return intersectsCC(other);
			}else{
				return other.intersectsRC(other);
			}
		}else{
			if(oiscircle){
				return intersectsRC(other);
			}else{
				return intersectsRR(other);
			}
		}
	}
	
	/**
	 * Like <i>intersects</i> but it assumes
	 * they are both rectangles
	 * 
	 * @param other
	 * @return
	 */
	public boolean intersectsRR(FCObj other){
		return intersectsPP(corners(),other.corners());
	}
	
	/**
	 * Like <i>intersects</i> but it assumes
	 * this is a rectangle and the other is a circle
	 * 
	 * @param other
	 * @return
	 */
	public boolean intersectsRC(FCObj other){
		double px = other.x,
				py = other.y,
				pr = Math.abs(other.w*0.5);
		final double x = this.x,
				y = this.y,
				rx = Math.abs(this.w*0.5),
				ry = Math.abs(this.h*0.5),
				r = -Math.toRadians(this.r),// Invert rotation
				cr = Math.cos(r),
				sr = Math.sin(r);
		px -= x;
		py -= y;
		final double nx = Math.abs(px*cr-py*sr);
		py = Math.abs(px*sr+py*cr);
		px = nx;
		if(px>rx+pr||py>ry+pr)return false;
		if(px<=rx||py<=ry)return true;
		return Math.hypot(px-rx, py-ry)<=pr;
	}
	
	/**
	 * Like <i>intersects</i> but it assumes
	 * they are both circles
	 * 
	 * @param other
	 * @return
	 */
	public boolean intersectsCC(FCObj other){
		return Math.hypot(x-other.x, y-other.y)<=Math.abs(w)+Math.abs(other.w);
	}
	
	/**
	 * With both polygons expressed as arrays of (x,y) arrays
	 * in either clockwise or counterclockwise order, tests
	 * for their intersection
	 * 
	 * @param a
	 * @param b
	 * @return
	 */
	public static boolean intersectsPP(double[][] a,double[][] b){
		for(int i=0;i<2;i++){
			double[][] poly = i==0?a:b;
			int n = poly.length;
			for(int i1=0;i1<n;i1++){
				int i2=i1+1;
				if(i2==n)i2=0;
				double[] p1 = poly[i1];
				double[] p2 = poly[i2];
				double nx = p2[1]-p1[1];
				double ny = p1[0]-p2[0];
				double mina = Double.POSITIVE_INFINITY, maxa = Double.NEGATIVE_INFINITY;
				for(double[] p:a){
					double proj = nx*p[0]+ny*p[1];
					mina = Math.min(mina, proj);
					maxa = Math.max(maxa, proj);
				}
				double minb = Double.POSITIVE_INFINITY, maxb = Double.NEGATIVE_INFINITY;
				for(double[] p:b){
					double proj = nx*p[0]+ny*p[1];
					minb = Math.min(minb, proj);
					maxb = Math.max(maxb, proj);
				}
				if(maxa<minb||maxb<mina)return false;
			}
		}
		return true;
	}
	
}
