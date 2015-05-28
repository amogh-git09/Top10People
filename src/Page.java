

import java.net.URL;
import java.util.Comparator;


public class Page implements Comparable<Page>{
	private int id;
	private String title;
	private float pageRank;
	private URL url;
	
	public Page(int id, String title, float pageRank, URL url){
		this.id = id;
		this.url = url;
		this.title = title;
		this.pageRank = pageRank;
	}
	
	public URL getUrl(){
		return url;
	}

	public int getId() {
		return id;
	}
	
	public String getTitle(){
		return title;
	}

	public float getPageRank() {
		return pageRank;
	}

	@Override
	public int compareTo(Page u) {
		return this.pageRank < u.getPageRank() ? 1 : -1;
	}
	
	public static class Comparators {
		public static Comparator<Page> ID = new Comparator<Page>() {
			@Override
			public int compare(Page u1, Page u2){
				if(u1.getPageRank() < u2.getPageRank())
					return 1;
				else if(u1.getPageRank() > u2.getPageRank())
					return -1;
				else
					return 0;
			}
		};
	}
}
