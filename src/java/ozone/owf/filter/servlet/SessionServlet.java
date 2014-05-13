package ozone.owf.filter.servlet;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class SessionServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;

	@Override
	public void doGet(final HttpServletRequest request,
			final HttpServletResponse response) throws ServletException,
			IOException {

		response.setContentType("text/html");

		final PrintWriter out = response.getWriter();

		final String paramName = request.getParameter("param");

		// Obtain the session object, create a new session if doesn't exist
		final HttpSession session = request.getSession(true);

		String paramValue = (String) session.getAttribute(paramName);
		if (paramValue == null) {
			paramValue = "";
		}

		out.print(paramValue);

		out.flush(); // Commits the response
		out.close();
	}

	@Override
	public void doPost(final HttpServletRequest request,
			final HttpServletResponse response) throws ServletException,
			IOException {

		final String paramName = request.getParameter("key");
		final String paramValue = request.getParameter("value");

		if (paramName != null && paramValue != null) {
			// Obtain the session object, create a new session if doesn't exist
			final HttpSession session = request.getSession(true);
			session.setAttribute(paramName, paramValue);
		}
	}

	@Override
	public void doDelete(final HttpServletRequest request,
			final HttpServletResponse response) throws ServletException,
			IOException {

		final String paramName = request.getParameter("key");

		// Obtain the session object
		final HttpSession session = request.getSession();

		// If paramName isn't null, delete session attribute. Otherwise,
		// invalidate session.
		if (paramName != null) {
			session.removeAttribute(paramName);
		} else {
			session.invalidate();
		}
	}
}
