/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ar.edu.itba.pod.tp.referee;

import java.rmi.RemoteException;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

import ar.edu.itba.pod.tp.interfaces.GameResult;
import ar.edu.itba.pod.tp.interfaces.Player;
import ar.edu.itba.pod.tp.interfaces.PlayerLoserException;
import ar.edu.itba.pod.tp.interfaces.Referee;
import ar.edu.itba.pod.tp.interfaces.Registration;
import ar.edu.itba.pod.tp.interfaces.Request;
import ar.edu.itba.pod.tp.interfaces.Response;
import ar.edu.itba.pod.tp.interfaces.Utils;

/**
 *
 * @author mariano
 */
public class RefereeServer implements Referee
{
	public static final String ENDPOINT = "endpoint:";
	public final int THREAD_N = 1;
	ExecutorService executorService = Executors.newFixedThreadPool(THREAD_N);

	final List<Player> playerServers = new ArrayList<Player>();
	final Map<Player, Registration> registrations = new HashMap();
	final Map<Player, Registration> initialRegistrations = new HashMap();
	final List<Registration> winners = new ArrayList<Registration>();
	final Random random = new Random();
	final Map<Integer, List<Request>> requests = new HashMap();
	boolean playing;
	final int requestsTotal;
	final Map<String, List<Player>> players = new HashMap();
	final String name;
	final Registry registry;

	public RefereeServer(String name, int requestsTotal, Registry registry)
	{
		this.name = name;
		this.requestsTotal = requestsTotal;
		this.registry = registry;
	}

	@Override
	public String getName()
	{
		return name;
	}
	
	@Override
	public Registration newPlayer(String playerName, Player playerClient) throws RemoteException
	{
		if (playing) {
			throw new RemoteException("ya estan jugando!");
		}
		String playerId = buildPlayerName(playerName, playerClient);
		System.out.println("nuevo player " + playerId);
		Registration result = register(playerId, playerClient);
		return result;
	}

	@Override
	public synchronized void registerRequest(Player player, Request request) throws RemoteException
	{
		Registration clientReg = findRegistration(player);
		System.out.println("REQ: " + clientReg.name + " - " + request);
		if (clientReg.id != request.playerId) {
			throw kickOutPlayer(player, "Fallo el PLAYER SEQ!!!");
		}
		if (clientReg.clientSeq != request.clientSeq) {
			throw kickOutPlayer(player, "Fallo el PLAYER OP SEQ!!! " + clientReg.clientSeq + "/" + request.clientSeq);
		}
		String check = hashMessage(clientReg, request.clientSeq, request.message);
		if (!check.equals(request.hash)) {
			throw kickOutPlayer(player, "Fallo el hash!!!");
		}
		clientReg.clientSeq++;
		clientReg.clientCount++;
		
		List<Request> playerRequests = requests.get(clientReg.id);
		playerRequests.add(request);

		if (clientReg.clientCount >= requestsTotal && clientReg.serverCount >= requestsTotal) {
			if (!winners.contains(clientReg)) {
				winners.add(clientReg);
			}
		}
	}

	@Override
	public synchronized void registerResponse(Player player, Response response) throws RemoteException
	{
		Registration clientReg = findRegistration(player);
		System.out.println("RES: " + clientReg.name + " - " + response);
		if (clientReg.id != response.rspPlayerId) {
			throw kickOutPlayer(player, "Fallo el PLAYER SEQ!!!");
		}
		// si habilitamos este check empieza a fallar enseguida
//		if (clientReg.serverSeq != response.rspServerSeq) {
//			throw new RemoteException("Fallo el SERVER OP SEQ!!!" + clientReg.serverSeq + "/" + response.rspServerSeq);
//		}
		String check = hashMessage(clientReg, response.rspServerSeq, response.rspMessage);
		if (!check.equals(response.rspHash)) {
			throw kickOutPlayer(player, "Fallo el hash!!!");
		}
		clientReg.serverSeq++;
		clientReg.serverCount++;
		
		
		List<Request> clientRequests = requests.get(response.reqPlayerId);
		if (!clientRequests.contains(response.toRequest())) {
			throw kickOutPlayer(player, "NO ESTA LA OPERACION!!!");
		}
		
		if (clientReg.clientCount >= requestsTotal && clientReg.serverCount >= requestsTotal) {
			if (!winners.contains(clientReg)) {
				winners.add(clientReg);
			}
		}
	}

	@Override
	public String showResults() throws RemoteException
	{
		final StringBuilder result = new StringBuilder();
		result.append("Current Results:\n");
		for (Map.Entry<Player, Registration> entry : initialRegistrations.entrySet()) {
			Registration registration = entry.getValue();
			boolean survived = registrations.containsKey(entry.getKey());
			boolean okClient = registration.clientCount >= requestsTotal;
			boolean okServer = registration.serverCount >= requestsTotal;
			if (survived) {
				result.append(String.format("Player:%s %s%s C:%s S: %s\n", registration.name, okClient ? "C" : "-", okServer ? "S" : "-", registration.clientCount, registration.serverCount));
			} else {
				result.append(String.format("Player:%s LOSER C:%s S: %s\n", registration.name, registration.clientCount, registration.serverCount));
			}
		}
		
		result.append("\n\n winners:\n");
		for (Registration registration : winners) {
			result.append(String.format("Player: %s\n", registration.name));
		}
		return result.toString();
	}
	
	private Registration register(String playerName, Player playerClient)
	{
		
		final String salt = UUID.randomUUID().toString();
		final int seq = random.nextInt();
		final int clientSeq = random.nextInt();
		final int serverSeq = random.nextInt();

		Registration result = new Registration(playerName, seq, clientSeq, serverSeq, salt, playerServers, requestsTotal);
		
		synchronized (registrations) {
			playerServers.add(playerClient);
			registrations.put(playerClient, result);
			initialRegistrations.put(playerClient, result);
			requests.put(result.id, new ArrayList());
		}

		synchronized (this) {
			return result;
		}
	}

	private String hashMessage(Registration registration, int opSeq, String message)
	{
		return Utils.hashMessage(registration.id, opSeq, message, registration.salt);
	}
	
	private RemoteException kickOutPlayer(Player playerClient, String message) throws RemoteException
	{
		synchronized (registrations) {
			registrations.remove(playerClient);
		}
		return new PlayerLoserException("[LOSER] "+ message + "\n" + showResults());
	}

	private Registration findRegistration(Player player) throws RemoteException
	{
		Registration clientReg = registrations.get(player);
		if (clientReg == null) {
			clientReg = initialRegistrations.get(player);
			if (clientReg != null) {
				throw new RemoteException("Ya perdiste " + clientReg.name);
			} else {
				throw new RemoteException("No te conozco");
			}
		}
		return clientReg;
	}

	private String buildPlayerName(String playerName, Player playerClient)
	{
		String tmp = playerClient.toString();
		int i = tmp.indexOf(ENDPOINT);
		tmp = tmp.substring(i + ENDPOINT.length());
		int j = tmp.indexOf("]") + 1;
		tmp = tmp.substring(0, j);
		String playerId = playerName + "-" + tmp;
		return playerId;
	}

	@Override
	public GameResult hostGame(int gameIn, String gameHash, List<String> guests) throws RemoteException
	{
		System.out.println("hostGame " + gameHash);
		for (String guest : guests) {
			System.out.println("Looking for referee guest " + guest);
			Referee guestReferee;
			try {
				guestReferee = (Referee) registry.lookup("referees/" + guest);

				guestReferee.joinGame(gameIn, gameHash, getName());
			}
			catch (Exception ex) {
				Logger.getLogger(RefereeServer.class.getName()).log(Level.SEVERE, null, ex);
			}
		}
		try {
			//		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
			Thread.sleep(20);
		}
		catch (InterruptedException ex) {
			Logger.getLogger(RefereeServer.class.getName()).log(Level.SEVERE, null, ex);
		}
		GameResult gameResult = new GameResult();
		gameResult.addPlayerResult("pepe", 0, GameResult.Status.SUCCESS, 0, 0);
		return gameResult;
	}

	@Override
	public void joinGame(int gameIn, String gameHash, String host) throws RemoteException
	{

		int thread=0;
		while(thread++<THREAD_N){
//			System.out.println("THREAD #"+ Thread.currentThread().getId() + " inicializado. Jugará "  + (loop / THREAD_N) + " veces");
			synchronized(players) {			       
				executorService.execute(new PlayerWorker(players, server, THREAD_N));
		    }
		}
		//TODO esto es lo que hay que implementar
		System.out.println("joinGame");
//		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}
}
