/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ar.edu.itba.pod.tp.interfaces;

import org.apache.commons.codec.digest.DigestUtils;

/**
 *
 * @author mariano
 */
public class Utils
{
	public static String hashMessage(Integer playerSeq, Integer opSeq, String message, String salt)
	{
		String data = "-" + playerSeq.toString() + "#" + opSeq + "#" + message + "##" + salt;
		String hash = DigestUtils.sha1Hex(data);
//		System.out.println("DATA[" + data + "] HASH [" + hash + "]");
		return hash;
	}
}
