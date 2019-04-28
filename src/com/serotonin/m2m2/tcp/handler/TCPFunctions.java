package com.serotonin.m2m2.tcp.handler;

public class TCPFunctions {

	/**
	 * 
	 * @param apdu
	 * @param i
	 * @param dataLength
	 * @return
	 */
	public static String byteArrayToHexString(byte[] apdu, int start, int dataLength) {
		StringBuilder sb = new StringBuilder();
		for (int i = start; i < dataLength; i++) {
			String hex = Integer.toHexString(0xFF & apdu[i]);
			if (hex.length() == 1) {
				sb.append('0');
			}
			sb.append(hex + " ");
		}
		return sb.toString().intern().toUpperCase();
	}
	
	
	public static String[] byteArrayToHexArray(byte[] apdu, int start, int dataLength) {
		String[] hexArray = new String[dataLength-start];
		for (int i = 0; i < hexArray.length; i++) {
			String hex = Integer.toHexString(0xFF & apdu[i+start]);
			if (hex.length() == 1) {
				hex="0"+hex;
			}
			hexArray[i]=hex.intern().toUpperCase();
		}
		return hexArray;
	}
	/** 
	 * Convert hex string to byte[] 
	 * @param hexString the hex string 
	 * @return byte[] 
	 */  
	public static byte[] hexStringToBytes(String hexString) {  
	    if (hexString == null || hexString.equals("")) {  
	        return null;  
	    }
	    hexString=hexString.replaceAll(" ", "").trim();
	    hexString = hexString.toUpperCase();  
	    int length = hexString.length() / 2;  
	    char[] hexChars = hexString.toCharArray();  
	    byte[] d = new byte[length];  
	    for (int i = 0; i < length; i++) {  
	        int pos = i * 2;  
	        d[i] = (byte) (charToByte(hexChars[pos]) << 4 | charToByte(hexChars[pos + 1]));  
	    }  
	    return d;  
	}   
	/** 
	 * Convert char to byte 
	 * @param c char 
	 * @return byte 
	 */  
	 private static byte charToByte(char c) {  
	    return (byte) "0123456789ABCDEF".indexOf(c);  
	}  
	 public static void main(String[] args) {
		String aString ="2";
		aString+="0";
		System.out.println(aString);
		StringBuffer sbBuffer=new StringBuffer("2");
		sbBuffer.append("0");
		System.out.println(sbBuffer.toString());
	}
}
