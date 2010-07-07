package ptdb.common.dto;

import java.util.List;

/**
 * 
 * The attribute that need to be stored or retrieved from the database.
 * 
 * <p> It is used as a data transfer object. </p>
 * 
 * @author Yousef Alsaeed
 * @version $Id$
 * @since Ptolemy II 8.1
 * @Pt.ProposedRating Red (yalsaeed)
 * @Pt.AcceptedRating Red (yalsaeed)
 * 
 */
public class XMLDBAttribute {

    /**
     * Construct an XMLDBAttribute instance with the given attribute name and 
     * type. 
     * 
     * @param attributeName The name of the attribute to create.
     * @param attributeType The type of the attribute to create. 
     */
    public XMLDBAttribute(String attributeName, String attributeType) {
        this._attributeName = attributeName;
        this._attributeType = attributeType;
    }

    /**
     * Construct a XMLDBAttribute instance with the given attribute name.
     * 
     * @param attributeName The name for the given attribute.
     * @param attributeType The type for the given attribute.
     * @param attributeId The it for the given attribute.
     */
    public XMLDBAttribute(String attributeName, String attributeType,
            String attributeId) {
        this._attributeName = attributeName;
        this._attributeType = attributeType;
        this._attributeId = attributeId;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////
    /** String Type. */
    public static final String ATTRIBUTE_TYPE_STRING = "String";

    /** Boolean Type. */
    public static final String ATTRIBUTE_TYPE_BOOLEAN = "Boolean";

    /** List Type. */
    public static final String ATTRIBUTE_TYPE_LIST = "List";

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /**
     * Return the attribute id.
     * @return The attribute id.
     * 
     * @see #setAttributeId
     */
    public String getAttributeId() {
        return _attributeId;
    }

    /**
     * Return the attribute name.
     * @return The attribute name.
     * 
     */
    public String getAttributeName() {
        return _attributeName;
    }

    /**
     * Return the attribute type.
     * @return A string representation of the attribute type.
     */
    public String getAttributeType() {
        return _attributeType;
    }

    /**
     * Return the attribute value.
     * @return A string representation of the attribute value.
     * 
     * @see #setAttributeValue
     */
    public List<String> getAttributeValues() {

        if (_attributeType.equals(XMLDBAttribute.ATTRIBUTE_TYPE_LIST)) {
            return _attributeValues;
        } else {
            return null;
        }

    }

    /**
     * Get the attribute list values without checking the attribute's type. 
     * 
     * @return The list of items for the attribute list type. 
     */
    public List<String> getAttributeValuesPlain() {
        return _attributeValues;
    }

    /**
     * Set the attribute Id.
     * @param attributeId the attribute id to be set.
     * 
     * @see #getAttributeId
     */
    public void setAttributeId(String attributeId) {
        this._attributeId = attributeId;
    }

    /**
     * Set the name of the attribute. 
     * 
     * @param attributName The name of the attribute. 
     */
    public void setAttributeName(String attributName) {
        _attributeName = attributName;
    }

    /**
     * Set the type of this attribute. 
     * 
     * @param attributeType The type to set to this attribute. 
     */
    public void setAttributeType(String attributeType) {
        _attributeType = attributeType;
    }

    /**
     * Set the attribute list value only when the attribute type is list.
     * @param attributeValues The list of attribute values.
     * 
     * @see #getAttributeValues
     */
    public void setAttributeValue(List<String> attributeValues) {

        if (_attributeType.equals(XMLDBAttribute.ATTRIBUTE_TYPE_LIST)) {
            _attributeValues = attributeValues;
        }
    }

    /**
     * Set the list values to the attribute regardless of the attribute type. 
     * 
     * @param attributeValues The list values to be set to this attribute. 
     */
    public void setAttributeValuesPlain(List<String> attributeValues) {
        _attributeValues = attributeValues;
    }

    /**
     * Get the String representation of this object information. 
     * 
     * @return String representation of the attribute information. 
     */
    @Override
    public String toString() {

        return super.toString() + "@Name:" + _attributeName + "@Type:"
                + _attributeType;
    }

    /**
     * Create an XML string representation of XMLDBAttribute object.
     * @return The XML string representation of XMLDBAttribute object.
     */
    public String getAttributeXMLStringFormat() {

        String attributeNode = "<attribute id='" + _attributeId + "'"
                + " name='" + _attributeName + "' type='" + _attributeType
                + "'>";

        if (_attributeType.equalsIgnoreCase(XMLDBAttribute.ATTRIBUTE_TYPE_LIST)) {

            if (_attributeValues != null && _attributeValues.size() > 0) {
                for (int i = 0; i < _attributeValues.size(); i++) {
                    attributeNode = attributeNode + "<item name='"
                            + _attributeValues.get(i).toString() + "'/>";
                }
            }
        }

        attributeNode = attributeNode + "</attribute>";

        return attributeNode;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** Attribute id. */
    private String _attributeId;

    /** Attribute name. */
    private String _attributeName;

    /** The type of the attribute in a string. */
    private String _attributeType;

    /** The value of the attribute in a string. */
    private List<String> _attributeValues;

}
