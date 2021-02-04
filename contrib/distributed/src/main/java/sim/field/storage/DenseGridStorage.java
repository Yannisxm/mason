package sim.field.storage;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.function.IntFunction;

import sim.engine.DObject;
import sim.util.*;

/**
 * internal local storage for distributed grids.
 *
 * @param <T> Type of objects to store
 */
public class DenseGridStorage<T extends DObject> extends GridStorage<T, Int2D> {
	private static final long serialVersionUID = 1L;

	public ArrayList<T>[] storage;

	/**
	 * Should we remove bags in the field if they have been emptied, and let them
	 * GC, or should we keep them around?
	 */
	public boolean removeEmptyBags = true;

	public DenseGridStorage(final IntRect2D shape) {
		super(shape);
		clear();
	}

	public String toString() {
		final int[] size = shape.getSizes();
		final ArrayList<T>[] array = storage;
		final StringBuffer buf = new StringBuffer(
				String.format("ObjectGridStorage<%s>-%s\n", array.getClass().getSimpleName(), shape));

		for (int i = 0; i < size[0]; i++) {
			for (int j = 0; j < size[1]; j++)
				buf.append(String.format(" %8s ", array[i * size[1] + j]));
			buf.append("\n");
		}

		return buf.toString();
	}

	public Serializable pack(final MPIParam mp) {
		final ArrayList<T>[] objs = new ArrayList[mp.size]; // alloc.apply(mp.size);
		final ArrayList<T>[] stor = storage;
		int curr = 0;

		for (final IntRect2D rect : mp.rects)
			for (final Int2D p : rect.getPointList())
				objs[curr++] = stor[getFlatIdx(p)];

		return objs;
	}

	public int unpack(final MPIParam mp, final Serializable buf) {
		final ArrayList<T>[] stor = (ArrayList<T>[]) storage;
		final ArrayList<T>[] objs = (ArrayList<T>[]) buf;
		int curr = 0;

		for (final IntRect2D rect : mp.rects)
			for (final Int2D p : rect.getPointList())
				stor[getFlatIdx(p)] = objs[curr++];

		return curr;
	}



	///// GRIDSTORAGE GUNK

	public void addObject(Int2D p, T t) {
		final ArrayList<T>[] array = storage;
		final int idx = getFlatIdx(p);

		if (array[idx] == null)
			array[idx] = new ArrayList<T>();

		array[idx].add(t);
	}

	public T getObject(Int2D p, long id) {
		ArrayList<T> ts = storage[getFlatIdx(p)];
		if (ts != null)
			{
			for (T t : ts)
				if (t.ID() == id)
					return t;
			}
		return null;
	}

	public ArrayList<T> getAllObjects(Int2D p) {
		return storage[getFlatIdx(p)];
	}

	boolean removeFast(ArrayList<T> list, int pos)
		{
		int top = list.size() - 1;
		if (top != pos)
			list.set(pos, list.get(top));
		return list.remove(top) != null;
		}

/*
	boolean removeFast(ArrayList<T> list, T t)
		{
		int pos = list.indexOf(t);
		if (pos >= 0)
			return removeFast(list, pos);
		else return (pos >= 0);
		}
*/

	public boolean removeObject(Int2D p, long id) {
		final ArrayList<T>[] array = storage;
		final int idx = getFlatIdx(p);
		boolean result = false;

		if (array[idx] != null)
			{
			for (int i = 0; i < array[idx].size(); i++) 
				{
				T t = array[idx].get(i);
				if (t.ID() == id) 
					{
					result = removeFast(array[idx], i);
					if (array[idx].size() == 0 && removeEmptyBags)
						array[idx] = null;
					break;
					}
				}
			}
		return result;
	}

	public void clear(Int2D p) {
		final ArrayList<T>[] array = storage;
		final int idx = getFlatIdx(p);

		if (array[idx] != null) {
			if (removeEmptyBags)
				array[idx] = null;
			else
				array[idx].clear();
		}
	}

	public void clear() {
		storage = new ArrayList[shape.getArea()];
	}
}
