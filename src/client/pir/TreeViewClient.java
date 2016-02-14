package client.pir;

import java.io.FileOutputStream;
import java.util.*;
import java.io.*;

import base.bptree.INode;
import base.bptree.Node;

public class TreeViewClient<Key extends Comparable<? super Key>, Value> {
    
	private long pid;
	private long num;
	private ArrayList<Key> keys;
	private ArrayList<Value> values;
	private ArrayList<TreeViewClient<Key, Value>> childrenNodes;
	
	TreeViewClient(long pid) {
		this.pid = pid;
		num = 0;
		keys = new ArrayList<Key>();
		values = new ArrayList<Value>();
		childrenNodes = new ArrayList<TreeViewClient<Key, Value>>();
		
	}

	TreeViewClient(long pid, long num, Key[] keys, Collection<TreeViewClient<Key, Value>> subtree) {
		this(pid);
		setChildrenNodes(num, keys, subtree);
	}
	
	public void setChildrenNodes(long num, Object[] keys, Collection<TreeViewClient<Key, Value>> subtree) {
		this.keys.clear();
		this.values.clear();
		this.childrenNodes.clear();
		this.num = num;
		for (Key k : (Key[])keys) { this.keys.add(k);	}
		for (TreeViewClient<Key, Value> t : subtree) { this.childrenNodes.add(t);	}
	}
	
	public void setLeafValues(long num, Object[] keys, Object[] values) {
		this.keys.clear();
		this.values.clear();
		this.num = num;
		for (Key k : (Key[])keys) { this.keys.add(k);	}
		for (Value v : (Value[])values) { this.values.add(v);	}
	}
	
	public ArrayList<TreeViewClient<Key, Value>> getChildren() {
		return this.childrenNodes;
	}
	public long getPid() {
		return this.pid;
	} 
	public boolean isLeaf() {
		return this.childrenNodes.isEmpty();
	}
	public ArrayList<Value> getValues() {
		return this.values;
	}
	public ArrayList<Key> getKeys() { 
		return this.keys;
	}
	
	public Key getMinKey() {
	  return keys.get(0);
	}

	public Key getMaxKey() {
		  return keys.get(keys.size() - 1);
	}
	
	public boolean isConsistent() {
		boolean valid = true;
		if (!childrenNodes.isEmpty()) {
			for(int i = 0; i < num && valid; ++i) { 
			  Key key = keys.get(i);
			  TreeViewClient<Key, Value> child = childrenNodes.get(i+1);
		      valid = key.compareTo(child.getMinKey()) <= 0 && child.isConsistent();
			}
			
			if(valid && num > 0) {
				int first = 0;			
				Key key = keys.get(first);
				TreeViewClient<Key, Value> child = childrenNodes.get(first);
				valid = key.compareTo(child.getMaxKey()) > 0 && child.isConsistent();
			}
		}
		return valid;
	}
	
	public void dump(String file)  {
	  try {
		FileWriter fw = new FileWriter(file);
		BufferedWriter  bfw = new BufferedWriter(fw);
		
		bfw.write("digraph tree {\n");
		Stack<TreeViewClient<Key,Value>> s = new Stack<TreeViewClient<Key,Value>>();
		s.push(this);
		while (!s.isEmpty()) {
			TreeViewClient<Key,Value> c = s.pop();
			bfw.write(""+c.getPid());
			bfw.write("[ label = \""+c.getPid()+":");
			if (c.isLeaf()) {
			   for (Value v : c.getValues()) {
				   bfw.write(" "+v); 
			   }
			} else {
				for (Key k : c.getKeys()) {
					   bfw.write(" "+k); 
				   }
			}
			bfw.write("\"];\n ");
			ArrayList<TreeViewClient<Key, Value>> a = c.getChildren();
			for (int u = 0; u < a.size(); u++) {
				if (a.get(u).getPid()!=0) { 
					TreeViewClient<Key, Value> child = a.get(u);
					bfw.write(c.getPid()+" -> "+child.getPid()+"[ label = \""+u+"\"];\n");
					s.push(child);
				}
			}
		}
		bfw.write("}\n");
		
		bfw.close();
	   	fw.close();
	  } catch (IOException ex) { 
		  
	  } 
		
	}
	public void dump( )  {
		dump("tree.dot");
	}	
}
