package com.example.examplemod.handler;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class VeinMinerTracker{
	private static final Set<UUID> activePlayers=ConcurrentHashMap.newKeySet();

	public static void activate(UUID uuid){activePlayers.add(uuid);}

	public static void deactivate(UUID uuid){activePlayers.remove(uuid);}

	public static boolean isActive(UUID uuid){return activePlayers.contains(uuid);}

	public static void onPlayerDisconnect(UUID uuid){activePlayers.remove(uuid);}
}
