
//source utilisé pour me guider : https://github.com/ymykoliv/SDES/blob/master/src/main/java/cryptography/SDES.java
public class SDES {
	static int permutate(int bits, int... pos) {
        int permutatedBits = 0;
        for(int i = 0; i < pos.length; i++)
            permutatedBits |= ((bits >>> pos[i]) & 1) << i;
        return permutatedBits;
    }
	static int P10(int key){ return permutate(key, 4,2,1,9,0,6,3,8,5,7);}
    static int LS(int key) { return permutate(key, 4,0,1,2,3,9,5,6,7,8);}
    static int P8(int key) { return permutate(key, 1,0,5,2,6,3,7,4);}
	//-----------------------------------------------------------------------------//

	static int getK1(int key) {return P8(LS(P10(key)));}
	static int getK2(int key) {
        //return P8(LS(LS(LS(P10(key))))); d'après le pdf d'ilustration transmise en TP
        return P8(LS(LS(P10(key))));
    }
	//-----------------------------------------------------------------------------//

	static int IP(int plainText) {return permutate(plainText, 1,3,0,4,7,5,2,6);}
	static int inverseIP(int cryptoText) {return permutate(cryptoText, 2, 0, 6, 1, 3, 5, 7, 4);}
	//-----------------------------------------------------------------------------//
	 
	 static int F(int plainText, int subKey) {
        int permutation = permutate(plainText, 3,0,1,2,1,2,3,0);
        permutation ^= subKey;

        int substituted = 0;
        int i = ((permutation & (1 << 7)) >>> 6) | (permutation & (1 << 4)) >>> 4;
        int j = ((permutation & (1 << 6)) >>> 5) | (permutation & (1 << 5)) >>> 5;
        substituted |= S0[i][j] << 2;
        i = ((permutation & (1 << 3)) >>> 2) | (permutation & 1);
        j = ((permutation & (1 << 2)) >>> 1) | (permutation & (1 << 1)) >>> 1;
        substituted |= S1[i][j];

        return permutate(substituted, 3,1,0,2);
     }

    private final static int[][] S0 = new int[][] {
            {1,0,3,2},
            {3,2,1,0},
            {0,2,1,3},
            {3,1,3,1}
    };

    private final static int[][] S1 = new int[][] {
            {1,1,2,3},
            {2,0,1,3},
            {3,0,1,0},
            {2,1,0,3}
    };
	//-----------------------------------------------------------------------------//

    static int f(int plainText, int subKey) {
	     int L = plainText >>> 4;
	     int R = plainText << 28 >>> 28;
	     return (L^F(R, subKey)) << 4 | R;
	 }
	 static int encrypt(int c, int k1,int k2) {
	     int result = f(IP(c), k1);
	     result = (result << 28) >>> 24 | (result >>> 4);
	     result = f(result,k2);
	     return inverseIP(result);
	 }
	 static int decrypt(int c, int k1,int k2) {return encrypt(c, k2,k1);}
	//-----------------------------------------------------------------------------//
	 
	 public static String encrypt(String msg,  int k1, int k2) {
	     StringBuilder builder = new StringBuilder(msg.length());
	     for (int i = 0; i < msg.length(); i++)
	         builder.append((char)encrypt(msg.charAt(i), k1,k2));

	     return builder.toString();
	 }
	 public static String decrypt(String msg, int k1, int k2) {
         StringBuilder builder = new StringBuilder(msg.length());
         for (int i = 0; i < msg.length(); i++)
             builder.append((char)decrypt(msg.charAt(i), k1,k2));

         return builder.toString();
    }
	//-----------------------------------------------------------------------------//

}
