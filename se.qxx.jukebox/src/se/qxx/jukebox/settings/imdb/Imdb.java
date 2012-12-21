//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vJAXB 2.1.10 in JDK 6 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2012.12.21 at 01:22:03 fm CET 
//


package se.qxx.jukebox.settings.imdb;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="settings">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;attribute name="sleepSecondsMin" use="required" type="{http://www.w3.org/2001/XMLSchema}int" />
 *                 &lt;attribute name="sleepSecondsMax" use="required" type="{http://www.w3.org/2001/XMLSchema}int" />
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *         &lt;element name="title">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="titleResultPattern" minOccurs="0">
 *                     &lt;complexType>
 *                       &lt;complexContent>
 *                         &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                           &lt;sequence>
 *                             &lt;element name="blockPattern" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *                             &lt;element name="recordPattern" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *                           &lt;/sequence>
 *                           &lt;attribute name="groupBlock" type="{http://www.w3.org/2001/XMLSchema}int" />
 *                           &lt;attribute name="groupRecordTitle" type="{http://www.w3.org/2001/XMLSchema}int" />
 *                           &lt;attribute name="groupRecordCountry" type="{http://www.w3.org/2001/XMLSchema}int" />
 *                         &lt;/restriction>
 *                       &lt;/complexContent>
 *                     &lt;/complexType>
 *                   &lt;/element>
 *                 &lt;/sequence>
 *                 &lt;attribute name="preferredLanguage" type="{http://www.w3.org/2001/XMLSchema}string" />
 *                 &lt;attribute name="useOriginalIfExists" type="{http://www.w3.org/2001/XMLSchema}boolean" />
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *         &lt;element name="searchUrl" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="searchPatterns" minOccurs="0">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="searchResultPattern" maxOccurs="unbounded" minOccurs="0">
 *                     &lt;complexType>
 *                       &lt;complexContent>
 *                         &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                           &lt;sequence>
 *                             &lt;element name="blockPattern" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *                             &lt;element name="recordPattern" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *                           &lt;/sequence>
 *                           &lt;attribute name="name" type="{http://www.w3.org/2001/XMLSchema}string" />
 *                           &lt;attribute name="enabled" use="required" type="{http://www.w3.org/2001/XMLSchema}boolean" />
 *                           &lt;attribute name="priority" use="required" type="{http://www.w3.org/2001/XMLSchema}int" />
 *                           &lt;attribute name="groupBlock" use="required" type="{http://www.w3.org/2001/XMLSchema}int" />
 *                           &lt;attribute name="groupRecordUrl" use="required" type="{http://www.w3.org/2001/XMLSchema}int" />
 *                           &lt;attribute name="groupRecordYear" use="required" type="{http://www.w3.org/2001/XMLSchema}int" />
 *                         &lt;/restriction>
 *                       &lt;/complexContent>
 *                     &lt;/complexType>
 *                   &lt;/element>
 *                 &lt;/sequence>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *         &lt;element name="infoPatterns" minOccurs="0">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="infoPattern" maxOccurs="unbounded" minOccurs="0">
 *                     &lt;complexType>
 *                       &lt;complexContent>
 *                         &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                           &lt;sequence>
 *                             &lt;element name="regex" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *                           &lt;/sequence>
 *                           &lt;attribute name="type" type="{}InfoPatternType" />
 *                           &lt;attribute name="group" type="{http://www.w3.org/2001/XMLSchema}int" />
 *                         &lt;/restriction>
 *                       &lt;/complexContent>
 *                     &lt;/complexType>
 *                   &lt;/element>
 *                 &lt;/sequence>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "settings",
    "title",
    "searchUrl",
    "searchPatterns",
    "infoPatterns"
})
@XmlRootElement(name = "imdb")
public class Imdb {

    @XmlElement(required = true)
    protected Imdb.Settings settings;
    @XmlElement(required = true)
    protected Imdb.Title title;
    @XmlElement(required = true)
    protected String searchUrl;
    protected Imdb.SearchPatterns searchPatterns;
    protected Imdb.InfoPatterns infoPatterns;

    /**
     * Gets the value of the settings property.
     * 
     * @return
     *     possible object is
     *     {@link Imdb.Settings }
     *     
     */
    public Imdb.Settings getSettings() {
        return settings;
    }

    /**
     * Sets the value of the settings property.
     * 
     * @param value
     *     allowed object is
     *     {@link Imdb.Settings }
     *     
     */
    public void setSettings(Imdb.Settings value) {
        this.settings = value;
    }

    /**
     * Gets the value of the title property.
     * 
     * @return
     *     possible object is
     *     {@link Imdb.Title }
     *     
     */
    public Imdb.Title getTitle() {
        return title;
    }

    /**
     * Sets the value of the title property.
     * 
     * @param value
     *     allowed object is
     *     {@link Imdb.Title }
     *     
     */
    public void setTitle(Imdb.Title value) {
        this.title = value;
    }

    /**
     * Gets the value of the searchUrl property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSearchUrl() {
        return searchUrl;
    }

    /**
     * Sets the value of the searchUrl property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSearchUrl(String value) {
        this.searchUrl = value;
    }

    /**
     * Gets the value of the searchPatterns property.
     * 
     * @return
     *     possible object is
     *     {@link Imdb.SearchPatterns }
     *     
     */
    public Imdb.SearchPatterns getSearchPatterns() {
        return searchPatterns;
    }

    /**
     * Sets the value of the searchPatterns property.
     * 
     * @param value
     *     allowed object is
     *     {@link Imdb.SearchPatterns }
     *     
     */
    public void setSearchPatterns(Imdb.SearchPatterns value) {
        this.searchPatterns = value;
    }

    /**
     * Gets the value of the infoPatterns property.
     * 
     * @return
     *     possible object is
     *     {@link Imdb.InfoPatterns }
     *     
     */
    public Imdb.InfoPatterns getInfoPatterns() {
        return infoPatterns;
    }

    /**
     * Sets the value of the infoPatterns property.
     * 
     * @param value
     *     allowed object is
     *     {@link Imdb.InfoPatterns }
     *     
     */
    public void setInfoPatterns(Imdb.InfoPatterns value) {
        this.infoPatterns = value;
    }


    /**
     * <p>Java class for anonymous complex type.
     * 
     * <p>The following schema fragment specifies the expected content contained within this class.
     * 
     * <pre>
     * &lt;complexType>
     *   &lt;complexContent>
     *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *       &lt;sequence>
     *         &lt;element name="infoPattern" maxOccurs="unbounded" minOccurs="0">
     *           &lt;complexType>
     *             &lt;complexContent>
     *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *                 &lt;sequence>
     *                   &lt;element name="regex" type="{http://www.w3.org/2001/XMLSchema}string"/>
     *                 &lt;/sequence>
     *                 &lt;attribute name="type" type="{}InfoPatternType" />
     *                 &lt;attribute name="group" type="{http://www.w3.org/2001/XMLSchema}int" />
     *               &lt;/restriction>
     *             &lt;/complexContent>
     *           &lt;/complexType>
     *         &lt;/element>
     *       &lt;/sequence>
     *     &lt;/restriction>
     *   &lt;/complexContent>
     * &lt;/complexType>
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "infoPattern"
    })
    public static class InfoPatterns {

        protected List<Imdb.InfoPatterns.InfoPattern> infoPattern;

        /**
         * Gets the value of the infoPattern property.
         * 
         * <p>
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the infoPattern property.
         * 
         * <p>
         * For example, to add a new item, do as follows:
         * <pre>
         *    getInfoPattern().add(newItem);
         * </pre>
         * 
         * 
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link Imdb.InfoPatterns.InfoPattern }
         * 
         * 
         */
        public List<Imdb.InfoPatterns.InfoPattern> getInfoPattern() {
            if (infoPattern == null) {
                infoPattern = new ArrayList<Imdb.InfoPatterns.InfoPattern>();
            }
            return this.infoPattern;
        }


        /**
         * <p>Java class for anonymous complex type.
         * 
         * <p>The following schema fragment specifies the expected content contained within this class.
         * 
         * <pre>
         * &lt;complexType>
         *   &lt;complexContent>
         *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
         *       &lt;sequence>
         *         &lt;element name="regex" type="{http://www.w3.org/2001/XMLSchema}string"/>
         *       &lt;/sequence>
         *       &lt;attribute name="type" type="{}InfoPatternType" />
         *       &lt;attribute name="group" type="{http://www.w3.org/2001/XMLSchema}int" />
         *     &lt;/restriction>
         *   &lt;/complexContent>
         * &lt;/complexType>
         * </pre>
         * 
         * 
         */
        @XmlAccessorType(XmlAccessType.FIELD)
        @XmlType(name = "", propOrder = {
            "regex"
        })
        public static class InfoPattern {

            @XmlElement(required = true)
            protected String regex;
            @XmlAttribute
            protected InfoPatternType type;
            @XmlAttribute
            protected Integer group;

            /**
             * Gets the value of the regex property.
             * 
             * @return
             *     possible object is
             *     {@link String }
             *     
             */
            public String getRegex() {
                return regex;
            }

            /**
             * Sets the value of the regex property.
             * 
             * @param value
             *     allowed object is
             *     {@link String }
             *     
             */
            public void setRegex(String value) {
                this.regex = value;
            }

            /**
             * Gets the value of the type property.
             * 
             * @return
             *     possible object is
             *     {@link InfoPatternType }
             *     
             */
            public InfoPatternType getType() {
                return type;
            }

            /**
             * Sets the value of the type property.
             * 
             * @param value
             *     allowed object is
             *     {@link InfoPatternType }
             *     
             */
            public void setType(InfoPatternType value) {
                this.type = value;
            }

            /**
             * Gets the value of the group property.
             * 
             * @return
             *     possible object is
             *     {@link Integer }
             *     
             */
            public Integer getGroup() {
                return group;
            }

            /**
             * Sets the value of the group property.
             * 
             * @param value
             *     allowed object is
             *     {@link Integer }
             *     
             */
            public void setGroup(Integer value) {
                this.group = value;
            }

        }

    }


    /**
     * <p>Java class for anonymous complex type.
     * 
     * <p>The following schema fragment specifies the expected content contained within this class.
     * 
     * <pre>
     * &lt;complexType>
     *   &lt;complexContent>
     *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *       &lt;sequence>
     *         &lt;element name="searchResultPattern" maxOccurs="unbounded" minOccurs="0">
     *           &lt;complexType>
     *             &lt;complexContent>
     *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *                 &lt;sequence>
     *                   &lt;element name="blockPattern" type="{http://www.w3.org/2001/XMLSchema}string"/>
     *                   &lt;element name="recordPattern" type="{http://www.w3.org/2001/XMLSchema}string"/>
     *                 &lt;/sequence>
     *                 &lt;attribute name="name" type="{http://www.w3.org/2001/XMLSchema}string" />
     *                 &lt;attribute name="enabled" use="required" type="{http://www.w3.org/2001/XMLSchema}boolean" />
     *                 &lt;attribute name="priority" use="required" type="{http://www.w3.org/2001/XMLSchema}int" />
     *                 &lt;attribute name="groupBlock" use="required" type="{http://www.w3.org/2001/XMLSchema}int" />
     *                 &lt;attribute name="groupRecordUrl" use="required" type="{http://www.w3.org/2001/XMLSchema}int" />
     *                 &lt;attribute name="groupRecordYear" use="required" type="{http://www.w3.org/2001/XMLSchema}int" />
     *               &lt;/restriction>
     *             &lt;/complexContent>
     *           &lt;/complexType>
     *         &lt;/element>
     *       &lt;/sequence>
     *     &lt;/restriction>
     *   &lt;/complexContent>
     * &lt;/complexType>
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "searchResultPattern"
    })
    public static class SearchPatterns {

        protected List<Imdb.SearchPatterns.SearchResultPattern> searchResultPattern;

        /**
         * Gets the value of the searchResultPattern property.
         * 
         * <p>
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the searchResultPattern property.
         * 
         * <p>
         * For example, to add a new item, do as follows:
         * <pre>
         *    getSearchResultPattern().add(newItem);
         * </pre>
         * 
         * 
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link Imdb.SearchPatterns.SearchResultPattern }
         * 
         * 
         */
        public List<Imdb.SearchPatterns.SearchResultPattern> getSearchResultPattern() {
            if (searchResultPattern == null) {
                searchResultPattern = new ArrayList<Imdb.SearchPatterns.SearchResultPattern>();
            }
            return this.searchResultPattern;
        }


        /**
         * <p>Java class for anonymous complex type.
         * 
         * <p>The following schema fragment specifies the expected content contained within this class.
         * 
         * <pre>
         * &lt;complexType>
         *   &lt;complexContent>
         *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
         *       &lt;sequence>
         *         &lt;element name="blockPattern" type="{http://www.w3.org/2001/XMLSchema}string"/>
         *         &lt;element name="recordPattern" type="{http://www.w3.org/2001/XMLSchema}string"/>
         *       &lt;/sequence>
         *       &lt;attribute name="name" type="{http://www.w3.org/2001/XMLSchema}string" />
         *       &lt;attribute name="enabled" use="required" type="{http://www.w3.org/2001/XMLSchema}boolean" />
         *       &lt;attribute name="priority" use="required" type="{http://www.w3.org/2001/XMLSchema}int" />
         *       &lt;attribute name="groupBlock" use="required" type="{http://www.w3.org/2001/XMLSchema}int" />
         *       &lt;attribute name="groupRecordUrl" use="required" type="{http://www.w3.org/2001/XMLSchema}int" />
         *       &lt;attribute name="groupRecordYear" use="required" type="{http://www.w3.org/2001/XMLSchema}int" />
         *     &lt;/restriction>
         *   &lt;/complexContent>
         * &lt;/complexType>
         * </pre>
         * 
         * 
         */
        @XmlAccessorType(XmlAccessType.FIELD)
        @XmlType(name = "", propOrder = {
            "blockPattern",
            "recordPattern"
        })
        public static class SearchResultPattern {

            @XmlElement(required = true)
            protected String blockPattern;
            @XmlElement(required = true)
            protected String recordPattern;
            @XmlAttribute
            protected String name;
            @XmlAttribute(required = true)
            protected boolean enabled;
            @XmlAttribute(required = true)
            protected int priority;
            @XmlAttribute(required = true)
            protected int groupBlock;
            @XmlAttribute(required = true)
            protected int groupRecordUrl;
            @XmlAttribute(required = true)
            protected int groupRecordYear;

            /**
             * Gets the value of the blockPattern property.
             * 
             * @return
             *     possible object is
             *     {@link String }
             *     
             */
            public String getBlockPattern() {
                return blockPattern;
            }

            /**
             * Sets the value of the blockPattern property.
             * 
             * @param value
             *     allowed object is
             *     {@link String }
             *     
             */
            public void setBlockPattern(String value) {
                this.blockPattern = value;
            }

            /**
             * Gets the value of the recordPattern property.
             * 
             * @return
             *     possible object is
             *     {@link String }
             *     
             */
            public String getRecordPattern() {
                return recordPattern;
            }

            /**
             * Sets the value of the recordPattern property.
             * 
             * @param value
             *     allowed object is
             *     {@link String }
             *     
             */
            public void setRecordPattern(String value) {
                this.recordPattern = value;
            }

            /**
             * Gets the value of the name property.
             * 
             * @return
             *     possible object is
             *     {@link String }
             *     
             */
            public String getName() {
                return name;
            }

            /**
             * Sets the value of the name property.
             * 
             * @param value
             *     allowed object is
             *     {@link String }
             *     
             */
            public void setName(String value) {
                this.name = value;
            }

            /**
             * Gets the value of the enabled property.
             * 
             */
            public boolean isEnabled() {
                return enabled;
            }

            /**
             * Sets the value of the enabled property.
             * 
             */
            public void setEnabled(boolean value) {
                this.enabled = value;
            }

            /**
             * Gets the value of the priority property.
             * 
             */
            public int getPriority() {
                return priority;
            }

            /**
             * Sets the value of the priority property.
             * 
             */
            public void setPriority(int value) {
                this.priority = value;
            }

            /**
             * Gets the value of the groupBlock property.
             * 
             */
            public int getGroupBlock() {
                return groupBlock;
            }

            /**
             * Sets the value of the groupBlock property.
             * 
             */
            public void setGroupBlock(int value) {
                this.groupBlock = value;
            }

            /**
             * Gets the value of the groupRecordUrl property.
             * 
             */
            public int getGroupRecordUrl() {
                return groupRecordUrl;
            }

            /**
             * Sets the value of the groupRecordUrl property.
             * 
             */
            public void setGroupRecordUrl(int value) {
                this.groupRecordUrl = value;
            }

            /**
             * Gets the value of the groupRecordYear property.
             * 
             */
            public int getGroupRecordYear() {
                return groupRecordYear;
            }

            /**
             * Sets the value of the groupRecordYear property.
             * 
             */
            public void setGroupRecordYear(int value) {
                this.groupRecordYear = value;
            }

        }

    }


    /**
     * <p>Java class for anonymous complex type.
     * 
     * <p>The following schema fragment specifies the expected content contained within this class.
     * 
     * <pre>
     * &lt;complexType>
     *   &lt;complexContent>
     *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *       &lt;attribute name="sleepSecondsMin" use="required" type="{http://www.w3.org/2001/XMLSchema}int" />
     *       &lt;attribute name="sleepSecondsMax" use="required" type="{http://www.w3.org/2001/XMLSchema}int" />
     *     &lt;/restriction>
     *   &lt;/complexContent>
     * &lt;/complexType>
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "")
    public static class Settings {

        @XmlAttribute(required = true)
        protected int sleepSecondsMin;
        @XmlAttribute(required = true)
        protected int sleepSecondsMax;

        /**
         * Gets the value of the sleepSecondsMin property.
         * 
         */
        public int getSleepSecondsMin() {
            return sleepSecondsMin;
        }

        /**
         * Sets the value of the sleepSecondsMin property.
         * 
         */
        public void setSleepSecondsMin(int value) {
            this.sleepSecondsMin = value;
        }

        /**
         * Gets the value of the sleepSecondsMax property.
         * 
         */
        public int getSleepSecondsMax() {
            return sleepSecondsMax;
        }

        /**
         * Sets the value of the sleepSecondsMax property.
         * 
         */
        public void setSleepSecondsMax(int value) {
            this.sleepSecondsMax = value;
        }

    }


    /**
     * <p>Java class for anonymous complex type.
     * 
     * <p>The following schema fragment specifies the expected content contained within this class.
     * 
     * <pre>
     * &lt;complexType>
     *   &lt;complexContent>
     *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *       &lt;sequence>
     *         &lt;element name="titleResultPattern" minOccurs="0">
     *           &lt;complexType>
     *             &lt;complexContent>
     *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *                 &lt;sequence>
     *                   &lt;element name="blockPattern" type="{http://www.w3.org/2001/XMLSchema}string"/>
     *                   &lt;element name="recordPattern" type="{http://www.w3.org/2001/XMLSchema}string"/>
     *                 &lt;/sequence>
     *                 &lt;attribute name="groupBlock" type="{http://www.w3.org/2001/XMLSchema}int" />
     *                 &lt;attribute name="groupRecordTitle" type="{http://www.w3.org/2001/XMLSchema}int" />
     *                 &lt;attribute name="groupRecordCountry" type="{http://www.w3.org/2001/XMLSchema}int" />
     *               &lt;/restriction>
     *             &lt;/complexContent>
     *           &lt;/complexType>
     *         &lt;/element>
     *       &lt;/sequence>
     *       &lt;attribute name="preferredLanguage" type="{http://www.w3.org/2001/XMLSchema}string" />
     *       &lt;attribute name="useOriginalIfExists" type="{http://www.w3.org/2001/XMLSchema}boolean" />
     *     &lt;/restriction>
     *   &lt;/complexContent>
     * &lt;/complexType>
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "titleResultPattern"
    })
    public static class Title {

        protected Imdb.Title.TitleResultPattern titleResultPattern;
        @XmlAttribute
        protected String preferredLanguage;
        @XmlAttribute
        protected Boolean useOriginalIfExists;

        /**
         * Gets the value of the titleResultPattern property.
         * 
         * @return
         *     possible object is
         *     {@link Imdb.Title.TitleResultPattern }
         *     
         */
        public Imdb.Title.TitleResultPattern getTitleResultPattern() {
            return titleResultPattern;
        }

        /**
         * Sets the value of the titleResultPattern property.
         * 
         * @param value
         *     allowed object is
         *     {@link Imdb.Title.TitleResultPattern }
         *     
         */
        public void setTitleResultPattern(Imdb.Title.TitleResultPattern value) {
            this.titleResultPattern = value;
        }

        /**
         * Gets the value of the preferredLanguage property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getPreferredLanguage() {
            return preferredLanguage;
        }

        /**
         * Sets the value of the preferredLanguage property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setPreferredLanguage(String value) {
            this.preferredLanguage = value;
        }

        /**
         * Gets the value of the useOriginalIfExists property.
         * 
         * @return
         *     possible object is
         *     {@link Boolean }
         *     
         */
        public Boolean isUseOriginalIfExists() {
            return useOriginalIfExists;
        }

        /**
         * Sets the value of the useOriginalIfExists property.
         * 
         * @param value
         *     allowed object is
         *     {@link Boolean }
         *     
         */
        public void setUseOriginalIfExists(Boolean value) {
            this.useOriginalIfExists = value;
        }


        /**
         * <p>Java class for anonymous complex type.
         * 
         * <p>The following schema fragment specifies the expected content contained within this class.
         * 
         * <pre>
         * &lt;complexType>
         *   &lt;complexContent>
         *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
         *       &lt;sequence>
         *         &lt;element name="blockPattern" type="{http://www.w3.org/2001/XMLSchema}string"/>
         *         &lt;element name="recordPattern" type="{http://www.w3.org/2001/XMLSchema}string"/>
         *       &lt;/sequence>
         *       &lt;attribute name="groupBlock" type="{http://www.w3.org/2001/XMLSchema}int" />
         *       &lt;attribute name="groupRecordTitle" type="{http://www.w3.org/2001/XMLSchema}int" />
         *       &lt;attribute name="groupRecordCountry" type="{http://www.w3.org/2001/XMLSchema}int" />
         *     &lt;/restriction>
         *   &lt;/complexContent>
         * &lt;/complexType>
         * </pre>
         * 
         * 
         */
        @XmlAccessorType(XmlAccessType.FIELD)
        @XmlType(name = "", propOrder = {
            "blockPattern",
            "recordPattern"
        })
        public static class TitleResultPattern {

            @XmlElement(required = true)
            protected String blockPattern;
            @XmlElement(required = true)
            protected String recordPattern;
            @XmlAttribute
            protected Integer groupBlock;
            @XmlAttribute
            protected Integer groupRecordTitle;
            @XmlAttribute
            protected Integer groupRecordCountry;

            /**
             * Gets the value of the blockPattern property.
             * 
             * @return
             *     possible object is
             *     {@link String }
             *     
             */
            public String getBlockPattern() {
                return blockPattern;
            }

            /**
             * Sets the value of the blockPattern property.
             * 
             * @param value
             *     allowed object is
             *     {@link String }
             *     
             */
            public void setBlockPattern(String value) {
                this.blockPattern = value;
            }

            /**
             * Gets the value of the recordPattern property.
             * 
             * @return
             *     possible object is
             *     {@link String }
             *     
             */
            public String getRecordPattern() {
                return recordPattern;
            }

            /**
             * Sets the value of the recordPattern property.
             * 
             * @param value
             *     allowed object is
             *     {@link String }
             *     
             */
            public void setRecordPattern(String value) {
                this.recordPattern = value;
            }

            /**
             * Gets the value of the groupBlock property.
             * 
             * @return
             *     possible object is
             *     {@link Integer }
             *     
             */
            public Integer getGroupBlock() {
                return groupBlock;
            }

            /**
             * Sets the value of the groupBlock property.
             * 
             * @param value
             *     allowed object is
             *     {@link Integer }
             *     
             */
            public void setGroupBlock(Integer value) {
                this.groupBlock = value;
            }

            /**
             * Gets the value of the groupRecordTitle property.
             * 
             * @return
             *     possible object is
             *     {@link Integer }
             *     
             */
            public Integer getGroupRecordTitle() {
                return groupRecordTitle;
            }

            /**
             * Sets the value of the groupRecordTitle property.
             * 
             * @param value
             *     allowed object is
             *     {@link Integer }
             *     
             */
            public void setGroupRecordTitle(Integer value) {
                this.groupRecordTitle = value;
            }

            /**
             * Gets the value of the groupRecordCountry property.
             * 
             * @return
             *     possible object is
             *     {@link Integer }
             *     
             */
            public Integer getGroupRecordCountry() {
                return groupRecordCountry;
            }

            /**
             * Sets the value of the groupRecordCountry property.
             * 
             * @param value
             *     allowed object is
             *     {@link Integer }
             *     
             */
            public void setGroupRecordCountry(Integer value) {
                this.groupRecordCountry = value;
            }

        }

    }

}
