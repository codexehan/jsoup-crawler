package com.bing.crawler;

import java.io.IOException;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.bing.dao.GterManager;
import com.bing.model.Gteroffer;
import com.bing.model.Gterperson;

public class CrawlerDB {
	private String breakpoint="http://bbs.gter.net/forum.php?mod=viewthread&tid=2110544&extra=page%3D1%26filter%3Dauthor%26orderby%3Ddateline%26typeid%3D995%26typeid%3D995%26orderby%3Ddateline";//the first page
	private String tmpBreakpoint = null;
	public CrawlerDB(){

	}
	/*public boolean hasUpdate(String pageContent){
		 char[] str = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
	                'a', 'b', 'c', 'd',  'e', 'f'};
		byte[] input = pageContent.getBytes();
		try{
			MessageDigest md = MessageDigest.getInstance("MD5");
			md.update(input);
			byte[] tmp=md.digest();
            char[] ch = new char[16*2];
            int index = 0;
            for(int i = 0; i < 16; i++) {
                byte b = tmp[i];
                ch[index++] = str[b >>> 4 & 0xf];
                ch[index++] = str[b & 0xf];
            }
            String res= new String(ch);
            System.out.println(res);
		}catch(Exception e){
			e.printStackTrace();
		}
		return false;
	}*/
	public boolean isBreakPoint(String link){
		return link.equals(breakpoint);
	}
	/**
	 * Entrance of Gter offer pages
	 */
	public void getGterOfferPages(){
		//this is the link sorted by publish date
		String link="http://bbs.gter.net/forum.php?mod=forumdisplay&fid=812&orderby=dateline&typeid=995&orderby=dateline&typeid=995&filter=author&page=";
		int pageAmount = 39;
		for(int i=1;i<=pageAmount&&getGterOfferLinks(link+i);i++);
		//update breakpoint;
		breakpoint = tmpBreakpoint;
		System.out.println("done...");
	}
	/**
	 * get all offer links in the page
	 * @param URL
	 * @return true: tell the {@link getGterOfferPages} to continue to crawl; false: tell the {@link getGterOfferPages} to stop at this page
	 */
	public boolean getGterOfferLinks(String URL){	
		try {
			Document document = Jsoup.connect(URL).get();
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
			////		GterOffer offer = new GterOffer();
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
				//person maybe null because some persons didn't provide personal details
				GterManager.INSTANCE.addGterperson(person);
				//bind the offer to person
	//			System.out.println("**********"+person.getId()+"*****"+URL+"**********");
				for(Gteroffer offer: offers){
					offer.setGterperson(person);
					GterManager.INSTANCE.addGterOffer(offer);
	//				System.out.println(offer.getId());
				}
				//release memory
				offers.clear();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}//use get method to visit
		
	}
	
	/**
	 * Set the university which needs to crawler
	 * @param university
	 * @return
	 */
	public boolean isWantted(String university){
		return university.contains("National University of Singapore")||university.contains("Nanyang Technological University");
	}
}
