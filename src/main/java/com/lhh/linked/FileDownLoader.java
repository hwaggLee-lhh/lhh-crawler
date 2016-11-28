package com.lhh.linked;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpConnectionManager;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.cookie.CookiePolicy;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.params.HttpConnectionManagerParams;
import org.apache.commons.httpclient.params.HttpMethodParams;

public class FileDownLoader {

	/**
	 * 根据 url 和网页类型生成需要保存的网页的文件名 去除掉 url 中非文件名字符
	 */
	public String getFileNameByUrl(String url, String contentType) {
		url = url.substring(7);// remove http://
		if (contentType.indexOf("html") != -1)// text/html
		{
			url = url.replaceAll("[\\?/:*|<>\"]", "_") + ".html";
			return url;
		} else// 如application/pdf
		{
			return url.replaceAll("[\\?/:*|<>\"]", "_") + "."+ contentType.substring(contentType.lastIndexOf("/") + 1);
		}
	}

	/**
	 * 保存网页字节数组到本地文件 filePath 为要保存的文件的相对地址
	 */
	private void saveToLocal(byte[] data, String filePath) {
		try {
			//System.out.println("---------------------------------将内容写入文件..."+ filePath);
			File file = new File(filePath);
			if (!file.exists()) {
				file.createNewFile();
			}
			DataOutputStream out = new DataOutputStream(new FileOutputStream(file));
			for (int i = 0; i < data.length; i++){
				out.write(data[i]);
			}
			out.flush();
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/* 下载 url 指向的网页 */
	public String downloadFile(String url) {
		//System.out.println("---------------------------------下载页面..." + url);
		String filePath = null;
		/* 1.生成 HttpClinet 对象并设置参数 */
		HttpClient httpClient = new HttpClient();
		// 设置 Http 连接超时 5s
		HttpConnectionManager connection = httpClient
				.getHttpConnectionManager();
		HttpConnectionManagerParams httpconnectionmanagerparams = connection
				.getParams();
		httpconnectionmanagerparams.setConnectionTimeout(5000);
		/* 2.生成 GetMethod 对象并设置参数 */
		GetMethod getMethod = null;
		try {
			getMethod = new GetMethod(url);
			HttpMethodParams params = getMethod.getParams();
			// 设置 get 请求超时 5s
			params.setParameter(HttpMethodParams.SO_TIMEOUT, 5000);
			// 防止Cookie rejected: violates RFC 2109: domain must start with a
			// dot错误
			params.setParameter(HttpMethodParams.COOKIE_POLICY,
					CookiePolicy.BROWSER_COMPATIBILITY);
			// 设置请求重试处理
			params.setParameter(HttpMethodParams.RETRY_HANDLER,
					new DefaultHttpMethodRetryHandler());
			getMethod.setRequestHeader(
							"User-Agent",
							"Mozilla/5.0 (Windows NT 6.2; WOW64) AppleWebKit/537.17 (KHTML, like Gecko) Chrome/24.0.1312.56 Safari/537.17");
			params.setParameter("http.protocol.allow-circular-redirects", true);
			/* 3.执行 HTTP GET 请求 */
			int statusCode = httpClient.executeMethod(getMethod);
			// 判断访问的状态码
			if (statusCode != HttpStatus.SC_OK) {
				System.err.println("Method failed: "+ getMethod.getStatusLine() + ";" + url);
				filePath = null;
			}
			/* 4.处理 HTTP 响应内容 */
			// byte[] responseBody = getMethod.getResponseBody();// 读取为字节数组

			BufferedReader reader = new BufferedReader(new InputStreamReader(getMethod.getResponseBodyAsStream()));
			StringBuffer stringBuffer = new StringBuffer();
			String str = "";
			while ((str = reader.readLine()) != null) {
				stringBuffer.append(str);
			}
			byte[] responseBody = stringBuffer.toString().getBytes();
			// 根据网页 url 生成保存时的文件名
			filePath = "temp\\"+ getFileNameByUrl(url,getMethod.getResponseHeader("Content-Type").getValue());
			saveToLocal(responseBody, filePath);
		} catch (HttpException e) {
			// 发生致命的异常，可能是协议不对或者返回的内容有问题
			System.out.println("Please check your provided http address!");
			e.printStackTrace();
		} catch (IOException e) {
			// 发生网络异常
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			// 释放连接
			if( getMethod != null )getMethod.releaseConnection();
			//System.out.println("---------------------------------下载完成..." + url);
		}

		return filePath;
	}

	// 测试的 main 方法
	public static void main(String[] args) {
		FileDownLoader downLoader = new FileDownLoader();
		downLoader.downloadFile("http://www.twt.edu.cn");
	}
}
