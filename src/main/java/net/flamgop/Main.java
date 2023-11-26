package net.flamgop;

import net.minestom.server.MinecraftServer;
import net.minestom.server.command.ConsoleSender;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.arguments.minecraft.ArgumentBlockState;
import net.minestom.server.command.builder.arguments.relative.ArgumentRelativeBlockPosition;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.Player;
import net.minestom.server.event.GlobalEventHandler;
import net.minestom.server.event.player.PlayerLoginEvent;
import net.minestom.server.event.player.PlayerSpawnEvent;
import net.minestom.server.instance.LightingChunk;
import net.minestom.server.instance.block.Block;
import net.minestom.server.utils.location.RelativeVec;
import net.minestom.server.world.DimensionType;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static net.flamgop.Rule.*;

public class Main {

    /**
     * All 2d neighbors (so y = 0). 9 total.
     */
    public static final @NotNull List<Point> NEIGHBORS_2D;

    /**
     * All 2d neighbors (so y = 0) excluding {@code 0, 0}. 8 total.
     */
    public static final @NotNull List<Point> NEIGHBORS_2D_NOT_SELF;

    /**
     * All 3d neighbors. 27 total.
     */
    public static final @NotNull List<Point> NEIGHBORS_3D;

    /**
     * Von Neumann Neighborhood, think plus sign
     */
    public static final @NotNull List<Point> NEIGHBORS_3D_V;

    /**
     * All 3d neighbors excluding {@code 0, 0, 0}. 26 total.
     */
    public static final @NotNull List<Point> NEIGHBORS_3D_NOT_SELF;

    static {
        List<Point> points2d = new ArrayList<>();
        for (int x : new int[]{-1, 0, 1}) {
            for (int z : new int[]{-1, 0, 1}) {
                points2d.add(new Vec(x, 0, z));
            }
        }

        NEIGHBORS_2D = List.copyOf(points2d);

        points2d.removeIf(Vec.ZERO::equals);
        NEIGHBORS_2D_NOT_SELF = List.copyOf(points2d);


        List<Point> points3d = new ArrayList<>();
        for (int x : new int[]{-1, 0, 1}) {
            for (int y : new int[]{-1, 0, 1}) {
                for (int z : new int[]{-1, 0, 1}) {
                    points3d.add(new Vec(x, y, z));
                }
            }
        }

        NEIGHBORS_3D = List.copyOf(points3d);

        points3d.removeIf(Vec.ZERO::equals);
        NEIGHBORS_3D_NOT_SELF = List.copyOf(points3d);

        points3d.removeIf(v -> Math.abs(v.blockX()) + Math.abs(v.blockY()) + Math.abs(v.blockZ()) > 1);
        points3d.add(Vec.ZERO);
        NEIGHBORS_3D_V = List.copyOf(points3d);
    }


    public static void main(String[] args) {
        MinecraftServer server = MinecraftServer.init();

        CellularInstance instance = new CellularInstance(UUID.randomUUID(), DimensionType.OVERWORLD, List.of(
                new Rule(
                        new Condition.And(
                                new Condition.Equal(Block.AIR),
                                new Condition.Not(new Condition.Equal(
                                    new Condition.Neighbors(NEIGHBORS_3D_V,
                                            new Condition.Or(List.of(
                                                    new Condition.Equal(Block.WHITE_WOOL)
                                            ))
                                    ),
                                    new Condition.Literal(0)
                                ))
                        ),
                        new Rule.Result.Set(Block.WHITE_WOOL)
                )
        ));
        MinecraftServer.getInstanceManager().registerInstance(instance);
        instance.setChunkSupplier(LightingChunk::new);

        GlobalEventHandler geh = MinecraftServer.getGlobalEventHandler();

        geh.addListener(PlayerLoginEvent.class, e -> e.setSpawningInstance(instance));
        geh.addListener(PlayerSpawnEvent.class, e -> {
            e.getPlayer().setGameMode(GameMode.CREATIVE);
            e.getPlayer().setFlying(true);
        });

        Command setBlock = new Command("setBlock");

        ArgumentRelativeBlockPosition blockPositionArg = new ArgumentRelativeBlockPosition("position");
        ArgumentBlockState blockStateArg = new ArgumentBlockState("block");

        setBlock.setCondition((sender, commandString) -> !(sender instanceof ConsoleSender));
        setBlock.addSyntax((s,ctx) -> {
            RelativeVec pos = ctx.get(blockPositionArg);
            Block block = ctx.get(blockStateArg);
            ((Player)s).getInstance().setBlock(pos.from((Player)s), block);
        }, blockPositionArg, blockStateArg);

        Command cellularAutomata = new Command("cellularAutomata");

        cellularAutomata.setCondition((sender, commandString) -> !(sender instanceof ConsoleSender));
        cellularAutomata.addSyntax((s, ctx) -> {
            if (((Player)s).getInstance() instanceof CellularInstance c) {
                c.setTickCA(true);
            }
        }, ArgumentType.Literal("start"));
        cellularAutomata.addSyntax((s, ctx) -> {
            if (((Player)s).getInstance() instanceof CellularInstance c) {
                c.setTickCA(false);
            }
        }, ArgumentType.Literal("stop"));

        MinecraftServer.getCommandManager().register(setBlock);
        MinecraftServer.getCommandManager().register(cellularAutomata);

        server.start("0.0.0.0", 25565);
    }
}