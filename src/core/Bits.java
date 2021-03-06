package core;

import java.util.*;

/**
 * Utility class containing all bit twiddling hacks and the like
 * <br>
 * Integers in Java are two's complement with big endian, and
 * floating point follows IEEE format
 * <br>
 * We use little endian when possible
 * <br>
 * In general, if it isn't provided here, the standard library
 * methods are already optimized (though the JIT would be able
 * to optimize the standard library but not these)
 * 
 * @author EPICI
 * @version 1.0
 */
public final class Bits {
	
	//Disallow invoking constructor
	private Bits(){}
	
	/**
	 * Sets a bit in an int value, little endian
	 * 
	 * @param encoded the original int value
	 * @param index the position of the bit to change, from 0 to 31 inclusive,
	 * 31 would be the sign bit
	 * @param bit the bit to write
	 * @return the modified int
	 */
	public static int writeBit(int encoded,int index,boolean bit){
		int bits = 1<<index;
		if(bit){
			return encoded | bits;
		}else{
			return encoded & (~bits);
		}
	}
	
	/**
	 * Reads a bit from an int value, little endian
	 * 
	 * @param encoded the int value to read from
	 * @param index the position of the bit to read, from 0 to 31 inclusive,
	 * 31 would be the sign bit
	 * @return true if the bit is 1, false if the bit is 0
	 */
	public static boolean readBit(int encoded,int index){
		return ((encoded>>index)&1)!=0;
	}
	
	/**
	 * Sets a bit in a long value, little endian
	 * 
	 * @param encoded the original long value
	 * @param index the position of the bit to change, from 0 to 63 inclusive,
	 * 63 would be the sign bit
	 * @param bit the bit to write
	 * @return the modified long
	 */
	public static long writeBit(long encoded,int index,boolean bit){
		long bits = 1L<<index;
		if(bit){
			return encoded | bits;
		}else{
			return encoded & (~bits);
		}
	}
	
	/**
	 * Reads a bit from an long value, little endian
	 * 
	 * @param encoded the long value to read from
	 * @param index the position of the bit to read, from 0 to 63 inclusive,
	 * 63 would be the sign bit
	 * @return true if the bit is 1, false if the bit is 0
	 */
	public static boolean readBit(long encoded,int index){
		return ((encoded>>index)&1)!=0;
	}
	
	/**
	 * Gets an int with a 1 at the specified position and
	 * the rest 0s, little endian
	 * 
	 * @param index the position, from 0 to 31 inclusive, 31 is sign bit
	 * @return said int value
	 */
	public static int oneAtInt(int index){
		return 1<<index;
	}
	
	/**
	 * Gets an long with a 1 at the specified position and
	 * the rest 0s, little endian
	 * 
	 * @param index the position, from 0 to 63 inclusive, 63 is sign bit
	 * @return said long value
	 */
	public static long oneAtLong(int index){
		return 1L<<index;
	}
	
	/**
	 * Fast (floor) log base 2 for integers
	 * <br>
	 * Taken from http://stackoverflow.com/questions/3305059/how-do-you-calculate-log-base-2-in-java-for-integers#3305710
	 * <br>
	 * Returns 0 for 0
	 * <br>
	 * If the original number was a power of 2, 1&lt;&lt;(the returned result) should be original number
	 * 
	 * @param bits the integer to calculate the log base 2 of
	 * @return the log base 2 of that integer
	 */
	public static int binLog(int bits){
		int log = 0;
		if( ( bits & 0xffff0000 ) != 0 ) { bits >>>= 16; log = 16; }
		if( bits >= 256 ) { bits >>>= 8; log += 8; }
		if( bits >= 16  ) { bits >>>= 4; log += 4; }
		if( bits >= 4   ) { bits >>>= 2; log += 2; }
		return log + ( bits >>> 1 );
	}
	
	/**
	 * Fast power of 2 test
	 * <br>
	 * Also returns true for 0, even though it isn't a power of 2
	 * 
	 * @param bits the number to test
	 * @return true if the number is a power of 2
	 */
	public static boolean isPo2(int bits){
		return (bits&(bits-1))==0;
	}
	
	/**
	 * Makes all bits to the right of the most significant bits 1
	 * <br>
	 * For any negative number, returns -1
	 * 
	 * @param bits the original number
	 * @return the modified number
	 */
	public static int fill(int bits){
		bits |= bits>>1;
		bits |= bits>>2;
		bits |= bits>>4;
		bits |= bits>>8;
		bits |= bits>>16;
		return bits;
	}
	
	/**
	 * Return the smallest power of 2 which is greater than
	 * the given number
	 * <br>
	 * No error checking
	 * 
	 * @param bits the number to find the next power of 2 for
	 * @return the next power of 2, or 0
	 */
	public static int gtPo2(int bits){
		return fill(bits)+1;
	}
	
	/**
	 * Returns the smallest power of 2 which is greater than
	 * or equals to the given number
	 * <br>
	 * No error checking
	 * 
	 * @param bits the number to find the next power of 2 for
	 * @return the next power of 2, or 0
	 */
	public static int gePo2(int bits){
		return fill(bits-1)+1;
	}
	
	/**
	 * Absolute value without branching
	 * <br>
	 * From https://graphics.stanford.edu/~seander/bithacks.html
	 * <br>
	 * Breaks for -2^31
	 * 
	 * @param bits some number
	 * @return the absolute value
	 */
	public static int abs(int bits){
		int mask = bits>>-1;
		return (bits+mask)^mask;
	}
	
	/**
	 * Absolute value without branching
	 * <br>
	 * From https://graphics.stanford.edu/~seander/bithacks.html
	 * <br>
	 * Breaks for -2^63
	 * 
	 * @param bits some number
	 * @return the absolute value
	 */
	public static long abs(long bits){
		long mask = bits>>-1;
		return (bits+mask)^mask;
	}
	
	/**
	 * Shift a bitset left (negative=right) by a certain amount,
	 * treating it as a little endian integer
	 * <br>
	 * Since we can't do it without copying anyway, this will always
	 * return a different object
	 * <br>
	 * Returns null if b is null
	 * 
	 * @param b bitset to shift
	 * @param n left shift amount
	 * @return shifted bitset
	 */
	public static BitSet shiftLeft(BitSet b,int n){
		if(b==null)return null;
		if(n==0)return (BitSet)b.clone();
		long[] a = b.toLongArray();
		int l = a.length;
		if(n>0){// Left shift, mul by 2^n
			int x=n>>>6,y=n&63,z=64-y;
			if(y==0){// No fractional words
				long[] r = new long[l+x];
				System.arraycopy(a, 0, r, x, l);
				return BitSet.valueOf(r);
			}
			long[] r = new long[l+x+1];
			r[x]=a[0]<<y;
			for(int i=1;i<l;i++){
				r[i+x]=a[i-1]>>>z|a[i]<<y;
			}
			r[l+x]=a[l-1]>>>z;
			return BitSet.valueOf(r);
		}else{// Right shift, floordiv by 2^n
			n=-n;
			int x=n>>>6,y=n&63,z=64-y;
			if(x>=l)return new BitSet();// All shifted out
			if(y==0){// No fractional words
				long[] r = new long[l-x];
				System.arraycopy(a, x, r, 0, l-x);
				return BitSet.valueOf(r);
			}
			long[] r = new long[l-x];
			r[l-x-1]=a[l-1]>>>y;
			for(int i=l-2;i>=x;i--){
				r[i-x]=a[i]>>>y|a[i+1]<<z;
			}
			return BitSet.valueOf(r);
		}
	}
	
	/**
	 * Main method, used only for testing
	 * 
	 * @param args ignored
	 */
	public static void main(String[] args){
		Random random = new Random();
		//Log base 2 sanity tests
		System.out.println("--- Binary logarithm ---");
		System.out.println("log2(1024)="+binLog(1024)+" (expected 10)");
		System.out.println("log2(3000)="+binLog(3000)+" (expected 11)");
		System.out.println("log2(14)="+binLog(14)+" (expected 3)");
		System.out.println("log2(73902)="+binLog(73902)+" (expected 16)");
		//Power of 2 sanity tests
		System.out.println("--- Power of 2 test ---");
		System.out.println("ispo2(65535)="+isPo2(65535)+" (expected false)");
		System.out.println("ispo2(65536)="+isPo2(65536)+" (expected true)");
		System.out.println("ispo2(65537)="+isPo2(65537)+" (expected false)");
		System.out.println("ispo2(3)="+isPo2(3)+" (expected false)");
		System.out.println("ispo2(4)="+isPo2(4)+" (expected true)");
		//Next power of 2 sanity tests
		System.out.println("--- Next power of 2 ---");
		System.out.println("gtpo2(2)="+gtPo2(2)+" (expected 4)");
		System.out.println("gtpo2(23)="+gtPo2(23)+" (expected 32)");
		System.out.println("gtpo2(234)="+gtPo2(234)+" (expected 256)");
		System.out.println("gepo2(511)="+gePo2(511)+" (expected 512)");
		System.out.println("gepo2(512)="+gePo2(512)+" (expected 512)");
		System.out.println("gepo2(513)="+gePo2(513)+" (expected 1024)");
		System.out.println("gtpo2(1<<30)="+gtPo2(1<<30)+" (expected -1<<31)");
		//Absolute value sanity tests
		System.out.println("--- Absolute value ---");
		System.out.println("abs(0)="+abs(0));
		System.out.println("abs(1)="+abs(1));
		System.out.println("abs(1777)="+abs(1777));
		System.out.println("abs(-1)="+abs(-1));
		System.out.println("abs(-1777)="+abs(1777));
		System.out.println("abs(12345678987654321)="+abs(12345678987654321L));
		System.out.println("abs(-12345678987654321)="+abs(-12345678987654321L));
		System.out.println("abs(2147483647)="+abs(2147483647));
		System.out.println("abs(-2147483647)="+abs(-2147483647));
		System.out.println("abs(-2147483648)="+abs(-2147483648));
		//BitSet manipulation sanity tests
		System.out.println("--- BitSet ---");
		BitSet bsa = new BitSet();
		bsa.set(100);
		for(int i=101;i<300;i++)bsa.set(i,random.nextBoolean());
		BitSet bsb = (BitSet) bsa.clone();
		bsa = shiftLeft(bsa,64);
		System.out.println("BitSet shift test 1: "+(!bsa.equals(bsb)?"pass":"fail"));
		bsa = shiftLeft(bsa,-5);
		bsa = shiftLeft(bsa,100);
		bsa = shiftLeft(bsa,-128);
		bsa = shiftLeft(bsa,-31);
		System.out.println("BitSet shift test 2: "+(bsa.equals(bsb)?"pass":"fail"));
		bsb.clear(100);
		bsa = shiftLeft(bsa,-101);
		bsa = shiftLeft(bsa,101);
		System.out.println("BitSet shift test 3: "+(bsa.equals(bsb)?"pass":"fail"));
	}
}
