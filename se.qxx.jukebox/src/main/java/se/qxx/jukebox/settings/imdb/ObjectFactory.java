//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.4-2 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2015.12.26 at 10:53:39 PM CET 
//


package se.qxx.jukebox.settings.imdb;

import javax.xml.bind.annotation.XmlRegistry;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the se.qxx.jukebox.settings.imdb package. 
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
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: se.qxx.jukebox.settings.imdb
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link Imdb }
     * 
     */
    public Imdb createImdb() {
        return new Imdb();
    }

    /**
     * Create an instance of {@link Imdb.EpisodePatterns }
     * 
     */
    public Imdb.EpisodePatterns createImdbEpisodePatterns() {
        return new Imdb.EpisodePatterns();
    }

    /**
     * Create an instance of {@link Imdb.InfoPatterns }
     * 
     */
    public Imdb.InfoPatterns createImdbInfoPatterns() {
        return new Imdb.InfoPatterns();
    }

    /**
     * Create an instance of {@link Imdb.SearchPatterns }
     * 
     */
    public Imdb.SearchPatterns createImdbSearchPatterns() {
        return new Imdb.SearchPatterns();
    }

    /**
     * Create an instance of {@link Imdb.Title }
     * 
     */
    public Imdb.Title createImdbTitle() {
        return new Imdb.Title();
    }

    /**
     * Create an instance of {@link Imdb.Settings }
     * 
     */
    public Imdb.Settings createImdbSettings() {
        return new Imdb.Settings();
    }

    /**
     * Create an instance of {@link Imdb.DatePatterns }
     * 
     */
    public Imdb.DatePatterns createImdbDatePatterns() {
        return new Imdb.DatePatterns();
    }

    /**
     * Create an instance of {@link Imdb.EpisodePatterns.EpisodePattern }
     * 
     */
    public Imdb.EpisodePatterns.EpisodePattern createImdbEpisodePatternsEpisodePattern() {
        return new Imdb.EpisodePatterns.EpisodePattern();
    }

    /**
     * Create an instance of {@link Imdb.InfoPatterns.InfoPattern }
     * 
     */
    public Imdb.InfoPatterns.InfoPattern createImdbInfoPatternsInfoPattern() {
        return new Imdb.InfoPatterns.InfoPattern();
    }

    /**
     * Create an instance of {@link Imdb.SearchPatterns.SearchResultPattern }
     * 
     */
    public Imdb.SearchPatterns.SearchResultPattern createImdbSearchPatternsSearchResultPattern() {
        return new Imdb.SearchPatterns.SearchResultPattern();
    }

    /**
     * Create an instance of {@link Imdb.Title.TitleResultPattern }
     * 
     */
    public Imdb.Title.TitleResultPattern createImdbTitleTitleResultPattern() {
        return new Imdb.Title.TitleResultPattern();
    }

}