// IReqWorkingAidlInterface.aidl
package com.example.batservice;
import com.example.batservice.IRspWorkingAidlInterface;
// Declare any non-default types here with import statements

interface IReqWorkingAidlInterface {
    /**
     * Demonstrates some basic types that you can use as parameters
     * and return values in AIDL.
     */
     String request();
     String mixInfo();
     String getError();
     void regsetRspCallback(in IRspWorkingAidlInterface callback);
     void unregsetRspCallback(in IRspWorkingAidlInterface callback);
     void tiggerReq();
     void setWorking(in String needWorking);
     boolean isAlive();
}
