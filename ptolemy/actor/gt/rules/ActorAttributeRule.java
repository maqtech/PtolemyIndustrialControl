/*

 Copyright (c) 2003-2006 The Regents of the University of California.
 All rights reserved.
 Permission is hereby granted, without written agreement and without
 license or royalty fees, to use, copy, modify, and distribute this
 software and its documentation for any purpose, provided that the above
 copyright notice and the following two paragraphs appear in all copies
 of this software.

 IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY
 FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES
 ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF
 THE UNIVERSITY OF CALIFORNIA HAS BEEN ADVISED OF THE POSSIBILITY OF
 SUCH DAMAGE.

 THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
 PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY OF
 CALIFORNIA HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
 ENHANCEMENTS, OR MODIFICATIONS.

 PT_COPYRIGHT_VERSION_2
 COPYRIGHTENDKEY
 */
package ptolemy.actor.gt.rules;

import ptolemy.actor.gt.Rule;
import ptolemy.actor.gt.RuleAttribute;
import ptolemy.actor.gt.RuleValidationException;
import ptolemy.actor.gt.StringRuleAttribute;
import ptolemy.kernel.util.NamedObj;

//////////////////////////////////////////////////////////////////////////
//// ActorAttributeRule

/**

@author Thomas Huining Feng
@version $Id$
@since Ptolemy II 6.1
@Pt.ProposedRating Red (tfeng)
@Pt.AcceptedRating Red (tfeng)
*/
public class ActorAttributeRule extends Rule {

    public ActorAttributeRule() {
        this("");
    }

    public ActorAttributeRule(String values) {
        this(null, null, null);
        setValues(values);
    }

    public ActorAttributeRule(String name, String type, String value) {
        super(3);
        _attributeName = name;
        _attributeType = type;
        _attributeValue = value;
    }

    public RuleAttribute[] getRuleAttributes() {
        return _ATTRIBUTES;
    }

    public Object getValue(int index) {
        switch (index) {
        case 0:
            return _attributeName;
        case 1:
            return _attributeType;
        case 2:
            return _attributeValue;
        default:
            return null;
        }
    }

    public String getValues() {
        StringBuffer buffer = new StringBuffer();
        _encodeStringField(buffer, 0, _attributeName);
        _encodeStringField(buffer, 1, _attributeType);
        _encodeStringField(buffer, 2, _attributeValue);
        return buffer.toString();
    }

    public boolean isAttributeNameEnabled() {
        return isEnabled(0);
    }

    public boolean isAttributeTypeEnabled() {
        return isEnabled(0);
    }

    public boolean isAttributeValueEnabled() {
        return isEnabled(0);
    }

    public NamedObjMatchResult match(NamedObj object) {
        // FIXME: Check actor's attribute.
        return NamedObjMatchResult.UNAPPLICABLE;
    }

    public void setAttributeNameEnabled(boolean enabled) {
        setEnabled(0, enabled);
    }

    public void setAttributeTypeEnabled(boolean enabled) {
        setEnabled(1, enabled);
    }

    public void setAttributeValueEnabled(boolean enabled) {
        setEnabled(2, enabled);
    }

    public void setValue(int index, Object value) {
        switch (index) {
        case 0:
            _attributeName = (String) value;
            break;
        case 1:
            _attributeType = (String) value;
            break;
        case 2:
            _attributeValue = (String) value;
            break;
        }
    }

    public void setValues(String values) {
        FieldIterator fieldIterator = new FieldIterator(values);
        _attributeName = _decodeStringField(0, fieldIterator);
        _attributeType = _decodeStringField(1, fieldIterator);
        _attributeValue = _decodeStringField(2, fieldIterator);
    }

    public void validate() throws RuleValidationException {
    }

    private String _attributeName;

    private static final RuleAttribute[] _ATTRIBUTES = {
        new StringRuleAttribute("name", true),
        new StringRuleAttribute("type"),
        new StringRuleAttribute("value", true)
    };

    private String _attributeType;

    private String _attributeValue;
}
