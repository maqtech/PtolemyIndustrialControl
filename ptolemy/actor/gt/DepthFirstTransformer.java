/* Model transformer based on graph isomorphism.

 Copyright (c) 1997-2005 The Regents of the University of California.
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
package ptolemy.actor.gt;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ptolemy.actor.AtomicActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.gt.data.FastLinkedList;
import ptolemy.actor.gt.data.Pair;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Port;
import ptolemy.kernel.Relation;
import ptolemy.kernel.util.NamedObj;

/**

 @author Thomas Huining Feng
 @version $Id$
 @since Ptolemy II 6.1
 @Pt.ProposedRating Red (tfeng)
 @Pt.AcceptedRating Red (tfeng)
 */
public class DepthFirstTransformer {

    public void match(CompositeActorMatcher lhsGraph, NamedObj hostGraph)
    throws SubgraphMatchingException {
        _match = new HashMap<NamedObj, NamedObj>();
        _lhsFrontier = new FastLinkedList<NamedObj>();
        _hostFrontier = new FastLinkedList<NamedObj>();

        _lhsFrontier.add(lhsGraph);
        _hostFrontier.add(hostGraph);

        if (_match(_lhsFrontier.getHead(), _hostFrontier.getHead())) {
            for (NamedObj lhsObject : _match.keySet()) {
                System.out.println(lhsObject.getName() + " : " +
                        _match.get(lhsObject).getName());
            }
        }
    }

    public NamedObj transform(NamedObj from, SingleRuleTransformer transformer)
    throws GraphTransformationException {
        CompositeActorMatcher leftHandSide = transformer.getLeftHandSide();
        match(leftHandSide, from);
        return null;
    }

    private AtomicActor _findFirstAtomicActor(CompositeEntity top,
            FastLinkedList<MarkedEntityList> markedList,
            Collection<NamedObj> excludedEntities) {
        List<?> entities = top.entityList(AtomicActor.class);
        if (!entities.isEmpty()) {
            int i = 0;
            for (Object entityObject : entities) {
                AtomicActor atomicEntity = (AtomicActor) entityObject;
                if (!excludedEntities.contains(atomicEntity)) {
                    markedList.add(new MarkedEntityList(entities, i));
                    return atomicEntity;
                }
                i++;
            }
        }

        entities = top.entityList(CompositeEntity.class);
        if (!entities.isEmpty()) {
            FastLinkedList<MarkedEntityList>.Entry tail = markedList.getTail();
            int i = 0;
            for (Object entityObject : entities) {
                CompositeEntity entity = (CompositeEntity) entityObject;
                if (!excludedEntities.contains(entity)) {
                    markedList.add(new MarkedEntityList(entities, i));
                    AtomicActor actor =
                        _findFirstAtomicActor(entity, markedList,
                                excludedEntities);
                    if (actor != null) {
                        return actor;
                    } else {
                        markedList.removeAllAfter(tail);
                    }
                }
                i++;
            }
        }
        return null;
    }

    private AtomicActor _findNextAtomicActor(CompositeEntity top,
            FastLinkedList<MarkedEntityList> markedList,
            Collection<NamedObj> excludedEntities) {
        if (markedList.isEmpty()) {
            return _findFirstAtomicActor(top, markedList, excludedEntities);
        } else {
            FastLinkedList<MarkedEntityList>.Entry entry = markedList.getTail();
            MarkedEntityList markedEntityList = entry.getValue();
            List<?> atomicEntityList = markedEntityList.getFirst();
            for (int index = markedEntityList.getSecond() + 1;
                   index < atomicEntityList.size(); index++) {
                AtomicActor atomicEntity =
                    (AtomicActor) atomicEntityList.get(index);
                if (!excludedEntities.contains(atomicEntity)) {
                    markedEntityList.setSecond(index);
                    return (AtomicActor) atomicEntity;
                }
            }

            entry = entry.getPrevious();
            while (entry != null) {
                markedEntityList = entry.getValue();
                List<?> compositeEntityList = markedEntityList.getFirst();
                for (int index = markedEntityList.getSecond() + 1;
                        index < compositeEntityList.size(); index++) {
                    CompositeEntity compositeEntity =
                        (CompositeEntity) compositeEntityList.get(index);
                    if (!excludedEntities.contains(compositeEntity)) {
                        markedList.removeAllAfter(entry);
                        AtomicActor atomicEntity =
                            _findFirstAtomicActor(compositeEntity, markedList,
                                    excludedEntities);
                        if (atomicEntity != null) {
                            return atomicEntity;
                        }
                    }
                }
                entry = entry.getPrevious();
            }
            return null;
        }
    }

    private boolean _match(FastLinkedList<NamedObj>.Entry lhsEntry,
            FastLinkedList<NamedObj>.Entry hostEntry) {
        if (lhsEntry == null) {
            return true;
        } else {
            // Arbitrarily pick an object in _lhsFrontier to match.
            NamedObj lhsObject = lhsEntry.getValue();
            while (hostEntry != null) {
                if (_tryToMatch(lhsObject, hostEntry.getValue())) {
                    return true;
                } else {
                    hostEntry = hostEntry.getNext();
                }
            }
            return false;
        }
    }

    private boolean _matchLoop(FastLinkedList<NamedObj>.Entry lhsStart,
            FastLinkedList<NamedObj>.Entry hostStart) {
        if (lhsStart == null) {
            return true;
        } else {
            FastLinkedList<NamedObj>.Entry lhsEntry = lhsStart;
            while (lhsEntry != null) {
                if (!_match(lhsEntry, hostStart)) {
                    return false;
                }
                lhsEntry = lhsEntry.getNext();
            }
            return true;
        }
    }

    private boolean _tryToMatch(NamedObj lhsObject, NamedObj hostObject) {
        if (_match.containsKey(lhsObject)) {
            return _match.get(lhsObject) == hostObject;
        } else if (_match.containsValue(hostObject)) {
            return false;
        } else if (lhsObject instanceof AtomicActor
                && hostObject instanceof AtomicActor) {
            return _tryToMatchAtomicActor((AtomicActor) lhsObject,
                    (AtomicActor) hostObject);
        } else if (lhsObject instanceof CompositeEntity
                && hostObject instanceof CompositeEntity) {
            return _tryToMatchCompositeEntity((CompositeEntity) lhsObject,
                    (CompositeEntity) hostObject);
        } else if (lhsObject instanceof Port
                && hostObject instanceof Port) {
            return _tryToMatchPort((Port) lhsObject, (Port) hostObject);
        } else if (lhsObject instanceof Relation
                && hostObject instanceof Relation) {
            return _tryToMatchRelation((Relation) lhsObject,
                    (Relation) hostObject);
        } else {
            return false;
        }
    }

    private boolean _tryToMatchAtomicActor(AtomicActor lhsActor,
            AtomicActor hostActor) {

        FastLinkedList<NamedObj>.Entry lhsTail = _lhsFrontier.getTail();
        FastLinkedList<NamedObj>.Entry hostTail = _hostFrontier.getTail();

        _match.put(lhsActor, hostActor);

        for (Object portObject : lhsActor.portList()) {
            Port port = (Port) portObject;
            if (!_match.containsKey(port)) {
                _lhsFrontier.add(port);
            }
        }

        for (Object portObject : hostActor.portList()) {
            Port port = (Port) portObject;
            if (!_match.containsValue(port)) {
                _hostFrontier.add(port);
            }
        }

        if (_matchLoop(lhsTail.getNext(), hostTail.getNext())) {
            return true;
        } else {
            _match.remove(lhsActor);
            _lhsFrontier.removeAllAfter(lhsTail);
            _hostFrontier.removeAllAfter(hostTail);
            return false;
        }
    }

    private boolean _tryToMatchCompositeEntity(CompositeEntity lhsEntity,
            CompositeEntity hostEntity) {

        FastLinkedList<MarkedEntityList> lhsMarkedList =
            new FastLinkedList<MarkedEntityList>();
        AtomicActor lhsNextActor =
            _findFirstAtomicActor(lhsEntity, lhsMarkedList, _match.keySet());

        if (lhsNextActor == null) {
            return true;
        } else {
            FastLinkedList<NamedObj>.Entry lhsTail = _lhsFrontier.getTail();
            FastLinkedList<NamedObj>.Entry hostTail = _hostFrontier.getTail();

            FastLinkedList<MarkedEntityList> hostMarkedList =
                new FastLinkedList<MarkedEntityList>();
            AtomicActor hostNextActor = _findFirstAtomicActor(hostEntity,
                    hostMarkedList, _match.values());

            while (hostNextActor != null) {
                _lhsFrontier.add(lhsNextActor);
                _hostFrontier.add(hostNextActor);

                if (_match(lhsTail.getNext(), hostTail.getNext())) {
                    return true;
                } else {
                    _hostFrontier.removeAllAfter(hostTail);
                    _lhsFrontier.removeAllAfter(lhsTail);
                    hostNextActor = _findNextAtomicActor(hostEntity,
                            hostMarkedList, _match.values());
                }
            }
            return false;
        }
    }

    private boolean _tryToMatchPort(Port lhsPort, Port hostPort) {
        
        if (!_checkPortMatch(lhsPort, hostPort)) {
            return false;
        }

        FastLinkedList<NamedObj>.Entry lhsTail = _lhsFrontier.getTail();
        FastLinkedList<NamedObj>.Entry hostTail = _hostFrontier.getTail();

        _match.put(lhsPort, hostPort);

        for (Object relationObject : lhsPort.linkedRelationList()) {
            Relation relation = (Relation) relationObject;
            if (!_match.containsKey(relation)) {
                _lhsFrontier.add(relation);
            }
        }

        for (Object relationObject : hostPort.linkedRelationList()) {
            Relation relation = (Relation) relationObject;
            if (!_match.containsValue(relation)) {
                _hostFrontier.add(relation);
            }
        }

        if (_matchLoop(lhsTail.getNext(), hostTail.getNext())) {
            return true;
        } else {
            _match.remove(lhsPort);
            _lhsFrontier.removeAllAfter(lhsTail);
            _hostFrontier.removeAllAfter(hostTail);
            return false;
        }
    }
    
    private boolean _checkPortMatch(Port lhsPort, Port hostPort) {
        if (lhsPort instanceof TypedIOPort) {
            if (hostPort instanceof TypedIOPort) {
                TypedIOPort lhsTypedPort = (TypedIOPort) lhsPort;
                TypedIOPort hostTypedPort = (TypedIOPort) hostPort;
                if (lhsTypedPort.isInput() && !hostTypedPort.isInput()) {
                    return false;
                } else if (lhsTypedPort.isOutput()
                        && !hostTypedPort.isOutput()) {
                    return false;
                } else {
                    return true;
                }
            } else {
                return false;
            }
        } else {
            return true;
        }
    }

    private boolean _tryToMatchRelation(Relation lhsRelation,
            Relation hostRelation) {

        FastLinkedList<NamedObj>.Entry lhsTail = _lhsFrontier.getTail();
        FastLinkedList<NamedObj>.Entry hostTail = _hostFrontier.getTail();

        _match.put(lhsRelation, hostRelation);

        for (Object portObject : lhsRelation.linkedPortList()) {
            Port port = (Port) portObject;
            NamedObj container = port.getContainer();
            if (!_match.containsKey(container)) {
                _lhsFrontier.add(container);
            }
        }

        for (Object portObject : hostRelation.linkedPortList()) {
            Port port = (Port) portObject;
            NamedObj container = port.getContainer();
            if (!_match.containsValue(container)) {
                _hostFrontier.add(container);
            }
        }

        if (_matchLoop(lhsTail.getNext(), hostTail.getNext())) {
            return true;
        } else {
            _match.remove(lhsRelation);
            _hostFrontier.removeAllAfter(hostTail);
            _lhsFrontier.removeAllAfter(lhsTail);
            return false;
        }
    }

    private FastLinkedList<NamedObj> _hostFrontier;

    private FastLinkedList<NamedObj> _lhsFrontier;

    private Map<NamedObj, NamedObj> _match;

    private static class MarkedEntityList extends Pair<List<?>, Integer> {

        MarkedEntityList(List<?> list, Integer mark) {
            super(list, mark);
        }

        private static final long serialVersionUID = -8862333308144377821L;

    }
}
