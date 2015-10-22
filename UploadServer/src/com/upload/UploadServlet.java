package com.upload;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.tomcat.util.http.fileupload.FileItem;
import org.apache.tomcat.util.http.fileupload.FileUploadException;
import org.apache.tomcat.util.http.fileupload.disk.DiskFileItemFactory;
import org.apache.tomcat.util.http.fileupload.servlet.ServletFileUpload;

@WebServlet("/UploadServlet")
public class UploadServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		try {
			receivePost(request,response);
		} catch (FileUploadException e) {
			e.printStackTrace();
		}
		
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		try {
			receivePost(request,response);
		} catch (FileUploadException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 接收post数据
	 * @param request
	 * @param response
	 * @throws FileUploadException
	 */
	@SuppressWarnings("rawtypes")
	public void receivePost(HttpServletRequest request,HttpServletResponse response) throws FileUploadException{
		boolean isMultipart = ServletFileUpload.isMultipartContent(request);
		PrintWriter out;
		if(isMultipart){
			DiskFileItemFactory factory = new DiskFileItemFactory();
			factory.setSizeThreshold(10*1024*2014);
			//设置缓存路径，不设置默认在C盘的缓存文件夹中
			factory.setRepository(new File("F://"));
			ServletFileUpload upload = new ServletFileUpload(factory);
			upload.setHeaderEncoding("UTF-8"); 
			List items = upload.parseRequest(request);
			String savePath = "F://";
			
			Iterator iter = items.iterator();
	        // 依次处理每个上传的文件
	        while (iter.hasNext()){
	            FileItem item = (FileItem) iter.next();
				if(!item.isFormField()){
					String name = item.getName();
					// 从全路径中提取文件名
					name = name.substring(name.lastIndexOf("/") + 1);
	                long size = item.getSize();
	                if (name == null || size == 0)
	                	continue;
	                File fNew = new File(savePath, name);
	                try{
	                	//保存文件
	                    item.write(fNew);
	                }catch (Exception e){
	                    e.printStackTrace();
	                }
				}else{
					//表单数据
					try {
						//如果包含中文应写为：(转为UTF-8编码)
						String key=item.getFieldName();
						System.out.println(key);
						String fieldvalue = new String(item.getString().getBytes(),"UTF-8");
						System.out.println(fieldvalue);
					} catch (UnsupportedEncodingException e) {
						e.printStackTrace();
					}
				}
			}
		}else{
			//普通参数
			Enumeration en = request.getParameterNames();
			while (en.hasMoreElements()) {
				String paramName = (String) en.nextElement();
				System.out.println(paramName);
				String paramValue = request.getParameter(paramName);
				System.out.println(paramValue);
			}
		}
		
		/*执行完数据后返回提示数据**/
		String json = "{\"success\":\"11111111\"}";
        //设置格式（必须设置，否则客户端接收到的json中文乱码）
        response.setContentType("text/html; charset=utf-8");
        
		try {
			out = response.getWriter();
			out.println(json);
			
			out.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
