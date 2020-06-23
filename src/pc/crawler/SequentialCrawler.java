package pc.crawler;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;

/**
 * Crawler - sequential version.
 */
public class SequentialCrawler {
	/**
	 * Program entry point.
	 * 
	 * @param args Arguments.
	 * @throws IOException if an I/O error occurs.
	 */
	public static void main(String[] args) throws IOException {
		String url = args.length != 0 ? args[0] : "http://127.0.0.1:8123/";
		SequentialCrawler sc = new SequentialCrawler();
		sc.setVerboseOutput(true);
		sc.crawl(url);
	}

	private boolean verbose = false;
	private final PrintStream log;

	/**
	 * Constructor.
	 * 
	 * @throws FileNotFoundException if log file <code>crawler.log</code> cannot be
	 *                               opened.
	 */
	public SequentialCrawler() throws FileNotFoundException {
		log = new PrintStream("crawler.log");
	}

	/**
	 * Enable / disable verbose output.
	 * 
	 * @param enable Value for setting.
	 */
	public void setVerboseOutput(boolean enable) {
		verbose = enable;
	}

	/**
	 * Crawl starting from given URL.
	 * 
	 * @param root Root URL.
	 * @throws IOException if an I/O error occurs.
	 */
	public void crawl(String root) throws IOException {
		if (!root.startsWith("http://")) {
			throw new IllegalArgumentException();
		}
		long t = System.currentTimeMillis();
		int rid = 0;
		LinkedList<String> toVisit = new LinkedList<>();
		HashSet<String> visited = new HashSet<>();
		log("Starting at %s", root);
		toVisit.add(root);
		while (!toVisit.isEmpty()) {
			String path = toVisit.removeFirst();
			if (visited.contains(path)) {
				continue;
			}
			rid++;
			URL url = new URL(path);
			visited.add(url.toString());
			List<String> links = performTransfer(rid, url);
			for (String link : links) {
				String newURL = new URL(url, new URL(url, link).getPath()).toString();
				if (!visited.contains(newURL)) {
					// info("%d | Added %s", rid, newURL);
					toVisit.addLast(newURL);
				} else {
					// info("%d | Already visited %s", rid, newURL);
				}
			}
		}
		t = System.currentTimeMillis() - t;
		log("Done: %d transfers in %d ms (%.2f transfers/s)", rid, t, (1e+03 * rid) / t);
	}

	private static final Pattern HTML_LINK_REGEX = Pattern.compile("\\<a\\s+href\\=\"(.*?)\"");

	protected List<String> performTransfer(int rid, URL url) {
		try {
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			long threadId = Thread.currentThread().getId();
			conn.setRequestMethod("GET");
			info("Thread: %d | %d | %s | %d | %d bytes | %s", threadId, rid, url, conn.getResponseCode(),
					conn.getContentLength(), conn.getContentType());

			if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
				// Data can not be obtained.
				return Collections.emptyList();
			}

			// Transfer to temporary file.
			File tmp = File.createTempFile("crawler", "bin");
			tmp.deleteOnExit();
			InputStream in = conn.getInputStream();
			byte[] buf = new byte[16384];
			int total = 0;
			try (FileOutputStream out = new FileOutputStream(tmp)) {
				// while (in.available() > 0) {
				while (total < conn.getContentLength()) {
					int n = in.read(buf);
					total += n;
					out.write(buf, 0, n);
				}
			}

			info("Thread: %d | %d | %s | %d bytes received", threadId, rid, url.toString(), total);
			in.close();

			if (!conn.getContentType().equals("text/html")) {
				// Not HTML
				return Collections.emptyList();
			}

			// Parse links in HTML data
			ArrayList<String> links = new ArrayList<>();
			try (Scanner sin = new Scanner(tmp)) {
				while (sin.hasNextLine()) {
					while (sin.findInLine(HTML_LINK_REGEX) != null) {
						MatchResult mr = sin.match();
						String link = mr.group(1);
						if (!link.contains(":") && !link.startsWith("#")) {
							links.add(link);
						} else {
							// info("%d | Ignoring %s ", rid, link);
						}
					}
					sin.nextLine();
				}
			}
			return links;
		} catch (Exception e) {
			log("%d | Error: %s - %s", rid, e.getClass().getName(), e.getMessage());
			// e.printStackTrace(System.out);
			return Collections.emptyList();
		}
	}

	protected void info(String format, Object... args) {
		if (verbose) {
			log(format, args);
		}
	}

	@SuppressWarnings("deprecation")
	protected synchronized void log(String format, Object... args) {
		String msg = String.format("%s | %s", new Date().toGMTString(), String.format(format, args));
		log.println(msg);
		System.out.println(msg);
	}

}
