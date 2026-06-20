package com.harry_hq.dh_s_veinminer;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.core.BlockPos;

import java.util.ArrayList;
import java.util.List;

/**
 * 立方体区域，由两个对角坐标定义。
 * 用于范围白名单：只有在此区域内的方块才可被连锁挖掘。
 */
public record CuboidRegion(int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {

    /**
     * 判断坐标是否在该区域内（含边界）。
     */
    public boolean contains(BlockPos pos) {
        return pos.getX() >= minX && pos.getX() <= maxX
            && pos.getY() >= minY && pos.getY() <= maxY
            && pos.getZ() >= minZ && pos.getZ() <= maxZ;
    }

    /**
     * 从 JSON 数组解析区域列表。
     * 格式: [{"from":[x1,y1,z1], "to":[x2,y2,z2]}, ...]
     */
    public static List<CuboidRegion> parseList(JsonArray array) {
        List<CuboidRegion> regions = new ArrayList<>();
        for (JsonElement elem : array) {
            if (!elem.isJsonObject()) continue;
            regions.add(fromJson(elem.getAsJsonObject()));
        }
        return regions;
    }

    /**
     * 从 JSON 对象解析单个区域。
     * 格式: {"from":[x1,y1,z1], "to":[x2,y2,z2]}
     */
    public static CuboidRegion fromJson(JsonObject obj) {
        JsonArray fromArr = obj.getAsJsonArray("from");
        JsonArray toArr = obj.getAsJsonArray("to");
        int x1 = fromArr.get(0).getAsInt();
        int y1 = fromArr.get(1).getAsInt();
        int z1 = fromArr.get(2).getAsInt();
        int x2 = toArr.get(0).getAsInt();
        int y2 = toArr.get(1).getAsInt();
        int z2 = toArr.get(2).getAsInt();
        return new CuboidRegion(
            Math.min(x1, x2), Math.min(y1, y2), Math.min(z1, z2),
            Math.max(x1, x2), Math.max(y1, y2), Math.max(z1, z2)
        );
    }
}
