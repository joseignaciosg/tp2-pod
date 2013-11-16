/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ar.edu.itba.pod.tp.interfaces;

import java.io.Serializable;

/**
 *
 * @author mariano
 */
public class Response implements Serializable
{
	public int reqPlayerId;
	public int reqClientSeq;
	public String reqMessage;
	public String reqHash;

	public int rspPlayerId;
	public int rspServerSeq;
	public String rspMessage;
	public String rspHash;	

	@Override
	public String toString()
	{
		return "Response{" + "reqPlayerSeq=" + reqPlayerId + ", reqOpSeq=" + reqClientSeq + ", reqMessage=" + reqMessage + ", reqHash=" + reqHash + ", rspPlayerSeq=" + rspPlayerId + ", rspOpSeq=" + rspServerSeq + ", rspMessage=" + rspMessage + ", rspHash=" + rspHash + '}';
	}

	public Request toRequest()
	{
		return new Request(reqPlayerId, reqClientSeq, reqMessage, reqHash);
	}
}
