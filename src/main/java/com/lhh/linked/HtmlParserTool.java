package com.lhh.linked;

import java.util.HashSet;
import java.util.Set;

import org.htmlparser.Node;
import org.htmlparser.NodeFilter;
import org.htmlparser.Parser;
import org.htmlparser.filters.NodeClassFilter;
import org.htmlparser.filters.OrFilter;
import org.htmlparser.tags.ImageTag;
import org.htmlparser.tags.LinkTag;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.ParserException;
import org.htmlparser.util.SimpleNodeIterator;

public class HtmlParserTool {
	
	// 循环访问所有节点，输出包含关键字的值节点
	public static void extractKeyWordText(String url, String keyword) {
		try {
            //生成一个解析器对象，用网页的 url 作为参数
			Parser parser = new Parser(url);
			//设置网页的编码,这里只是请求了一个 gb2312 编码网页
			parser.setEncoding("utf-8");
			//迭代所有节点, null 表示不使用 NodeFilter
			NodeList list = parser.parse(null);
            //从初始的节点列表跌倒所有的节点
			processNodeList(list, keyword);
		} catch (ParserException e) {
			e.printStackTrace();
		}
	}

	private static void processNodeList(NodeList list, String keyword) {
		//迭代开始
		SimpleNodeIterator iterator = list.elements();
		while (iterator.hasMoreNodes()) {
			Node node = iterator.nextNode();
			//得到该节点的子节点列表
			NodeList childList = node.getChildren();
			//孩子节点为空，说明是值节点
			if (null == childList)
			{
				//得到值节点的值
				String result = node.toPlainTextString();
				//若包含关键字，则简单打印出来文本
				if (result.indexOf(keyword) != -1){
					System.out.println("----start---->"+result+"<-----end------");
				}
			} //end if
			//孩子节点不为空，继续迭代该孩子节点
			else 
			{
				processNodeList(childList, keyword);
			}//end else
		}//end wile
	}
	
	
	
	// 获取一个网站上的链接,filter 用来过滤链接
	public static Set<String> extracLinks(String url, LinkFilter filter) {
		System.out.println("--------------------------------->解析单个页面中的全部url:"+url);
		Set<String> links = new HashSet<String>();
		try {
			Parser parser = new Parser(url);
			parser.setEncoding("utf-8");
			// 过滤 <frame >标签的 filter，用来提取 frame 标签里的 src 属性所表示的链接
			NodeFilter frameFilter = new NodeFilter() {
				private static final long serialVersionUID = 1L;
				public boolean accept(Node node) {
					if (node.getText().startsWith("frame src=")) {
						return true;
					} else {
						return false;
					}
				}
			};
			// OrFilter 来设置过滤 <a> 标签，和 <frame> 标签
			OrFilter linkFilter = new OrFilter(new NodeClassFilter(LinkTag.class), frameFilter);
			// 得到所有经过过滤的标签
			NodeList list = parser.extractAllNodesThatMatch(linkFilter);
			for (int i = 0; i < list.size(); i++) {
				Node tag = list.elementAt(i);
				if (tag instanceof LinkTag)// <a> 标签
				{
					LinkTag link = (LinkTag) tag;
					String linkUrl = link.getLink();// url
					if (filter.accept(linkUrl)){
						links.add(linkUrl);
					}
					String text = link.getLinkText();//链接文字
					System.out.println("==========================================================>连接的文字" + text);
				}
				else if (tag instanceof ImageTag)//<img> 标签
				{
					ImageTag image = (ImageTag) list.elementAt(i);
					System.out.println("==========================================================>图片连接地址" + image.getImageURL() );
					System.out.println("==========================================================>图片连接文字" + image.getText() );
				} else// <frame> 标签
				{
					// 提取 frame 里 src 属性的链接如 <frame src="test.html"/>
					String frame = tag.getText();
					int start = frame.indexOf("src=");
					frame = frame.substring(start);
					int end = frame.indexOf(" ");
					if (end == -1)
						end = frame.indexOf(">");
					String frameUrl = frame.substring(5, end - 1);
					if (filter.accept(frameUrl))
						links.add(frameUrl);
					System.out.println("==========================================================>frame连接" + frameUrl );
				}
			}
		} catch (ParserException e) {
			//e.printStackTrace();
			System.err.println("---------------------------->连接异常:"+e.getMessage());
		}
		return links;
	}

	// 测试的 main 方法
	public static void main(String[] args) {
		System.out.println("------>start");
		/*Set<String> links = HtmlParserTool.extracLinks("http://www.twt.edu.cn",
				new LinkFilter() {
					// 提取以 http://www.twt.edu.cn 开头的链接
					public boolean accept(String url) {
						if (url.startsWith("http://www.twt.edu.cn"))
							return true;
						else
							return false;
					}

				});
		System.out.println("------>link..");
		for (String link : links)
			System.out.println(link);*/
		HtmlParserTool.extractKeyWordText("http://weixin.sogou.com/gzh?openid=oIWsFtylOkpdmF7oLNuA0hKcovm4&ext=_L45N5QlA_XWJfZtw6hHqNfcGHPnOuNefc5LiVAYAow_oYf2Fz7WelFoVNDjzwED", "丼丼屋");
		System.out.println("------>end");
	}
}
