package ar.edu.itba.pod.tp.interfaces;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface Referee extends Remote
{
	String getName() throws RemoteException;
	
	/**
	 * Start a new game, notify players, let them play and process results and return the results
	 * @param gameIn 
	 * @param gameHash
	 * @throws RemoteException 
	 */
	GameResult hostGame(final int gameIn, final String gameHash, List<String> guests) throws RemoteException;
	
	/**
	 * A player from this Referee must join an new game.
	 * @param gameIn
	 * @param refereeName the lookup name in the registry of the host referee 
	 * @throws RemoteException 
	 */
	void joinGame(final int gameIn, final String gameHash, final String host) throws RemoteException;
	
	Registration newPlayer(String playerName, Player playerClient) throws RemoteException;
	
	void registerRequest(Player player, Request request) throws RemoteException;

	void registerResponse(Player player, Response response) throws RemoteException;
	
	String showResults() throws RemoteException;
}
