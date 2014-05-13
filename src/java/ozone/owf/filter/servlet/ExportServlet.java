package ozone.owf.filter.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

public class ExportServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;

	@Override
	public void doGet(final HttpServletRequest request,
			final HttpServletResponse response) throws ServletException,
			IOException {
		response.setContentType("application/x-unknown");
		response.setHeader("Content-Disposition",
				"attachment; filename=dashboard.json");

		final PrintWriter out = response.getWriter();

		final boolean isMultipart = ServletFileUpload
				.isMultipartContent(request);
		if (isMultipart) {
			try {
				final FileItemFactory factory = new DiskFileItemFactory();
				final ServletFileUpload upload = new ServletFileUpload(factory);

				@SuppressWarnings("unchecked")
				final List<FileItem> items = upload.parseRequest(request);
				final Iterator<FileItem> iter = items.iterator();

				while (iter.hasNext()) {
					final FileItem item = iter.next();
					if (item.isFormField()) {
						final String name = item.getFieldName();
						if (name.equals("json")) {
							out.println(item.getString());
						}
					}
				}
			} catch (final Exception e) {
				System.out.println(e.toString());
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
}
