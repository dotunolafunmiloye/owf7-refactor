package ozone.owf.filter.servlet;

import grails.converters.JSON;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.codehaus.groovy.grails.web.json.JSONArray;
import org.codehaus.groovy.grails.web.json.JSONElement;
import org.codehaus.groovy.grails.web.json.JSONObject;

public class FileServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;

	@Override
	public void doGet(final HttpServletRequest request,
			final HttpServletResponse response) throws ServletException,
			IOException {

		response.setContentType("text/html");

		String errorMsg = "";
		final PrintWriter out = response.getWriter();

		final boolean isMultipart = ServletFileUpload
				.isMultipartContent(request);
		if (isMultipart) {
			try {

				// Create a new file upload handler
				final ServletFileUpload upload = new ServletFileUpload();

				// Parse the request
				final FileItemIterator iter = upload.getItemIterator(request);
				final String failureJsonString = "{\"success\": false,\n"
						+ "\"value\": {}\n}";
				while (iter.hasNext()) {
					final FileItemStream item = iter.next();
					final InputStream stream = item.openStream();
					if (item.isFormField()) {
						// Do nothing
					} else {
						// Process the input stream
						String data = convertStreamToString(stream);
						if (data.length() == 0) {
							data = failureJsonString;
						} else {
							try {
								final JSONElement je = JSON.parse(data.replace(
										"<", "&lt;").replace(">", "&gt;"));
								if (je instanceof JSONObject) {
									final JSONObject jo = (JSONObject) je;
									if (jo.isEmpty()) {
										data = failureJsonString;
									} else {
										data = "{\"success\": true,\n"
												+ "\"value\": " + jo.toString()
												+ "\n}";
									}
								} else {
									if (je instanceof JSONArray) {
										final JSONArray ja = (JSONArray) je;
										if (ja.isEmpty()) {
											data = failureJsonString;
										} else {
											data = "{\"success\": true,\n"
													+ "\"value\": "
													+ ja.toString() + "\n}";
										}
									}
								}
							} catch (final Exception e) {
								// Set success to false and value to empty JSON
								data = failureJsonString;
							}
						}
						out.println(data);
					}
				}
			} catch (final Exception e) {
				errorMsg = e.toString();
				out.println(errorMsg);
			}
		}

		out.flush(); // Commits the response
		out.close();
	}

	@Override
	public void doPost(final HttpServletRequest request,
			final HttpServletResponse response) throws ServletException,
			IOException {

		doGet(request, response);
	}

	public String convertStreamToString(final InputStream is) {
		/*
		 * To convert the InputStream to String we use the
		 * BufferedReader.readLine() method. We iterate until the BufferedReader
		 * return null which means there's no more data to read. Each line will
		 * appended to a StringBuilder and returned as String.
		 */
		final BufferedReader reader = new BufferedReader(new InputStreamReader(
				is));
		final StringBuilder sb = new StringBuilder();
		boolean hasGuid = false;

		String line = null;
		try {
			while ((line = reader.readLine()) != null) {
				// The character string 'guid' must exist in the data.
				// If not found clear return string ('sb') and bail out.
				if (!(line.indexOf("guid") == -1)) {
					hasGuid = true;
				}
				sb.append(line + "\n");
			}
			if (!hasGuid) {
				sb.setLength(0);
			}
		} catch (final IOException e) {
			sb.setLength(0);
		} finally {
			try {
				is.close();
			} catch (final IOException e) {
				e.printStackTrace();
			}
		}

		return sb.toString();
	}
}
