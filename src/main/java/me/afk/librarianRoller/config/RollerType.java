package me.afk.librarianRoller.config;

public enum RollerType {
    V1,
    V4,
    V6;


    /**
     * Returns the name of this enum constant, as contained in the
     * declaration.  This method may be overridden, though it typically
     * isn't necessary or desirable.  An enum class should override this
     * method when a more "programmer-friendly" string form exists.
     *
     * @return the name of this enum constant
     */
    @Override
    public String toString() {
        return switch (this) {
            case V1 -> "V1";
            case V4 -> "V4";
            case V6 -> "V6";
        };
    }
}
