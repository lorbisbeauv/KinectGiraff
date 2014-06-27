package semiautonomy;

import java.awt.Point;
import java.util.HashMap;
import java.util.Map;


public class TopologyImp implements Topology {
	public String sNodeList="empty",sArcList="empty";
    public Map<String,Point> nodesMap= new HashMap<String,Point>();
    public Map<Integer,String> nodesNameMap= new HashMap<Integer,String>();
    public SynchronizedMap arcsMap= new SynchronizedMap();
    public boolean updatingTopology=false;
    Utils m_utils;
    public TopologyImp(){
    	m_utils=new Utils();
    }
    public class SynchronizedMap {
        private Map<String,String> map;
        public SynchronizedMap(){
        	map= new HashMap<String,String>();
        }
        public synchronized void mapClear() {
        	map.clear();
        }

        public synchronized boolean mapContainsKey(String key) {
           return map.containsKey(key);
        }
        public synchronized void mapPut(String name, String dest) {
            map.put(name,dest);
        }

        public synchronized Map<String,String> getMap() {
            return map;
        }

    }
}
