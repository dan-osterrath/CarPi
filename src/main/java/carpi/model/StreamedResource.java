/**
 * (C) 2017 by 3m5. Media GmbH. http://www.3m5.de
 */
package carpi.model;

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

/**
 * Interface for a streamed resource that can be sent to client.
 * 
 * @author osterrath
 * 
 */
public interface StreamedResource {
	/**
	 * Returns the input stream for this resource.
	 * 
	 * @return input stream
	 */
	public InputStream getInputStream() throws IOException;

	/**
	 * Returns the file name of the file.
	 * 
	 * @return file name
	 */
	public String getFileName();

	/**
	 * Returns the maximum age for caching.
	 * 
	 * @return max age
	 */
	public Integer getMaxCachingAge();

	/**
	 * Returns the last modified date for this resource.
	 * 
	 * @return last modified date
	 */
	public Date getLastModified();

	/**
	 * Returns the MIME type for the resource.
	 * 
	 * @return MIME type
	 */
	public String getMimeType();
}