package com.sto.transport.event.infrastructure.util.common;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.http.*;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class HttpUtils {
	public static final Charset DEF_CONTENT_CHARSET = Consts.UTF_8;
	private static Logger logger= Logger.getLogger(HttpUtils.class);

	/*后台模拟发送GET请求*/
	public static String doGet(String url, List<NameValuePair> paramList) {
		// 响应结果
		String result = null;
		// 获取httpclient
		CloseableHttpClient httpclient = HttpClients.createDefault();
		CloseableHttpResponse response = null;
		try {
			// 处理中文乱码
			String paramStr = EntityUtils.toString(new UrlEncodedFormEntity(paramList, DEF_CONTENT_CHARSET));
			// 拼接参数
			StringBuffer buffer = new StringBuffer();
			buffer.append(url).append("?").append(paramStr);
			// 创建get请求
			HttpGet httpGet = new HttpGet(buffer.toString());
			// 设置请求和传输超时时间
			RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(2000).setConnectTimeout(2000).build();
			httpGet.setConfig(requestConfig);
			// 提交参数发送请求
			response = httpclient.execute(httpGet);
			// 得到响应信息状态码
			int statusCode = response.getStatusLine().getStatusCode();
			// 判断响应信息是否正确
			if (statusCode != HttpStatus.SC_OK) {
				// 终止请求并手动抛出异常
				httpGet.abort();
				throw new RuntimeException("HttpClient error,status code ===》" + statusCode);
			}
			HttpEntity entity = response.getEntity();
			if (entity != null) {
				result = EntityUtils.toString(entity);
			}
			// 确认内容流消费完成，并关闭内容流
			EntityUtils.consume(entity);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			// 关闭所有资源连接
			if (response != null) {
				try {
					response.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (httpclient != null) {
				try {
					httpclient.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return result.toString();
	}

	/*后台模拟发送POST请求*/
	public static String doPost(String url, List<NameValuePair> paramList) {
		// 响应结果
		String result = null;
		// 获取httpclient
		CloseableHttpClient httpclient = HttpClients.createDefault();
		CloseableHttpResponse response = null;
		try {
			// 创建post请求
			HttpPost httpPost = new HttpPost(url);
			httpPost.setHeader(HTTP.CONTENT_TYPE, "application/x-www-form-urlencoded; charset=UTF-8");
			// 设置请求和传输超时时间
			RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(3000).setConnectTimeout(3000).build();
			httpPost.setConfig(requestConfig);
			// 提交参数发送请求，处理中文乱码
			UrlEncodedFormEntity urlEncodedFormEntity = new UrlEncodedFormEntity(paramList, DEF_CONTENT_CHARSET);
			httpPost.setEntity(urlEncodedFormEntity);
			response = httpclient.execute(httpPost);
			// 得到响应信息状态码
			int statusCode = response.getStatusLine().getStatusCode();
			// 判断响应信息是否正确
			if (statusCode != HttpStatus.SC_OK) {
				// 终止请求并手动抛出异常
				httpPost.abort();
				throw new RuntimeException("HttpClient error,status code ===》" + statusCode);
			}
			HttpEntity entity = response.getEntity();
			if (entity != null) {
				result = EntityUtils.toString(entity);
			}
			// 确认内容流消费完成，并关闭内容流
			EntityUtils.consume(entity);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			// 关闭所有资源连接
			if (response != null) {
				try {
					response.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (httpclient != null) {
				try {
					httpclient.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		return result;
	}


	public static String httpBodyPost(String url, String postData) {
		String result;
		HttpPost post = null;

		try {
			HttpClient client = new DefaultHttpClient();
			// 设置3秒超时时间
			client.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 3000);
			client.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, 3000);

			post = new HttpPost(url);
			post.setHeader(HTTP.CONTENT_TYPE, "application/json; charset=UTF-8");
			post.setHeader("Accept", "application/json; charset=UTF-8");

			StringEntity entity = new StringEntity(postData, DEF_CONTENT_CHARSET);
			post.setEntity(entity);

			HttpResponse response = client.execute(post);
			// 得到响应信息状态码
			int rspCode = response.getStatusLine().getStatusCode();
			// 判断响应信息是否正确
			if (rspCode != HttpStatus.SC_OK) {
				// 终止请求并手动抛出异常
				post.abort();
				throw new RuntimeException("HttpClient error,status code ===》" + rspCode);
			}
			result = EntityUtils.toString(response.getEntity(), "UTF-8");
			return result;
		} catch (Exception e) {
			logger.error("HttpUtils.httpBodyPost异常===>", e);
		} finally {
			if(post != null) {
				post.releaseConnection();
			}
		}
		return null;
	}

	public static void main(String[] args) {
		/* 测试后台模拟发送GET请求 */
		/*List<NameValuePair> paramList = new ArrayList<>();
		paramList.add(new BasicNameValuePair("name", "天明"));
		paramList.add(new BasicNameValuePair("career", "teacher"));
		String result = doGet("http://192.168.2.111:9992/orderQuery/execute", paramList);
		System.out.println(result);*/

		String orgUrl="http://10.33.75.155:7001/api/organize/getObjectByCodeOrId";
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String timestamp = sdf.format(new Date());
		String appKey="testapiuser";
		String appSecret="4ff5d26f8669476daf9774c4ebea153b";
		String sign = SignUtils.signRequest(timestamp, appSecret);



		/* 测试后台模拟发送POST请求 */
		List<NameValuePair> paramPostList = new ArrayList<NameValuePair>();
		paramPostList.add(new BasicNameValuePair("code", "223302"));
		paramPostList.add(new BasicNameValuePair("timestamp", timestamp));
		paramPostList.add(new BasicNameValuePair("sign", sign));
		paramPostList.add(new BasicNameValuePair("appKey", appKey));
		String res = doPost(orgUrl, paramPostList);
		JsonObject returnData = new JsonParser().parse(res).getAsJsonObject();
		JsonObject data = returnData.get("data").getAsJsonObject();
		String id = data.get("id").toString();
		String success = returnData.get("success").toString();
		


		System.out.println(res);

	}
}
