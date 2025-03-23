package us.crazycrew.crazycrates.api.enums.types;

public enum CrateType {
    menu("Menu"),
    cosmic("Cosmic"),
    crate_on_the_go("CrateOnTheGo"),
    csgo("CSGO"),
    casino("Casino"),
    fire_cracker("FireCracker"),
    quad_crate("QuadCrate"),
    quick_crate("QuickCrate"),
    roulette("Roulette"),
    wheel("Wheel"),
    wonder("Wonder"),
    war("War"),
    gacha("Gacha");

    private final String name;

    CrateType(String name) {
        this.name = name;
    }

    public static CrateType getFromName(String name) {
        if (!name.isEmpty()) {
            for (CrateType crate : values()) {
                if (crate.getName().equalsIgnoreCase(name)) {
                    return crate;
                }
            }

        }
        return csgo;
    }

    public String getName() {
        return this.name;
    }

    public boolean isGacha() {
        return this == gacha;
    }
}
