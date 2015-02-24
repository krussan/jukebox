//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.4-2 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2015.02.24 at 08:39:31 PM CET 
//


package se.qxx.jukebox.settings.parser;

import javax.xml.bind.annotation.XmlRegistry;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the se.qxx.jukebox.settings.parser package. 
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {


    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: se.qxx.jukebox.settings.parser
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link Parser }
     * 
     */
    public Parser createParser() {
        return new Parser();
    }

    /**
     * Create an instance of {@link Parser.Keywords }
     * 
     */
    public Parser.Keywords createParserKeywords() {
        return new Parser.Keywords();
    }

    /**
     * Create an instance of {@link WordType }
     * 
     */
    public WordType createWordType() {
        return new WordType();
    }

    /**
     * Create an instance of {@link Parser.Keywords.Type }
     * 
     */
    public Parser.Keywords.Type createParserKeywordsType() {
        return new Parser.Keywords.Type();
    }

    /**
     * Create an instance of {@link Parser.Keywords.Format }
     * 
     */
    public Parser.Keywords.Format createParserKeywordsFormat() {
        return new Parser.Keywords.Format();
    }

    /**
     * Create an instance of {@link Parser.Keywords.Sound }
     * 
     */
    public Parser.Keywords.Sound createParserKeywordsSound() {
        return new Parser.Keywords.Sound();
    }

    /**
     * Create an instance of {@link Parser.Keywords.Language }
     * 
     */
    public Parser.Keywords.Language createParserKeywordsLanguage() {
        return new Parser.Keywords.Language();
    }

    /**
     * Create an instance of {@link Parser.Keywords.Other }
     * 
     */
    public Parser.Keywords.Other createParserKeywordsOther() {
        return new Parser.Keywords.Other();
    }

}
