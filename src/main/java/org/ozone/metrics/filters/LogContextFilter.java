package org.ozone.metrics.filters;

import java.io.IOException;
import java.security.cert.X509Certificate;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

import org.apache.log4j.Logger;
import org.apache.log4j.MDC;

/**
 * Custom servlet filter which adds the user's common name to the Log4J mapped
 * diagnostic context for use in log files (use the %X{mapKeyName}). This
 * allows, for example, individual users' actions to be tracked in audit logs.
 * All fields default to zero for numeric fields, "-" for non-numeric.
 * <p>
 * After the request is served and the end of request fields have been added,
 * the filter logs a msg at info level. The content is "$METHOD $PATH" (e.g.
 * "GET /index.html" if it is an http request, otherwise "Request Complete".
 * <p>
 * Fields added to MDC at start of the request:
 * <ul>
 * <li>client_cn - principal name off the first cert presented by the user</li>
 * <li>method - HTTP Method of the request (GET, PUT, DELETE)
 * <li>request - the path of the request
 * <li>query_params - parameters for querying
 * <li>user_agent - User-Agent header from the request
 * <li>referer - Referer header from the request
 * <li>client_ip - ip of the requester
 * </ul>
 * <p>
 * Fields added at the end of the request:
 * <ul>
 * <li>duration_usec - time in microseconds
 * <li>status - HTTP response code as a number
 * <li>content_type - the Content-Type header on the response
 * </ul>
 */
public class LogContextFilter implements Filter {

	Logger log = Logger.getLogger(getClass());

	@Override
	public void destroy() { /* empty */
	}

	@Override
	public void init(final FilterConfig f) throws ServletException { /* empty */
	}

	@Override
	public void doFilter(final ServletRequest request,
			final ServletResponse response, final FilterChain chain)
			throws IOException, ServletException {

		final long startTime = System.nanoTime();
		StatusCodeHttpServletResponse wrappedResponse = null;
		ServletResponse usedResponse = response;
		String msg = "Request Complete";
		try {
			final X509Certificate[] certs = (X509Certificate[]) request
					.getAttribute("javax.servlet.request.X509Certificate");

			if (response instanceof HttpServletResponse) {
				wrappedResponse = new StatusCodeHttpServletResponse(
						(HttpServletResponse) response);
				usedResponse = wrappedResponse;
			}

			// Info from the first cert presented
			String name = "-";
			if (certs != null && certs.length > 0 && certs[0] != null) {
				name = wrap(certs[0].getSubjectX500Principal().getName());
			}
			MDC.put("client_cn", name);

			// if an httprequest(should be), add interesting params
			if (request instanceof HttpServletRequest) {
				final HttpServletRequest httpRequest = (HttpServletRequest) request;
				MDC.put("method", httpRequest.getMethod());
				MDC.put("request", httpRequest.getRequestURI());
				MDC.put("query_params", wrap(httpRequest.getQueryString()));
				MDC.put("user_agent", wrap(httpRequest.getHeader("User-Agent")));
				MDC.put("referer", wrap(httpRequest.getHeader("Referer")));
				MDC.put("client_ip", wrap(httpRequest.getRemoteAddr()));
				msg = name + " " + httpRequest.getMethod() + " "
						+ httpRequest.getRequestURI();
				httpRequest.getSession().setAttribute("client_cn", name);
			}
			// push down the pipe
			chain.doFilter(request, usedResponse);
		} finally {
			// total request time in microseconds (to match apache)
			final long totalTime = System.nanoTime() - startTime;
			MDC.put("duration_usec", totalTime / 1000);

			// Info about the response
			if (wrappedResponse != null) {
				MDC.put("status", wrappedResponse.statusCode);
				MDC.put("content_type", wrappedResponse.contentType);
			}

			if (log.isInfoEnabled()) {
				log.info(msg);
			}
			MDC.clear();
		}
	}

	static String wrap(final String val) {
		if (val == null) {
			return "-";
		} else {
			return val;
		}

	}

	static class StatusCodeHttpServletResponse extends
			HttpServletResponseWrapper {
		// by servlet spec, if not set then the response is 200 OK
		int statusCode = 200;
		String contentType = "";

		StatusCodeHttpServletResponse(final HttpServletResponse response) {
			super(response);
		}

		@Override
		public void setContentType(final String type) {
			super.setContentType(type);
			contentType = type;
		}

		@Override
		public void setStatus(final int sc) {
			super.setStatus(sc);
			statusCode = sc;
		}

		@Override
		public void setStatus(final int sc, final String msg) {
			super.setStatus(sc, msg);
			statusCode = sc;
		}

		@Override
		public void sendError(final int sc) throws IOException {
			super.sendError(sc);
			statusCode = sc;
		}

		@Override
		public void sendError(final int sc, final String msg)
				throws IOException {
			super.sendError(sc, msg);
			statusCode = sc;
		}
	}
}
