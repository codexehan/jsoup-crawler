package com.bing.crawler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.TreeMap;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.bing.dao.GterManager;
import com.bing.model.Gteroffer;
import com.bing.model.Gterperson;

public class CrawlerNoDB {
	private final static int Time_Out = 60*1000;// 1 min
	private TreeMap<Integer, String> universities = new TreeMap<Integer, String>();
	private HashMap<String, Integer> countries = new HashMap<String, Integer>();//构造函数中手动初始化, 院校库包含的国家和地区
	private HashMap<String, String> countryLink = new HashMap<String, String>();//构造函数中手动初始化
	private String universitySo;//搜索的城市
	private String countrySo;//搜索的国家地区
	private String breakpoint="http://bbs.gter.net/forum.php?mod=viewthread&tid=2110544&extra=page%3D1%26filter%3Dauthor%26orderby%3Ddateline%26typeid%3D995%26typeid%3D995%26orderby%3Ddateline";
	private String tmpBreakpoint = null;
	private String[] countriesArray = new String[] {"美国","加拿大","香港","英国","新加坡","澳大利亚","欧洲诸国","澳门","台湾","日本","韩国","新西兰"};
	public CrawlerNoDB(){
		//initialize gter country
		this.countries.put("美国", 1);
		this.countries.put("加拿大", 306);
		this.countries.put("香港", 951);
		this.countries.put("英国", 720);
		this.countries.put("新加坡", 952);
		this.countries.put("澳大利亚", 953);
		//initialize gter country link
		this.countryLink.put("美国", "http://bbs.gter.net/forum.php?mod=forumdisplay&fid=49&orderby=dateline&typeid=158&filter=author&orderby=dateline&typeid=158&page=");
		this.countryLink.put("加拿大", "http://bbs.gter.net/forum.php?mod=forumdisplay&fid=565&orderby=dateline&typeid=991&filter=author&orderby=dateline&typeid=991&page=");
		this.countryLink.put("英国", "http://bbs.gter.net/forum.php?mod=forumdisplay&fid=486&orderby=dateline&typeid=992&filter=author&orderby=dateline&typeid=992&page=");
		this.countryLink.put("欧洲诸国", "http://bbs.gter.net/forum.php?mod=forumdisplay&fid=539&orderby=dateline&typeid=993&filter=author&orderby=dateline&typeid=993&page=");
		this.countryLink.put("香港", "http://bbs.gter.net/forum.php?mod=forumdisplay&fid=811&orderby=dateline&typeid=994&filter=author&orderby=dateline&typeid=994&page=");
		this.countryLink.put("澳门", "http://bbs.gter.net/forum.php?mod=forumdisplay&fid=811&orderby=dateline&typeid=994&filter=author&orderby=dateline&typeid=994&page=");
		this.countryLink.put("台湾", "http://bbs.gter.net/forum.php?mod=forumdisplay&fid=811&orderby=dateline&typeid=994&filter=author&orderby=dateline&typeid=994&page=");
		this.countryLink.put("新加坡", "http://bbs.gter.net/forum.php?mod=forumdisplay&fid=812&orderby=dateline&typeid=995&filter=author&orderby=dateline&typeid=995&page=");
		this.countryLink.put("日本", "http://bbs.gter.net/forum.php?mod=forumdisplay&fid=484&orderby=dateline&typeid=996&filter=author&orderby=dateline&typeid=996&page=");
		this.countryLink.put("韩国", "http://bbs.gter.net/forum.php?mod=forumdisplay&fid=484&orderby=dateline&typeid=996&filter=author&orderby=dateline&typeid=996&page=");
		this.countryLink.put("澳大利亚", "http://bbs.gter.net/forum.php?mod=forumdisplay&fid=128&orderby=dateline&typeid=1464&filter=author&orderby=dateline&typeid=1464&page=");
		this.countryLink.put("新西兰", "http://bbs.gter.net/forum.php?mod=forumdisplay&fid=128&orderby=dateline&typeid=1464&filter=author&orderby=dateline&typeid=1464&page=");
	}
	public void start() {
		preGetGterUniversityPages();//确定国家或地区
		getGterUniversityPages();//获取大学李彪
		preGetGterOfferPages();//universitySo
		getGterOfferPages();
	}
	public boolean isBreakPoint(String link){
		return link.equals(breakpoint);
	}
	///////////////////////////get offers///////////////////////////
	/**
	 * Entrance of Gter offer pages
	 */
	public void getGterOfferPages(){
		//this is the link sorted by publish date
		String link= this.countryLink.get(this.countrySo);
		int pageAmount = 44/*Integer.MAX_VALUE*/;
		int i=1;
		for(;i<=pageAmount&&getGterOfferLinks(link+i);i++);
		//update breakpoint;
		breakpoint = tmpBreakpoint;
		System.out.println("done...");
	}
	public void preGetGterOfferPages() {
		int min = Integer.MAX_VALUE, max = Integer.MIN_VALUE;
		if(this.universities.isEmpty()) {//非院校库国家和地区，无法获取专业列表
			Scanner reader = new Scanner(System.in);
			System.out.println("Input the official English name of university: ");
			this.universitySo = reader.nextLine();
			reader.close();
		}
		else {
			for(Entry<Integer, String> entry: this.universities.entrySet()) {
				System.out.println(entry.getKey()+". "+ entry.getValue());
				min = Math.min(min, entry.getKey());
				max = Math.max(max, entry.getKey());
			}
			Scanner reader = new Scanner(System.in);  // Reading from System.in
			System.out.println("Choose the university ("+min+"-"+max+"): ");
			int n = Integer.parseInt(reader.nextLine()); // Scans the next token of the input as an int.
			this.universitySo = this.universities.get(n);
			//once finished
			reader.close();
		}
	}
	/**
	 * get all offer links in the page
	 * @param URL
	 * @return true: tell the {@link getGterOfferPages} to continue to crawl; false: tell the {@link getGterOfferPages} to stop at this page
	 */
	public boolean getGterOfferLinks(String URL){	
		try {
			Document document = Jsoup.connect(URL).timeout(Time_Out).get();//1 min
			Elements offerLinks = document.select(".icn a[href]");
			//whether need update
			if(!isBreakPoint(offerLinks.attr("href"))){//check the first link in the offer page
				if(tmpBreakpoint==null){
					//update new breakpoint
					tmpBreakpoint = offerLinks.attr("href");
				}
				for(Element offerLink:offerLinks){
					if(!isBreakPoint(offerLink.attr("href"))){
						getOfferDetails(offerLink.attr("href"));
					}
					else{
						return false;//stop at this link in this page
					}
				}
			}
			else{
				return false; //stop at this page
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}//use get method to visit
		return true; // go to next page;
	}
	/**
	 * extract all required offers from the URL
	 * @param URL
	 */
	public void getOfferDetails(String URL){
	///	Person person = new Person();
		Gterperson person = new Gterperson();
	////	List<Offer> offers = new ArrayList<Offer>();
		List<Gteroffer> offers = new ArrayList<Gteroffer>();
		try {
			Document document = Jsoup.connect(URL).get();
			Elements tbodys = document.select("caption+tbody");
			//one tbody one offer
			for(Element tbody:tbodys){
			//	extractOffer(tbody, URL);
				String name = null;
				String val = null;
				Elements rows = tbody.select("tr");
				if(rows.size()!=0&&rows.get(0).getAllElements().size()==4){//offer
					String university=rows.get(0).getAllElements().get(2).select("a").text();
					if(isWantted(university)){
						Gteroffer offer = new Gteroffer();
						offer.setLink(URL);
						for(Element row:rows){// th td
							Element th = row.getAllElements().get(1);
							Element td = row.getAllElements().get(2);
							if(td.getAllElements().size() > 1){
								//university <a></a>
								name=th.text();
								val=td.select("a").text();
							}
							else{
								name=th.text();
								val=td.text();
							}
							//update offer
							offer.updateOffer(name, val);
						}
						offers.add(offer);
					}
				}
				else if(rows.size()!=0&&rows.get(0).getAllElements().size()==3){//personal details
					
					for(Element row:rows){// th td
						Element th = row.getAllElements().get(1);
						Element td = row.getAllElements().get(2);
						name=th.text();
						val=td.text();
						//update personal details
						person.updatePersonDetails(name, val);
					}
				}
			}
			//filter some persons didn't provide the offer details
			if(!offers.isEmpty()){
////			//////////////save the information to database////////////////////////
				/*//person maybe null because some persons didn't provide personal details
				GterManager.INSTANCE.addGterperson(person);
				//bind the offer to person
	//			System.out.println("**********"+person.getId()+"*****"+URL+"**********");
				for(Gteroffer offer: offers){
					offer.setGterperson(person);
					GterManager.INSTANCE.addGterOffer(offer);
	//				System.out.println(offer.getId());
				}*/
				/////////////////////////print out information//////////////////////////////////////////
				System.out.println("*******************个人信息及offer**************************");
				System.out.println(person.toString());
				for(Gteroffer offer : offers) {
					System.out.println(offer.toString());
				}
				System.out.println("********************************************************");
				//release memory
				offers.clear();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			
			e.printStackTrace();
		}//use get method to visit
		
	}
	/////////////////////////////////////获取选中国家的学校信息////////////////////////////////////////
	public void preGetGterUniversityPages() {
		//TODO:
		int min = Integer.MAX_VALUE, max = Integer.MIN_VALUE;
		for(int i=0; i<this.countriesArray.length;i++) {
			System.out.println(i+1+" "+this.countriesArray[i]);
		}
		Scanner reader = new Scanner(System.in);  // Reading from System.in
		System.out.println("Choose the country ("+1+"-"+this.countriesArray.length+"): ");
		int n = Integer.parseInt(reader.nextLine()); // Scans the next token of the input as an int.
		this.countrySo = this.countriesArray[n-1];
		//once finished
//		reader.close();
	}

	/**
	 * get all universities in a country
	 * @param countryId
	 */
	public void getGterUniversityPages() {
		//TODO: get countryId
		if(this.countries.containsKey(this.countrySo)) {
			int countryId = this.countries.get(this.countrySo);
			//this is the link sorted by publish date
			String link="http://school.gter.net/search/countries.html?countrieid="+countryId+"&page=";
			int pageAmount = getUniversityPageAmount(link+1);/*Integer.MAX_VALUE*/;
			int i=1, index=1;
			for(;i<=pageAmount;i++) {
				index = getGterUniversityNames(link+i, index);
			}
		}
	}
	/**
	 * get all university links in the page
	 * @param URL
	 */
	public int getGterUniversityNames(String URL, int index){	
		try {
			Document document = Jsoup.connect(URL).timeout(Time_Out).get();//1 min
			Elements universityEvenLinks = document.select(".even a");
			Elements universityOddLinks = document.select(".odd a");
			for(Element universityEvenLink:universityEvenLinks) {
				String text = universityEvenLink.html();//text is chinese and english
				this.universities.put(index, text.split("<br>")[1]);
				index++;
			}
			for(Element universityOddLink:universityOddLinks) {
				String text = universityOddLink.html();//text is chinese and english
				this.universities.put(index, text.split("<br>")[1]);
				index++;
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}//use get method to visit
		return index;
	}
	/**
	 * Set the university which needs to crawler
	 * @param university
	 * @return
	 */
	public boolean isWantted(String university){
		//return university.contains("National University of Singapore")||university.contains("Nanyang Technological University");
		return universitySo==null?true:university.contains(this.universitySo);
	}
	public int getUniversityPageAmount(String URL) {
		int amount = 1;
		try {
			Document document = Jsoup.connect(URL).timeout(Time_Out).get();//1 min
			Elements pageLinks= document.select(".last");
			int i=1;
			for(Element pageLink : pageLinks) {
				String href = pageLink.attr("href");
				int index = href.lastIndexOf("=");
				String amountStr = href.substring(index+1, href.length());
				amount = Integer.parseInt(amountStr);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}//use get method to visit
		return amount;
	}
}
