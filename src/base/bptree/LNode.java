package base.bptree;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.util.ArrayList;
import java.util.Arrays;

import base.VarUtils;
import base.disk.Disk;
import base.disk.DiskSuperBlock;

/**
 * The class implementing the leaf node
 * 
 * @author Tommaso
 *
 * @param <Key>
 * @param <Value>
 */
public class LNode<Key extends Comparable<? super Key>, Value> extends Node<Key, Value> {
	// In some sense, the following casts are almost always illegal
	// (if Value was replaced with a real type other than Object,
	// the cast would fail); but they make our code simpler
	// by allowing us to pretend we have arrays of certain types.
	// They work because type erasure will erase the type variables.
	// It will break if we return it and other people try to use it.
	/** The maximum number of keys in the leaf node, M must be > 0 */
	public final int M;
	/**
	 * The maximum number of keys in inner node, the number of pointer is N+1, N
	 * must be > 2
	 */
	public final int N;
	/** The vector containing the values */
	public Value[] values;

	/**
	 * The class constructor
	 * 
	 * @param n
	 *            the maximum number of keys in inner node
	 * @param m
	 *            the maximum number of keys in the leaf node
	 */
	@SuppressWarnings("unchecked")
	public LNode(int m, int n) {
		super((Disk<Key, Value>) null);
		pid = -1;
		vid = -1;
		M = m;
		N = n;
		values = (Value[]) new Object[M];
		keys = (Key[]) new Comparable[M];
		keys = (Key[]) new Comparable[M];
		for (int i = 0; i < M; i++)
			keys[i] = (Key) Long.valueOf(0);
		values = (Value[]) new Object[M];
		try {
			byte[] b = null;
			//Create an empty string with length padlen
			b = String.format("%-"+VarUtils.padlen+"s", " ").getBytes("UTF-16");
			for (int i = 0; i < M; i++)
				values[i] = (Value) new String(b);
		}catch(Exception e){
			System.out.println("Errore in costruttore LNode vuoto");
		}
	}

	/**
	 * The class constructor
	 * 
	 * @param disk
	 *            the object used to write and read nodes from disk
	 */
	public LNode(Disk<Key, Value> disk) {
		super(disk);
		pid = -1;
		vid = -1;
		M = disk.getDiskSuperBlock().getM();
		N = disk.getDiskSuperBlock().getN();
		values = (Value[]) new Object[M];
		keys = (Key[]) new Comparable[M];
		keys = (Key[]) new Comparable[M];
		for (int i = 0; i < M; i++)
			keys[i] = (Key) Long.valueOf(0);
		values = (Value[]) new Object[M];
		try {
			byte[] b = null;
			//Create an empty string with length padlen
			b = String.format("%-"+VarUtils.padlen+"s", " ").getBytes("UTF-16");
			for (int i = 0; i < M; i++)
				values[i] = (Value) new String(b);
		}catch(Exception e){
			System.out.println("Errore in costruttore LNode vuoto");
		}
	}


	public LNode(LNode<Key, Value> a) {
		super(a);
		this.N = a.N;
		this.M = a.M;
		this.values = a.values.clone();
	}

	/**
	 * The class constructor
	 * 
	 * @param d
	 *            the object used to write and read nodes from disk
	 * @param n
	 *            the maximum number of keys in inner node
	 * @param m
	 *            the maximum number of keys in the leaf node
	 * @param nodeBytes
	 *            the bytes vector containing the node info
	 */
	public LNode(Disk<Key, Value> d, int m, int n, byte[] nodeBytes) {
		disk = d;
		M = m;
		N = n;
		keys = (Key[]) new Comparable[M];
		values = (Value[]) new Object[M];
		ByteBuffer bBuffer;
		bBuffer = ByteBuffer.wrap(nodeBytes, 0, 4);
		IntBuffer iBuffer = bBuffer.asIntBuffer();
		num = iBuffer.get();
		bBuffer = ByteBuffer.wrap(nodeBytes, 4, (M + 3) * 8);
		LongBuffer lBuffer = bBuffer.asLongBuffer();
		nonce = lBuffer.get();
		pid = lBuffer.get();
		vid = lBuffer.get();
		for (int i = 0; i < M; i++)
			keys[i] = (Key) new Long(lBuffer.get());
		/*
		* Extract from nodeBytes the single values
		* Since every string has the same length, we can easily calculate their position
		* Consider, ALWAYS, N = M
		* 4+(M+3)*8 is the number of bytes occupied by headers
		* M*VarUtils.padlen is the number of bytes occupied by values
		* */
		byte[] dst = new byte[VarUtils.padlen];
		bBuffer = ByteBuffer.wrap(nodeBytes, 4 + (M + 3) * 8,M*VarUtils.padlen);
		for (int i = 0; i < M; i++) {
			bBuffer.get(dst);
			values[i] = (Value) new String(dst);
		}

	}

	/**
	 * Returns the position where 'key' should be inserted in a leaf node that
	 * has the given keys.
	 * 
	 * @param key
	 */
	public int getLoc(Key key) {
		// Simple linear search. Faster for small values of N or M, binary
		// search would be faster for larger M / N
		for (int i = 0; i < num; i++) {
			if (keys[i].compareTo(key) >= 0) {
				return i;
			}
		}
		return num;
	}

	public Key getFirstKey() {
		return keys[0];
	}


	public Key splitNodeInMemory(ArrayList<Node<Key, ?>> ns, DiskSuperBlock sup) {
		int mid = this.num / 2;
		Key promoted_value = this.keys[mid];
		int i = 0;
		ns.add(new LNode<Key, Value>(this.M, this.N));
		ns.add(new LNode<Key, Value>(this.M, this.N));
		LNode<Key, Value> tref = (LNode<Key, Value>) ns.get(0);
		tref.pid = this.pid;
		tref.vid = this.vid;
		for (i = 0; i < mid; i++)
			tref.keys[i] = this.keys[i];
		tref.num = mid;
		for (i = 0; i < mid; i++)
			tref.values[i] = this.values[i];
		tref = (LNode<Key, Value>) ns.get(1);
		tref.pid = sup.getNewNodeOffsets(0);
		tref.vid = tref.pid;
		for (i = mid; i < num; i++)
			tref.keys[i - mid] = this.keys[i];
		tref.num = num - mid;
		for (i = mid; i < num; i++)
			tref.values[i - mid] = this.values[i];
		sup.setNewNodeOffset(0, tref.pid + this.getBytes().length);
		return promoted_value;
	}

	/**
	 * Splits the node if it's full into 2 nodes
	 * 
	 * @param key
	 *            to insert
	 * @param value
	 *            to insert
	 * 
	 * @return Split the object that contains the nodes obtained after the split
	 */
	public Split<Key, Value> insert(Key key, Value value) {
		// Simple linear search
		int i = getLoc(key);
		if (this.num == M) { // The node was full. We must split it
			int mid = (M + 1) / 2;
			int sNum = this.num - mid;
			LNode<Key, Value> sibling = new LNode<Key, Value>(disk);
			sibling.num = sNum;
			System.arraycopy(this.keys, mid, sibling.keys, 0, sNum);
			System.arraycopy(this.values, mid, sibling.values, 0, sNum);
			this.num = mid;
			if (i < mid) {
				// Inserted element goes to left sibling
				this.insertNonfull(key, value, i);
			} else {
				// Inserted element goes to right sibling
				sibling.insertNonfull(key, value, i - mid);
			}
			// Notify the parent about the split
			Split<Key, Value> result = new Split<Key, Value>(sibling.keys[0], // make
																				// the
																				// right's
																				// key
																				// >=
																				// result.key
					this, sibling);
			// WRITE NODE "sibling"
			disk.writeNode(sibling, true);
			// WRITE NODE "this"
			disk.writeNode(this, false);
			return result;
		} else {
			// The node was not full
			this.insertNonfull(key, value, i);
			// WRITE NODE "this"
			disk.writeNode(this, false);
			return null;
		}
	}

	/**
	 * Inserts the pair (key, value) into the node which is not full
	 * 
	 * @param key
	 * @param value
	 * @param idx
	 *            where inserts the new element
	 */
	private void insertNonfull(Key key, Value value, int idx) {
		// if (idx < M && keys[idx].equals(key)) {
		if (idx < num && keys[idx].equals(key)) {
			// We are inserting a duplicate value, simply overwrite the old one
			values[idx] = value;
		} else {
			// The key we are inserting is unique
			System.arraycopy(keys, idx, keys, idx + 1, num - idx);
			System.arraycopy(values, idx, values, idx + 1, num - idx);
			keys[idx] = key;
			values[idx] = value;
			num++;
		}
	}

	/**
	 * Checks if the node contains the given key
	 * 
	 * @param key
	 * 
	 * @return true if the key exists, false otherwise
	 */
	public boolean contains(Key key) {
		for (int i = 0; i < num; i++)
			if (keys[i].compareTo(key) == 0)
				return true;
		return false;
	}

	/**
	 * Returns the value associated with the given key
	 * 
	 * @param key
	 * 
	 * @return the value associated with the given key or -1 if the key doesn't
	 *         exist
	 */
	@SuppressWarnings("unchecked")
	public String getValue(Key key) {
		if (contains(key))
			for (int i = 0; i < num; i++)
				if (keys[i].compareTo(key) == 0)
					return values[i].toString();
		return "-1";
	}

	/**
	 * Prints all the information about the node
	 */
	public void printInfo() {
		System.out.println("Pid:" + pid + " Vid:" + vid + " Nonce:" + nonce + " Key_number:" + num);
		System.out.print(" Keys: ");
		for (int i = 0; i < num; i++) {
			System.out.print(keys[i] + " ");
		}
		for (int i = num; i < M; i++) {
			System.out.print("- ");
		}
		System.out.print("\n");
		System.out.print(" Values: ");
		for (int i = 0; i < num; i++) {
			System.out.print(values[i] + " ");
		}
		for (int i = num; i < M; i++) {
			System.out.print("- ");
		}
		System.out.print("\n");
	}

	/**
	 * Print pid, vid and the values list of the node
	 */
	public void print() {

		System.out.print(": Pid:" + pid + " Vid:" + vid + " Values: ");
		for (int i = 0; i < num; i++)
		{
			// .trim() used to remove the spaces we added for paddings
			if (i==125){
				System.out.println("STOP THIS!");
			}
			System.out.print((values[i].toString()).trim() + " ~ ");
		}
		for (int i = num; i < M; i++) {
			System.out.print("- ");
		}
		System.out.print("\n");
	}

	/**
	 * Returns the bytes vector containing the node information
	 * 
	 * @return byte[] the bytes vector containing the node information
	 */
	public byte[] getBytes() {
		byte[] nodeBytes = null;
		/**
		 * 1*8B : pid 1*8B : vid 1*8B : nonce N*8B : keys N*8B : values valBytes
		 * (sum of lengths) : number of keys
		 */

		// valBytes contains the length of all values
		int valBytes = 0;
		valBytes = VarUtils.padlen * M;

		nodeBytes = new byte[1 + (M + 3) * 8 + 4 + valBytes];

		ByteBuffer bBuffer;
		LongBuffer lBuffer;
		IntBuffer iBuffer;

		/* Left alone the first 5 bytes (used for storing the number of keys),
		* the next 24 are used to store the longs representing
		* a nonce, the nodes' pid ands vids
		* */
		bBuffer = ByteBuffer.wrap(nodeBytes, 1 + 4, 3 * 8);
		lBuffer = bBuffer.asLongBuffer();

		lBuffer.put(nonce);
		lBuffer.put(pid);
		lBuffer.put(vid);

		bBuffer = ByteBuffer.wrap(nodeBytes, 1 + 4 + (3 * 8), M * 8);
		lBuffer = bBuffer.asLongBuffer();

		for (int i = 0; i < num; i++) {
			lBuffer.put(Long.parseLong(keys[i].toString()));
		}

		// Copy exactly 100 characters and place them in the buffer
		bBuffer = ByteBuffer.wrap(nodeBytes, 1 + 4 + ((3 + M) * 8), valBytes);
		byte[] subarray = new byte[VarUtils.padlen];
		for (int i = 0; i < num; i++) {
			subarray = Arrays.copyOfRange(values[i].toString().getBytes(), 0, VarUtils.padlen);
			bBuffer.put(subarray);
			
		}

		bBuffer = ByteBuffer.wrap(nodeBytes, 1, 4);
		iBuffer = bBuffer.asIntBuffer();
		iBuffer.put(num);
		bBuffer = ByteBuffer.wrap(nodeBytes, 0, 1);
		bBuffer.put((byte) 0);

		return nodeBytes;
	}

	/**
	 * Returns a string containing the xml code that represent the node's
	 * information
	 * 
	 * @return a string containing the xml code that represent the node's
	 *         information
	 */
	public String getXml() {
		String nodeString = "<node ";
		nodeString = nodeString + "pid=\"" + pid + "\" ";
		nodeString = nodeString + "vid=\"" + vid + "\" ";
		nodeString = nodeString + "num=\"" + num + "\" ";
		nodeString = nodeString + "M=\"" + M + "\" ";
		nodeString = nodeString + "N=\"" + N + "\" ";
		nodeString = nodeString + "leaf=\"true\" >";
		for (int c = 0; c < num; c++) {
			nodeString = nodeString + "<key>" + keys[c] + "</key>";
		}
		for (int c = 0; c < num; c++) {
			nodeString = nodeString + "<value>" + values[c] + "</value>";
		}
		nodeString = nodeString + "</node>";
		return nodeString;
	}

	/**
	 * Sets the key at the given index
	 * 
	 * @param index
	 *            of the keys vector
	 * @param k
	 *            the new key value
	 */
	public void setKey(int index, Key k) {
		keys[index] = k;
	}

	/**
	 * Sets the value at the given index
	 * 
	 * @param index
	 *            of the values vector
	 * @param v
	 *            the new value
	 */
	public void setValue(int index, Value v) {
		values[index] = v;
	}

	public void insertNewValue(Key k, Value v) {
		if (this.num >= this.M)
			return;
		int i;
		for (i = 0; i < this.num && (this.keys[i].compareTo(k) < 0); i++)
			;
		for (int j = num; j >= i; j--) {
			this.keys[j] = this.keys[j - 1];
			this.values[j] = this.values[j - 1];
		}
		this.keys[i] = k;
		this.values[i] = v;
		this.num = this.num + 1;
	}

	public void updateKeyValue(Key targetKey, Value insertValue) {
		for (int i = 0; i < num; i++)
			if (keys[i].compareTo(targetKey) == 0) {
				this.values[i] = insertValue;
				break;
			}
	}

	public boolean hasChild(Node<Key, ?> c) {
		return false;
	}

	/**
	 * Returns the obvious fact that everything instanced as an LNode
	 * can't have children
	 * */
	public boolean hasChild(){return false;}
}