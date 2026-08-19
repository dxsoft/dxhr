package com.dxsoft.rsgzgl.security.ukey.sm2;

import java.math.BigInteger;



public class SM2SM3 {
	private static final int BYTE_LENGTH = 32;
	private static final int BLOCK_LENGTH = 64;
	private static final int BUFFER_LENGTH = 64*2;
	private byte[] xBuf = new byte[BUFFER_LENGTH];
	private int xBufOff;
	private byte[] V = SM3.iv;
	private int cntBlock = 0; 
	
	public static BigInteger ecc_p;
	public static BigInteger ecc_a;
	public static BigInteger ecc_b;
	public static BigInteger ecc_n;
	public static BigInteger ecc_gx;
	public static BigInteger ecc_gy;
	
	public static ECCurve ecc_curve;
	public static ECPoint ecc_point_g;
	
	public SM2SM3() {
	}
	

     public static byte[] GetE(byte[] z, byte[] HashMsgValue)
     {
    	 SM2SM3 digest = new SM2SM3();

    	 digest.update(z, 0, z.length);

         digest.update(HashMsgValue, 0, 32);

         byte[] md = new byte[32];
         digest.doFinal(md, 0);
         return md;

     }

     public static byte[] GetMsgHash(String msg)
     {
    	 SM2SM3 digest = new SM2SM3();
         byte[] p = msg.getBytes();
         digest.update(p, 0, p.length);

         byte[] md = new byte[32];
         digest.doFinal(md, 0);
         return md;

     }
	
	public  static boolean YtVerfiy(String id, String InString, String PubKeyX,String PubKeyY,String VerfiySign )
    {
    		
    		SM2SM3 digest = new SM2SM3();
    		BigInteger affineX = new BigInteger(PubKeyX, 16);
    		BigInteger affineY = new BigInteger(PubKeyY, 16);

    		byte[] z =  digest.Sm2GetZ(affineX, affineY, id.getBytes());
    		
    		byte []MsgHash=GetMsgHash(InString);
    		
    		byte []E=GetE(z,MsgHash);
    		
    		BigInteger r = new BigInteger(VerfiySign.substring(0, 64), 16);
    		BigInteger s = new BigInteger(VerfiySign.substring(64, 128), 16);

            return digest.subSm2Verify(E,affineX,affineY, r, s );
            

    }
	
	private static boolean subSm2Verify(byte[] md,  BigInteger PubKeyX, BigInteger PubKeyY,BigInteger r, BigInteger s)
	{
		SM2Result sm2Ret= new SM2Result();;
		ECFieldElement ecc_gx_fieldelement;
		ECFieldElement ecc_gy_fieldelement;

        ecc_gx_fieldelement = new ECFieldElement.Fp(Util.p, Util.Gx);
        ecc_gy_fieldelement = new ECFieldElement.Fp(Util.p,  Util.Gy);
        
        ecc_curve = new ECCurve.Fp(Util.p, Util.a, Util.b);
        ecc_point_g=new ECPoint.Fp(ecc_curve, ecc_gx_fieldelement, ecc_gy_fieldelement);
        
        ECFieldElement ecc_kx_fieldelement = new ECFieldElement.Fp(Util.p, PubKeyX);
        ECFieldElement ecc_ky_fieldelement = new ECFieldElement.Fp(Util.p, PubKeyY);
        ECPoint userKey = new ECPoint.Fp(ecc_curve, ecc_kx_fieldelement, ecc_ky_fieldelement);
		
		sm2Ret.R = null;
		
		// e_
        BigInteger e = new BigInteger(1, md);
		// t
		BigInteger t = r.add(s).mod(Util.n);

        if (t.equals(BigInteger.ZERO))
			return false;
		
		// x1y1
		ECPoint x1y1 = ecc_point_g.multiply(s);
		x1y1 = x1y1.add(userKey.multiply(t));
		
		// R
		sm2Ret.R = e.add(x1y1.x.toBigInteger()).mod(Util.n);
		
		if (r.equals(sm2Ret.R))
		{
			return true;
		}
		return false;
	}
	
	/**
	 * SM3������
	 * @param out ����SM3�ṹ�Ļ�����
	 * @param outOff ������ƫ����
	 * @return
	 */
	public int doFinal(byte[] out, int outOff) {
		byte[] tmp = doFinal();
		System.arraycopy(tmp, 0, out, 0, tmp.length);
		return BYTE_LENGTH;
	}

	public String getAlgorithmName() {
		return "SM3";
	}

	public int getDigestSize() {
		return BYTE_LENGTH;
	}

	public void reset() {
		xBufOff = 0;
		cntBlock = 0;
		V = SM3.iv;
	}

	
	/**
	 * ��������
	 * @param in �������뻺����
	 * @param inOff ������ƫ����
	 * @param len ���ĳ���
	 */
	public void update(byte[] in, int inOff, int len) {
		if(xBufOff+len > BUFFER_LENGTH) {
			int tmpLen = xBufOff+len-BUFFER_LENGTH;
			System.arraycopy(in, inOff, xBuf, xBufOff, BUFFER_LENGTH-xBufOff);
			doUpdate();
			xBufOff = 0;
			int i=1;
			while(tmpLen > BUFFER_LENGTH) {
				tmpLen -= BUFFER_LENGTH;
				System.arraycopy(in, inOff+BUFFER_LENGTH*i, xBuf, xBufOff, BUFFER_LENGTH-xBufOff);
				doUpdate();
				xBufOff = 0;
				i++;
			}
			System.arraycopy(in, inOff+len-tmpLen, xBuf, xBufOff, tmpLen);
			xBufOff += tmpLen;
			
		} else if(xBufOff+len == BUFFER_LENGTH) {
			System.arraycopy(in, inOff, xBuf, xBufOff, len);
			doUpdate();
			xBufOff = 0;
		} else {
			System.arraycopy(in, inOff, xBuf, xBufOff, len);
			xBufOff += len;
		}
	}
	
	private void doUpdate() {
		byte[] B = new byte[BLOCK_LENGTH];
		for(int i=0; i<BUFFER_LENGTH; i +=BLOCK_LENGTH) {
			System.arraycopy(xBuf, i, B, 0, B.length);
			doHash(B);
		}
		cntBlock += BUFFER_LENGTH/BLOCK_LENGTH;
	}
	
	private void doHash(byte[] B) {
		V = SM3.CF(V, B);
	}
	
	private byte[] doFinal() {
		byte[] B = new byte[BLOCK_LENGTH];
		byte[] buffer = new byte[xBufOff];
		System.arraycopy(xBuf, 0, buffer, 0, buffer.length);
		byte[] tmp = SM3.padding(buffer, cntBlock);
		for(int i=0; i<tmp.length; i +=BLOCK_LENGTH) {
			System.arraycopy(tmp, i, B, 0, B.length);
			doHash(B);
			cntBlock++;
		}
		
		return V;
	}
	
	private byte[] getSM2Za(byte[] x, byte[] y, byte[] id) {
		byte[] tmp = Util.IntToByte(id.length*8);
		byte[] buffer = new byte[2];
		buffer[0] = tmp[1];
		buffer[1] = tmp[0];
		byte[] a = Util.getA();
		byte[] b = Util.getB();
		byte[] gx = Util.getGx();
		byte[] gy = Util.getGy();

		
		SM2SM3 digest = new SM2SM3();
		digest.update( buffer,0,2);

		
		digest.update( id,0, id.length );
		
		digest.update(  a,0, a.length );
		
		digest.update(  b,0, b.length );

		digest.update(  gx,0, gx.length );

		digest.update(  gy,0, gy.length );
		
		digest.update(  x,0, x.length );
		
		digest.update(  y,0, y.length );
		
		byte[] out = new byte[32];
		digest.doFinal(out, 0);
		return out;

		
		

	}

	/**
	 * @param affineX SM2��Կ��������X
	 * @param affineX SM2��Կ��������Y
	 * @param id 
	 * @return 
	 */
	public byte[] Sm2GetZ(BigInteger PubKeyX, BigInteger PubKeyY, byte[] id) {
		byte[] x =Util.asUnsigned32ByteArray(PubKeyX);
		byte[] y = Util.asUnsigned32ByteArray(PubKeyY);
		byte[] tmp = getSM2Za(x, y, id);
		reset();
		return tmp;

	}
}

class SM3 {
	public static final byte[] iv = new BigInteger("7380166f4914b2b9172442d7da8a0600a96f30bc163138aae38dee4db0fb0e4e", 16).toByteArray();
	public static int[] Tj = new int[64];
	static {
		for(int i=0; i<16; i++) {
			Tj[i] = 0x79cc4519;
		}
		for(int i=16; i<64; i++) {
			Tj[i] = 0x7a879d8a;
		}
	}

	public static byte[] CF(byte[] V, byte[] B) {
		int[] v, b;
		v = convert(V);
		b = convert(B);
		
		return convert(CF(v, b));
	}
	
	private static int[] convert(byte[] arr) {
		int[] out = new int[arr.length/4];
		byte[] tmp = new byte[4];
		for(int i=0; i<arr.length; i += 4) {
			System.arraycopy(arr, i, tmp, 0, 4);
			out[i/4] = bigEndianByteToInt(tmp);
		}
		
		return out;
	}
	
	private static byte[] convert(int[] arr) {
		byte[] out = new byte[arr.length*4];
		byte[] tmp = null;
		for(int i=0; i<arr.length; i++) {
			tmp = bigEndianIntToByte(arr[i]);
			System.arraycopy(tmp, 0, out, i*4, 4);
		}
		
		return out;
	}
	
	public static int[] CF(int[] V, int[] B) {
		int a, b, c, d, e, f, g, h;
		int ss1, ss2, tt1, tt2;
		a = V[0];
		b = V[1];
		c = V[2];
		d = V[3];
		e = V[4];
		f = V[5];
		g = V[6];
		h = V[7];
		/*System.out.print("  ");
		System.out.print(Integer.toHexString(a)+" ");
		System.out.print(Integer.toHexString(b)+" ");
		System.out.print(Integer.toHexString(c)+" ");
		System.out.print(Integer.toHexString(d)+" ");
		System.out.print(Integer.toHexString(e)+" ");
		System.out.print(Integer.toHexString(f)+" ");
		System.out.print(Integer.toHexString(g)+" ");
		System.out.print(Integer.toHexString(h)+" ");
		System.out.println();*/
		
		int[][] arr = expand(B);
		int[] w = arr[0];
		int[] w1 = arr[1];
		/*System.out.println("W");
		print(w);
		System.out.println("W1");
		print(w1);*/
		for(int j=0; j<64; j++) {
			ss1 = (bitCycleLeft(a, 12) + e + bitCycleLeft(Tj[j], j));
			ss1 = bitCycleLeft(ss1, 7);
			ss2 = ss1 ^ bitCycleLeft(a, 12);
			tt1 = FFj(a, b, c, j) + d + ss2 + w1[j];
			tt2 = GGj(e, f, g, j) + h + ss1 + w[j];
			d = c;
			c = bitCycleLeft(b, 9);
			b = a;
			a = tt1;
			h = g;
			g = bitCycleLeft(f, 19);
			f = e;
			e = P0(tt2);
			
			/*System.out.print(j+" ");
			System.out.print(Integer.toHexString(a)+" ");
			System.out.print(Integer.toHexString(b)+" ");
			System.out.print(Integer.toHexString(c)+" ");
			System.out.print(Integer.toHexString(d)+" ");
			System.out.print(Integer.toHexString(e)+" ");
			System.out.print(Integer.toHexString(f)+" ");
			System.out.print(Integer.toHexString(g)+" ");
			System.out.print(Integer.toHexString(h)+" ");
			System.out.println();*/
		}
		//System.out.println("*****************************************");
		
		int[] out = new int[8];
		out[0] = a ^ V[0];
		out[1] = b ^ V[1];
		out[2] = c ^ V[2];
		out[3] = d ^ V[3];
		out[4] = e ^ V[4];
		out[5] = f ^ V[5];
		out[6] = g ^ V[6];
		out[7] = h ^ V[7];
		
		return out;
	}
	
	private static int[][] expand(byte[] B) {
		int W[] = new int[68];
		int W1[] = new int[64];
		byte[] tmp = new byte[4];
		for(int i=0; i<B.length; i+=4) {
			for(int j=0; j<4; j++) {
				tmp[j] = B[i+j];
			}
			W[i/4] = bigEndianByteToInt(tmp);
		}
		
		for(int i=16; i<68; i++) {
			W[i] = P1(W[i-16] ^ W[i-9] ^ bitCycleLeft(W[i-3], 15)) ^ bitCycleLeft(W[i-13], 7) ^ W[i-6];
		}	
		
		for(int i=0; i<64; i++) {
			W1[i] = W[i] ^ W[i+4];
		}
		
		int arr[][] = new int[][]{W, W1};
		
		return arr;
	}
	
	private static int[][] expand(int[] B) {
		return expand(convert(B));
	}
	
	private static byte[] bigEndianIntToByte(int num) {
		return back(Util.IntToByte(num));
	}
	
	private static int bigEndianByteToInt(byte[] bytes) {
		return Util.ByteToInt(back(bytes));
	}
	
	private static int FFj(int X, int Y, int Z, int j) {
		if(j>=0 && j<=15) {
			return FF1j(X, Y, Z);
		} else {
			return FF2j(X, Y, Z);
		}
	}
	private static int GGj(int X, int Y, int Z, int j) {
		if(j>=0 && j<=15) {
			return GG1j(X, Y, Z);
		} else {
			return GG2j(X, Y, Z);
		}
	}
	/***********************************************/
	// �߼�λ���㺯��
	private static int FF1j(int X, int Y, int Z) {
		int tmp = X ^ Y ^ Z;
		
		return tmp;
	}
	
	private static int FF2j(int X, int Y, int Z) {
		int tmp = ( (X & Y) | (X & Z) | (Y & Z) );
		
		return tmp;
	}
	
	private static int GG1j(int X, int Y, int Z) {
		int tmp = X ^ Y ^ Z;
		
		return tmp;
	}
	
	private static int GG2j(int X, int Y, int Z) {
		int tmp = (X & Y) | (~X & Z) ;
		
		return tmp;
	}
	
	private static int P0(int X) {
		int t = X ^ bitCycleLeft(X, 9) ^ bitCycleLeft(X, 17);
		
		return t;
	}
	
	private static int P1(int X) {
		int t = X ^ bitCycleLeft(X, 15) ^ bitCycleLeft(X, 23);
		
		return t;
	}
	
	/**
	 * �����һ�������ֽ�����padding
	 * @param in
	 * @param bLen �������
	 * @return
	 */
	public static byte[] padding(byte[] in, int bLen) {
		//��һbitΪ1 ���Գ���=8 * in.length+1 kΪ������bit k+1/8 Ϊ��Ҫ�����ֽ�
		int k = 448 - (8 * in.length+1) % 512;
		if( k < 0) {
			k = 960 - (8 * in.length+1) % 512;
		}
		k += 1;
		byte[] padd = new byte[k/8];
		padd[0] = (byte)0x80;
		long n = in.length * 8+bLen*512;
		//64/8 �ֽ� ����
		//k/8 �ֽ�padding
		byte[] out = new byte[in.length+k/8+64/8];
		int pos = 0;
		System.arraycopy(in, 0, out, 0, in.length);
		pos += in.length;
		System.arraycopy(padd, 0, out, pos, padd.length);
		pos += padd.length;
		byte[] tmp = back(Util.LongToByte(n));
		System.arraycopy(tmp, 0, out, pos, tmp.length);
		
		return out;
	}
	
	/**
	 * �ֽ���������
	 * @param in
	 * @return
	 */
	private static byte[] back(byte[] in) {
		byte[] out = new byte[in.length];
		for(int i=0; i<out.length; i++) {
			out[i] = in[out.length-i-1];
		}
		
		return out;
	}

	private static int bitCycleLeft(int n, int bitLen) {
		bitLen %= 32;
		byte[] tmp = bigEndianIntToByte(n);
		int byteLen = bitLen / 8;
		int len = bitLen % 8;
		if( byteLen > 0) {
			tmp = byteCycleLeft(tmp, byteLen);
		}
		
		if(len > 0) {
			tmp = bitSmall8CycleLeft(tmp, len);
		}
		
		return bigEndianByteToInt(tmp);
	}

	private static byte[] bitSmall8CycleLeft(byte[] in, int len) {
		byte[] tmp = new byte[in.length];
		int t1, t2, t3;
		for(int i=0; i<tmp.length; i++) {
			t1 = (byte) ((in[i] & 0x000000ff)<<len);
			t2 = (byte) ((in[(i+1)%tmp.length] & 0x000000ff)>>(8-len));
			t3 = (byte) (t1 | t2);
			tmp[i] = (byte)t3;
		}
		
		
		return tmp;
	}
	
	private static byte[] byteCycleLeft(byte[] in, int byteLen) {
		byte[] tmp = new byte[in.length];
		System.arraycopy(in, byteLen, tmp, 0, in.length-byteLen);
		System.arraycopy(in, 0, tmp, in.length-byteLen, byteLen);
		
		return tmp;
	}
	
}

class Util {
	public static BigInteger p = new BigInteger("FFFFFFFEFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF00000000FFFFFFFFFFFFFFFF", 16);
	public static BigInteger a = new BigInteger("FFFFFFFEFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF00000000FFFFFFFFFFFFFFFC", 16);
	public static BigInteger b = new BigInteger("28E9FA9E9D9F5E344D5A9E4BCF6509A7F39789F515AB8F92DDBCBD414D940E93", 16);
	public static BigInteger n = new BigInteger("FFFFFFFEFFFFFFFFFFFFFFFFFFFFFFFF7203DF6B21C6052B53BBF40939D54123", 16);
	public static BigInteger Gx = new BigInteger("32C4AE2C1F1981195F9904466A39C9948FE30BBFF2660BE1715A4589334C74C7", 16);
	public static BigInteger Gy = new BigInteger("BC3736A2F4F6779C59BDCEE36B692153D0A9877CC62A474002DF32E52139F0A0", 16);
	public static byte[] getP() {
		return p.toByteArray();
	}
	public static byte[] getA() {
		return asUnsigned32ByteArray(a);
	}
	public static byte[] getB() {
		return asUnsigned32ByteArray(b);
	}
	public static byte[] getN() {
		return asUnsigned32ByteArray(n);
	}
	public static byte[] getGx() {
		return asUnsigned32ByteArray(Gx);
	}
	public static byte[] getGy() {
		return asUnsigned32ByteArray(Gy);
	}
	
	/**
	 * ����ת�������紫����ֽ������ֽ����飩������
	 * @param num һ����������
	 * @return 4���ֽڵ��Լ�����
	 */
	public static byte[] IntToByte(int num) {
		byte[] bytes = new byte[4];
		
		bytes[0] = (byte)(0xff&(num>>0));
		bytes[1] = (byte)(0xff&(num>>8));
		bytes[2] = (byte)(0xff&(num>>16));
		bytes[3] = (byte)(0xff&(num>>24));
		
		return bytes;
	}
	
	/**
	 * �ĸ��ֽڵ��ֽ�����ת����һ����������
	 * @param bytes 4���ֽڵ��ֽ�����
	 * @return һ����������
	 */
	public static int ByteToInt(byte[] bytes) {
		int num = 0;
		int temp;
		temp = (0x000000ff & (bytes[0]))<<0;
		num = num | temp;
		temp = (0x000000ff & (bytes[1]))<<8;
		num = num | temp;
		temp = (0x000000ff & (bytes[2]))<<16;
		num = num | temp;
		temp = (0x000000ff & (bytes[3]))<<24;
		num = num | temp;
		
		return num;
	}
	
	public static byte[] LongToByte(long num) {
		byte[] bytes = new byte[8];
		
		for(int i=0; i<8; i++) {
			bytes[i] = (byte)(0xff&(num>>(i*8)));
		}
		
		return bytes;
	}
	public static byte[] asUnsigned32ByteArray(BigInteger n) {
		return asUnsignedNByteArray(n, 32);
	}
	public static byte[] asUnsignedNByteArray(BigInteger x, int length) {
		if(x == null) {
			return null;
		}
		
		byte[] tmp = new byte[length];
		int len = x.toByteArray().length;
		if(len > length+1) {
			return null;
		}
		
		if(len == length+1) {
			if(x.toByteArray()[0] != 0) {
				return null;
			} else {
				System.arraycopy(x.toByteArray(), 1, tmp, 0, length);
				return tmp;
			}
		} else {
			System.arraycopy(x.toByteArray(), 0, tmp, length-len, len);
			return tmp;
		}
		
	}
	
	
	
}
