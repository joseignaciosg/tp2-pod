/*
 * To change this template, choose Tools | Templates
 * and open the template position the editor.
 */
package ar.edu.itba.pod.tp.master;

import ar.edu.itba.pod.tp.interfaces.GameResult;
import ar.edu.itba.pod.tp.interfaces.Master;
import ar.edu.itba.pod.tp.interfaces.Referee;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author mariano
 */
public class MasterServer implements Master
{
	private final Registry registry;
	private final Map<String, Referee> referees = Collections.synchronizedMap(new HashMap());
	private final int requestsTotal;
	private final int gameTimeout;
	private final int totalTime;
	private final Map<String, GameResult> results = Collections.synchronizedMap(new HashMap());
	private Integer currentGameIn = 0;
	private boolean gameRunning = true;

	MasterServer(Registry registry, int requestsTotal, int gameTimeout, int totalTime)
	{
		this.registry = registry;
		this.requestsTotal = requestsTotal;
		this.gameTimeout = gameTimeout;
		this.totalTime = totalTime;
	}
	
	@Override
	public int getRequestsTotal()
	{
		return requestsTotal;
	}

	@Override
	public int getGameTimeout() throws RemoteException
	{
		return gameTimeout;
	}

	@Override
	public int getTotalTime() throws RemoteException
	{
		return totalTime;
	}

	@Override
	public void registerReferee(Referee referee) throws RemoteException
	{
		try {
			if (referees.containsKey(referee.getName())) {
				throw new IllegalArgumentException("Referee already exists!");
			}
			final Remote refereeRemote = registry.lookup("referees/" + referee.getName());
			if (!(refereeRemote instanceof Referee)) {
				throw new IllegalArgumentException("Remote object " + referee.getName() + " bind is not a Referee");
			}
			if (!referee.getName().equals(((Referee) refereeRemote).getName())) {
				throw new IllegalArgumentException("[BUG] Invalid name");
			}
			referees.put(referee.getName(), referee);
			System.out.println("Referee registered: " + referee.getName());
		}
		catch (Exception ex) {
			throw new RemoteException("Referee Registration failed", ex);
		}
	}

	void printResults()
	{
		System.out.println("Total Games: " + results.size());
		final Map<String, Integer> points = new HashMap();
		for (GameResult gameResult : results.values()) {
			final List<GameResult.PlayerResult> results1 = gameResult.getResults();
			for (GameResult.PlayerResult playerResult : results1) {
				if (playerResult.status != GameResult.Status.SUCCESS) {
					System.out.println("Discard " + playerResult);
					continue;
				}
				int currentScore = points.containsKey(playerResult.player) ? points.get(playerResult.player) : 0;
				int gameScore = results1.size() - playerResult.position;
				points.put(playerResult.player, currentScore + gameScore);
			}
		}
		final Map<String, Integer> sortedPoints = sortByValue(points);
		for (Map.Entry<String, Integer> entry : sortedPoints.entrySet()) {
			System.out.println(String.format("Player: %s Score:%s", entry.getKey(), entry.getValue()));
		}
	}
	
	void shutdown()
	{
		this.gameRunning = false;
	}
	
	Map<String, Referee> getReferees()
	{
		return referees;
	}

	Runnable newRunner()
	{
		return new GamesRunner();
	}
	
	class GamesRunner implements Runnable
	{
		//cada uno de estos "monitores" va a correr los juegos pero no en simultaneo
		//sino que usando un timeout
		final ExecutorService executor = Executors.newSingleThreadExecutor();

		public void run()
		{
			do {
				try {
					final String gameHash = UUID.randomUUID().toString();
					List<Referee> myReferees;
					int gameIn;
					synchronized (referees) {
						myReferees = new ArrayList<Referee>(referees.values());
						gameIn = currentGameIn++;
					}
					
					hostGame(gameIn, gameHash, myReferees);
				}
				catch (Exception ex) {
					Logger.getLogger(MasterServer.class.getName()).log(Level.SEVERE, null, ex);
				}
			} while (gameRunning);
			System.out.println("Runner finished");
		}
		
		private void hostGame(final int gameIn, final String gameHash, final List<Referee> myReferees) throws Exception
		{
			final int opt = (int) (java.lang.Math.random() * myReferees.size());
			final Referee referee = myReferees.remove(opt); //se elige un referee
			final List<String> players = new ArrayList();
			for (Referee player : myReferees) {
				players.add(player.getName());
			}
			//Game
			Future<GameResult> submit = executor.submit(new Callable<GameResult>() {
				public GameResult call() throws Exception
				{
					System.out.println("Game started: " + gameHash);
					return referee.hostGame(gameIn, gameHash, players);
				}
			});

			final GameResult result = submit.get(gameTimeout, TimeUnit.SECONDS);
			System.out.println("Game finished: " + gameHash + " Result: " + result != null ? result.toString() : "Timeout");
			if (result != null) {
				results.put(gameHash, result);
			}
		}
	}

	private static Map sortByValue(Map map)
	{
		List list = new LinkedList(map.entrySet());
		Collections.sort(list, new Comparator()
		{
			public int compare(Object o1, Object o2)
			{
				return ((Comparable) ((Map.Entry) (o1)).getValue())
						.compareTo(((Map.Entry) (o2)).getValue());
			}
		});

		Map result = new LinkedHashMap();
		for (Iterator it = list.iterator(); it.hasNext();) {
			Map.Entry entry = (Map.Entry) it.next();
			result.put(entry.getKey(), entry.getValue());
		}
		return result;
	}
}
