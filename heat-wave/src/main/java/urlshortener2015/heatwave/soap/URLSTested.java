//
// Este archivo ha sido generado por la arquitectura JavaTM para la implantación de la referencia de enlace (JAXB) XML v2.2.8-b130911.1802 
// Visite <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Todas las modificaciones realizadas en este archivo se perderán si se vuelve a compilar el esquema de origen. 
// Generado el: 2016.01.03 a las 11:10:19 PM CET 
//


package urlshortener2015.heatwave.soap;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Clase Java para anonymous complex type.
 * 
 * <p>El siguiente fragmento de esquema especifica el contenido que se espera que haya en esta clase.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="TESTED" type="{http://www.w3.org/2001/XMLSchema}string"/>
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
    "tested"
})
@XmlRootElement(name = "URLSTested")
public class URLSTested {

    @XmlElement(name = "TESTED", required = true)
    protected String tested;

    /**
     * Obtiene el valor de la propiedad tested.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTESTED() {
        return tested;
    }

    /**
     * Define el valor de la propiedad tested.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTESTED(String value) {
        this.tested = value;
    }

}
