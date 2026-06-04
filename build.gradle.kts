plugins {
    id("com.gtnewhorizons.gtnhconvention")
}

minecraft {
    extraRunJvmArguments.addAll(
        "-Xmx4G",
        "-Xms4G",
        "-XX:+UseShenandoahGC",
        "-XX:ShenandoahGCMode=generational",
        "-XX:+UseCompactObjectHeaders"
    )
}
