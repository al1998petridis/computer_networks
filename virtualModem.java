/*---------------------------------------*
 *	Author: Alexandros Petridis	 * 
 *	Class: Computer Networks 1	 *
 *---------------------------------------*/

package dyktia;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import ithakimodem.Modem;

public class virtualModem {
	
	
	
	
	public static String echo_request_code = "E4806\r";
	public static String image_request_code = "M4379\r";
	public static String image_error_request_code = "G2686\r";
	public static String gps_request_code = "P3089R=1000099\r";
	public static String ack_request_code = "Q6139\r";
	public static String nack_request_code = "R1050\r";
	
	
	public static void main(String[] args) throws IOException {
		
		DateFormat df = new SimpleDateFormat();
		Date today = Calendar.getInstance().getTime();
		String str = df.format(today);
		String str1 = str.substring(0, 8);
		str1 = str1.replace('/', '.');
		String path = ("D:\\AUTH\\AUTH(3.12.18)\\6ï åîÜìçíï\\Äßêôõá Õðïëïãéóôþí 1\\myProject\\" + str1);
		File file = new File(path);
		file.mkdirs();
		System.out.println("All Files from this section will be saved to: "+ path + "\n");
		
		
		Modem modem; 
		modem = new Modem();
		modem.setSpeed(80000);
		modem.setTimeout(1000);
		modem.open("ithaki");
		//For starting message.
		int k=0;
		for (;;) {
			try {
				k = modem.read();
				if (k == -1)
					break;
				System.out.print((char) k);
			} catch (Exception x) {
				break;
			}
		}
		
 		echoPackets(echo_request_code, modem, path);

		
		/* Working
		
		
		arq(ack_request_code, nack_request_code, modem, "\\ARQ_ResponseTimes.csv", "\\NumOfRetransmissions.csv", "\\BER.txt",  path);

		String Image = path + "\\Image.jpg";
		String ImageWithError = path + "\\ImageWithError.jpg";
		
 		echoPackets(echo_request_code, modem, path);
		images(image_request_code, modem, Image);
		images(image_error_request_code, modem, ImageWithError);
		gps(gps_request_code, modem, path);
		
		
		*/


		modem.close();

	}
	
	
	public static void echoPackets(String echoCode, Modem m, String path) throws IOException {
		
		byte[] echo_code = echoCode.getBytes();
		
		System.out.println("Echo function starting.\n");
		
		PrintWriter responseTimesEcho = new PrintWriter(path + "\\echoResponseTime.csv");
		
		long forminTime = System.currentTimeMillis();
		while ((System.currentTimeMillis()-forminTime) < 4*60*1000) {		//Running for 4 minutes.
			m.write(echo_code);
			int k;
			long startTime = System.currentTimeMillis();
			long endTime = 0;
			long responseTime;
			//35 times because the String is with 35 chars and we need the response time of the system.
			for (int i = 0; i<35; i++) {
				try {
	 				k = m.read();
	 				if (k == -1)
						break;
	 				System.out.print((char) k);
				} catch (Exception x) {
	 				break;
				}
	 		}
			System.out.print("\n");
			endTime = System.currentTimeMillis();	
			responseTime = endTime - startTime;
			responseTimesEcho.println(responseTime);
		}
		responseTimesEcho.close();
		System.out.println("echoResponseTime.csv is in " + path + "\nEcho function is terminated.");
	}
	
	
	public static void images(String image_code, Modem m, String path) throws IOException {
		
		System.out.println("Image function starting.");
		
		FileOutputStream image = new FileOutputStream(path);
		
		int fin = 0;
		int bfin = 0;
		int n = 0;
		
		byte[] image_byte = image_code.getBytes();
		m.write(image_byte);
		while(true) {
			bfin = fin;
			fin = m.read();
			if (fin == -1)
				break;
			if (bfin == 255 && fin == 216) {
				n = 1;
				image.write(255);
				image.write(216);
				
				while(true) {
					try {
						fin = m.read();
						if (bfin == 255 && fin == 217) {
							image.write((byte) fin);
							break;
						}
						bfin = fin;
						
						image.write((byte) fin);
						
						if (fin == -1)
							break;
					}
					catch(Exception e) {
						break;
					}
				}
				if (n == 1)
					break;
			}
		}
		image.close();
		System.out.println(path + " created.\nImage function is terminated.");
	}
		
	
	public static void gps(String gps_code, Modem m, String path) throws IOException {
		
		
		int numOfTrace = 99; //By default if R=XPPPPLL is not there.

		
		if(gps_code.length() > 6 && gps_code.substring(5) == "R") {
			// Example P5524R=×PPPPLL\r
			String nOT = gps_code.substring(12,14); 	
			numOfTrace = Integer.parseInt(nOT);
		}
		  
		
		System.out.println("GPS function starting.\n");

		
		int stars = 0;
		byte[] code = gps_code.getBytes();
		m.write(code);
		int k = 0;
		
		//We throw START ITHAKI GPS TRACKING\r\n (length = 27)
		for (int i=0; i<27; i++) { 
			try {
				k = m.read();
				System.out.print((char) k);
			} catch (Exception x) {
				break;
			}
		}
		
		int[][] traces = new int[numOfTrace][80]; 
		int i = -1; // Because in the first $ in the beggining it is going to be +1 = 0
		int j = 0;
		// Example line $GPGGA,045208.000,4037.6331,N,02257.5633,E,1,07,1.5,57.8,M,36.1,M,,0000*6D 
		for (;;) {
			try {
 				k = m.read();
 				if (k == 42) // 42 is ascii code for *
 					stars++;
 				if (stars == numOfTrace) // LL in R=×PPPPLL
 					break;
 				if (k == 36) {	// 36 is ascii code for $
 					i++;
 					j = 0;
 				}
 				traces[i][j] = k;
 				j++;
 				if (k == -1)
 					break;
 				System.out.print((char) k);
			} catch (Exception x) {
 				break;
			}
 		}
		
		int[][] coordinates = new int[numOfTrace][17]; // coordinates are length 19 - 2 dots = 17
		int p = 0;
		for (int i1=0; i1<numOfTrace; i1++) {
			p = 0;
			for (int i2 = 18; i2<27; i2++) {	//coordinates starts at length 18 and ends at length 27. //latitude 
				if (traces[i1][i2] == 46) {		// 46 is ascii for .
					p += 1;
					continue;
				} 
				coordinates[i1][i2-18-p] = traces[i1][i2] - 48 ; //-p for jumping over dots(.) and -48 to convert acsii code in decimal.
			}
			p=0;
			for (int i3 = 30; i3<40; i3++) {	//longitude 			 
				if (traces[i1][i3] == 46) {
					p += 1;
					continue;
				} 
				coordinates[i1][i3 - 22 - p] = traces[i1][i3] - 48;
			}
		}
		
		
		String[] strTrace = new String[numOfTrace];
		int[] latitude = new int[numOfTrace];
		int[] longitude = new int[numOfTrace];
		
		for (int q = 0; q < numOfTrace; q++) {
			strTrace[q] = "";
			for (int q1 = 0; q1 < 17; q1++) {
				strTrace[q] += String.valueOf(coordinates[q][q1]);	//converting coordinates into strings and put them into a new string array 
			}

			latitude[q] = (int) (Integer.parseInt(strTrace[q].substring(4, 8))*0.006);		//Need this for last 2 points of each coordinate.
			longitude[q] = (int) (Integer.parseInt(strTrace[q].substring(13, 17))*0.006);	
			
		}
					
				
		
		int[][] finalcoors = new int[numOfTrace][10]; // 10 = 4 first 4 of latitude + 1 type-casted (can be 2 digits) + 4 first of longitude + 1 type-+casted.
		
		for (int c = 0; c < numOfTrace; c++) {
			for (int c1 = 0; c1 < 4; c1++) {
				finalcoors[c][c1] = coordinates[c][c1 + 9];
			}
			finalcoors[c][4] = longitude[c];
			for (int c2 = 5; c2 < 9; c2++) {
				finalcoors[c][c2] = coordinates[c][c2 - 5];
			}
			finalcoors[c][9] = latitude[c];
		}
		
		int[] timeValues = new int[numOfTrace];
		for (int e = 0; e < numOfTrace; e++) {
			timeValues[e] = ((traces[e][7] - 48) * 10 + (traces[e][8] - 48)) * 3600
					+ ((traces[e][9] - 48) * 10 + (traces[e][10] - 48)) * 60
					+ ((traces[e][11] - 48) * 10 + (traces[e][12] - 48));
		}
		
		int indexPos = 0;
		int t = 1;
		int[] positions = new int[5];		// Positions of coordinates that we want (for gpsImage)
		for (int r = 0; r < numOfTrace; r++) {		// Getting the string of the final coordinations
			if (Math.abs(timeValues[indexPos] - timeValues[r]) >= 19) {
				indexPos = r;
				positions[t] = r;
				t++;
			}
			if (t == 5)  
				break;
		}
		
		long[] coords = new long[5];
		String[] str2 = new String[5];

		for (int q1 = 0; q1 < 5; q1++) {
			str2[q1] = "";
			for (int q2 = 0; q2< 10; q2++)
				str2[q1] += Integer.toString(finalcoors[positions[q1]][q2]);
			coords[q1] = (long) Long.parseLong(str2[q1]);
		}
		
		
		String codeforPins = gps_request_code.substring(0, 5) + "T=" + coords[4];	
		for (int f = 0; f < 4; f++) {
			codeforPins += "T=" + coords[3 - f];
		}
		codeforPins += "\r";

		String GpsImage = path + "\\GPSImage.jpg";
		images(codeforPins, m, GpsImage);
		
		System.out.println("GPS function finished.\n");		
	}


	public static void arq(String ackCode, String nackCode, Modem m, String arqResponce, String numOfRetr, String ber, String path) throws IOException {
		
		
		byte[] ack_code = ackCode.getBytes();
		byte[] nack_code = nackCode.getBytes();
		
		PrintWriter ARQ_ResponseTimes = new PrintWriter(path + arqResponce);
		PrintWriter NumOfRetransmissions = new PrintWriter(path + numOfRetr);
		PrintWriter BER = new PrintWriter(path + ber);
		
		int counter = 0;
		int k = 0;
		int packet[] = new int[58]; 	// PSTART DD-MM-YYYY HH-MM-SS PC <××××××××××××××××> FCS PSTOP length is 58.
		int hexacode[] = new int[16]; 	// for <××××××××××××××××>
		int xOR = 0;
		int FCSValue = 0;
		int[] stats = new int[2];
		long endTime = 0;
		long responseTime = 0;
		long formintime = System.currentTimeMillis();
		while((System.currentTimeMillis() - formintime) <  4*60*1000) {		//Running for 4 minutes.
			m.write(ack_code);
			long startTime = System.currentTimeMillis();
			for (;;) {
				try {
					for (counter = 0; counter < 58; counter++) {	// Store packet array
						k = m.read();
						packet[counter] = k; 
					}
					for (int j = 0; j < 16; j++) {		// Store hexacode array
						hexacode[j] = packet[31 + j];
					}
					// Compute XOR
					xOR = hexacode[0] ^ hexacode[1];
					for (int l = 0; l < 14; l++) {
						xOR = xOR ^ hexacode[l + 2];
					}

					// Store value of FCS
					String str1;
					str1 = "";
					for (int i = 0; i < 3; i++) {
						str1 += Integer.toString(packet[49 + i] - 48); // For hold FCS Value and make ascii transform on it.
					}
					FCSValue = Integer.parseInt(str1);	//transform it into int.
					// Check if we have correct packet.
					if (FCSValue == xOR) {
						for (int j = 0; j < 58; j++) {
							System.out.print((char) packet[j]);
						}
						System.out.println();
						stats[0]++; //	++ on Number of ACK Packets.
						System.out.println("FCS value = " + FCSValue + " == XOR Value  = " + xOR);
						break;
					}
					// If we have false packet we send NACK.
					else {
						for (int j = 0; j < 58; j++) {
							System.out.print((char) packet[j]);
						}
						System.out.println();
						m.write(nack_code);
						stats[1]++;	// ++ on Number of errors.
						System.out.println("We had error in: " + "FCS value = " + FCSValue + " != XOR Value  = " + xOR);
					}
	 
					if (k == -1)
						break;
				} catch (Exception x) {
					break;
				}
			}
			endTime = System.currentTimeMillis();	//Time until we have correct packet again.
	 
			responseTime = endTime - startTime;
	 
			ARQ_ResponseTimes.println(responseTime);
			NumOfRetransmissions.println(stats[1]);

		}
				
		ARQ_ResponseTimes.close();
		NumOfRetransmissions.close();
		BER.println("Number of NACK packets: " + stats[1] + "\nNumber of ACK packets: " + stats[0]);
		BER.close();
	}
	

}
