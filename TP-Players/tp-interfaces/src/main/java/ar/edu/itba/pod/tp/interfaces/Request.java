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
public class Request implements Serializable
{
	public int playerId;
	public int clientSeq;
	public String message;
	public String hash;

	public Request(int playerId, int clientSeq, String message, String hash)
	{
		this.playerId = playerId;
		this.clientSeq = clientSeq;
		this.message = message;
		this.hash = hash;
	}

	@Override
	public String toString()
	{
		return "Request{" + "playerSeq=" + playerId + ", opSeq=" + clientSeq + ", message=" + message + ", hash=" + hash + '}';
	}

	@Override
	public int hashCode()
	{
		int hashB = 5;
		hashB = 37 * hashB + (this.hash != null ? this.hash.hashCode() : 0);
		return hashB;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final Request other = (Request) obj;
		if (this.playerId != other.playerId) {
			return false;
		}
		if (this.clientSeq != other.clientSeq) {
			return false;
		}
		if ((this.message == null) ? (other.message != null) : !this.message.equals(other.message)) {
			return false;
		}
		return true;
	}
	
}
