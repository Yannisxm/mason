/*
  Copyright 2019 by Sean Luke and George Mason University
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/

package sim.engine;

/**
 * Changes constructor to set Stoppable. Overrides stop() to set Stoppable as
 * null
 */
public class DistributedTentativeStep extends TentativeStep {
	private static final long serialVersionUID = 1;

	public DistributedTentativeStep(final Steppable step) {
		super(step);
		if (step instanceof Stopping) {
			((Stopping) step).setStoppable(this);
		} else
			throw new RuntimeException("DistributedTentativeStep built on a non-Stopping Steppable");
	}

	public void stop() {
		synchronized (lock) {
			if (step != null) {
				((Stopping) step).setStoppable(null);
			}
			super.stop();
		}
	}
}
