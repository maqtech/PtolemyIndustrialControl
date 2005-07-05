/* An actor that reads an array of images.   */


package ptolemy.domains.gr.lib.vr;


import java.awt.Image;
import java.lang.String;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;


import ij.ImagePlus;
import ij.ImageStack;
import ij.process.ColorProcessor;
import ij.plugin.StackEditor;





import ptolemy.actor.TypedIOPort;
import ptolemy.kernel.CompositeEntity;

import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.actor.TypedAtomicActor;
import ptolemy.domains.sdf.lib.SDFTransformer;
import ptolemy.data.AWTImageToken;
import ptolemy.data.ObjectToken;
import ptolemy.data.StringToken;
import ptolemy.data.Token;
import ptolemy.data.IntToken;
import ptolemy.data.type.ArrayType;
import ptolemy.data.type.BaseType;
import ptolemy.actor.parameters.FilePortParameter;
import ptolemy.actor.parameters.ParameterPort;
import ptolemy.data.expr.Parameter;
import ptolemy.graph.Inequality;
import ptolemy.graph.InequalityTerm;

//////////////////////////////////////////////////////////////////////////
////StackReader
/**
   An actor that reads an array of images.

   @see ptolemy.actor.lib.medicalimaging

   @author T. Crawford
   @version
   @since
   @Pt.ProposedRating Red
   @Pt.AcceptedRating Red

*/public class StackToImage extends SDFTransformer{
    /**Construct an actor with the given container and name.
     * @param container The container
     * @param name The name of this actor
     * @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     * @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */

    public StackToImage(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);



        input_tokenConsumptionRate.setExpression("1");
        output_tokenProductionRate.setExpression("50");



        xResolution = new Parameter(this, "xResolution");
        xResolution.setExpression("256");
        xResolution.setTypeEquals(BaseType.INT);

        yResolution = new Parameter(this, "yResolution");
        yResolution.setExpression("256");
        yResolution.setTypeEquals(BaseType.INT);

        stackSize = new Parameter(this, "stackSize");
        stackSize.setExpression("50");
        stackSize.setTypeEquals(BaseType.INT);



    }

    ////////////////////////////////////////////////////////////////////
    ////////               ports and parameters                  ////////

    //public FilePortParameter input;
    public Parameter xResolution;
    public Parameter yResolution;
    public Parameter stackSize;


    ////////////////////////////////////////////////////////////////////
    ////////                public methods                     ////////


    /** Output the data read in the prefire.
     *  @exception IllegalActionException If there's no director.
     */
    public void fire() throws IllegalActionException {
        super.fire();
        _index++;
        ObjectToken objectToken = (ObjectToken)input.get(0);
        _currentImagePlus = (ImagePlus)objectToken.getValue();
        for(int i = 0; i< _stackSize; i++){
            //_currentImagePlus.setSlice(_index);
            _currentImagePlus.setSlice(i);
            //System.out.println("Output Slice " + _index);
            System.out.println("Output Slice " + i);
            _image = _currentImagePlus.getImage();
            // _imagePlus = new ImagePlus("Image Stack", _imageStack);
            // System.out.println("stackSize = " + _imageStack.getSize());

            output.broadcast(new AWTImageToken(_image));
        }
    }


    public void initialize() throws IllegalActionException
    {
        // _parameterPort =  input.getPort();
        _xResolution = ((IntToken)xResolution.getToken()).intValue();
        _yResolution = ((IntToken)yResolution.getToken()).intValue();
        _stackSize = ((IntToken)stackSize.getToken()).intValue();

    }

    /*public boolean prefire() throws IllegalActionException {
      ObjectToken objectToken = (ObjectToken)input.get(0);
      _currentImagePlus = (ImagePlus)objectToken.getValue();
      Token rateToken = input_tokenConsumptionRate.getToken();
      int required = ((IntToken) rateToken).intValue();

      // Derived classes may convert the input port to a multiport.
      for (int i = 0; i < input.getWidth(); i++) {
      if (!input.hasToken(i, required)) {
      if (_debugging) {
      _debug("Called prefire(), which returns false");
      }

      return false;
      }
      }

      return super.prefire();

      }*/

    public boolean postfire() throws IllegalActionException{
        if (!_stopRequested && _index < _stackSize){
            if (_debugging) {
                _debug("Called postfire(), which returns true");
            }
            return true;
        }else{
            if (_debugging) {
                _debug("Called postfire(), which returns false");
            }
            return false;
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private members                   ////

    //Image that is readin
    private ImagePlus _imagePlus;

    private ImagePlus _currentImagePlus;

    //Image that is readin
    private ColorProcessor _colorProcessor;


    //Image that is readin
    private ImageStack _imageStack;


    // Image that is read in.
    private Image _image;

    private int _stackSize;

    private int _xResolution;

    private int _yResolution;

    private int _index = 0;
}
