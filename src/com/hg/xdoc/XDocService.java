package com.hg.xdoc;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * XDoc服务
 * @author xdoc
 * @version 11.5.5
 */
public class XDocService {
	/**
	 * 默认服务器地址
	 */
	public static String DEFAULT_URL = "http://www.xdocin.com";
	/**
	 * 默认账号口令
	 */
	public static String DEFAULT_KEY = "";
	private String url;
	private String key;
	/**
	 * 服务地址
	 * @return
	 */
	public String getUrl() {
		return url;
	}
	/**
	 * 服务地址
	 * @param url
	 */
	public void setUrl(String url) {
		this.url = url;
	}
	/**
	 * 账号口令
	 * @return
	 */
	public String getKey() {
		return key;
	}
	/**
	 * 账号口令
	 * @param key
	 */
	public void setKey(String key) {
		this.key = key;
	}
	/**
	 * 构造器
	 */
	public XDocService() {
		this(DEFAULT_URL, DEFAULT_KEY); 
	}
	/**
	 * 构造器
	 * @param url 服务地址
	 */
	public XDocService(String url) {
		this(url, DEFAULT_KEY);
	}
	/**
	 * 构造器
	 * @param url 服务地址
	 * @param key 账号
	 */
	public XDocService(String url, String key) {
		this.url = url;
		this.key = key;
	}
	/**
	 * 转换为其它格式文件
	 * @param xdoc xdoc
	 * @param file 其它格式文件，如：a.pdf
	 * @throws IOException
	 */
	public void to(File xdoc, File file) throws IOException {
		to(xdoc.getAbsolutePath(), file);
	}
	/**
	 * 转换为其它格式文件
	 * @param xdoc xdoc文本<br>
	 *        URL：文档URL地址，格式支持：xdoc、json、docx、epub、txt、rtf等，支持datauri协议，可传递二进制数据，支持本地文件<br>
	 *        纯文本：以"text:"开头的文本<br>
	 *        JSON：符合XDOC-JSON规范的JSON文本<br>
	 *        XML：符合XDOC-XML规范的XML文本<br>
	 *        HTML：用html标签括起来的html文本，如：&lt;html&gt;&lt;h1&gt;Hello&lt;/h1&gt;&lt;/html&gt;
	 * @param file 其它格式文件，如：a.pdf
	 * @throws IOException
	 */
	public void to(String xdoc, File file) throws IOException {
		to(xdoc, new FileOutputStream(file), getFormat(file.getName()));
	}
	/**
	 * 转换为其它格式，保存到指定流中
	 * @param xdoc xdoc
	 * @param out 输出目标，OutputStream或HttpServletResponse
	 * @param format format
	 * @throws IOException
	 */
	public void to(String xdoc, Object out, String format) throws IOException {
		Map<String, Object> param = new HashMap<String, Object>();
		param.put("_func", "to");
		param.put("_xdoc", xdoc);
		param.put("_format", format);
		invoke(checkParam(param), out);
	}
	/**
	 * 转换为其它格式并发送
	 * @param xdoc xdoc
	 * @param to 目标，支持ftp、http、mail、datauri等
	 * @param format format
	 * @throws IOException
	 */
	public String to(String xdoc, String to, String format) throws IOException {
		Map<String, Object> param = new HashMap<String, Object>();
		param.put("_func", "to");
		param.put("_xdoc", xdoc);
		param.put("_to", to);
		param.put("_format", format);
		ByteArrayOutputStream out = new ByteArrayOutputStream(); 
		invoke(checkParam(param), out);
		return new String(out.toByteArray(), "UTF-8");
	}
	/**
	 * 运行xdoc
	 * @param xdoc xdoc
	 * @param param 参数
	 * @param file 目标文件
	 * @throws IOException
	 */
	public void run(File xdoc, Map<String, Object> param, File file) throws IOException {
		if (!param.containsKey("_xformat")) {
			param.put("_xformat", getFormat(file.getName()));
		}
		run(xdoc.getAbsolutePath(), param, file);
	}
	/**
	 * 运行xdoc
	 * @param xdoc xdoc
	 * @param param 参数
	 * @param file 目标文件
	 * @throws IOException
	 */
	public void run(String xdoc, Map<String, Object> param, File file) throws IOException {
		run(xdoc, param, new FileOutputStream(file), getFormat(file.getName()));
	}
	/**
	 * 运行xdoc
	 * @param xdoc xdoc
	 * @param param 参数
	 * @param out 输出目标，OutputStream或HttpServletResponse
	 * @param format 目标格式
	 * @throws IOException
	 */
	public void run(String xdoc, Map<String, Object> param, Object out, String format) throws IOException {
		param.put("_func", "run");
		param.put("_xdoc", xdoc);
		param.put("_format", format);
		invoke(checkParam(param), out);
	}
	/**
	 * 运行xdoc并发送
	 * @param xdoc xdoc
	 * @param param 参数
	 * @param to 目标，支持ftp、http、mail、datauri等
	 * @param format 目标格式
	 * @throws IOException
	 */
	public String run(String xdoc, Map<String, Object> param, String to, String format) throws IOException {
		param.put("_func", "run");
		param.put("_xdoc", xdoc);
		param.put("_to", to);
		param.put("_format", format);
		ByteArrayOutputStream out = new ByteArrayOutputStream(); 
		invoke(checkParam(param), out);
		return new String(out.toByteArray(), "UTF-8");
	}
	/**
	 * 运行注解XDoc
	 * @param obj
	 * @param file
	 * @throws IOException
	 */
	public void run(Object obj, File file) throws IOException {
		run(obj, new FileOutputStream(file), getFormat(file.getName()));
	}
	/**
	 * 运行注解XDoc
	 * @param obj
	 * @param out 目标流
	 * @param format 目标格式
	 * @throws IOException
	 */
	public void run(Object obj, Object out, String format) throws IOException {
		run(obj, out, null, format);
	}
	/**
	 * 运行注解XDoc
	 * @param obj
	 * @param to 目标，支持ftp、http、mail、datauri等
	 * @param format 目标格式
	 * @throws IOException
	 */
	public void run(Object obj, String to, String format) throws IOException {
		run(obj, null, to, format);
	}
	private void run(Object obj, Object out, String to, String format) throws IOException {
		String xurl = "";
		XDoc xdoc = obj.getClass().getAnnotation(XDoc.class);
		if (xdoc != null) {
			xurl = xdoc.value();
		}
		if (xurl.length() == 0) {
			 xurl = "./" + obj.getClass().getSimpleName() + ".xdoc";
		}
		Field[] fields = obj.getClass().getDeclaredFields();
		boolean hasXParam = false;
		XParam xParam;
		Map<String, Object> param = new HashMap<String, Object>();
		String name;
		Object value;
		for (Field field : fields) {
			xParam = field.getAnnotation(XParam.class);
			if (xParam != null) {
				hasXParam = true;
				name = xParam.value();
				if (name.length() == 0) {
					name = field.getName(); 
				}
				try {
					field.setAccessible(true);
					value = field.get(obj);
					if (name.equals("_xdoc")) {
						xurl = String.valueOf(value);
					} else {
						param.put(name, value);
					}
				} catch (Exception e) {
					throw new IOException(e);
				}
			}
		}
		if (!hasXParam) { //没有指定xparam，传入所有属性
			for (Field field : fields) {
				try {
					field.setAccessible(true);
					param.put(field.getName(), field.get(obj));
				} catch (Exception e) {
					throw new IOException(e);
				}
			}
		}
		if (out != null) {
			this.run(xurl, param, out, format);
		} else {
			this.run(xurl, param, to, format);
		}
	}
	/**
	 * 招呼
	 * @return
	 * @throws IOException
	 */
	public boolean hi() throws IOException {
		return invokeStringFunc("hi").equals("ok");
	}
	/**
	 * 关于
	 */
	public String about() throws IOException {
		return invokeStringFunc("about");
	}
	/**
	 * 动态口令
	 * @return
	 * @throws IOException
	 */
	public String dkey() throws IOException {
		return invokeStringFunc("dkey");
	}
	/**
	 * 修改口令
	 * @return
	 * @throws IOException
	 */
	public String ckey() throws IOException {
		return invokeStringFunc("ckey");
	}
	/**
	 * 注册
	 * @param mail 邮件
	 * @return
	 * @throws IOException
	 */
	public String reg(String mail) throws IOException {
		Map<String, String> params = new HashMap<String, String>();
		params.put("_func", "reg");
		params.put("_mail", mail);
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		invoke(params, out);
		return (String) parse(out.toByteArray());
	}
	/**
	 * 账户信息
	 * @return
	 * @throws IOException
	 */
	@SuppressWarnings("unchecked")
	public Map<String, String> acc() throws IOException {
		Map<String, String> params = new HashMap<String, String>();
		params.put("_func", "acc");
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		invoke(params, out);
		return (Map<String, String>) parse(out.toByteArray());
	}
	/**
	 * 基于ID上传
	 * @param id
	 * @param file
	 * @return
	 * @throws IOException
	 */
	public void sup(String id, File file) throws IOException {
		sup(id, toDataURI(file.getAbsolutePath()));
	}
	/**
	 * 基于ID上传
	 * @param id
	 * @param in
	 * @throws IOException
	 */
	public void sup(String id, InputStream in) throws IOException {
		sup(id, toDataURI(in));
	}
	private void sup(String id, String dataUri) throws IOException {
		Map<String, String> params = new HashMap<String, String>();
		params.put("_func", "sup");
		params.put("_id", id);
		params.put("_data", dataUri);
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		invoke(params, out);
		parse(out.toByteArray());
	}
	/**
	 * 基于ID下载
	 * @param id
	 * @param file
	 * @throws IOException
	 */
	public void sdown(String id, File file) throws IOException {
		sdown(id, new FileOutputStream(file));
	}
	/**
	 * 基于ID下载
	 * @param id
	 * @param out 输出目标，OutputStream或HttpServletResponse
	 * @throws IOException
	 */
	public void sdown(String id, Object out) throws IOException {
		Map<String, String> params = new HashMap<String, String>();
		params.put("_func", "sdown");
		params.put("_id", id);
		invoke(params, out);
	}
	/**
	 * 基于ID删除
	 * @param id
	 * @return
	 * @throws IOException
	 */
	public boolean sremove(String id) throws IOException {
		Map<String, String> params = new HashMap<String, String>();
		params.put("_func", "sremove");
		params.put("_id", id);
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		invoke(params, out);
		return parse(out.toByteArray()).equals("ok");
	}
	/**
	 * 创建目录
	 * @param dir
	 * @return
	 * @throws IOException
	 */
	public boolean mkdir(String dir) throws IOException {
		Map<String, String> params = new HashMap<String, String>();
		params.put("_func", "mkdir");
		params.put("_dir", dir);
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		invoke(params, out);
		return parse(out.toByteArray()).equals("ok");
	}
	/**
	 * 目录列表
	 * @param dir
	 * @return
	 * @throws IOException
	 */
	@SuppressWarnings("unchecked")
	public List<Map<String, String>> dirlist(String dir) throws IOException {
		Map<String, String> params = new HashMap<String, String>();
		params.put("_func", "dirlist");
		params.put("_dir", dir);
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		invoke(params, out);
		return (List<Map<String, String>>) parse(out.toByteArray());
	}
	/**
	 * 文件列表
	 * @param dir
	 * @return
	 * @throws IOException
	 */
	@SuppressWarnings("unchecked")
	public List<Map<String, String>> filelist(String dir) throws IOException {
		Map<String, String> params = new HashMap<String, String>();
		params.put("_func", "filelist");
		params.put("_dir", dir);
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		invoke(params, out);
		return (List<Map<String, String>>) parse(out.toByteArray());
	}
	/**
	 * 上传
	 * @param dir
	 * @param file
	 * @return
	 * @throws IOException
	 */
	public void up(String dir, File file) throws IOException {
		up(dir, toDataURI(file.getAbsolutePath()));
	}
	/**
	 * 上传
	 * @param dir
	 * @param in
	 * @throws IOException
	 */
	public void up(String dir, InputStream in) throws IOException {
		up(dir, toDataURI(in));
	}
	private void up(String dir, String dataUri) throws IOException {
		Map<String, String> params = new HashMap<String, String>();
		params.put("_func", "up");
		params.put("_dir", dir);
		params.put("_data", dataUri);
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		invoke(params, out);
		parse(out.toByteArray());
	}
	/**
	 * 下载
	 * @param dir
	 * @param file
	 * @throws IOException
	 */
	public void down(String dir, File file) throws IOException {
		down(dir, new FileOutputStream(file));
	}
	/**
	 * 下载
	 * @param dir
	 * @param out 输出目标，OutputStream或HttpServletResponse
	 * @throws IOException
	 */
	public void down(String dir, Object out) throws IOException {
		Map<String, String> params = new HashMap<String, String>();
		params.put("_func", "down");
		params.put("_dir", dir);
		invoke(params, out);
	}
	/**
	 * 删除
	 * @param dir
	 * @return
	 * @throws IOException
	 */
	public boolean remove(String dir) throws IOException {
		Map<String, String> params = new HashMap<String, String>();
		params.put("_func", "remove");
		params.put("_dir", dir);
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		invoke(params, out);
		return parse(out.toByteArray()).equals("ok");
	}
	/**
	 * 文件是否存在
	 * @param dir
	 * @return
	 * @throws IOException
	 */
	public boolean exists(String dir) throws IOException {
		Map<String, String> params = new HashMap<String, String>();
		params.put("_func", "exists");
		params.put("_dir", dir);
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		invoke(params, out);
		return parse(out.toByteArray()).equals("true");
	}
	/**
	 * 数据查询
	 * @param sql SQL
	 * @return
	 * @throws IOException
	 */
	@SuppressWarnings("unchecked")
	public List<Map<String, String>> query(String sql) throws IOException {
		Map<String, String> params = new HashMap<String, String>();
		params.put("_func", "query");
		params.put("_sql", sql);
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		invoke(params, out);
		return (List<Map<String, String>>) parse(out.toByteArray());
	}
	/**
	 * 基于ID的XDATA转换
	 * @param id
	 * @param format 目标格式：xml、json、csv
	 * @return
	 * @throws IOException 
	 */
	public String xdataById(String id, String format) throws IOException {
		Map<String, String> params = new HashMap<String, String>();
		params.put("_func", "xdata");
		params.put("_id", id);
		params.put("_format", format);
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		invoke(params, out);
		return (String) parse(out.toByteArray());
	}
	/**
	 * XDATA转换
	 * @param data xdata数据，格式：xml、json、csv
	 * @param format 目标格式：xml、json、csv
	 * @return
	 * @throws IOException 
	 */
	public String xdata(String xdata, String format) throws IOException {
		Map<String, String> params = new HashMap<String, String>();
		params.put("_func", "xdata");
		params.put("_xdata", xdata);
		params.put("_format", format);
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		invoke(params, out);
		return (String) parse(out.toByteArray());
	}
	/**
	 * 通过url地址调用服务，支持本地文件xdoc和xdata
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length > 0 && args[0].length() > 0) {
			String url = args[0];
			if (url.charAt(0) == '@') { //命令文件
				File cmdFile = new File(url.substring(1));
				try {
					FileReader reader = new FileReader(cmdFile);
					url = (new BufferedReader(reader)).readLine();
					reader.close();
					cmdFile.delete();
				} catch (Exception e) {
					e.printStackTrace();
					return;
				}
			}
			String server = DEFAULT_URL;
			int pos = url.indexOf('?');
			if (pos > 0) {
				server = url.substring(0, pos);
				if (server.endsWith("/xdoc")) {
					server = server.substring(0, server.length() - 5);
				}
				url = url.substring(pos + 1);
			}
			String xkey = "";
			try {
				String[] params = url.split("&");
				Map<String, String> map = new HashMap<String, String>();
				String key, value;
				String to = null;
				for (int i = 0; i < params.length; i++) {
					pos = params[i].indexOf('=');
					if (pos > 0) {
						key = decode(params[i].substring(0, pos));
						value = decode(params[i].substring(pos + 1));
						if (isXDocData(key, value)) {
							value = toDataURI(value);
						} else if (key.indexOf("@file") > 0) {
							key = key.substring(0, key.length() - 5);
							value = toDataURI(value);
						} else if (key.equals("_key")) {
							xkey = value;
							continue;
						} else if (key.equals("_to") && isFile(value)) {
							to = value;
							continue;
						}
						map.put(key, value);
					}
				}
				if (!map.containsKey("_format") && to != null && to.indexOf('.') > 0) {
					map.put("_format", to.substring(to.lastIndexOf('.') + 1));
				}
				XDocService client  = new XDocService(server, xkey);
				OutputStream out;
				if (to != null) {
					out = new FileOutputStream(to);
				} else {
					out = System.out;
				}
				client.invoke(map, out);
				if (to != null) {
					out.flush();
					out.close();
					System.out.println(">> " + to);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	private void invoke(Map<String, String> param, Object out) throws IOException {
		String xurl = this.url + (this.url.endsWith("/") ? "xdoc" : "/xdoc");
		HttpURLConnection httpConn = (HttpURLConnection) new URL(xurl).openConnection();
		httpConn.setDoOutput(true);
		OutputStream reqOut = httpConn.getOutputStream();
	    reqOut.write(("&_key=").getBytes());
	    reqOut.write(encode(this.key).getBytes());
	    Iterator<String> it = param.keySet().iterator();
	    String key;
	    while (it.hasNext()) {
	        key = it.next();
	        reqOut.write(("&" + encode(key) + "=").getBytes());
	        reqOut.write(encode(param.get(key)).getBytes());
	    }
	    reqOut.flush();
	    reqOut.close();
	    OutputStream os = null;
	    if (out instanceof OutputStream) {
	    	os = (OutputStream) out;
	    } else {
			try {
				Method method = out.getClass().getMethod("getOutputStream");
				os = (OutputStream) method.invoke(out);
				method = out.getClass().getMethod("setHeader", String.class, String.class);
				String[] headerNames = new String[] {"Content-Type", "Content-Disposition"};
				String headerValue;
				for (String headerName : headerNames) {
					headerValue = httpConn.getHeaderField(headerName);
					if (headerValue != null) {
						method.invoke(out, headerName, headerValue);
					}
				}
			} catch (Exception e) {
				throw new IOException(e);
			}
	    }
	    pipe(httpConn.getInputStream(), os);
	}
	private Object parse(byte[] data) throws IOException {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();   
		factory.setValidating(false);   
		try {
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document document = builder.parse(new ByteArrayInputStream(data));
			document.getDocumentElement().normalize();   
			Element root = document.getDocumentElement();
			if (root.getAttribute("success").equals("true")) {
				Element result = (Element) root.getElementsByTagName("result").item(0);
				String dataType = result.getAttribute("dataType");
				if (dataType.equals("string")) {
					return result.getTextContent();
				} else if (dataType.equals("map")) {
					NodeList items = result.getElementsByTagName("value");
					Map<String, String> map = new HashMap<String, String>();
					Element value;
					NamedNodeMap atts;
					for (int i = 0; i < items.getLength(); i++) {
						value = (Element) items.item(i);
						atts = value.getAttributes();
						for (int j = 0; j < atts.getLength(); j++) {
							map.put(atts.item(j).getNodeName(), atts.item(j).getNodeValue());
						}
					}
					return map;
				} else if (dataType.equals("rowset")) {
					Map<String, String> fieldMap = new HashMap<String, String>();
					String[] fields = result.getAttribute("fields").split(",");
					String[] formerFields = fields;
					if (result.hasAttribute("formerFields")) {
						formerFields = csvSplit(result.getAttribute("formerFields"));
					}
					for (int j = 0; j < formerFields.length; j++) {
						fieldMap.put(fields[j], formerFields[j]);
					}
					NodeList eleList = result.getElementsByTagName("row");
					Element ele;
					Map<String, String> map;
					List<Map<String, String>> List = new ArrayList<Map<String, String>>();
					for (int i = 0; i < eleList.getLength(); i++) {
						ele = (Element) eleList.item(i);
						map = new HashMap<String, String>();
						List.add(map);
						for (int j = 0; j < fields.length; j++) {
							map.put(formerFields[j], ele.getAttribute(fields[j]));
						}
					}
					return List;
				} else {
					return "";
				}
			} else {
				throw new IOException(root.getElementsByTagName("error").item(0).getTextContent());
			}
		} catch (ParserConfigurationException e) {
			throw new IOException(e);
		} catch (SAXException e) {
			throw new IOException(e);
		}   
	}
	private String invokeStringFunc(String func) throws IOException {
		Map<String, String> params = new HashMap<String, String>();
		params.put("_func", func);
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		invoke(params, out);
		return (String) parse(out.toByteArray());
	}
	private Map<String, String> checkParam(Map<String, Object> param) throws IOException {
		Map<String, String> map = new HashMap<String, String>();
		String key, value;
		Iterator<String> it = param.keySet().iterator();
		while (it.hasNext()) {
			key = it.next();
			value = toParamString(param.get(key));
			if (key.equals("_xdoc") && value.startsWith("@")) {
				value = value.substring(1);
			} else if (isXDocData(key, value)) {
				value = toDataURI(value);
			} else if (key.endsWith("@file")) {
				key = key.substring(0, key.length() - 5);
				value = toDataURI(value);
			}
			map.put(key, value);
		}
		return map;
	}
	private static String toParamString(Object obj) throws IOException {
		String str;
		if (obj == null) {
			str = "";
		} else if (obj.getClass().isPrimitive()
				|| obj instanceof Boolean
				|| obj instanceof Number
				|| obj instanceof CharSequence) {
			str = obj.toString();
		} else if (obj instanceof Date) {
			str = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format((Date) obj);
		} else if (obj instanceof File) {
			str = toDataURI(((File) obj).getAbsolutePath());
		} else if (obj instanceof InputStream) {
			str = toDataURI((InputStream) obj);
		} else {
			StringBuilder sb = new StringBuilder();
			Set<Object> chainSet = new HashSet<Object>();
			writeParamString(sb, obj, chainSet);
			str = sb.toString();
		}
		return str;
	}
	private static void writeParamString(StringBuilder sb, Object obj, Set<Object> set) throws IOException {
		if (obj == null) {
			sb.append("null");
		} else if (obj.getClass().isPrimitive()
				|| obj instanceof Boolean
				|| obj instanceof Number) {
			sb.append(toParamString(obj));
		} else if (obj instanceof CharSequence
				|| obj instanceof Date) {
			jencode(toParamString(obj), sb);
		} else if (obj instanceof Collection) {
			sb.append("[");
			boolean b = false;
			Iterator<?> it = ((Collection<?>) obj).iterator();
			while (it.hasNext()) {
				if (b) sb.append(",");
				writeParamString(sb, it.next(), set);
				b = true;
			}
			sb.append("]");
		} else if (obj.getClass().isArray()) {
			sb.append("[");
			boolean b = false;
			int n = Array.getLength(obj);
			for (int i = 0; i < n; i++) {
				if (b) sb.append(",");
				writeParamString(sb, Array.get(obj, i), set);
				b = true;
			}
			sb.append("]");
		} else if (obj instanceof Map) {
			sb.append("{");
			Map<?, ?> map = (Map<?, ?>) obj;
			boolean b = false;
			Object key;
			Iterator<?> it = map.keySet().iterator();
			while (it.hasNext()) {
				if (b) sb.append(",");
				key = it.next();
				jencode(key.toString(), sb);
				sb.append(":");
				writeParamString(sb, map.get(key), set);
				b = true;
			}
			sb.append("}");
		} else {
			sb.append("{");
			if (!set.contains(obj) && obj.getClass() != Object.class && obj.getClass() != Class.class) {
				set.add(obj);
				try {
					boolean b = false;
					BeanInfo bi = Introspector.getBeanInfo(obj.getClass(), Object.class);
					PropertyDescriptor[] pds = bi.getPropertyDescriptors();
					Object res;
					Method method;
					for (PropertyDescriptor pd : pds) {
						method = pd.getReadMethod();
						if (method != null) {
							if (b) sb.append(",");
							jencode(pd.getName(), sb);
							sb.append(":");
							res = method.invoke(obj, new Object[0]);
							writeParamString(sb, res, set);
							b = true;
						}
					}
				} catch (Exception e) {
					throw new IOException(e);
				}
				set.remove(obj);
			}
			sb.append("}");
		}
	}
	private static void jencode(String str, StringBuilder sb) {
        sb.append("\"");
        char c;
        for (int i = 0; i < str.length(); i++) {
            c = str.charAt(i);
            if (c == '\\') {
                sb.append("\\\\");
            } else if (c == '/') {
                sb.append("\\/");
            } else if (c == '\n') {
                sb.append("\\n");
            } else if (c == '\r') {
                sb.append("\\r");
            } else if (c == '\t') {
                sb.append("\\t");
            } else if (c == '\'') {
                sb.append("\\\'");
            } else if (c == '\"') {
                sb.append("\\\"");
            } else {
                sb.append(c);
            }
        }
        sb.append("\"");
    }
	private static boolean isXDocData(String name, String value) {
		if (name.equals("_xdoc") || name.equals("_xdata")) {
	    	if (value.startsWith("./")
	    			|| value.startsWith("<")
	    			|| value.startsWith("{")
	    			|| value.startsWith("[")
	    			|| value.startsWith("data:")
	    			|| name.equals("_xdoc") && value.startsWith("text:")) {
	    		return false;
	    	} else {
	    		return true;
	    	}
		}
		return false;
	}
	private static String getFormat(String url) {
	    String format = "xdoc";
	    int pos = url.lastIndexOf(".");
	    if (pos > 0) {
	        format = url.substring(pos + 1).toLowerCase();
	        if (format.equals("zip")) {
	            url = url.substring(0, pos);
	            pos = url.lastIndexOf(".");
	            if (pos > 0) {
	                format = url.substring(pos + 1).toLowerCase() + ".zip";
	            }
	        }
	    }
	    return format;
	}
	private static String encode(Object str) {
        try {
            return URLEncoder.encode(String.valueOf(str), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            return String.valueOf(str);
        }
    }
	private static String decode(String str) {
        try {
            return URLDecoder.decode(str, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            return str;
        }
    }
    private static void pipe(InputStream in, OutputStream out) throws IOException {
        int len;
        byte[] buf = new byte[4096];
        while (true) {
            len = in.read(buf);
            if (len > 0) {
                out.write(buf, 0, len);
            } else {
                break;
            }
        }
        out.flush();
        out.close();
        in.close();
    }
    private static boolean isFile(String url) {
    	int pos = url.indexOf(':');
    	return pos < 0
    			|| pos == 1
    			|| (pos == 2 && url.charAt(0) == '/');
    }
    private static String toDataURI(InputStream in) throws IOException {
    	ByteArrayOutputStream out = new ByteArrayOutputStream();
    	pipe(in, out);
    	StringBuffer sb = new StringBuffer();
    	sb.append("data:application/octet-stream;base64,");
    	sb.append(toBase64(out.toByteArray()));
    	return sb.toString();
    }
    private static String toDataURI(String url) throws IOException {
    	if (url.length() > 0) {
    		StringBuffer sb = new StringBuffer();
    		String format = null;;
    		InputStream in = null;
    		if (isFile(url) || url.startsWith("class://")) {
    			int pos = url.lastIndexOf('.');
    			if (pos > 0) {
    				format = url.substring(pos + 1);
    				if (format.equals("jpg")) {
    					format = "jpeg";
    				} else if (format.equals("htm")) {
    					format = "html";
    				}
    				if (format.equals("png") || format.equals("jpeg") || format.equals("gif")) {
    					format = "image/" + format;
    				} else if (format.equals("html") || format.equals("xml")) {
    					format = "text/" + format;
    				} else {
    					format = "application/" + format;
    				}
    			}
    			if (url.startsWith("class://")) {
                    String cls = url.substring(8, url.indexOf("/", 8));
                    String path = url.substring(url.indexOf("/", 8) + 1);
                    try {
						in = Class.forName(cls).getResourceAsStream(path);
					} catch (Exception e) {
						throw new IOException(e);
					}
    			} else {
    				in = new FileInputStream(url);
    			}
    		} else {
    			URLConnection conn = new URL(url).openConnection();
    			in = conn.getInputStream();
    			format = ((HttpURLConnection) conn).getContentType();
    		}
    		if (format == null) {
    			format = "application/octet-stream";
    		}
    		ByteArrayOutputStream out = new ByteArrayOutputStream();
    		pipe(in, out);
    		sb.append("data:").append(format).append(";base64,");
    		sb.append(toBase64(out.toByteArray()));
    		return sb.toString();
    	} else {
    		return "";
    	}
    }
    private static String toBase64(final byte[] data) {
        final char[] out = new char[((data.length + 2) / 3) * 4];
        for (int i = 0, index = 0; i < data.length; i += 3, index += 4) {
            boolean quad = false;
            boolean trip = false;

            int val = (0xFF & data[i]);
            val <<= 8;
            if ((i + 1) < data.length) {
                val |= (0xFF & data[i + 1]);
                trip = true;
            }
            val <<= 8;
            if ((i + 2) < data.length) {
                val |= (0xFF & data[i + 2]);
                quad = true;
            }
            out[index + 3] = alphabet[(quad ? (val & 0x3F) : 64)];
            val >>= 6;
            out[index + 2] = alphabet[(trip ? (val & 0x3F) : 64)];
            val >>= 6;
            out[index + 1] = alphabet[val & 0x3F];
            val >>= 6;
            out[index + 0] = alphabet[val & 0x3F];
        }
        return new String(out);
    }
    private static char[] alphabet =
        "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/=".toCharArray();
    private static byte[] codes = new byte[256];
    static {
        for (int i = 0; i < 256; i++) {
            codes[i] = -1;
        }
        for (int i = 'A'; i <= 'Z'; i++) {
            codes[i] = (byte) (i - 'A');
        }
        for (int i = 'a'; i <= 'z'; i++) {
            codes[i] = (byte) (26 + i - 'a');
        }
        for (int i = '0'; i <= '9'; i++) {
            codes[i] = (byte) (52 + i - '0');
        }
        codes['+'] = 62;
        codes['/'] = 63;
    }
	private static String[] csvSplit(String str) {
		List<List<String>> list = csvList(str);
		if (list.size() > 0) {
			List<String> cols = (List<String>) list.get(0);
			String[] strs = new String[cols.size()];
			for (int i = 0; i < strs.length; i++) {
				strs[i] = cols.get(i);
			}
			return strs;
		} else {
			return new String[0];
		}
	}
    private static List<List<String>> csvList(String txt) {
    	if (txt.length() > 0) {
    		ArrayList<List<String>> rows = new ArrayList<List<String>>();
    		ArrayList<String> cols = new ArrayList<String>();
    		rows.add(cols);
    		char c;
    		boolean strBegin = false;
    		StringBuffer sb = new StringBuffer();
    		for (int i = 0; i < txt.length(); i++) {
    			c = txt.charAt(i);
    			if (strBegin) {
    				if (c == '"') {
    					if (i + 1 < txt.length()) {
    						if (txt.charAt(i + 1) == '"') {
    							sb.append(c);
    							i++;
    						} else {
    							strBegin = false;
    						}
    					} else {
    						strBegin = false;
    					}
    				} else {
    					sb.append(c);
    				}
    			} else {
    				if (c == ',') {
    					cols.add(sb.toString());
    					sb.setLength(0);
    				} else if (c == '\n') {
    					cols.add(sb.toString());
    					sb.setLength(0);
    					cols = new ArrayList<String>();
    					rows.add(cols);
    				} else if (c == '"') {
    					strBegin = true;
    				} else if (c != '\r') {
    					sb.append(c);
    				}
    			}
    		}
    		if (sb.length() > 0) {
    			cols.add(sb.toString());
    		}
    		return rows;
    	} else {
    		return new ArrayList<List<String>>();
    	}
    }
    /**
     * XDOC注解
     * @author XDOC
     */
    @Target(ElementType.TYPE)
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    public @interface XDoc {
    	/**
    	 * xdoc
    	 * @return
    	 */
    	public String value() default "";
    }
    /**
     * XDOC参数注解
     * @author XDOC
     */
    @Target(ElementType.FIELD)
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    public @interface XParam {
    	/**
    	 * 参数名称
    	 * @return
    	 */
    	public String value() default "";
    }
}
