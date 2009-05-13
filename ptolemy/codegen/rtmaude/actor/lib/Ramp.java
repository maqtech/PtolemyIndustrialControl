package ptolemy.codegen.rtmaude.actor.lib;

import java.util.Map;

import ptolemy.codegen.rtmaude.kernel.Entity;
import ptolemy.kernel.util.IllegalActionException;

public class Ramp extends Entity {

    public Ramp(ptolemy.actor.lib.Ramp component) {
        super(component);
    }
    
    @Override
    protected Map<String, String> _generateAttributeTerms()
            throws IllegalActionException {
        Map<String,String> atts = super._generateAttributeTerms();
        atts.put("init", "'init");
        atts.put("step", "'step");
        atts.put("count", "0");
        atts.put("output-value", "# 0");
        return atts;
    }
}