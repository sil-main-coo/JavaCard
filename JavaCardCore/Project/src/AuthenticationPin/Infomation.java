package AuthenticationPin;

import javacard.framework.*;
import org.globalplatform.GPSystem;
import javacard.security.AESKey;
import javacardx.crypto.Cipher;
import javacard.security.KeyBuilder;
import javacard.security.RandomData;



public class Infomation extends Applet
{
	// khai bao bien
	private Cipher aesCipher;
    private AESKey aesKey;
    private final static short KEY_SIZE = 32;
    
	//CLA
	final static byte Bot_CLA = (byte) 0xB0;
	
	//INS
	final static byte INS_THONGTIN = (byte)0x00;
	final static byte INS_NAPANH = (byte)0x01;
	final static byte INS_ANH = (byte)0x02;
	final static byte INS_INSERT = (byte)0x03;
	final static byte INS_COUNTINSERT = (byte)0x04;
	final static byte INS_SETCOUNT = (byte)0x05;
	final static byte INS_COUNTANH = (byte)0x06;
	final static byte INS_BLOCK = (byte)0x07;
	final static byte INS_UNBLOCK = (byte)0x08;
	final static byte INS_ERROR = (byte)0x09;
	final static byte INS_CHECKBLOCKE = (byte)0xA0;
	final static byte INS_CHECKERROR = (byte)0x0A1;
	
	// variable
	private static byte[] image, size;
	public static byte[] arrayhoten = new byte[256];
	public static byte[] arrayngaysinh = new byte[256];
	public static byte[] arrayCMND = new byte[256];
	public static byte[] arrayGPLX = new byte[256];
	public static byte[] arrayvehicle = new byte[256];
	
	// variable	 encrypt
	public static byte[] arrayhotenencrypt = new byte[256];
	public static byte[] arrayngaysinhencrypt = new byte[256];
	public static byte[] arrayCMNDencrypt = new byte[256];
	public static byte[] arrayGPLXencrypt = new byte[256];
	public static byte[] arrayvehicleencrypt = new byte[256];	
	// bien dem so lan vi pham giao thong
	private short error = 0;
	// neu nhu the block thi bien block = 1; Neu the khong bi block thi block = 0
	private short block = 0;
	final static byte phi = (byte) 0x03;
	byte balance = (byte) 0x0A;
	short countht, countns, countcmnd, countgplx, countvehicle;
	private Infomation(byte[] bArray, short bOffset, byte bLength){
		image = new byte[10000];
		size = new byte[7];
		aesCipher = Cipher.getInstance(Cipher.ALG_AES_BLOCK_128_CBC_NOPAD, false);
        aesKey = (AESKey) KeyBuilder.buildKey(KeyBuilder.TYPE_AES, KeyBuilder.LENGTH_AES_128, false);
        byte[] keyBytes = JCSystem.makeTransientByteArray(KEY_SIZE, JCSystem.CLEAR_ON_DESELECT);
        try {
            RandomData rng = RandomData.getInstance(RandomData.ALG_SECURE_RANDOM);
            rng.generateData(keyBytes, (short) 0, KEY_SIZE);
            aesKey.setKey(keyBytes, (short) 0);
        } finally {
            Util.arrayFillNonAtomic(keyBytes, (short) 0, KEY_SIZE, (byte) 0);
        }
        
        register();
	}
	
	public static void install(byte[] bArray, short bOffset, byte bLength) 
	{
		Infomation reg = new Infomation(bArray,bOffset,bLength);
	}

	public void process(APDU apdu)
	{
		if (selectingApplet())
		{
			return;
		}

		byte[] buf = apdu.getBuffer();
		apdu.setIncomingAndReceive();
		switch (buf[ISO7816.OFFSET_INS])
		{
			// case dem do d�i cua th�ng tin truyen v�o
			// th�ng tin truyen v�o se duoc phan cach boi dau !						
			// all case thong tin Du lieu truyen vao phai giong nhau
			// send = hoten+"!"+ngaysinh+"!"+diachi+"!"+bienks;
			
	case INS_COUNTINSERT:
			short dataLen1 = (short)(buf[ISO7816.OFFSET_LC]&0xff);
			short flag1 = (short)1;
			// khai b�o bien dem lenght
			countht = 0; // h t�n
			countns = 0; // ng�y sinh
			countcmnd = 0; // cmnd
			countgplx = 0; // gplx
			countvehicle = 0; // phng tin
				for(short i = (short)(ISO7816.OFFSET_CDATA);i<(short)(ISO7816.OFFSET_CDATA +1+dataLen1);i++ ){
					if(buf[i]==(byte)0x21){
						flag1+=(short)1;
						continue;
					}
					if(flag1 ==(short)1){
						countht++;
					}
					else if(flag1 ==(short)2){
							countns++;
					}
					else if(flag1 ==(short)3){
						countcmnd++;
					}
					else if(flag1 ==(short)4){
						countgplx++;
					}else if(flag1 ==(short)5){
						countvehicle++;
					}
				}
			countht++;
			countns++;
			countcmnd++;
			countgplx++;
			countvehicle++;
			break;
			// luu thong tin vao array cua tung thong tin 
		case INS_INSERT:
			short dataLen = (short)(buf[ISO7816.OFFSET_LC]&0xff);
			short flag = (short)1;
			arrayhoten = new byte[countht]; // array ho t�n
			arrayngaysinh = new byte[countns]; // array ngay sinh
			arrayCMND = new byte[countcmnd]; // array CMND
			arrayGPLX = new byte[countgplx]; // array GPLX
			arrayvehicle = new byte[countvehicle]; // array phung tien
			
			// vong lap luu thong tin
			short tempIndex = (short)0;
				for(short i = (short)(ISO7816.OFFSET_CDATA);i<(short)(ISO7816.OFFSET_CDATA +1+dataLen);i++ ){
					if(buf[i]==(byte)0x21){			
						if(flag ==(short)1){
							arrayhoten[tempIndex++]=buf[i];
						}
						else if(flag ==(short)2){
							arrayngaysinh[tempIndex++]=buf[i];
						}
						else if(flag ==(short)3){
							arrayCMND[tempIndex++]=buf[i];
						}else if(flag ==(short)4){
							arrayGPLX[tempIndex++]=buf[i];
						}else if(flag ==(short)5){
							arrayvehicle[tempIndex++]=buf[i];
						}			
						flag+=(short)1;
						tempIndex = (short)0;
						continue;
					}
					if(flag ==(short)1){
						arrayhoten[tempIndex++]=buf[i];
					}
					else if(flag ==(short)2){
						arrayngaysinh[tempIndex++]=buf[i];
					}
					else if(flag ==(short)3){
						arrayCMND[tempIndex++]=buf[i];
					}
					else if(flag ==(short)4){
						arrayGPLX[tempIndex++]=buf[i];						
					}else if(flag ==(short)5){
						arrayvehicle[tempIndex++]=buf[i];
					}
				}
				// encrypt
				 byte[] ht = encrypt(arrayhoten);
				 byte[] ns = encrypt(arrayngaysinh);
				 byte[] cmnd = encrypt(arrayCMND);
				 byte[] gplx = encrypt(arrayGPLX);
				 byte[] vc = encrypt(arrayvehicle);
				 
				 Util.arrayCopy(ht,(short)0,arrayhotenencrypt,(short)0, (short)ht.length);
				 Util.arrayCopy(ns,(short)0,arrayngaysinhencrypt,(short)0, (short)ns.length);
				 Util.arrayCopy(cmnd,(short)0,arrayCMNDencrypt,(short)0, (short)cmnd.length);
				 Util.arrayCopy(gplx,(short)0,arrayGPLXencrypt,(short)0, (short)gplx.length);
				 Util.arrayCopy(vc,(short)0,arrayvehicleencrypt,(short)0, (short)vc.length);
			break;				
						
		case INS_THONGTIN:
			// decrypt
			byte[] htin = decrypt(arrayhotenencrypt);
			byte[] nsin = decrypt(arrayngaysinhencrypt);
			byte[] cmndin = decrypt(arrayCMNDencrypt);
			byte[] gplxin = decrypt(arrayGPLXencrypt);
			byte[] vcin = decrypt(arrayvehicleencrypt);
				 
			short lenhoten = (short) arrayhoten.length;
			short lenngaysinh = (short) arrayngaysinh.length;
			short lencmnd = (short) arrayCMND.length;
			short lengplx = (short) arrayGPLX.length;
			short lenvehicle = (short) arrayvehicle.length;
			short len = (short) (lenhoten + lenngaysinh + lencmnd + lengplx + lenvehicle);
			
			apdu.setOutgoing();
			apdu.setOutgoingLength(len);
			Util.arrayCopy(htin,(short)0,buf,(short)0,lenhoten);
			apdu.sendBytes((short)0, lenhoten);
			Util.arrayCopy(nsin,(short)0,buf,(short)0,lenngaysinh);
			apdu.sendBytes((short)0, lenngaysinh);
			Util.arrayCopy(cmndin,(short)0,buf,(short)0,lencmnd);
			apdu.sendBytes((short)0, lencmnd);
			Util.arrayCopy(gplxin,(short)0,buf,(short)0,lengplx);
			apdu.sendBytes((short)0, lengplx);
			Util.arrayCopy(vcin,(short)0,buf,(short)0,lenvehicle);
			apdu.sendBytes((short)0, lenvehicle);
			break;		
			
		case INS_NAPANH:
			short p1 = (short)(buf[ISO7816.OFFSET_P1]&0xff);
			short count1 = (short)(249 * p1);
			Util.arrayCopy(buf, ISO7816.OFFSET_CDATA, image, count1, (short)249);
			break;
			
		case INS_SETCOUNT:
			Util.arrayCopy(buf, ISO7816.OFFSET_CDATA, size, (short)0, (short)7);
			break;
			
		case INS_COUNTANH:
			Util.arrayCopy(size, (short)0, buf, (short)0, (short)(size.length));
			apdu.setOutgoingAndSend((short)0,(short)7);
			break;
		
		case INS_ANH:
			apdu.setOutgoing();
			short p = (short)(buf[ISO7816.OFFSET_P1]&0xff);
			short count = (short)(249 * p);
			apdu.setOutgoingLength((short)249);
			apdu.sendBytesLong(image, count, (short)249);
			break;
			
			// dem loi vi pham giao thong
		case INS_ERROR:
			error++;
			break;
			
			// check xem da vi pham bao nhieu loi
		case INS_CHECKERROR:
			byte[] checkerror = new byte[1];
			checkerror[0] = (byte)error;
			apdu.setOutgoing();
			apdu.setOutgoingLength((short)1);
			Util.arrayCopy(checkerror,(short)0,buf,(short)0,(short)1);
			apdu.sendBytes((short)0, (short)1);
			break;
			
			// check xem co block hay khong
		case INS_CHECKBLOCKE:
			byte[] checkblock = new byte[1];
			checkblock[0] = (byte)block;
			apdu.setOutgoing();			
			apdu.setOutgoingLength((short)1);
			Util.arrayCopy(checkblock,(short)0,buf,(short)0,(short)1);
			apdu.sendBytes((short)0, (short)1);
			break;
			
			// chuc nang block the
		case INS_BLOCK:			
			block = (short)1;
			break;
			
			// chuc nang unblock the
		case INS_UNBLOCK:
			block = (short)0;
			error = (short)0;
			break;
		default:
			ISOException.throwIt(ISO7816.SW_INS_NOT_SUPPORTED);
		}
	}
/*AES*/
	private byte[] encrypt(byte[] encryptData) {
        aesCipher.init(aesKey, Cipher.MODE_ENCRYPT);
        short flag = (short) 1;
	    byte[] temp = new byte[256];
    	while(flag == (short)1){
    		for(short i=0;i<=(short) encryptData.length;i++){
    			if(i!=(short) encryptData.length){
					temp[i] = encryptData[i];
    			}
    			else{
	    			flag = (short) 0;
    			}
    		}
    	}
        // short newLength = addPadding(temp, (short) 0, (short) encryptData.length);
        byte[] dataEncrypted = JCSystem.makeTransientByteArray((short)256, JCSystem.CLEAR_ON_DESELECT);        
        aesCipher.doFinal(temp, (short) 0 , (short)256, dataEncrypted, (short) 0x00);
        return dataEncrypted;
    }

    
    private byte[] decrypt(byte[] decryptData) {
        aesCipher.init(aesKey, Cipher.MODE_DECRYPT);
        byte[] dataDecrypted = JCSystem.makeTransientByteArray((short) 256, JCSystem.CLEAR_ON_DESELECT);
        aesCipher.doFinal(decryptData, (short) 0, (short) 256, dataDecrypted, (short) 0x00);
        // short newLength = removePadding(dataDecrypted, (short) length);
        return dataDecrypted;
    }
   
    // private short addPadding(byte[] data, short offset, short length) {
        // data[(short) (offset + length++)] = (byte)0x80;
        // while (length < 16 || (length % 16 != 0)) {
            // data[(short) (offset + length++)] = 0x00;
        // }
        // return length;
    // }
   
    // private short removePadding(byte[] buffer, short length) {
        // while ((length != 0) && buffer[(short) (length - 1)] == (byte) 0x00) {
            // length--;
        // }
        // if (buffer[(short) (length - 1)] != (byte) 0x80) {
            // ISOException.throwIt(ISO7816.SW_DATA_INVALID);
        // }
        // length--;
        // return length;
    // }

}
