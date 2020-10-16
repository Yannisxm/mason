package sim.field.proxy;
import sim.engine.*;
import sim.field.*;
import sim.engine.*;
import sim.field.storage.*;
import sim.field.partitioning.*;
import java.rmi.*;
import java.rmi.registry.*;
import java.util.*;

/**
	A subclass of SimState designed to visualize remote distributed models.
	
	You set up this SimState something like this:
	
	<pre>
	
	public class MySimStateProxy extends SimStateProxy
		{
		public MySimStateProxy()
			{
			setRegistryHost("my.host.org");
			setRegistryPort(21242);
			}
		
		DoubleGrid2DProxy heat = new DoubleGrid2DProxy(1,1);	// width and height don't matter, they'll be changed
		DenseGrid2DProxy bugs = new DenseGrid2DProxy(1,1);		// width and height don't matter, they'll be changed
		
		public void start()
			{
			super.start();
			registerFieldProxy(heat);
			registerFieldProxy(bugs);
			}
		}
	
	</pre>

	... then you'd set up MASON visualization for these two fields as usual.

*/

public class SimStateProxy extends SimState
	{
	String host = "localhost";
	/** Returns the IP address of the distributed RMI registry.  You need to set this before start() is called. */
	public String getRegistryHost() { return host; }
	/** Sets the IP address of the distributed RMI registry.  You need to set this before start() is called. */
	public void setRegistryHost(String host) { this.host = host; }
	
	int port = 5000;
	/** Returns the IP address of the distributed RMI registry.  You need to set this before start() is called. */
	public int getRegistryPort() { return port; }
	/** Sets the IP address of the distributed RMI registry.  You need to set this before start() is called. */
	public void setRegistryPort(int port) { this.port = port; }
	
	/** Returns the string by which the visualization root (a VisualizationRoot instance) is registered with the Registry. */
	public final String getVisualizationRootString() { return "root"; }						// or whatever
	
	/** Returns the string by which a given visualization processor (a VisualizationProcessor instance) is registered with the Registry. */
	public final String VisualizationProcessorString(int pid) { return "proc" + pid; }		// or whatever
	
	// The registry proper
	Registry registry = null;
	// World bounds
	IntHyperRect worldBounds = null;
	// The visualization root
	VisualizationRoot visualizationRoot = null;
	// a cache of Visualization Processors so we don't keep querying for them
	VisualizationProcessor[] visualizationCache = null;
	// The number of pids.
	int numProcessors = 0;
	// which processor are we currently visualizing?
	int processor = 0;
	// The SimState's fields (on the MASON side), all field proxies.
	// These need to be in the same order as the order associated with the remote grids
	ArrayList<UpdatableProxy> fields = new ArrayList<UpdatableProxy>();
	
	/** Registers a field proxy with the SimState.  Each timestep or whatnot the proxy will get updated,
		which causes it to go out and load information remotely.  The order in which the fields are registered
		must be the same as the order associated with the remote grids' storage objects returned by the VisualizationProcessor. */
	public void registerFieldProxy(UpdatableProxy proxy)
		{
		fields.add(proxy);
		}
	
	/** Returns the registry */
	public Registry getRegistry()
		{
		return registry;
		}
	
	/** Returns the number of processors */
	public int getNumProcessors() { return numProcessors; }

	/** Sets the current processor to be visualized */
	public void setCurrentProcessor(int pid)
		{
		if (pid < 0 || pid > numProcessors) return;
		processor = pid;
		}
		
	/** Sets the current processor to be visualized */
	public int getCurrentProcessor() { return processor; }
	
	/** Returns the current Visualization Processor either cached or by fetching it remotely. */
	public VisualizationProcessor getVisualizationProcessor() throws RemoteException, NotBoundException
		{
		if (visualizationCache[processor] == null)
			{
			visualizationCache[processor] = (VisualizationProcessor)(registry.lookup(VisualizationProcessorString(processor)));
			}
		return visualizationCache[processor];
		}
		
	/** Fetches the requested storage from the current Visualization Processor. */
	public GridStorage getStorage(int storage) throws RemoteException, NotBoundException
		{
		return getVisualizationProcessor().getStorage(storage);
		}
		
	/** Fetches the halo bounds from the current Visualization Processor. */
	public IntHyperRect getBounds() throws RemoteException, NotBoundException
		{
		return getVisualizationProcessor().getBounds();
		}
		
	public void start()
		{
		super.start();
		try
			{
			// grab the registry and query it for basic information
			registry = LocateRegistry.getRegistry(getRegistryHost(), getRegistryPort());
			visualizationRoot = (VisualizationRoot)(registry.lookup(getVisualizationRootString()));
			worldBounds = visualizationRoot.getWorldBounds();
			numProcessors = visualizationRoot.getNumProcessors();
			
			// set up the cache
			visualizationCache = new VisualizationProcessor[numProcessors];

			// set up the field proxies to be updated.  We may wish to change the rate at which they're updated, dunno
			schedule.scheduleRepeating(new Steppable()
				{
				public void step(SimState state)
					{
					try
						{
						VisualizationProcessor vp = getVisualizationProcessor();
						vp.lock();
						for(int i = 0; i < fields.size(); i++)
							{
							fields.get(i).update(SimStateProxy.this, i);
							}
						vp.unlock();
						}
					catch (RemoteException ex)
						{
						ex.printStackTrace();
						}
					catch (NotBoundException ex)
						{
						ex.printStackTrace();
						}
					}
				});
			}
		catch (RemoteException ex)
			{
			ex.printStackTrace();
			// we're done
			}	
		catch (NotBoundException ex)
			{
			ex.printStackTrace();
			}	
		}
		
	public SimStateProxy(long seed)
		{
		super(seed);
		}
	}
	