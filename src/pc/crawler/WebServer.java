package pc.crawler;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.URLConnection;
import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

/**
 * Web server.
 *
 */
public class WebServer {

	static boolean verbose;

	/**
	 * Main program.
	 * 
	 * @param args Arguments.
	 * @throws IOException if an error occurs during server creation.
	 */
	public static void main(String[] args) throws IOException {
		String home = args.length >= 1 ? args[0] : "cooperari-0.3/doc/javadoc";
		int port = args.length >= 2 ? Integer.parseInt(args[1]) : 8123;
		int threads = args.length >= 3 ? Integer.parseInt(args[2]) : 4;
		verbose = args.length >= 4 ? (Integer.parseInt(args[3]) == 1 ? true : false) : true;

		WebServer ws = new WebServer(home, port, threads);
		ws.start();
	}

	private static final boolean WORK_STEALING_POOL = true;
	private final HttpServer theActualServer;
	private final File home;
	private final AtomicInteger reqId = new AtomicInteger();

	/**
	 * Constructor.
	 * 
	 * @param path    Home for pages served.
	 * @param port    Port number to use.
	 * @param threads Number of threads.
	 * @throws IOException If an occurs creating the server.
	 */
	public WebServer(String path, int port, int threads) throws IOException {
		this.home = new File(path);
		if (!home.isDirectory()) {
			throw new IllegalArgumentException(path + " must be a directory!");
		}
		info("Home: %s", home.getPath());
		info("Port: %d", port);
		info("Threads: %d", threads);
		theActualServer = HttpServer.create(new InetSocketAddress(port), 8);
		if (WORK_STEALING_POOL) {
			theActualServer.setExecutor(Executors.newWorkStealingPool(threads));
		} else {
			theActualServer.setExecutor(Executors.newFixedThreadPool(threads));
		}
		theActualServer.createContext("/", this::handler);
	}

	/**
	 * Start the server.
	 */
	public void start() {
		info("Starting server ... ");
		theActualServer.start();
		info("Server started %s", theActualServer.getAddress());
	}

	/**
	 * Stop the server.
	 */
	public void stop() {
		info("Stopping server ... ");
		theActualServer.stop(1_000);
		info("Server stopped");
	}

	private void handler(HttpExchange ex) throws IOException {
		try {
			Headers headers = ex.getResponseHeaders();
			String path = ex.getRequestURI().toString().trim();
			File file = new File(home, path);
			int rid = reqId.incrementAndGet();
			info("%d | Request for '%s'", rid, path);
			if (!file.exists()) {
				info("%d | Not found!", rid);
				ex.sendResponseHeaders(HttpURLConnection.HTTP_NOT_FOUND, 0);
				return;
			}
			File fileToSend = file;
			if (file.isDirectory()) {
				if (!path.endsWith("/")) {
					path += "/";
				}
				info("%d | Listing directory", rid);
				fileToSend = generateListing(path, file);
			}
			// Send file contents
			String mimeType = URLConnection.guessContentTypeFromName(fileToSend.getName());
			if (mimeType == null) {
				mimeType = "application/octet-stream";
			}
			long length = fileToSend.length();
			info("%d | Sending %s (%s, %d bytes)", rid, path, mimeType, length);
			headers.set("Content-type", mimeType);
			ex.sendResponseHeaders(200, length);
			try (OutputStream out = ex.getResponseBody()) {
				try (FileInputStream in = new FileInputStream(fileToSend)) {
					byte[] buf = new byte[16384];
					long sent = 0;
					while (sent < length) {
						int n = in.read(buf);
						out.write(buf, 0, n);
						sent += n;
					}
				}
				out.flush();
			} finally {
				if (fileToSend != file) {
					fileToSend.delete();
				}
			}
		} catch (IOException e) {
			e.printStackTrace(System.out);
		} finally {
			ex.close();
		}
	}

	private static File generateListing(String path, File directory) throws IOException {
		File listFile = File.createTempFile("dir", ".html");
		try (PrintStream out = new PrintStream(listFile)) {
			out.printf("<html>%n<head><title>%s</title></head>%n<body>%n", path);
			out.printf("<b><a href=\"/\">root</a>%n");
			if (!path.equals("/")) {
				String prefix = "/";
				for (String x : path.substring(1).split("/")) {
					prefix += x;
					out.printf("&nbsp;&nbsp;/&nbsp;&nbsp;<a href=\"%s\">%s</a>%n", prefix, x);
					prefix += "/";
				}
			}
			out.println("</b>");
			for (String f : directory.list()) {
				out.printf("<li><a href=\"%s%s\">%s</a></li>%n", path, f, f);
			}
			out.printf("</ul>%n</p>%n</body>%n</html>%n");
		}
		return listFile;
	}

	@SuppressWarnings("deprecation")
	private synchronized void info(String format, Object... args) {
		if (!verbose)
			return;
		String msg = String.format("%s | %s", new Date().toGMTString(), String.format(format, args));
		System.out.println(msg);
	}

}
