

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;


public class Top10Finder {
	public static final String CHARSET = "SJIS";
	public static final String PAGERANKS_TEXT_FILE = "pageranks.txt";
	public static final String TOP10_TEXT_FILE = "top10.txt";
	static ArrayList<Page> people;
	static int TOP = 10;

	public static void main (String args[]){
		long startTime = System.currentTimeMillis();
		File pageRanksTextFile = new File(PAGERANKS_TEXT_FILE);

		if(!pageRanksTextFile.exists() || pageRanksTextFile.length() == 0){
			System.out.println("PageRanks file does not exist, run PageRanks program first. Exiting.");
			return;
		}

		people = prepareUserList(pageRanksTextFile);
		writeTop10();

		int i = 1;
		for(Page person : people){
			System.out.format("Rank %d: %d %s %.10f %.5f\n", i++, person.getId(), person.getTitle(), 
					person.getPageRank(), Math.log10(person.getPageRank()));
		}

		long endTime = System.currentTimeMillis();

		System.out.println("\nDONE: Time taken = " + (endTime - startTime)/1000f + " secs");
	}

	static ArrayList<Page> prepareUserList(File ranksFile){
		ArrayList<Page> users = new ArrayList<Page>();
		System.out.println("Search starting Now");
		ArrayList<Page> result = new ArrayList<Page>();

		try {
			System.out.println("Reading pages now");
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					new FileInputStream(ranksFile), CHARSET));
			String line;

			while((line = reader.readLine()) != null){				
				String[] items = line.split(" ");
				if(items.length != 4){
					System.out.printf("SqlReader.inLinks(): items length not appropriate (%d),"
							+ " skipping tuple\n", items.length);
					continue;
				}

				int id = Integer.parseInt(items[0]);
				String title = items[1];
				Float pageRank = Float.parseFloat(items[2]);
				URL url = new URL("http://ja.wikipedia.org/wiki/" + title);

				users.add(new Page(id, title, pageRank, url));
			}

			reader.close();
		} catch (UnsupportedEncodingException e) {
			System.out.println("EXCEPTION: " + e.getMessage());
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			System.out.println("EXCEPTION: " + e.getMessage());
			e.printStackTrace();
		} catch (IOException e) {
			System.out.println("EXCEPTION: " + e.getMessage());
			e.printStackTrace();
		}

		System.out.println("Parsing html now");
		BufferedReader br;
		URLConnection conn;
		int count = 0, i = 0;

		for(Page user : users){
			URL url = user.getUrl();			
			try {
				i++;
				conn = url.openConnection();
				br = new BufferedReader(
						new InputStreamReader(conn.getInputStream()));

				String inputLine;
				boolean flag = false;
				int j=0;
				int limit = 160;
				while((inputLine = br.readLine()) != null && j<limit){
					++j;
					if((inputLine.contains("出生</th>") || inputLine.contains("生年月日</th>") ||
							inputLine.contains("生誕</th>"))){
						flag = true;
					}
					if(inputLine.contains("死没</th>") || inputLine.contains("没年</th>") || 
							inputLine.contains("没年月日</th>") || inputLine.contains("死去</th>") || 
							inputLine.contains("死亡</th>")){
						flag = false;
					}
				}
				
				if(flag){
					System.out.println(user.getTitle() + " is human! Total = " + count);
					result.add(new Page(user.getId(), user.getTitle(), user.getPageRank(), url));
					count++;
				}

				if(count >= TOP)
					break;

				System.out.println(i + ". Parsed: " + user.getTitle());

				br.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		System.out.println("User list prepared, size: " + result.size());
		return result;
	}
	
	private static void writeTop10(){
		File top10TextFile = new File(TOP10_TEXT_FILE);
		try {
			PrintWriter writer = new PrintWriter (new OutputStreamWriter (
					new BufferedOutputStream(new FileOutputStream(top10TextFile)), CHARSET));
			
			for(Page p : people){
				writer.format("%d %s %.15f %.7f\n", p.getId(), p.getTitle(), 
						p.getPageRank(), Math.log10(p.getPageRank()));
			}
			
			writer.close();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
}
