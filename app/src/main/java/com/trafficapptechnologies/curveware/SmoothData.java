package com.trafficapptechnologies.curveware;

import java.util.LinkedList;

public class SmoothData {

    LinkedList<Integer> queue;
    int size;
    double avg;

    public void MovingAverage(int size){

        this.queue = new LinkedList<Integer>();
        this.size = size;
    }

    public static float smoothX(float[] currentX){
        float f = 0f;
        return  f;
    }

}
