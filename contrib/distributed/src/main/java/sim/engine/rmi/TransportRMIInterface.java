package sim.engine.rmi;

import java.io.Serializable;
import java.rmi.Remote;
import java.rmi.RemoteException;

import sim.engine.*;
import sim.util.NumberND;

/**
 * Implemented by fields that will be used for RMI
 *
 *
 * @param <P> The Type of PointND to use
 * @param <T> The Type of Object in the field
 */
public interface TransportRMIInterface<T extends Serializable, P> extends Remote {

	/**
	 * Used internally for RMI
	 *
	 * @param p       location
	 * @param promise
	 *
	 * @return item(s) stored on the point p if and only if p is local to the remote
	 *         field
	 *
	 * @throws RemoteException If the point requested is not local to the remote
	 *                         field
	 */
	public abstract void getRMI(P p, Promised promise) throws RemoteException;

	public abstract void getRMI(NumberND p, long id, Promised promise) throws RemoteException;

	/**
	 * Used internally for RMI
	 *
	 * @param p location
	 * @param t Object
	 *
	 * @throws RemoteException If the point requested is not local to the remote
	 *                         field
	 */
	public abstract void addRMI(P p, T t) throws RemoteException;

	/**
	 * Used internally for RMI
	 *
	 * @param p location
	 * @param t Object
	 *
	 * @throws RemoteException If the point requested is not local to the remote
	 *                         field
	 */
	public abstract void removeRMI(P p, long id) throws RemoteException;

	/**
	 * Used internally for RMI
	 *
	 * @param p location
	 *
	 * @throws RemoteException If the point requested is not local to the remote
	 *                         field
	 */
	public abstract void removeRMI(P p) throws RemoteException;

//	/**
//	 * Used internally for RMI
//	 *
//	 * @param fromP
//	 * @param toP
//	 * @param t
//	 *
//	 * @throws RemoteException If the points are not local to the remote field
//	 */
//	public abstract void moveRMI(final P fromP, final P toP, final T t) throws RemoteException;

}