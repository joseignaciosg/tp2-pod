package ar.edu.itba.pod.tp.interfaces;

import java.io.Serializable;
import java.util.List;

public class PlayerRefereeRegistration implements Serializable
{
	public final String playerName;
	public final String clientName;
	public final int id;
	public final String salt;
	public final int requestsPerGame;
	public final int playersPerGame;

	public PlayerRefereeRegistration(final String playerName, final String clientName, final int id, final String salt, final int requestsPerGame, final int playersPerGame)
	{
		this.playerName = playerName;
		this.clientName = clientName;
		this.id = id;
		this.salt = salt;
		this.requestsPerGame = requestsPerGame;
		this.playersPerGame = playersPerGame;
	}
}
