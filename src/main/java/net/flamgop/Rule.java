// RIPPED FROM https://github.com/GoldenStack/minestom-ca/blob/master/src/main/java/dev/goldenstack/minestom_ca/Rule.java
package net.flamgop;

import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.instance.block.Block;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * A generic rule for a cellular automata situated in a Minecraft world.
 *
 * @param condition the condition for rule application
 * @param result    the calculated result of rule application
 */
public record Rule(@NotNull Condition condition, @NotNull Result result) {

    public sealed interface Condition {
        record Literal(int value) implements Condition {}

        record And(@NotNull List<Condition> conditions) implements Condition {
            public And {
                conditions = List.copyOf(conditions);
            }

            public And(@NotNull Condition... conditions) {
                this(List.of(conditions));
            }
        }

        record Or(@NotNull List<Condition> conditions) implements Condition {
            public Or {
                conditions = List.copyOf(conditions);
            }
        }

        record Not(@NotNull Condition condition) implements Condition {
        }

        record Equal(@NotNull Condition first, @NotNull Condition second) implements Condition {
            public Equal(Block block) {
                this(new Index(0), new Literal(block.stateId()));
            }
        }

        record Index(int stateIndex) implements Condition {
        }

        record Compare(@NotNull Condition first, @NotNull Condition second) implements Condition {
        }

        record Neighbors(@NotNull List<Point> offsets, @NotNull Condition condition) implements Condition {
            public Neighbors {
                offsets = List.copyOf(offsets);
            }


            public Neighbors(int x, int y, int z, @NotNull Condition condition) {
                this(List.of(new Vec(x, y, z)), condition);
            }
        }
    }

    public sealed interface Result {
        record And(@NotNull List<Result> others) implements Result {
            public And {
                others = List.copyOf(others);
            }
        }

        record Set(@NotNull Point offset, int index, int value) implements Result {
            public Set(Point offset, Block block) {
                this(offset, 0, block.stateId());
            }

            public Set(Block block) {
                this(new Vec(0, 0, 0), block);
            }
        }
    }
}
