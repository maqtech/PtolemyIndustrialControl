/*

@Copyright (c) 2007-2008 The Regents of the University of California.
All rights reserved.

Permission is hereby granted, without written agreement and without
license or royalty fees, to use, copy, modify, and distribute this
software and its documentation for any purpose, provided that the
above copyright notice and the following two paragraphs appear in all
copies of this software.

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

package ptolemy.actor.gt;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import ptolemy.actor.gt.data.CombinedCollection;
import ptolemy.data.Token;
import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Entity;
import ptolemy.kernel.Port;
import ptolemy.kernel.Relation;
import ptolemy.kernel.attributes.URIAttribute;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Nameable;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Workspace;
import ptolemy.moml.MoMLChangeRequest;
import ptolemy.moml.MoMLParser;

/**

 @author Thomas Huining Feng
 @version $Id$
 @since Ptolemy II 6.1
 @Pt.ProposedRating Red (tfeng)
 @Pt.AcceptedRating Red (tfeng)
 */
public class GTTools {

    public static NamedObj cleanupModel(NamedObj model)
    throws IllegalActionException {
        Workspace workspace = model.workspace();
        try {
            workspace.getReadAccess();
            MoMLParser parser = new MoMLParser(workspace);
            URIAttribute uriAttribute = (URIAttribute) model.getAttribute(
                    "_uri", URIAttribute.class);
            NamedObj newModel;
            if (uriAttribute != null) {
                newModel = parser.parse(uriAttribute.getURL(),
                        model.exportMoML());
            } else {
                newModel = parser.parse(model.exportMoML());
            }
            return newModel;
        } catch (Exception e) {
            throw new IllegalActionException(model, e,
                    "Unable to clean up model.");
        } finally {
            workspace.doneReading();
        }
    }

    public static void deepAddAttributes(NamedObj container,
            Class<? extends Attribute> attributeClass)
            throws IllegalArgumentException, InstantiationException,
            IllegalAccessException, InvocationTargetException {
        Constructor<?>[] constructors = attributeClass.getConstructors();
        for (Constructor<?> constructor : constructors) {
            Class<?>[] types = constructor.getParameterTypes();
            if (types.length == 2 && types[0].isInstance(container)
                    && types[1].equals(String.class)) {
                constructor.newInstance(container, container.uniqueName("_" +
                        attributeClass.getSimpleName()));
                break;
            }
        }
        for (Object child : getChildren(container, false, true, true, true)) {
            deepAddAttributes((NamedObj) child, attributeClass);
        }
    }

    public static void deepRemoveAttributes(NamedObj container,
            Class<? extends Attribute> attributeClass)
            throws IllegalActionException, NameDuplicationException {
        List<Object> attributes = new LinkedList<Object>(
                container.attributeList(attributeClass));
        for (Object attribute : attributes) {
            ((Attribute) attribute).setContainer(null);
        }
        for (Object child : getChildren(container, false, true, true, true)) {
            deepRemoveAttributes((NamedObj) child, attributeClass);
        }
    }

    public static Attribute findMatchingAttribute(Object object,
            Class<? extends Attribute> attributeClass,
            boolean searchContainers) {
        if (object instanceof NamedObj) {
            NamedObj namedObj = (NamedObj) object;
            List<?> list = namedObj.attributeList(attributeClass);
            if (!list.isEmpty()) {
                return (Attribute) list.get(0);
            } else if (searchContainers) {
                return findMatchingAttribute(namedObj.getContainer(),
                        attributeClass, searchContainers);
            }
        }
        return null;
    }

    public static NamedObj getChild(NamedObj object, String name,
            boolean allowAttribute, boolean allowPort, boolean allowEntity,
            boolean allowRelation) {
        NamedObj child = null;
        if (allowAttribute) {
            child = object.getAttribute(name);
        }
        if (child == null && allowPort && object instanceof Entity) {
            child = ((Entity) object).getPort(name);
        }
        if (object instanceof CompositeEntity) {
            if (child == null && allowEntity) {
                child = ((CompositeEntity) object).getEntity(name);
            }
            if (child == null && allowRelation) {
                child = ((CompositeEntity) object).getRelation(name);
            }
        }
        return child;
    }

    public static Collection<NamedObj> getChildren(NamedObj object,
            boolean includeAttributes, boolean includePorts,
            boolean includeEntities, boolean includeRelations) {
        Collection<NamedObj> collection = new CombinedCollection<NamedObj>();
        if (includeAttributes) {
            collection.addAll(object.attributeList());
        }
        if (includePorts && object instanceof Entity) {
            Entity entity = (Entity) object;
            collection.addAll(entity.portList());
        }
        if (object instanceof CompositeEntity) {
            CompositeEntity entity = (CompositeEntity) object;
            if (includeEntities) {
                collection.addAll(entity.entityList());
            }
            if (includeRelations) {
                collection.addAll(entity.relationList());
            }
        }
        return collection;
    }

    public static String getCodeFromObject(NamedObj object,
            NamedObj topContainer) {
        String replacementAbbrev = getObjectTypeAbbreviation(object);
        String name = topContainer == null ? object.getName() : object
                .getName(topContainer);
        return replacementAbbrev + name;
    }

    public static CompositeActorMatcher getContainingPatternOrReplacement(
            NamedObj entity) {
        Nameable parent = entity;
        while (parent != null && !(parent instanceof Pattern)
                && !(parent instanceof Replacement)) {
            parent = parent.getContainer();
        }
        return (CompositeActorMatcher) parent;
    }

    public static NamedObj getCorrespondingPatternObject(
            NamedObj replacementObject) {
        if (replacementObject instanceof Replacement) {
            return ((TransformationRule) replacementObject.getContainer())
                    .getPattern();
        }

        PatternObjectAttribute attribute = getPatternObjectAttribute(
                replacementObject);
        if (attribute == null) {
            return null;
        }

        CompositeActorMatcher container = getContainingPatternOrReplacement(
                replacementObject);
        if (container == null) {
            return null;
        }

        String patternObjectName = attribute.getExpression();
        if (patternObjectName.equals("")) {
            return null;
        }

        TransformationRule transformer = (TransformationRule) container
                .getContainer();
        Pattern pattern = transformer.getPattern();
        if (replacementObject instanceof Entity) {
            return pattern.getEntity(patternObjectName);
        } else if (replacementObject instanceof Relation) {
            return pattern.getRelation(patternObjectName);
        } else {
            return null;
        }
    }

    public static MoMLChangeRequest getDeletionChangeRequest(Object originator,
            NamedObj object) {
        String moml;
        if (object instanceof Attribute) {
            moml = "<deleteProperty name=\"" + object.getName() + "\"/>";
        } else if (object instanceof Entity) {
            moml = "<deleteEntity name=\"" + object.getName() + "\"/>";
        } else if (object instanceof Port) {
            moml = "<deletePort name=\"" + object.getName() + "\"/>";
        } else if (object instanceof Relation) {
            moml = "<deleteRelation name=\"" + object.getName() + "\"/>";
        } else {
            return null;
        }
        return new MoMLChangeRequest(originator, object.getContainer(), moml);
    }

    public static NamedObj getObjectFromCode(String code, NamedObj topContainer) {
        String abbreviation = code.substring(0, 2);
        String name = code.substring(2);
        if (abbreviation.equals("A:")) {
            return topContainer.getAttribute(name);
        } else if (abbreviation.equals("E:")
                && topContainer instanceof CompositeEntity) {
            return ((CompositeEntity) topContainer).getEntity(name);
        } else if (abbreviation.equals("P:")
                && topContainer instanceof Entity) {
            return ((Entity) topContainer).getPort(name);
        } else if (abbreviation.equals("R:")
                && topContainer instanceof CompositeEntity) {
            return ((CompositeEntity) topContainer).getRelation(name);
        } else {
            return null;
        }
    }

    public static String getObjectTypeAbbreviation(NamedObj object) {
        if (object instanceof Attribute) {
            return "A:";
        } else if (object instanceof Entity) {
            return "E:";
        } else if (object instanceof Port) {
            return "P:";
        } else if (object instanceof Relation) {
            return "R:";
        } else {
            return null;
        }
    }

    public static PatternObjectAttribute getPatternObjectAttribute(
            NamedObj object) {
        Attribute attribute = object.getAttribute("patternObject");
        if (attribute != null && attribute instanceof PatternObjectAttribute) {
            return (PatternObjectAttribute) attribute;
        } else {
            return null;
        }
    }

    public static boolean isCreated(Object object) {
        return findMatchingAttribute(object, CreationAttribute.class, true)
                != null;
    }

    public static boolean isIgnored(Object object) {
        return findMatchingAttribute(object, IgnoringAttribute.class, true)
                != null;
    }

    public static boolean isInPattern(NamedObj entity) {
        CompositeActorMatcher container = getContainingPatternOrReplacement(
                entity);
        return container != null && container instanceof Pattern;
    }

    public static boolean isInReplacement(NamedObj entity) {
        CompositeActorMatcher container = getContainingPatternOrReplacement(
                entity);
        return container != null && container instanceof Replacement;
    }

    public static boolean isNegated(Object object) {
        return findMatchingAttribute(object, NegationAttribute.class, true)
                != null;
    }

    public static boolean isOptional(Object object) {
        return findMatchingAttribute(object, OptionAttribute.class, false)
                != null;
    }

    public static boolean isPreserved(Object object) {
        return findMatchingAttribute(object, PreservationAttribute.class, true)
                != null;
    }

    /** Restore the values of the parameters that implement the {@link
     *  ValueIterator} interface within the root entity using the values
     *  recorded in the given table previously. The values are restored
     *  bottom-up.
     *
     *  @param root The root.
     *  @param records The table with the previously stored values.
     *  @throws IllegalActionException If the values of those parameters cannot
     *  be set.
     */
    public static void restoreValues(ComponentEntity root,
            Hashtable<ValueIterator, Token> records)
            throws IllegalActionException {
        if (root instanceof CompositeEntity) {
            for (Object entity : ((CompositeEntity) root).entityList()) {
                restoreValues((ComponentEntity) entity, records);
            }
        }
        List<?> iterators = root.attributeList(ValueIterator.class);
        ListIterator<?> listIterator = iterators.listIterator(iterators.size());
        while (listIterator.hasPrevious()) {
            ValueIterator iterator = (ValueIterator) listIterator.previous();
            Token value = records.get(iterator);
            if (value != null) {
                iterator.setToken(value);
                iterator.validate();
            }
        }
    }

    /** Save the values of parameters that implement the {@link ValueIterator}
     *  interface in the given records table, starting from the root entity.
     *
     *  @param root The root.
     *  @param records The table to store the values.
     *  @throws IllegalActionException If the values of those parameters cannot
     *  be obtained.
     */
    public static void saveValues(ComponentEntity root,
            Hashtable<ValueIterator, Token> records)
            throws IllegalActionException {
        List<?> iterators = root.attributeList(ValueIterator.class);
        for (Object iteratorObject : iterators) {
            ValueIterator iterator = (ValueIterator) iteratorObject;
            records.put(iterator, iterator.getToken());
        }
        if (root instanceof CompositeEntity) {
            for (Object entity : ((CompositeEntity) root).entityList()) {
                saveValues((ComponentEntity) entity, records);
            }
        }
    }
}
