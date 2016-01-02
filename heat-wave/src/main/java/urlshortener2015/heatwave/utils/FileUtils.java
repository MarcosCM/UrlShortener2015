package urlshortener2015.heatwave.utils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;

public class FileUtils {

	/**
	 * Gets the content of the file
	 * @param file File path
	 * @return File content
	 * @throws IOException
	 */
	public static String readContent(String file) throws IOException{
		return FileUtils.readContent(new File(file));
	}
	
	/**
	 * Gets the content of the file
	 * @param file File path
	 * @return File content
	 * @throws IOException
	 */
	public static String readContent(String file, Charset charset) throws IOException {
		return FileUtils.readContent(new File(file), charset);
	}
	
	/**
	 * Gets the content of the file
	 * @param file File
	 * @return File content
	 * @throws IOException
	 */
	public static String readContent(File file) throws IOException {
		return FileUtils.readContent(file, Charset.defaultCharset());
	}
	
	/**
	 * Gets the content of the file
	 * @param file File
	 * @return File content
	 * @throws IOException
	 */
	public static String readContent(File file, Charset charset) throws IOException {
		byte[] encoded = Files.readAllBytes(Paths.get(file.getPath()));
		return new String(encoded, charset);
	}
}
