/*
 * To change this template, choose Tools | Templates
 * and open the template position the editor.
 */
package ar.edu.itba.pod.tp.interfaces;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author mariano
 */
public class GameResult implements Serializable
{
	private final List<PlayerResult> results = new ArrayList();

	public List<PlayerResult> getResults()
	{
		return results;
	}
	
	public void addPlayerResult(String player, int position, Status status, int serverCount, int playerCount)
	{
		PlayerResult playerResult = new PlayerResult(player, position, status, serverCount, playerCount);
		results.add(playerResult);
		
	}
	public class PlayerResult implements Serializable
	{
		public String player;
		public int position;
		public Status status;
		public int serverCount;
		public int playerCount;

		public PlayerResult(String player, int position, Status status, int serverCount, int playerCount)
		{
			this.player = player;
			this.position = position;
			this.status = status;
			this.serverCount = serverCount;
			this.playerCount = playerCount;
		}

		@Override
		public String toString()
		{
			return "PlayerResult{" + "player=" + player + ", position=" + position + ", status=" + status + '}';
		}
	}
	
	public enum Status 
	{
		SUCCESS,
		SLOW,
		ERROR
	}

	@Override
	public String toString()
	{
		return "GameResult{" + results + '}';
	}
	
	
}
