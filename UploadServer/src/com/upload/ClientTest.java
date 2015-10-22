package com.upload;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import net.sf.json.JSONObject;

public class ClientTest {
	public static void main(String[] args) throws Exception {
		String filePath = "D://ngrok/ngrok.cfg";
		String actionUrl = "http://127.0.0.1:8090/UploadServer/upload";
		String parameter = "{\"BUGRECORDID\":\"233211\",\"RECORDTYPE\":\"1\",\"CONTENT\":\"hheeso\",\"ST_ID\":\"122\"}";
	
		//只提交文件
		//doPostFile(actionUrl,filePath);
		//只提交普通参数
		//doPostParameter(actionUrl, parameter);
		//同时提交文件和参数
		doPostAll(actionUrl, parameter, filePath);
	}
	
	
	//不知道什么原因服务端获取不到数据
	public static void doPostParameter1(String url,String outStr){
		HttpClient client = new DefaultHttpClient();
		HttpPost httpost = new HttpPost(url);
		try {
			httpost.setEntity(new StringEntity(outStr,"UTF-8"));
			HttpResponse response = client.execute(httpost);
			String result = EntityUtils.toString(response.getEntity(),"UTF-8");
			JSONObject jsonObject = JSONObject.fromObject(result);
			System.out.println(jsonObject);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 只提交文件
	 * @param filePath
	 * @param actionUrl
	 * @throws IOException
	 * @throws NoSuchAlgorithmException
	 * @throws NoSuchProviderException
	 * @throws KeyManagementException
	 */
	public static void doPostFile(String actionUrl,String filePath) throws IOException, NoSuchAlgorithmException, NoSuchProviderException, KeyManagementException {
		File file = new File(filePath);
		if (!file.exists() || !file.isFile()) {
			throw new IOException("文件不存在");
		}

		
		URL urlObj = new URL(actionUrl);
		//连接
		HttpURLConnection con = (HttpURLConnection) urlObj.openConnection();

		con.setRequestMethod("POST"); 
		con.setDoInput(true);
		con.setDoOutput(true);
		con.setUseCaches(false); 

		//设置请求头信息
		con.setRequestProperty("Connection", "Keep-Alive");
		con.setRequestProperty("Charset", "UTF-8");

		//设置边界
		String BOUNDARY = "----------" + System.currentTimeMillis();
		con.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + BOUNDARY);

		StringBuilder sb = new StringBuilder();
		sb.append("--");
		sb.append(BOUNDARY);
		sb.append("\r\n");
		sb.append("Content-Disposition: form-data;name=\"file\";filename=\"" + file.getName() + "\"\r\n");
		sb.append("Content-Type:application/octet-stream\r\n\r\n");

		byte[] head = sb.toString().getBytes("utf-8");

		//获得输出流
		OutputStream out = new DataOutputStream(con.getOutputStream());
		//输出表头
		out.write(head);

		//文件正文部分
		//把文件已流文件的方式 推入到url中
		DataInputStream in = new DataInputStream(new FileInputStream(file));
		int bytes = 0;
		byte[] bufferOut = new byte[1024];
		while ((bytes = in.read(bufferOut)) != -1) {
			out.write(bufferOut, 0, bytes);
		}
		in.close();

		//结尾部分
		byte[] foot = ("\r\n--" + BOUNDARY + "--\r\n").getBytes("utf-8");//定义最后数据分隔线

		out.write(foot);

		out.flush();
		out.close();

		StringBuffer buffer = new StringBuffer();
		BufferedReader reader = null;
		String result = null;
		try {
			//定义BufferedReader输入流来读取URL的响应
			reader = new BufferedReader(new InputStreamReader(con.getInputStream()));
			String line = null;
			while ((line = reader.readLine()) != null) {
				buffer.append(line);
			}
			if (result == null) {
				result = buffer.toString();
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (reader != null) {
				reader.close();
			}
		}

		JSONObject jsonObj = JSONObject.fromObject(result);
		System.out.println(jsonObj);
	}
	
	/**
	 * 只提交普通参数
	 * @param requestUrl
	 * @param outputStr
	 */
	public static void doPostParameter (String actionUrl, String parameter) {
		StringBuffer buffer = new StringBuffer();
		try {
			URL url = new URL(actionUrl);
			HttpURLConnection httpUrlConn = (HttpURLConnection) url.openConnection();

			httpUrlConn.setDoOutput(true);
			httpUrlConn.setDoInput(true);
			httpUrlConn.setUseCaches(false);
			// 设置请求方式（GET/POST）
			httpUrlConn.setRequestMethod("POST");

			// 当有数据需要提交时
			if (null != parameter) {
				OutputStream outputStream = httpUrlConn.getOutputStream();
				// 注意编码格式，防止中文乱码
				outputStream.write(parameter.getBytes("UTF-8"));
				outputStream.close();
			}

			// 将返回的输入流转换成字符串
			InputStream inputStream = httpUrlConn.getInputStream();
			InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "utf-8");
			BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

			String str = null;
			while ((str = bufferedReader.readLine()) != null) {
				buffer.append(str);
			}
			bufferedReader.close();
			inputStreamReader.close();
			// 释放资源
			inputStream.close();
			inputStream = null;
			httpUrlConn.disconnect();
			JSONObject jsonObject = JSONObject.fromObject(buffer.toString());
			System.out.println(jsonObject);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 文件和参数可以同时提交
	 * @throws ClientProtocolException
	 * @throws IOException
	 */
	public static void doPostAll(String actionUrl,String parameter,String filePath) throws ClientProtocolException, IOException{
		HttpClient httpclient = new DefaultHttpClient();
		HttpPost httppost = new HttpPost(actionUrl);
		
		//MultipartEntity mpEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
		//mpEntity.addPart("id", new StringBody("10001", "text/plain", Charset.forName("UTF-8")));
		
		MultipartEntity reqEntity = new MultipartEntity();
		reqEntity.addPart("param", new StringBody(parameter));
		reqEntity.addPart("file", new FileBody(new File(filePath)));
		
		httppost.setEntity(reqEntity);
		
		System.out.println("执行: " + httppost.getRequestLine());
		HttpResponse response = httpclient.execute(httppost);
		HttpEntity resEntity = response.getEntity(); 
		String result = EntityUtils.toString(resEntity,"UTF-8");
		System.out.println(result);
	}
}
