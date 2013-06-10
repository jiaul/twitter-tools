package cc.twittertools.index;

import java.io.File;
import java.io.PrintStream;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.log4j.Logger;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.store.FSDirectory;

import cc.twittertools.index.IndexStatuses.StatusField;

/**
 * Reference implementation for indexing statuses.
 */
public class ExtractTweetidsFromIndex {
  private static final Logger LOG = Logger.getLogger(ExtractTweetidsFromIndex.class);

  private ExtractTweetidsFromIndex() {}

  private static final String INDEX_OPTION = "index";

  @SuppressWarnings("static-access")
  public static void main(String[] args) throws Exception {
    Options options = new Options();

    options.addOption(OptionBuilder.withArgName("dir").hasArg()
        .withDescription("index location").create(INDEX_OPTION));

    CommandLine cmdline = null;
    CommandLineParser parser = new GnuParser();
    try {
      cmdline = parser.parse(options, args);
    } catch (ParseException exp) {
      System.err.println("Error parsing command line: " + exp.getMessage());
      System.exit(-1);
    }

    if (!cmdline.hasOption(INDEX_OPTION)) {
      HelpFormatter formatter = new HelpFormatter();
      formatter.printHelp(ExtractTweetidsFromIndex.class.getName(), options);
      System.exit(-1);
    }

    long startTime = System.currentTimeMillis();

    File indexLocation = new File(cmdline.getOptionValue(INDEX_OPTION));
    if (!indexLocation.exists()) {
      System.err.println("Error: " + indexLocation + " does not exist!");
      System.exit(-1);
    }

    IndexReader reader = DirectoryReader.open(FSDirectory.open(indexLocation));
    PrintStream out = new PrintStream(System.out, true, "UTF-8");
    for (int i=0; i<reader.maxDoc(); i++) {
      Document doc = reader.document(i);
      out.println(doc.getField(StatusField.ID.name).stringValue() + "\t" +
          doc.getField(StatusField.SCREEN_NAME.name).stringValue());
    }
    out.close();
    reader.close();

    LOG.info("Total elapsed time: " + (System.currentTimeMillis() - startTime) + "ms");
  }
}
