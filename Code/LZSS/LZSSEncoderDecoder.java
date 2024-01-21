package LZSS;

/**
 * Assignment Final
 * Submitted by: 
 * Student 1. Itamar Abir 	ID# 
 * Student 2. Rami Abo Rabia	ID# 
 */

// Uncomment if you wish to use FileOutputStream and FileInputStream for file access.
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import base.Compressor;

public class LZSSEncoderDecoder implements Compressor {

	int searchWindowSize;
	int minimuMatchSize;
	int maximuMatchSize;

	FileInputStream fis;
	FileOutputStream fos;
	BufferedInputStream bis;
	BufferedOutputStream bos;

	int matchLength;
	int matchOffset;
	String theMatch;

	StringBuilder encoded;
	StringBuilder decoded;

	int bitSetIterator;

	//	BitSet outFile;
	//	int currentBit;

	public LZSSEncoderDecoder(int searchWindowSize, int lookAhead, int match) {
		this.searchWindowSize = searchWindowSize;
		this.maximuMatchSize = lookAhead;
		this.minimuMatchSize = match;

		this.matchLength = 0;
		this.matchOffset = 0;
		this.theMatch = "";
	}

	@Override
	public void Compress(String[] input_names, String[] output_names) throws IOException {

		fis = new FileInputStream(input_names[0]);
		fos = new FileOutputStream(output_names[0]);
		bis = new BufferedInputStream(fis);
		//bos = new BufferedOutputStream(fos);

		// Initialize StringBuilder to hold the encoded data
		encoded = new StringBuilder();

		// Write sizes metadata to the encoded data
		writeSizesToFile();

		// Initialize the search window and write initial characters to it
		StringBuilder searchWindow = new StringBuilder();
		writeSingle(searchWindow, minimuMatchSize);

		// Initialize the look-ahead buffer and fill it with data
		StringBuilder lookAhead = new StringBuilder();
		lookAheadBuilder(lookAhead, maximuMatchSize);

		while(lookAhead.length() != 0) {		

			// Find the longest matching sequence in search window and look-ahead buffer
			matchIterator(searchWindow, lookAhead);

			// If a match of sufficient length is found, write encoded match data
			if(matchLength > minimuMatchSize) {
				writeAmatch();
				//System.out.println("A match !!!");
			}

			else {

				// If a match is too short, write it as individual characters
				smallMatchWrite();

			}

			//System.out.print(theMatch);

			// Update buffers and search windows for next iteration
			updateBuffers(searchWindow, lookAhead);

		}

		// Write the encoded data to the output file
		writeEncodedToFile(fos);

		bis.close();
		fos.close();

	}

	private void writeEncodedToFile(FileOutputStream fos) throws IOException {

		//System.out.println("desired: " + encoded.length());

		// Calculate the number of unnecessary bits for alignment
		int unnecessary = ((encoded.length() + 63) / 64 * 64) - encoded.length();
		encoded.insert(0, ByteToBits(unnecessary));// Insert the number of unnecessary bits as metadata at the beginning of encoded data

		//System.out.println(unnecessary);
		
		// Create a BitSet to store the encoded bits
		BitSet encodedBits = new BitSet(encoded.length());
		
		// Find the indices of '1' bits in the encoded data and set the corresponding bits in the BitSet
		int nextSetBitIndex = encoded.indexOf("1");
		while(nextSetBitIndex != -1) {

			encodedBits.set(nextSetBitIndex);
			nextSetBitIndex = encoded.indexOf("1", nextSetBitIndex + 1);

		}

		//int size = encodedBits.size() - unnecessary;
		//we need this on the file head after we did all of the encoding, since we didn't know from the beginning what the size will be:
		//System.out.println(encodedBits.size());

		ObjectOutputStream oos = new ObjectOutputStream(fos);
		oos.writeObject(encodedBits);

	}

	private void smallMatchWrite() throws IOException {

		for(int i = 0; i < matchLength; i++) {
			
			// Append '0' to signify a small match (single character)
			encoded.append('0');
			
			// Convert the current character to its binary representation
			String out = ByteToBits(theMatch.charAt(i));
			encoded.append(out);	

		}

	}

	private void updateBuffers(StringBuilder searchWindow, StringBuilder lookAhead) throws IOException {

		// Append the encoded match to the search window
		searchWindow.append(theMatch);
		
		// Delete the matched part from the look-ahead buffer
		lookAhead.delete(0, matchLength);
		lookAheadBuilder(lookAhead, maximuMatchSize);// Fill the look-ahead buffer with new data

		if(searchWindow.length() > searchWindowSize) {// If the search window exceeds its maximum size "slide" it
			searchWindow.delete(0, searchWindow.length() - searchWindowSize);
		}

		theMatch = "";
		matchOffset = 0;
		matchLength = 0;

	}

	private void writeAmatch() throws IOException {
		
		String writeToFileOffset ="";
		String writeToFileLength ="";

		encoded.append('1'); //write 1 to represent a match		

		//an efficient way to write the offset and length

		//write how many bytes the offset consumes
		writeToFileOffset = byteUsage(matchOffset);
		encoded.append(writeToFileOffset);

		//write the offset itself
		writeToFileOffset = ByteToBits(matchOffset);
		encoded.append(writeToFileOffset);

		//then the length:

		//write how many bytes the Length consumes
		writeToFileLength = byteUsage(matchLength);
		encoded.append(writeToFileLength);

		//write the Length itself
		writeToFileLength = ByteToBits(matchLength);
		encoded.append(writeToFileLength);

	}

	//each time the decompress after reading 1 will read the next 2 bit to see how many bytes each variable consumes 
	private String byteUsage(int matchThing) {

		int howManyBytes, howManyBits = 0;

		//we use maximum of 2 bits it will provide a length that can reach 3 bytes maximum
		String matchThingBit = Integer.toBinaryString(matchThing);
		howManyBits = matchThingBit.length(); //we will check how many bits it consumes to know how many bytes we should read
		howManyBytes = (howManyBits + 7) / 8;// Calculate the number of bytes needed (rounded up)

		matchThingBit = Integer.toBinaryString(howManyBytes);
		while(matchThingBit.length() < 2)
			matchThingBit = '0' + matchThingBit;

		return matchThingBit;
	}

	private void lookAheadBuilder(StringBuilder lookAhead, int matchSize) throws IOException {

		int byteIterator;
		int i=0;

		while((byteIterator = bis.read()) != -1 && i < matchSize) {

			if(byteIterator < 0)
				byteIterator+=256;

			char nextByte = (char)(byteIterator);
			lookAhead.append(nextByte);

			i++;

		}

	}
	// This method encodes and writes a sequence of single characters to the encoded data.

	private void writeSingle(StringBuilder data, int matchLength) throws IOException {

		for(int i = 0 ; i < matchLength ; i ++){			
			// Read the next byte from the input stream
			int byteIterator = bis.read();

			// Check if we have reached the end of the input stream
			if(byteIterator != -1) {

				// Append '0' to signify a single character
				encoded.append('0');

				// Adjust byte value if it's negative
				if(byteIterator < 0)
					byteIterator+=256;

				// Convert the byte to its binary representation
				String out = ByteToBits(byteIterator);

				// Append the binary representation to the encoded data
				encoded.append(out);
				data.append((char)byteIterator);// Append the character to the data StringBuilder

			}
		}	

	}
	private void matchIterator(StringBuilder searchWindow, StringBuilder lookAhead) throws IOException {
		// Iterate while there's enough data to be collected in the match sequence and in the look-ahead buffer
		int lookAheadIndex = 0;
		while((theMatch.length() + 1 < maximuMatchSize) && (lookAhead.length() > lookAheadIndex)) {
			
			// Get the next character from the look-ahead buffer
			char nextByte = (char)(lookAhead.charAt(lookAheadIndex));
			
			// Check if the current match sequence combined with the next character is present in the search window
			if(searchWindow.toString().contains(theMatch + nextByte)) {

				theMatch = theMatch + nextByte;
				matchOffset = searchWindow.indexOf(theMatch);

				matchLength++;
				lookAheadIndex++;// Move to the next character in the look-ahead buffer

			}

			else {
				// If a match is not found, and the current match length is 0, consider the next character as a small match
				if(matchLength == 0){

					matchLength = 1;
					theMatch = theMatch + lookAhead.charAt(0);

				}

				return;
			}


		}//while ends

	}

	private void writeSizesToFile() throws IOException {

		//how many bits it use to represent in byte -> 512 uses 9
		int SearchWindowBits = (int)(Math.log(searchWindowSize) / Math.log(2));
		int maximuMatchBits = (int)(Math.log(maximuMatchSize) / Math.log(2));

		String SearchWindowSizeBits = ByteToBits(SearchWindowBits);
		String maximuMatchSizeBits = ByteToBits(maximuMatchBits);

		encoded.append(SearchWindowSizeBits);
		encoded.append(maximuMatchSizeBits);


	}

	//delete doesn't worked:
	private void writeBitsToEncoded(String bits) throws IOException {
		int bitIndex = 0;
		while (bitIndex < bits.length()) {
			// Create a byte from the next 8 bits
			String byteBits = bits.substring(bitIndex, Math.min(bitIndex + 8, bits.length()));
			byte b = (byte) Integer.parseInt(byteBits, 2);

			// Write the byte to the BufferedOutputStream
			bos.write(b);

			// Move to the next 8 bits
			bitIndex += 8;
		}		
	}

	private String ByteToBits(int toBits) {

		String binaryVal = Integer.toBinaryString(toBits);

		while(binaryVal.length() % 8 != 0)
			binaryVal = '0' + binaryVal;

		return binaryVal;

	}

	@Override
	public void Decompress(String[] input_names, String[] output_names) throws ClassNotFoundException, IOException {

		fis = new FileInputStream(input_names[0]);
		fos = new FileOutputStream(output_names[0]);
		//bis = new BufferedInputStream(fis);
		bos = new BufferedOutputStream(fos);

		BitSet encoded = importData();
		bitSetIterator = 0;

		int encodedSize = readSizes(encoded);

		//Remember to check if match is empty!!!!

		StringBuilder searchWindow = new StringBuilder();

		while(bitSetIterator < encodedSize) {

			if(encoded.get(bitSetIterator)) { //if this is a match

				bitSetIterator++; //to read the next 2 bits that represent how many bytes consumed

				int bytes = 0;

				bytes = howManyBytes(encoded);// Calculate the number of bytes needed to represent the match offset
				matchOffset = readBytes(bytes, encoded);// Read the match offset value from the encoded data

				bytes = howManyBytes(encoded);// Calculate the number of bytes needed to represent the match length
				matchLength = readBytes(bytes, encoded);
				// Extract the matched sequence from the search window based on the offset and length
				theMatch = searchWindow.substring(matchOffset, matchOffset + matchLength);


				//write match to file
				writeDecoded();
			}

			else {//a symbol for a single char

				bitSetIterator++;
				matchLength = 1;
				theMatch += (char)(readBytes(1, encoded));


				//write to file
				writeDecoded();

			}

			updateSearchWindow(searchWindow);

		}

		bos.close();

	}

	private void writeDecoded() throws IOException {

		byte[] contentBytes = theMatch.getBytes();
		bos.write(contentBytes);

	}

	private void updateSearchWindow(StringBuilder searchWindow) {

		searchWindow.append(theMatch);

		if(searchWindow.length() > searchWindowSize)
			searchWindow.delete(0, searchWindow.length() - searchWindowSize);

		theMatch = "";
		matchOffset = 0;
		matchLength = 0;

	}

	private int howManyBytes(BitSet encoded) {

		int bytes = 0;

		for(int j = 1; j >= 0; j--) {

			if(encoded.get(bitSetIterator)) {//if 1

				bytes += Math.pow(2, j);
				bitSetIterator++;

			}
			else
				bitSetIterator++; //until the next 1 founded

		}

		return bytes;
	}

	private BitSet importData() throws IOException, ClassNotFoundException {

		ObjectInputStream ois = new ObjectInputStream(fis);

		BitSet encoded = (BitSet) ois.readObject();

		ois.close();

		return encoded;

	}

	private int readSizes(BitSet encoded) throws IOException {

		//We don't want to read also the data that was inserted to fill the bitSet
		int encodedSize = encoded.size() - readBytes(1, encoded); //according to the writing order

		//System.out.println(encodedSize);

		searchWindowSize = (int)(Math.pow(2, readBytes(1 ,encoded)));
		maximuMatchSize = (int)(Math.pow(2, readBytes(1 ,encoded)));

		return encodedSize;

	}
	//will read 1, 2, 3 bytes
	private int readBytes(int i, BitSet encoded) {

		int howManyBites = i * 8 - 1;
		int bytes = 0;

		for(int j = howManyBites; j >= 0; j--) {

			if(encoded.get(bitSetIterator)) {//if 1

				bytes += Math.pow(2, j);
				bitSetIterator++;

			}
			else
				bitSetIterator++; //until the next 1 founded

		}

		return bytes;
	}

	@Override
	public byte[] CompressWithArray(String[] input_names, String[] output_names) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public byte[] DecompressWithArray(String[] input_names, String[] output_names) {
		// TODO Auto-generated method stub
		return null;
	}
}
