package ar.edu.itba.pod.tp.interfaces;

import java.io.Serializable;
import java.util.List;

public class Registration implements Serializable
{
	public List<Player> players;
	public String name;
	public String salt;
	public int id;
	public int clientSeq;
	public int clientCount;
	public int serverSeq;
	public int serverCount;
	public final int clientTotal;

	public Registration(String name, int id, int clientSeq, int serverSeq, String salt, List<Player> players, int clientTotal)
	{
		this.name = name;
		this.id = id;
		this.clientSeq = clientSeq;
		this.serverSeq = serverSeq;
		this.salt = salt;
		this.players = players;
		this.clientTotal = clientTotal;
	}
}
