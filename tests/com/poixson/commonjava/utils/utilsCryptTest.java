package com.poixson.commonjava.utils;

import junit.framework.TestCase;

import org.junit.Test;

import com.poixson.commonjava.xLogTest;
import com.poixson.commonjava.Utils.utils;
import com.poixson.commonjava.Utils.utilsCrypt;


public class utilsCryptTest extends TestCase {
	static final String TEST_NAME_ENCODE = "Base64 Encode";
	static final String TEST_NAME_DECODE = "Base64 Decode";

	static final String TEST_DECODED = "This is the test data! This is the test data!";
	static final String TEST_ENCODED = "VGhpcyBpcyB0aGUgdGVzdCBkYXRhISBUaGlzIGlzIHRoZSB0ZXN0IGRhdGEh";



	// base64 encode
	@Test
	public void testEncodeBase64() {
		xLogTest.testStart(TEST_NAME_ENCODE);
		xLogTest.publish("Input:  "+TEST_DECODED);
		final String data = utilsCrypt.Base64Encode(TEST_DECODED);
		xLogTest.publish("Output: "+data);
		assertTrue(utils.notEmpty(data));
		assertEquals(TEST_ENCODED, data);
		xLogTest.testPassed(TEST_NAME_ENCODE);
	}
	// base64 decode
	@Test
	public void testDecodeBase64() {
		xLogTest.testStart(TEST_NAME_DECODE);
		xLogTest.publish("Input:  "+TEST_ENCODED);
		final String data = utilsCrypt.Base64Decode(TEST_ENCODED);
		xLogTest.publish("Output: "+data);
		assertTrue(utils.notEmpty(data));
		assertEquals(TEST_DECODED, data);
		xLogTest.testPassed(TEST_NAME_DECODE);
	}



}
