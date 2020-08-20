package com.ds.common;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.matrixone.servlet.LogoutServlet;

public class PWCLogoutServlet extends LogoutServlet 
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 921415799204604478L;
	private static final Logger _LOG = Logger.getLogger("PWCLogoutServlet");
	
	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
	{
		_LOG.debug("Start PWCLogoutServlet::doGet()");
		super.doGet(req, resp);
		ServletOutputStream out = resp.getOutputStream();
        out.println("<script>");
        out.println("document.execCommand('ClearAuthenticationCache', 'false');");
        out.println("</script>");
        resp.setContentType("text/html;charset=UTF-8");
        _LOG.debug("->Added  <script>document.execCommand('ClearAuthenticationCache', 'false');</script>");
        _LOG.debug("End PWCLogoutServlet::doGet()");
	}
}