//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.4-2 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2015.03.20 at 10:20:37 PM CET 
//


package se.qxx.jukebox.settings.imdb;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for InfoPatternType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="InfoPatternType">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="title"/>
 *     &lt;enumeration value="poster"/>
 *     &lt;enumeration value="story"/>
 *     &lt;enumeration value="year"/>
 *     &lt;enumeration value="rating"/>
 *     &lt;enumeration value="genres"/>
 *     &lt;enumeration value="duration"/>
 *     &lt;enumeration value="director"/>
 *     &lt;enumeration value="seasons"/>
 *     &lt;enumeration value="value"/>
 *     &lt;enumeration value="firstAirDate"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "InfoPatternType")
@XmlEnum
public enum InfoPatternType {

    @XmlEnumValue("title")
    TITLE("title"),
    @XmlEnumValue("poster")
    POSTER("poster"),
    @XmlEnumValue("story")
    STORY("story"),
    @XmlEnumValue("year")
    YEAR("year"),
    @XmlEnumValue("rating")
    RATING("rating"),
    @XmlEnumValue("genres")
    GENRES("genres"),
    @XmlEnumValue("duration")
    DURATION("duration"),
    @XmlEnumValue("director")
    DIRECTOR("director"),
    @XmlEnumValue("seasons")
    SEASONS("seasons"),
    @XmlEnumValue("value")
    VALUE("value"),
    @XmlEnumValue("firstAirDate")
    FIRST_AIR_DATE("firstAirDate");
    private final String value;

    InfoPatternType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static InfoPatternType fromValue(String v) {
        for (InfoPatternType c: InfoPatternType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
