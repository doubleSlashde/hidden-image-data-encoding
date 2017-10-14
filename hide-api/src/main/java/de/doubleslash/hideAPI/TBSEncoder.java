package de.doubleslash.hideAPI;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.stream.IntStream;

/**
 * 
 * HIDE - Hidden Image Data Encoding - is a tool for steganography - encoding
 * and signing information into a PNG picture with cryptographic security.
 * Disclaimer: This is a proof-of-concept or prototype and may include
 * unfinished functions or inconsistencies.
 * 
 * Copyright (C) 2017 Wolfgang Michael Hermann
 * 
 * This file is part of HIDE.
 * 
 * HIDE is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * HIDE is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * HIDE. If not, see <http://www.gnu.org/licenses/>.
 * 
 * This File is published toghether with the thesis "Hidden Image Data Encoding"
 * by Wolfgang M. Hermann and is a resource for said thesis.
 * 
 * @author whermann
 *
 */

class TBSEncoder {

	private BufferedImage canvas;
	private byte[] message;

	private int byteCapacity;

	private int canvasCursor;

	private int rgb;
	private int[] rgbarray = new int[3];

	static final int BYTELENGTH = 8;
	static final int CURSOR_MINPOS = 5;

	/**
	 * Protected Default Constructor.
	 */
	protected TBSEncoder() {
		canvasCursor = 0;
		byteCapacity = 0;
	}

	// GETTERS AND SETTERS

	public byte[] getMessage() {
		return message;
	}

	public void setMessage(byte[] message) {
		this.message = message;
	}

	public void setImage(BufferedImage image) {
		this.canvas = image;
	}

	public BufferedImage getImage() {
		return this.canvas;
	}

	/**
	 * This overload function calls "encode()" after setting message and image.
	 * Lookup "encode()" Javadoc for details
	 * 
	 * @param message
	 *            - The message to be encoded
	 * @param image
	 *            - The image to be modified
	 * @return
	 */
	public BufferedImage encode(byte[] message, BufferedImage image) {
		if (message == null || image == null) {
			return null;
		}
		setMessage(message);
		setImage(image);

		return encode();
	}

	/**
	 * This function starts the encoding algorithm. Once it has set foot, it
	 * will modify the image held by this class and return the result to save !
	 * in png format !, otherwise Data is deemed to be lost and / or
	 * irreconstructible.
	 * 
	 * @return The modified Image, unless the encoding failed or the image /
	 *         message were null.
	 */
	public BufferedImage encode() {

		if (this.canvas == null) {
			System.err.println("Canvas not set!");
			return null;
		}

		if (this.message.length < 1) {
			System.err.println("Message not set!");
			return null;
		}

		byteCapacity = this.getImageCapacity();
		if (message.length > byteCapacity) {
			System.err.println("Message bigger than image capacity!");
			return null;
		}

		// System.out.println("Kapazität des Bildes: " + byteCapacity + "
		// Byte");

		// this.is where the magic happens
		this.encodeMessage(message, false);

		return canvas;
	}

	/**
	 * Encode the Verificator (Signature, Verificator Hash and public key) into
	 * the image, that will be used to verify its integrity.
	 * 
	 * @param hash
	 * @param sig
	 * @return true if successful
	 */
	public Boolean encodeVerificator(byte[] hash, byte[] sig, byte[] key) {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		try {
			IntStream.range(1, ImageHandler.SEPERATOR_SIZE).forEach(i -> out.write(ImageHandler.SEPERATOR));
			out.write(hash);
			out.write(sig);
			out.write(key);
		} catch (IOException e) {
			// never thrown.
			e.printStackTrace();
		}
		byte[] combined = out.toByteArray();

		return this.encodeMessage(combined, true);
	}

	/**
	 * This overload function calls "decode()" after setting the image. Lookup
	 * "decode()" Javadoc for details
	 * 
	 * @param image
	 * @return
	 */
	public byte[] decode(BufferedImage image) {
		if (image == null) {
			return null;
		}

		setImage(image);

		return decode();
	}

	/**
	 * Decodes the message inside the Image. Returns a byte array, which then
	 * can be handled by MessageHandler class. It should first be verified, that
	 * the bytestream from the picture is indeed a valid message.
	 * 
	 * @return the byte array read from the picture.
	 */
	public byte[] decode() {

		if (this.canvas == null) {
			System.err.println("Canvas not set!");
			return null;
		}

		return this.decodeMessage();
	}

	/**
	 * Call this Message to get an incredibly long string, as in infeasibly long
	 * - it is the color values of every pixel in the picture in a row. This is
	 * for signing and hashing, with signature and hash encoded in a different
	 * place. IMPORTANT: call this function *after* encode() but *before*
	 * encodeHashAndSig(); you need this String to create the hash and Signature
	 * anyway.
	 * 
	 * Printing this String to console when handling high resolution picture is
	 * absolutely ill - advised, you may call String.length() to see for
	 * yourself.
	 * 
	 * @return The colorVerificationString, which is all Integer Color values of
	 *         the whole picture in a row
	 */
	public String getColorVerificationString() {
		if (canvas == null || canvasCursor < 1) {
			return null;
		}

		int hashAndSigSize = BYTELENGTH * (ImageHandler.VERIFICATOR_SIZE + ImageHandler.SIGNATURE_SIZE
				+ ImageHandler.KEY_SIZE + ImageHandler.SEPERATOR_SIZE);
		int canvasCursorLocal = 0;
		int rgb2 = 0;
		int[] rgbarray2 = new int[3];
		StringBuilder verificator = new StringBuilder();
		// System.out.println("TBSE: verificationPosition: " + canvasCursor);
		// System.out.println("Ignoring Pixels Nr.: ");
		for (int x = 0; x < canvas.getWidth(); x++) {
			for (int y = 0; y < canvas.getHeight(); y++) {

				rgb2 = canvas.getRGB(x, y);

				rgbarray2[0] = (rgb2 >> BYTELENGTH * 2) & 0xff;
				rgbarray2[1] = (rgb2 >> BYTELENGTH) & 0xff;
				rgbarray2[2] = rgb2 & 0xff;

				if (canvasCursorLocal > canvasCursor && hashAndSigSize > 0) {
					// System.out.print(" " + canvasCursorLocal);
					hashAndSigSize -= getValueCapacity(rgbarray2[0]) + getValueCapacity(rgbarray2[1])
							+ getValueCapacity(rgbarray2[2]);
				} else {
					verificator.append(String.valueOf(rgbarray2[0]));
					verificator.append(String.valueOf(rgbarray2[1]));
					verificator.append(String.valueOf(rgbarray2[2]));
				}

				canvasCursorLocal++;
			}
		}

		return verificator.toString();
	}

	/**
	 * This Function will return the color verification string to compare the
	 * hashes with the one form the picture. This function takes the start of
	 * the verificator portion of the encoding as parameter, so the verification
	 * String can be properly generated. Use MessageHandler function to get the
	 * position out of the bytestream read from this picture.
	 * 
	 * @param verificatorPosition
	 * @return
	 */
	public String getColorVerificationStringForComparison() {

		byte[] seperator = new byte[ImageHandler.SEPERATOR_SIZE - 1];
		Arrays.fill(seperator, ImageHandler.SEPERATOR);

		String bytes = new String(message);
		String sep = new String(seperator);

		int bitcount = (bytes.indexOf(sep) + ImageHandler.SEPERATOR_SIZE) * BYTELENGTH;

		int i = 0;
		int rgb2 = 0;
		int[] rgbarray2 = new int[3];
		for (int x = 0; x < canvas.getWidth(); x++) {
			for (int y = 0; y < canvas.getHeight(); y++) {

				rgb2 = canvas.getRGB(x, y);

				rgbarray2[0] = (rgb2 >> BYTELENGTH * 2) & 0xff;
				rgbarray2[1] = (rgb2 >> BYTELENGTH) & 0xff;
				rgbarray2[2] = rgb2 & 0xff;

				bitcount -= getValueCapacity(rgbarray2[0]) + getValueCapacity(rgbarray2[1])
						+ getValueCapacity(rgbarray2[2]);

				if (bitcount <= 0) {
					break;
				}

				i++;
			}
			if (bitcount <= 0) {
				break;
			}
		}

		canvasCursor = i + 1;

		canvasCursor = (canvasCursor < CURSOR_MINPOS) ? CURSOR_MINPOS : canvasCursor;

		String result = getColorVerificationString();

		return result;
	}

	// PRIVATE functions

	/**
	 * Returns the size of the Image in (!) Bits
	 * 
	 * @return
	 */
	private int getImageCapacity() {
		int capacity = 0;
		int rgb2 = 0;
		int[] rgbarray2 = new int[3];

		for (int x = 0; x < canvas.getWidth(); x++) {
			for (int y = 0; y < canvas.getHeight(); y++) {
				rgb2 = canvas.getRGB(x, y);

				rgbarray2[0] = (rgb2 >> BYTELENGTH * 2) & 0xff;
				rgbarray2[1] = (rgb2 >> BYTELENGTH) & 0xff;
				rgbarray2[2] = rgb2 & 0xff;

				for (int i = 0; i < 3; i++) {
					capacity += this.getValueCapacity(rgbarray2[i]);
				}
			}
		}

		// System.out.println("Image Capacity is " + capacity + " Byte.");
		return capacity;
	}

	/**
	 * Returns the bit - capacity of the color luminosity value handed over.
	 * 
	 * @param value
	 * @return
	 */
	private int getValueCapacity(int value) {
		int capacity = 1;

		// if MSB is set, exit. Else increase capacity value and shift value.
		// Repeat.
		while ((value & 0x80) == 0 && capacity < 4) {
			capacity++;
			value = value << 1;
		}

		return capacity;
	}

	/**
	 * Actual Treshold Based Steganography encoding Algorithm.
	 * 
	 * Check inline comments and go get a coffee. PS: i am sorry.
	 * 
	 * @return
	 */
	private Boolean encodeMessage(byte[] message, Boolean isSignature) {
		if (message == null) {
			throw new IllegalArgumentException("byte array is null");
		}
		// create a single Pixel space between signature and message
		if (isSignature) {
			canvasCursor += 1;
		}
		int messageIterator = 0;
		byte messageByte = message[messageIterator];

		int byteIterator = 0;

		int capacity = 0;
		byte b = (byte) 0x00;

		// iterate through picture. 
		for (int x = 0; x < canvas.getWidth(); x++) {
			for (int y = 0; y < canvas.getHeight(); y++) {

				// jump over the pixels that have already been written, if the
				// message to encode is a Signature
				if (!isSignature || canvasCursor <= 0) {

					// read color values and bit shift (fastest operation) them
					// into an array
					rgb = canvas.getRGB(x, y);
					rgbarray[0] = (rgb >> BYTELENGTH * 2) & 0xff;
					rgbarray[1] = (rgb >> BYTELENGTH) & 0xff;
					rgbarray[2] = rgb & 0xff;

					// canvasCursor numbers the last encoded pixel, increase
					// only when we encode a message
					if (!isSignature) {
						canvasCursor++;
					}
					// Separately handle every color channel (repeat 3 loop)
					for (int colorValue = 0; colorValue < 3; colorValue++) {

						// create byte with current rgb value with the correct
						// number of LSBits set to 0
						capacity = this.getValueCapacity(rgbarray[colorValue]);
						b = (byte) (rgbarray[colorValue] & 0xff);
						b &= (byte) (0xff << capacity);

						// The TBS Algo will allow multiple byte in color values
						// dependant of the value. So this is erratic and will
						// run from 1 to 4 times.
						for (int i = 0; i < capacity; i++) {
							// See if current message byte is already
							// "exhausted",
							// if yes get next message byte. If message is
							// completely written, return.
							if (byteIterator >= BYTELENGTH) {
								byteIterator = 0;
								messageIterator++;
								if (messageIterator >= message.length) {
									return true;
								} else {
									messageByte = message[messageIterator];
								}
							}

							// write single bit of information to current byte
							byte a = (byte) (((messageByte >> byteIterator) & 0x1) << i);
							b |= a;
							byteIterator++;
						}

						// finally write modified byte into color value
						rgbarray[colorValue] = Byte.toUnsignedInt(b);
					}

					// remask the separate color channel into one int and set
					// new color value for current pixel, while retaining alpha
					// value from current pixel
					rgb = (rgb & 0xff000000) | (rgbarray[0] << BYTELENGTH * 2) | (rgbarray[1] << BYTELENGTH)
							| rgbarray[2];
					canvas.setRGB(x, y, rgb);
				}

				// decrease the cursor value if we encode a signature.
				if (isSignature) {
					canvasCursor--;
				}
			}
		}

		return true;
	}

	/**
	 * Actual Treshold Based Steganography decoding Algorithm.
	 * 
	 * Check inline comments and go get a coffee.
	 * 
	 * @return
	 */
	private byte[] decodeMessage() {
		// see encodeMessage() for variable explanation

		// message = new byte[this.getImageCapacity()];
		int messageIterator = 0;
		int byteIterator = 0;
		byte messageByte = (byte) 0x00;
		int capacity = 0;

		message = new byte[this.getImageCapacity()];

		// iterate through picture. 
		for (int x = 0; x < canvas.getWidth(); x++) {
			for (int y = 0; y < canvas.getHeight(); y++) {

				// read color values and bit shift (fastest operation) them into
				// an array
				rgb = canvas.getRGB(x, y);
				rgbarray[0] = (rgb >> BYTELENGTH * 2) & 0xff;
				rgbarray[1] = (rgb >> BYTELENGTH) & 0xff;
				rgbarray[2] = rgb & 0xff;

				// we need to do this 3 times, for every color. While this might
				// be migrated to another function, we need to keep track over
				// the position in the message byte array
				for (int colorValue = 0; colorValue < 3; colorValue++) {
					capacity = this.getValueCapacity(rgbarray[colorValue]);

					// The TBS Algo will allow multiple byte in color values
					// dependant of the value. so this is erratic and will run
					// from 1 to 4 times. the variable "i" is never used;
					// IntStream is not yet an option because of local
					// variables. List<Byte> is just too slow.
					for (int i = capacity; i > 0; i--) {
						// read bit and put it into the message byte, increasing
						// the position.
						messageByte |= (byte) ((rgbarray[colorValue] & 0x01) << byteIterator);
						byteIterator++;
						//
						rgbarray[colorValue] = rgbarray[colorValue] >>> 1;

						// if the "messageByte" is filled, then put it into the
						// main bytearray
						if (byteIterator >= BYTELENGTH) {
							message[messageIterator] = messageByte;

							messageByte = (byte) 0x00;
							messageIterator++;

							// Here we read the whole picture, exit
							if (messageIterator >= message.length) {
								return message;
							}

							byteIterator = 0;
						}
					}
				}
			}
		}
		return message;
	}

}
