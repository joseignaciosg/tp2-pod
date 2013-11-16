/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ar.edu.itba.pod.tp.interfaces;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 *
 * @author mariano
 */
public interface Player extends Remote
{
	Response operate(Request request) throws RemoteException;
}
