/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ar.edu.itba.pod.tp.interfaces;

import java.rmi.RemoteException;

/**
 *
 * @author mariano
 */
public class PlayerDownException extends RemoteException
{

	public PlayerDownException(String s)
	{
		super(s);
	}

	public PlayerDownException(String s, Throwable cause)
	{
		super(s, cause);
	}
	
}
