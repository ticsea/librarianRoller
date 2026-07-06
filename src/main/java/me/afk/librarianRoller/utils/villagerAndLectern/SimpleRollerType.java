package me.afk.librarianRoller.utils.villagerAndLectern;

public class SimpleRollerType implements IRollerMode {
    String name;
    int requireCount;
    double radius;

    private SimpleRollerType(Builder builder) {
        this.name = builder.name;
        this.requireCount = builder.requireCount;
        this.radius = builder.radius;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public int getRequireCount() {
        return requireCount;
    }

    @Override
    public double getRadius() {
        return radius;
    }

    public static class Builder {
        private String name = "V9";
        private int requireCount = 0;
        private double radius = 0D;

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder requireCount(int requireCount) {
            this.requireCount = requireCount;
            return this;
        }

        public Builder radius(double radius) {
            this.radius = radius;
            return this;
        }

        public SimpleRollerType build() {
            return new SimpleRollerType(this);
        }
    }
}
