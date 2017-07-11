package net.packsam.carpi.api;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.EntityTag;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.StreamingOutput;

import org.apache.commons.io.IOUtils;

import net.packsam.carpi.model.StreamedResource;

/**
 * Base REST service for streaming resources to the client.
 * 
 * @author osterrath
 * 
 */
abstract public class StreamingEndpoint {

	/**
	 * Creates a response that streams the given resource or answers with "not modified".
	 * 
	 * @param resource
	 *            resource to be streamed
	 * @param request
	 *            http request
	 * @param download
	 *            flag if the file should be downloaded by client
	 */
	protected Response createResponse(StreamedResource resource, Request request, boolean download) {
		// load the cache check values
		Date lastModified = resource.getLastModified();
		ResponseBuilder builder = null;
		EntityTag eTag = null;
		CacheControl cc = null;
		if (lastModified != null) {
			eTag = new EntityTag(Long.toHexString(lastModified.getTime()));

			// live time of response
			if (resource.getMaxCachingAge() != null) {
				cc = new CacheControl();
				cc.setMaxAge(resource.getMaxCachingAge());
				cc.setMustRevalidate(true);
			}

			// check 'if modified' request
			builder = request.evaluatePreconditions(lastModified, eTag);
		}

		// cached resource did change -> serve updated content
		if (builder == null) {
			try {
				// create output stream for fast file streaming
				final InputStream in = resource.getInputStream();
				StreamingOutput out = new StreamingOutput() {
					@Override
					public void write(OutputStream output) throws IOException, WebApplicationException {
						try {
							IOUtils.copy(in, output);
						} catch (Exception e) {
							// ignore exceptions as client may hung up
						} finally {
							IOUtils.closeQuietly(in);
						}
					}
				};

				// build response
				builder = Response.ok(out, resource.getMimeType());
				if (eTag != null) {
					builder.tag(eTag);
				}
				if (cc != null) {
					builder.cacheControl(cc);
				}
			} catch (IOException e) {
				builder = Response.status(Status.NOT_FOUND);
			}
		} else {
			// if (eTag != null) {
			// builder.tag(eTag);
			// }
			if (cc != null) {
				builder.cacheControl(cc);
			}
		}

		if (download) {
			builder.header("Content-Disposition", "attachment; filename=" + resource.getFileName());
		} else {
			builder.header("Content-Disposition", "inline; filename=" + resource.getFileName());
		}

		return builder.build();
	}
}
