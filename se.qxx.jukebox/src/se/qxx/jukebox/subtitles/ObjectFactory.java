//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.4-2 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2015.12.07 at 11:09:22 PM CET 
//


package se.qxx.jukebox.subtitles;

import javax.xml.bind.annotation.XmlRegistry;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the se.qxx.jukebox.subtitles package. 
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
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: se.qxx.jukebox.subtitles
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link Subs }
     * 
     */
    public Subs createSubs() {
        return new Subs();
    }

    /**
     * Create an instance of {@link Subs.Movie }
     * 
     */
    public Subs.Movie createSubsMovie() {
        return new Subs.Movie();
    }

    /**
     * Create an instance of {@link Subs.Movie.Sub }
     * 
     */
    public Subs.Movie.Sub createSubsMovieSub() {
        return new Subs.Movie.Sub();
    }

}
