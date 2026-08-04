package com.nhnacademy.inventory.global.util;

import java.util.UUID;

public class UserContext {
    private static final ThreadLocal<UUID> currentUserUuid = new ThreadLocal<>();

    public static void setUserUuid(UUID uuid){
        currentUserUuid.set(uuid);
    }

    public static UUID getUserUuid(){
        return currentUserUuid.get();
    }

    public static void clear(){
        currentUserUuid.remove();
    }
}
