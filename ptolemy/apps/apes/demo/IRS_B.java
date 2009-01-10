package ptolemy.apps.apes.demo;

import java.util.Iterator;
import java.util.List;

import ptolemy.actor.Actor;
import ptolemy.actor.CompositeActor;
import ptolemy.actor.NoRoomException;
import ptolemy.apps.apes.CPUScheduler;
import ptolemy.apps.apes.CTask;
import ptolemy.apps.apes.InterruptServiceRoutine;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;

public class IRS_B extends InterruptServiceRoutine {

    public IRS_B() {  
    }

    public IRS_B(Workspace workspace) {
        super(workspace);  
    }

    public IRS_B(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);  
    }
     
    
    
    protected void _callCMethod() { 
        long period;
        System.out.println(this.getName() + ".fire() - Time: " + getDirector().getModelTime());
        try {
            accessPointCallback(-1.0, 0.0, "");
            cpuScheduler.ActivateTask(2);
            accessPointCallback(0.2, -1.0,"");
        } catch (NoRoomException e) { 
            e.printStackTrace();
        } catch (IllegalActionException e) { 
            e.printStackTrace();
        }          
    }

    public void accessPointCallback(double extime, double minNextTime, String syscall) throws NoRoomException,
    IllegalActionException {
        // TODO Auto-generated method stub
        super.accessPointCallback(extime, minNextTime, syscall);
    }
    
    private CPUScheduler cpuScheduler; 
    
    
    public void initialize() throws IllegalActionException {
        super.initialize();
        CompositeActor compositeActor = (CompositeActor) getContainer();
        List entities = compositeActor.entityList();
        for (Iterator it = entities.iterator(); it.hasNext();) {
            Object entity = it.next();
            if (entity instanceof Actor) {
                Actor actor = (Actor) entity;
                if (actor instanceof CPUScheduler) {
                    cpuScheduler = (CPUScheduler) actor;
                    return;
                }
            }
        }
    }
 
}
