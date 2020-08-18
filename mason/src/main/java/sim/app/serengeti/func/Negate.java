/*
  Copyright 2006 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/


package sim.app.serengeti.func;
import ec.*;
import sim.app.serengeti.*;
import ec.gp.*;
import ec.util.*;

public class Negate extends GPNode
    {
    public String toString() { return "-"; }

    public int expectedChildren() { return 1; }

    public void eval(final EvolutionState state,
        final int thread,
        final GPData input,
        final ADFStack stack,
        final GPIndividual individual,
        final Problem problem)
        {
        double resultx;
        double resulty;
        
        SerengetiData rd = ((SerengetiData)(input));

        children[0].eval(state,thread,input,stack,individual,problem);
       	rd.x = 0 - rd.x;
        rd.y = 0 - rd.y;
        }
    }



