import org.apache.commons.cli.*;
import org.apache.commons.lang3.StringEscapeUtils;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by jobjects on 5/10/17.
 */
public class CoreNlpToHighlightsXml {

  public static void main(String[] args) throws Exception {

    Options options = new Options();
    options.addOption("c", "corenlp-file", true, "CoreNLP XML file");
    options.addOption("h", "highlights-file", true, "Highlights XML output file");
    options.addOption("person", true, "Person term color");
    options.addOption("org", true, "Organization term color");
    options.addOption("location", true, "Location term color");
    options.addOption("date", true, "Date term color");
    options.addOption("number", true, "Number term color");
    options.addOption("negative", true, "Negative sentiment term color");
    options.addOption("positive", true, "Positive sentiment term color");

    CommandLine cmd = null;
    boolean printUsageAndExit = false;
    try {
      CommandLineParser parser = new DefaultParser();
      cmd = parser.parse( options, args );

      if(!cmd.hasOption("c")) {
        System.out.println("Missing required option: c");
        printUsageAndExit = true;
      }
      if(!cmd.hasOption("h")) {
        System.out.println("Missing required option: h");
        printUsageAndExit = true;
      }

    }
    catch( ParseException exp ) {
      System.out.println( "Unexpected exception:" + exp.getMessage() );
    }

    if (printUsageAndExit || cmd == null) {
      HelpFormatter formatter = new HelpFormatter();
      formatter.printHelp(CoreNlpToHighlightsXml.class.getName(), options);
      return;
    }

    CoreNlpToHighlightsXml processor = new CoreNlpToHighlightsXml();

    processor.setPersonColor(cmd.getOptionValue("person"));
    processor.setOrganizationColor(cmd.getOptionValue("org"));
    processor.setLocationColor(cmd.getOptionValue("location"));
    processor.setDateColor(cmd.getOptionValue("date"));
    processor.setNumberColor(cmd.getOptionValue("number"));
    processor.setSentimentNegativeColor(cmd.getOptionValue("negative"));
    processor.setSentimentPositiveColor(cmd.getOptionValue("positive"));

    processor.process(cmd.getOptionValue("corenlp-file"), cmd.getOptionValue("highlights-file"));
  }

  String personColor = null;
  String organizationColor = null;
  String locationColor = null;
  String dateColor = null;
  String numberColor = null;
  String sentimentNegativeColor = null;
  String sentimentPositiveColor = null;


  public CoreNlpToHighlightsXml() {
  }

  public void process(String coreNLPXml, String outputFile) throws ParserConfigurationException, SAXException, IOException {

    File inputFile = new File(coreNLPXml);
    SAXParserFactory factory = SAXParserFactory.newInstance();
    SAXParser saxParser = factory.newSAXParser();

    Writer out = null;
    try {
      out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputFile), "UTF-8"));
      out.write("<xml>\n");
      out.write("<body units=\"characters\" positions=\"internal\">\n");
      out.write("<highlight>\n");

      System.out.println(String.format("Processing CoreNLP output file %s ... writing highlights to %s", coreNLPXml, outputFile));

      final AtomicInteger highlightedItems = new AtomicInteger(0);
      final Writer output = out;
      CoreNlpXmlHandler xmlHandler = new CoreNlpXmlHandler() {
        @Override
        protected void handleToken(String word, String lemma, int begin, int end, String pos, String ner, String sentiment) throws Exception {
          if (begin >= 0 && end > begin) {

            String color = null;

            if ("PERSON".equals(ner))
              color = personColor;
            else if ("LOCATION".equals(ner))
              color = locationColor;
            else if ("ORGANIZATION".equals(ner))
              color = organizationColor;
            else if ("DATE".equals(ner))
              color = dateColor;
            else if ("NUMBER".equals(ner))
              color = numberColor;
            else if ("Negative".equals(sentiment))
              color = sentimentNegativeColor;
            else if ("Positive".equals(sentiment))
              color = sentimentPositiveColor;

            if (color == null)
              return;

            StringBuilder buf = new StringBuilder();
            buf.append("<loc pos=\"").append(begin).append("\" len=\"").append(end-begin).append("\"");
            buf.append(" color=\"").append(color).append("\"");

            // xml attributes below are not used by Highlighter - we add them only for the reference
            if (word != null) buf.append(" word=\"").append(StringEscapeUtils.escapeHtml4(word)).append("\"");
            if (pos != null) buf.append(" nlp-pos=\"").append(pos).append("\"");
            if (ner != null) buf.append(" nlp-ner=\"").append(ner).append("\"");
            if (sentiment != null) buf.append(" nlp-sentiment=\"").append(sentiment).append("\"");

            buf.append(" />\n");
            output.write(buf.toString());

            highlightedItems.incrementAndGet();
          }
        }
      };
      saxParser.parse(inputFile, xmlHandler);

      System.out.println("Done processing " + inputFile);
      System.out.println("Highlighted " + highlightedItems.get() + " items");
    }
    finally {
      if (out != null) {
        out.write("</highlight>\n</body>\n</xml>");
        out.close();
      }
    }
  }

  public String getPersonColor() {
    return personColor;
  }

  public void setPersonColor(String personColor) {
    this.personColor = personColor;
  }

  public String getOrganizationColor() {
    return organizationColor;
  }

  public void setOrganizationColor(String organizationColor) {
    this.organizationColor = organizationColor;
  }

  public String getLocationColor() {
    return locationColor;
  }

  public void setLocationColor(String locationColor) {
    this.locationColor = locationColor;
  }

  public String getDateColor() {
    return dateColor;
  }

  public void setDateColor(String dateColor) {
    this.dateColor = dateColor;
  }

  public String getNumberColor() {
    return numberColor;
  }

  public void setNumberColor(String numberColor) {
    this.numberColor = numberColor;
  }

  public String getSentimentNegativeColor() {
    return sentimentNegativeColor;
  }

  public void setSentimentNegativeColor(String sentimentNegativeColor) {
    this.sentimentNegativeColor = sentimentNegativeColor;
  }

  public String getSentimentPositiveColor() {
    return sentimentPositiveColor;
  }

  public void setSentimentPositiveColor(String sentimentPositiveColor) {
    this.sentimentPositiveColor = sentimentPositiveColor;
  }
}

