package edu.umbc.idynin1.twittersaver;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Scanner;
import java.util.TreeMap;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.zip.GZIPOutputStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import twitter4j.FilterQuery;
import twitter4j.StallWarning;
import twitter4j.Status;
import twitter4j.StatusDeletionNotice;
import twitter4j.StatusListener;
import twitter4j.TwitterException;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;
import twitter4j.json.DataObjectFactory;

public class TwitterSaver {
	private static final String PROPERTIES_FILE_NAME = "twitter4j.properties";
	private static final String PROPERTIES_FILE_TEMPLATE_NAME = "template.twitter4j.properties";
	private static final String LOG_FILE_NAME = "twitterSaver.log";
	private static final String TEMP_FILE_EXTENSION = ".txt.tmp";
	private static final String COMPRESSED_FILE_EXTENSION = ".txt.gz";
	private static final Logger LOGGER = Logger.getLogger(TwitterSaver.class
			.getName());

	public static void main(String[] args) throws IOException {

		Properties props = new Properties();
		File propsFile = new File(PROPERTIES_FILE_NAME);
		try {
			props.load(new FileInputStream(propsFile));
		} catch (FileNotFoundException e) {
			Logger.getGlobal().info(
					".properties file not found. Creating default template.");
			FileUtils.writeLines(propsFile, IOUtils
					.readLines(TwitterSaver.class
							.getResourceAsStream("/" + PROPERTIES_FILE_TEMPLATE_NAME)));
			props.load(new FileInputStream(propsFile));
		}
		String temp, boxen = "";
		for (int i = 1; i <= 10; i++) {
			if ((temp = (String) props.get("saver.boundingBox." + i)) != null
					&& validateBoundingBox(temp)) {
				boxen += temp + ",";
			}
		}
		if (boxen.length() > 0) {
			boxen = boxen.substring(0, boxen.length() - 1);
		}

		String temppath = props.getProperty("saver.outputPath", "");
		if (!temppath.endsWith("/")) {
			temppath += "/";
		}
		final String outputPath = temppath;

		File outDir = new File(outputPath);
		outDir.mkdirs();

		try {
			FileHandler fh = new FileHandler(outputPath + LOG_FILE_NAME);
			fh.setFormatter(new SimpleFormatter());
			LOGGER.addHandler(fh);
		} catch (SecurityException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		StatusListener listener = new StatusListener() {
			TreeMap<String, BufferedWriter> logWriters = new TreeMap<String, BufferedWriter>();
			HashMap<String, Long> lastMessageTimestamp = new HashMap<String, Long>();
			long lastCleanup = System.currentTimeMillis();
			DateFormat df = new SimpleDateFormat("yyyy/MM/dd/HH/mm");

			@Override
			public void onStatus(Status status) {
				String logBin;
				synchronized (df) {
					logBin = df.format(status.getCreatedAt());
				}
				BufferedWriter bw = logWriters.get(logBin);
				if (bw == null) {
					try {
						File f = new File(outputPath + logBin + TEMP_FILE_EXTENSION);
						f.getParentFile().mkdirs();
						bw = new BufferedWriter(new OutputStreamWriter(
								new FileOutputStream(f, true), "UTF-8"));
						// bw = new OutputStreamWriter(new FileOutputStream(f,
						// true), Charset.forName("UTF-8"));
						logWriters.put(logBin, bw);
					} catch (FileNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (UnsupportedEncodingException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

				}

				lastMessageTimestamp.put(logBin, System.currentTimeMillis());

				try {
					if (bw != null) {
						bw.newLine();
						bw.append(DataObjectFactory.getRawJSON(status));
					}
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}

				if (System.currentTimeMillis() - lastCleanup > 2000
						&& lastMessageTimestamp.size() > 1) {
					Iterator<Map.Entry<String, Long>> lastlogentries = lastMessageTimestamp
							.entrySet().iterator();
					while (lastlogentries.hasNext()) {
						Map.Entry<String, Long> lastlogentry = lastlogentries
								.next();
						if (lastlogentry.getValue() < (System
								.currentTimeMillis() - 10000)) {
							if ((bw = logWriters.remove(lastlogentry.getKey())) != null) {
								try {
									bw.newLine();
									bw.flush();
									bw.close();
									IOUtils.copy(new FileInputStream(new File(outputPath + logBin
											+ TEMP_FILE_EXTENSION)), new GZIPOutputStream(
											new FileOutputStream(
													new File(
															outputPath + logBin
																	+ COMPRESSED_FILE_EXTENSION),
													false)));
									FileUtils.deleteQuietly(new File(outputPath + logBin
											+ ".txt.tmp"));
									LOGGER.info("Cleaned up log bin "
											+ lastlogentry.getKey());
									lastlogentries.remove();
								} catch (IOException e) {

									// TODO Auto-generated catch block
									e.printStackTrace();
								}
							}
						}
					}
				}
			}

			@Override
			public void onException(Exception ex) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onDeletionNotice(StatusDeletionNotice statusDeletionNotice) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onTrackLimitationNotice(int numberOfLimitedStatuses) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onScrubGeo(long userId, long upToStatusId) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onStallWarning(StallWarning warning) {
				// TODO Auto-generated method stub

			}
		};

		TwitterStream twitterStream = new TwitterStreamFactory().getInstance();
		twitterStream.addListener(listener);

		try {
			LOGGER.info("Logging via account: " + twitterStream.getScreenName()
					+ "(" + twitterStream.getId() + ")");
		} catch (IllegalStateException | TwitterException e) {
			if (e instanceof IllegalStateException && e.getMessage().contains("OAuth"))
				LOGGER.warning("!! MISSING OR INVALID CREDENTIALS. Fill out/verify twitter4j.properties file !!");
			else
				e.printStackTrace();
			System.exit(-1);
		}

		double[][] boundingBox = boundingBoxStringToBoundingBox(boxen);
		if (boundingBox.length >= 2) {
			LOGGER.info("Bounding Boxes: " + Arrays.deepToString(boundingBox));
			twitterStream.filter(new FilterQuery().locations(boundingBox));
		} else {
			LOGGER.info("Not filtering sample stream.");
			twitterStream.sample();
		}

		Scanner s = new Scanner(System.in);
		while (true) {
			System.out.println("Type stop to stop.");
			if (s.nextLine().equalsIgnoreCase("stop")) {
				break;
			}
		}
		s.close();

		System.out.println("Shutting down...");

		twitterStream.cleanUp();
		twitterStream.shutdown();

		System.out.println("Terminated.");

	}

	private static boolean validateBoundingBox(String s) {
		String[] parts = s.trim().replaceAll("\\s+", "").split(",");
		try {
			if (parts.length == 4) {
				Double.parseDouble(parts[0]);
				Double.parseDouble(parts[1]);
				Double.parseDouble(parts[2]);
				Double.parseDouble(parts[3]);

				return true;
			}
		} catch (NumberFormatException nfe) {
			Logger.getGlobal().warning("Bad bounding box: " + s);
		}
		return false;
	}

	private static double[][] boundingBoxStringToBoundingBox(String in) {
		String[] parts = in.trim().replaceAll("\\s+", "").split(",");
		double[][] out = new double[parts.length / 2][2];
		if (parts.length >= 4 && parts.length % 2 == 0) {
			for (int i = 0; i < parts.length; i += 2) {
				out[i / 2][0] = Double.parseDouble(parts[i]);
				out[i / 2][1] = Double.parseDouble(parts[i + 1]);
			}
		}
		return out;
	}
}
