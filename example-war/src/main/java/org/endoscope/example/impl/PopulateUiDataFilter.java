package org.endoscope.example.impl;

import org.endoscope.example.TheRestController;

import javax.inject.Inject;
import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import java.io.IOException;

@WebFilter(filterName = "PopulateUiDataFilter",urlPatterns = {"/rest/endoscope/ui/*"})
public class PopulateUiDataFilter implements Filter {
    @Inject
    TheRestController controller;

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        controller.process();
        filterChain.doFilter(servletRequest, servletResponse);
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void destroy() {

    }
}
