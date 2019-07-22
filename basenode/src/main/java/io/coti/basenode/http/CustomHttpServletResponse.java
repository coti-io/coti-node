package io.coti.basenode.http;

import com.google.gson.Gson;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import java.io.IOException;
import java.io.PrintWriter;

public class CustomHttpServletResponse extends HttpServletResponseWrapper {

    public CustomHttpServletResponse(HttpServletResponse response) {
        super(response);
    }

    public void printResponse(BaseResponse response, int status) throws IOException {
        printResponse(new Gson().toJson(response), status);
    }

    public void printResponse(String string, int status) throws IOException {
        PrintWriter writer = this.getWriter();
        this.setStatus(status);
        this.setContentType("application/json");
        this.setCharacterEncoding("UTF-8");
        writer.print(string);
        writer.flush();
    }

}
