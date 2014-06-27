/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package semiautonomy;

import java.awt.Point;


/**
 *
 * @author Curro
 */
public class Utils { 
//PUBLIC METHODS    
//////////////////////////////////
//  GENERIC UTILS               //
//////////////////////////////////   
    public Point transformToSchematicMapPoint(double pointX,double pointY,int centerX,int centerY)
    {
        Point schematicPoint= new Point();
        //System.out.println("OPENMORA GeometricPoint_1:"+pointX+","+pointY);
        double x=centerX+(pointX/0.03);
        double y=centerY-(pointY/0.03);
        //System.out.println("OPENMORA SchematicPoint:"+x+","+y);
        schematicPoint.setLocation(x, y);
        //System.out.println("OPENMORA Location:"+schematicPoint.getLocation().toString());
        return schematicPoint;
    }
    public double transformToGeometricXCoord(Point schematicPoint,int centerX,int centerY){
    	//System.out.println("TransformToGeometricX------------------------");
    	//System.out.println("SchematicPoint:"+schematicPoint.getX()+","+schematicPoint.getY());
        double geometricX=(schematicPoint.getX()-(double)centerX)*Double.valueOf("0.03");
       // System.out.println("geometricX_1:"+geometricX);
        geometricX = Math.round(geometricX * 100);
        //System.out.println("geometricX_2:"+geometricX);
        geometricX = geometricX/100;
        //System.out.println("geometricX_3:"+geometricX);
        //System.out.println("---------------------------------------------");
    	return geometricX;
    } 
    public double transformToGeometricYCoord(Point schematicPoint,int centerX,int centerY){
    	//System.out.println("TransformToGeometricY-----------------");
        double geometricY=((double)centerY-schematicPoint.getY())*Double.valueOf("0.03");
       // System.out.println("geometricY_3:"+geometricY);
        geometricY = Math.round(geometricY * 100);
       // System.out.println("geometricY_2:"+geometricY);
        geometricY = geometricY/100;
        //System.out.println("geometricY_3:"+geometricY);
        //System.out.println("---------------------------------------------");
    	return geometricY;
    } 
}
