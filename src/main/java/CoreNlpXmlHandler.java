import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class CoreNlpXmlHandler extends DefaultHandler {

  private boolean inToken;
  private boolean inWord, inLemma, inOffsetBegin, inOffsetEnd, inPos, inNer, inSentiment;
  private String word, lemma, pos, ner, sentiment;
  private int begin, end = -1;
  private String content;

  protected void handleToken(String word, String lemma, int begin, int end, String pos, String ner, String sentiment) throws Exception {
    // extend class and override this method to do something with token data...
  }

  @Override
  public void characters(char[] ch, int start, int length) throws SAXException {
    content = new String(ch, start, length);
  }

  @Override
  public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
    if ("token".equals(qName)) {
      inToken = true;
    }
    else if (inToken) {

      if ("word".equals(qName)) {
        inWord = true;
      }
      else if ("lemma".equals(qName)) {
        inLemma = true;
      }
      else if ("CharacterOffsetBegin".equals(qName)) {
        inOffsetBegin = true;
      }
      else if ("CharacterOffsetEnd".equals(qName)) {
        inOffsetEnd = true;
      }
      else if ("POS".equals(qName)) {
        inPos = true;
      }
      else if ("NER".equals(qName)) {
        inNer = true;
      }
      else if ("sentiment".equals(qName)) {
        inSentiment = true;
      }
    }
  }

  @Override
  public void endElement(String uri, String localName, String qName) throws SAXException {
    if (inToken) {
      if (inWord && "word".equals(qName)) {
        word = content;
        inWord = false;
      }
      else if (inLemma && "lemma".equals(qName)) {
        lemma = content;
        inLemma = false;
      }
      else if (inOffsetBegin && "CharacterOffsetBegin".equals(qName)) {
        if (content != null) {
          begin = Integer.parseInt(content);
        }
        inOffsetBegin = false;
      }
      else if (inOffsetEnd && "CharacterOffsetEnd".equals(qName)) {
        if (content != null) {
          end = Integer.parseInt(content);
        }
        inOffsetEnd = false;
      }
      else if (inPos && "POS".equals(qName)) {
        pos = content;
        inPos = false;
      }
      else if (inNer && "NER".equals(qName)) {
        ner = content;
        inNer = false;
      }
      else if (inSentiment && "sentiment".equals(qName)) {
        sentiment = content;
        inSentiment = false;
      }
      else if ("token".equals(qName)) {
        try {
          handleToken(word, lemma, begin, end, pos, ner, sentiment);
        } catch (Exception e) {
          throw new SAXException(e);
        }
        word = lemma = pos = ner = sentiment = null;
        begin = end = -1;
        inToken = false;
      }
      content = null;
    }
  }

}
