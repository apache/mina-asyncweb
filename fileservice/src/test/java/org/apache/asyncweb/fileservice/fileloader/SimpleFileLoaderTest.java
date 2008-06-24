package org.apache.asyncweb.fileservice.fileloader;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Random;

import org.apache.asyncweb.fileservice.fileloader.SimpleFileLoader;
import org.apache.mina.common.buffer.IoBuffer;
import org.junit.Test;

public class SimpleFileLoaderTest {

	private static final int TEMP_SIZE=1024*100+3; // ~100K file
	
	@Test
	public void testLoadFile() throws Exception {
		// generate temp file
		File tempFile=File.createTempFile("dummy",null);
		tempFile.deleteOnExit();
		FileOutputStream fos=new FileOutputStream(tempFile);
		Random rng=new Random();
		
		byte[] data = new byte[TEMP_SIZE];
		rng.nextBytes(data);
		fos.write(data);
		fos.close();
		
		SimpleFileLoader sfl=new SimpleFileLoader();
		IoBuffer buffer=sfl.loadFile(tempFile);

		assertTrue(buffer.remaining()==TEMP_SIZE);
		
		for(int i=0;i<TEMP_SIZE;i++) {
			assertTrue(data[i]==buffer.get());
		}
		
		buffer=sfl.loadFile(tempFile);

		assertTrue(buffer.remaining()==TEMP_SIZE);
		
		for(int i=0;i<TEMP_SIZE;i++) {
			assertTrue(data[i]==buffer.get());
		}
	}
}
