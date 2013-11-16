package ar.edu.itba.pod.tp.player;

import java.rmi.RemoteException;
import java.util.List;

import ar.edu.itba.pod.tp.interfaces.Player;
import ar.edu.itba.pod.tp.interfaces.PlayerDownException;

public class PlayerWorker implements Runnable {

	List<Player> players;
	PlayerServer server;
	int thread_n;

	public PlayerWorker(List<Player> players, PlayerServer player, int thread_n) {
		this.players = players;
		this.server = player;
		this.thread_n = thread_n;
	}

	@Override
	public void run() {
		int plays = 0;
		int loop = server.total / thread_n;
		System.out.println("Thread #" + Thread.currentThread().getId()
				+ " running loops:" + loop);
		do {
			int opt = (int) (java.lang.Math.random() * players.size());
			Player other = players.get(opt);
			try {
				server.play("hola! estamos jugando " + plays, other);
			} catch(PlayerDownException e){
				if(e.getMessage().startsWith("[LOSER]")){
					players.remove(other);
				}
			}catch (RemoteException e) {			
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} while (++plays < loop);
		
		System.out.println("salio!");


	}

}
