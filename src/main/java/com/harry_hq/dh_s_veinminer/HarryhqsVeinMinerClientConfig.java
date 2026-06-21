package com.harry_hq.dh_s_veinminer;

/**
 * 客户端缓存从服务端同步过来的配置值。
 * 当连接服务器时，服务端的配置覆盖本地值，确保按键逻辑对齐。
 * 未同步时（单机/未收到包）回退到本地 Config 值。
 */
public class HarryhqsVeinMinerClientConfig {
	public static String serverTriggerAction = null; // null = 未同步，回退本地
	public static boolean serverEnabled = true;

	public static String getEffectiveTriggerAction() {
		return serverTriggerAction != null ? serverTriggerAction : Config.triggerAction;
	}

	public static boolean getEffectiveEnabled() {
		return serverTriggerAction != null ? serverEnabled : Config.enabled;
	}

	public static void reset() {
		serverTriggerAction = null;
		serverEnabled = true;
	}
}
