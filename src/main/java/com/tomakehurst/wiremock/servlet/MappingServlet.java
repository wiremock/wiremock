package com.tomakehurst.wiremock.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.tomakehurst.wiremock.mapping.Mappings;

public class MappingServlet extends HttpServlet {

	private static final long serialVersionUID = -6602042274260495538L;
	
	private static Mappings mappings;

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		// TODO Auto-generated method stub
		super.doPost(req, resp);
	}

	public static void setMappings(Mappings mappings) {
		MappingServlet.mappings = mappings;
	}
	
}
