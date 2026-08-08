package pl.fuzjajadrowa.locatorbar.server;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import pl.fuzjajadrowa.locatorbar.LocatorBar;
import pl.fuzjajadrowa.locatorbar.config.LocatorBarEnums.LocatorBarStyle;
import pl.fuzjajadrowa.locatorbar.config.LocatorBarEnums.PlayerMarkerType;
import pl.fuzjajadrowa.locatorbar.config.LocatorBarServerConfig;
import pl.fuzjajadrowa.locatorbar.config.LocatorBarServerConfig.ServerSettings;

public final class LocatorBarCommands {
    private LocatorBarCommands() {
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
            Commands.literal("locatorbar")
                //? if >=1.21.11 {
                .requires(source -> isMultiplayerOrLan(source) && Commands.hasPermission(Commands.LEVEL_GAMEMASTERS).test(source))
                //?} else {
                /*.requires(source -> isMultiplayerOrLan(source) && source.hasPermission(2))
                *///?}
                .then(Commands.literal("server")
                    .executes(ctx -> showServerSettings(ctx))
                    .then(Commands.literal("style")
                        .then(Commands.literal("reworked").executes(ctx -> setStyle(ctx, LocatorBarStyle.REWORKED)))
                        .then(Commands.literal("classic").executes(ctx -> setStyle(ctx, LocatorBarStyle.CLASSIC)))
                        .then(Commands.literal("off").executes(ctx -> setStyle(ctx, LocatorBarStyle.OFF)))
                    )
                    .then(Commands.literal("showCoordinates")
                        .then(Commands.argument("value", BoolArgumentType.bool()).executes(ctx -> setShowCoordinates(ctx)))
                    )
                    .then(Commands.literal("showDays")
                        .then(Commands.argument("value", BoolArgumentType.bool()).executes(ctx -> setShowDays(ctx)))
                    )
                    .then(Commands.literal("showWorldDirections")
                        .then(Commands.argument("value", BoolArgumentType.bool()).executes(ctx -> setShowWorldDirections(ctx)))
                    )
                    .then(Commands.literal("playerMarkerType")
                        .then(Commands.literal("heads").executes(ctx -> setPlayerMarkerType(ctx, PlayerMarkerType.HEADS)))
                        .then(Commands.literal("dots").executes(ctx -> setPlayerMarkerType(ctx, PlayerMarkerType.DOTS)))
                        .then(Commands.literal("off").executes(ctx -> setPlayerMarkerType(ctx, PlayerMarkerType.OFF)))
                    )
                    .then(Commands.literal("maxVisiblePlayers")
                        .then(Commands.argument("value", IntegerArgumentType.integer(1, 64)).executes(ctx -> setMaxVisiblePlayers(ctx)))
                    )
                    .then(Commands.literal("playerMarkerFadeStartDistance")
                        .then(Commands.argument("value", FloatArgumentType.floatArg(0.0F, LocatorBarServerConfig.INFINITE_PLAYER_HEAD_DISTANCE)).executes(ctx -> setPlayerMarkerFadeStartDistance(ctx)))
                    )
                    .then(Commands.literal("playerMarkerFadeToMinDistance")
                        .then(Commands.argument("value", FloatArgumentType.floatArg(0.0F, LocatorBarServerConfig.INFINITE_PLAYER_HEAD_DISTANCE)).executes(ctx -> setPlayerMarkerFadeToMinDistance(ctx)))
                    )
                    .then(Commands.literal("playerMarkerHideDistance")
                        .then(Commands.literal("inf").executes(ctx -> setPlayerMarkerHideDistanceInf(ctx)))
                        .then(Commands.argument("value", FloatArgumentType.floatArg(0.0F, LocatorBarServerConfig.INFINITE_PLAYER_HEAD_DISTANCE)).executes(ctx -> setPlayerMarkerHideDistance(ctx)))
                    )
                    .then(Commands.literal("playerMarkerMinAlphaPercent")
                        .then(Commands.argument("value", FloatArgumentType.floatArg(0.0F, 100.0F)).executes(ctx -> setPlayerMarkerMinAlphaPercent(ctx)))
                    )
                    .then(Commands.literal("showWaypoints")
                        .then(Commands.argument("value", BoolArgumentType.bool()).executes(ctx -> setShowWaypoints(ctx)))
                    )
                    .then(Commands.literal("maxVisibleWaypoints")
                        .then(Commands.argument("value", IntegerArgumentType.integer(1, 64)).executes(ctx -> setMaxVisibleWaypoints(ctx)))
                    )
                    .then(Commands.literal("showDeathWaypoint")
                        .then(Commands.argument("value", BoolArgumentType.bool()).executes(ctx -> setShowDeathWaypoint(ctx)))
                    )
                )
                .then(Commands.literal("help")
                    .executes(ctx -> showGeneralHelp(ctx))
                    .then(Commands.literal("style").executes(ctx -> showHelp(ctx, "style")))
                    .then(Commands.literal("showCoordinates").executes(ctx -> showHelp(ctx, "showCoordinates")))
                    .then(Commands.literal("showDays").executes(ctx -> showHelp(ctx, "showDays")))
                    .then(Commands.literal("showWorldDirections").executes(ctx -> showHelp(ctx, "showWorldDirections")))
                    .then(Commands.literal("playerMarkerType").executes(ctx -> showHelp(ctx, "playerMarkerType")))
                    .then(Commands.literal("maxVisiblePlayers").executes(ctx -> showHelp(ctx, "maxVisiblePlayers")))
                    .then(Commands.literal("playerMarkerFadeStartDistance").executes(ctx -> showHelp(ctx, "playerMarkerFadeStartDistance")))
                    .then(Commands.literal("playerMarkerFadeToMinDistance").executes(ctx -> showHelp(ctx, "playerMarkerFadeToMinDistance")))
                    .then(Commands.literal("playerMarkerHideDistance").executes(ctx -> showHelp(ctx, "playerMarkerHideDistance")))
                    .then(Commands.literal("playerMarkerMinAlphaPercent").executes(ctx -> showHelp(ctx, "playerMarkerMinAlphaPercent")))
                    .then(Commands.literal("showWaypoints").executes(ctx -> showHelp(ctx, "showWaypoints")))
                    .then(Commands.literal("maxVisibleWaypoints").executes(ctx -> showHelp(ctx, "maxVisibleWaypoints")))
                    .then(Commands.literal("showDeathWaypoint").executes(ctx -> showHelp(ctx, "showDeathWaypoint")))
                )
        );
    }

    private static void updateSettings(CommandContext<CommandSourceStack> ctx, ServerSettings newSettings, String option, Object value) {
        LocatorBarServerConfig.set(newSettings);
        LocatorBarServerConfig.save();
        if (LocatorBar.broadcaster != null) {
            LocatorBar.broadcaster.broadcastConfig(newSettings);
        }
        ctx.getSource().sendSystemMessage(Component.literal(
                "Server config option '" + option + "' set to " + value
        ));
    }

    private static int showServerSettings(CommandContext<CommandSourceStack> ctx) {
        ServerSettings settings = LocatorBarServerConfig.get();
        if (settings == null) {
            ctx.getSource().sendSystemMessage(Component.literal("Server config is not loaded."));
            return 0;
        }
        ctx.getSource().sendSystemMessage(Component.literal("§6--- Locator Bar Server Settings ---"));
        ctx.getSource().sendSystemMessage(Component.literal("style: §a" + settings.style()));
        ctx.getSource().sendSystemMessage(Component.literal("showCoordinates: §a" + settings.showCoordinates()));
        ctx.getSource().sendSystemMessage(Component.literal("showDays: §a" + settings.showDays()));
        ctx.getSource().sendSystemMessage(Component.literal("showWorldDirections: §a" + settings.showWorldDirections()));
        ctx.getSource().sendSystemMessage(Component.literal("playerMarkerType: §a" + settings.playerMarkerType()));
        ctx.getSource().sendSystemMessage(Component.literal("maxVisiblePlayers: §a" + settings.maxVisiblePlayers()));
        ctx.getSource().sendSystemMessage(Component.literal("playerMarkerFadeStartDistance: §a" + settings.playerMarkerFadeStartDistance()));
        ctx.getSource().sendSystemMessage(Component.literal("playerMarkerFadeToMinDistance: §a" + settings.playerMarkerFadeToMinDistance()));
        ctx.getSource().sendSystemMessage(Component.literal("playerMarkerHideDistance: §a" + settings.playerMarkerHideDistance()));
        ctx.getSource().sendSystemMessage(Component.literal("playerMarkerMinAlphaPercent: §a" + settings.playerMarkerMinAlphaPercent() + "%"));
        ctx.getSource().sendSystemMessage(Component.literal("showWaypoints: §a" + settings.showWaypoints()));
        ctx.getSource().sendSystemMessage(Component.literal("maxVisibleWaypoints: §a" + settings.maxVisibleWaypoints()));
        ctx.getSource().sendSystemMessage(Component.literal("showDeathWaypoint: §a" + settings.showDeathWaypoint()));
        return 1;
    }

    private static int showGeneralHelp(CommandContext<CommandSourceStack> ctx) {
        ctx.getSource().sendSystemMessage(Component.translatable("locatorbar.commands.help.title").withStyle(net.minecraft.ChatFormatting.GOLD));
        ctx.getSource().sendSystemMessage(Component.translatable("locatorbar.commands.help.usage"));
        ctx.getSource().sendSystemMessage(Component.translatable("locatorbar.commands.help.options_list"));
        ctx.getSource().sendSystemMessage(Component.literal("§7style, showCoordinates, showDays, showWorldDirections, playerMarkerType, maxVisiblePlayers, playerMarkerFadeStartDistance, playerMarkerFadeToMinDistance, playerMarkerHideDistance, playerMarkerMinAlphaPercent, showWaypoints, maxVisibleWaypoints, showDeathWaypoint"));
        return 1;
    }

    private static int showHelp(CommandContext<CommandSourceStack> ctx, String option) {
        ctx.getSource().sendSystemMessage(Component.translatable("locatorbar.commands.help.header", option).withStyle(net.minecraft.ChatFormatting.GOLD));
        
        ctx.getSource().sendSystemMessage(Component.translatable("locatorbar.commands.help.label.description")
                .withStyle(net.minecraft.ChatFormatting.YELLOW)
                .append(Component.translatable("locatorbar.commands.help." + option + ".desc").withStyle(net.minecraft.ChatFormatting.RESET)));
                
        ctx.getSource().sendSystemMessage(Component.translatable("locatorbar.commands.help.label.type")
                .withStyle(net.minecraft.ChatFormatting.YELLOW)
                .append(Component.literal(getType(option)).withStyle(net.minecraft.ChatFormatting.GREEN)));
                
        ctx.getSource().sendSystemMessage(Component.translatable("locatorbar.commands.help.label.default")
                .withStyle(net.minecraft.ChatFormatting.YELLOW)
                .append(Component.literal(getDefaultValue(option)).withStyle(net.minecraft.ChatFormatting.GREEN)));
        return 1;
    }

    private static String getType(String option) {
        return switch (option) {
            case "style" -> "Enum (reworked, classic, off)";
            case "showCoordinates", "showDays", "showWorldDirections", "showWaypoints", "showDeathWaypoint" -> "Boolean (true, false)";
            case "playerMarkerType" -> "Enum (heads, dots, off)";
            case "maxVisiblePlayers", "maxVisibleWaypoints" -> "Integer (1 - 64)";
            case "playerMarkerFadeStartDistance", "playerMarkerFadeToMinDistance" -> "Float (0.0 - 60000000.0)";
            case "playerMarkerHideDistance" -> "Float (0.0 - 60000000.0) / inf";
            case "playerMarkerMinAlphaPercent" -> "Float (0.0 - 100.0)";
            default -> "";
        };
    }

    private static String getDefaultValue(String option) {
        ServerSettings def = LocatorBarServerConfig.ServerSettings.defaults();
        switch (option) {
            case "style" -> { return def.style().name().toLowerCase(java.util.Locale.ROOT); }
            case "showCoordinates" -> { return String.valueOf(def.showCoordinates()); }
            case "showDays" -> { return String.valueOf(def.showDays()); }
            case "showWorldDirections" -> { return String.valueOf(def.showWorldDirections()); }
            case "playerMarkerType" -> { return def.playerMarkerType().name().toLowerCase(java.util.Locale.ROOT); }
            case "maxVisiblePlayers" -> { return String.valueOf(def.maxVisiblePlayers()); }
            case "playerMarkerFadeStartDistance" -> { return String.valueOf(def.playerMarkerFadeStartDistance()); }
            case "playerMarkerFadeToMinDistance" -> { return String.valueOf(def.playerMarkerFadeToMinDistance()); }
            case "playerMarkerHideDistance" -> {
                if (def.playerMarkerHideDistance() >= LocatorBarServerConfig.INFINITE_PLAYER_HEAD_DISTANCE) {
                    return "inf";
                }
                return String.valueOf(def.playerMarkerHideDistance());
            }
            case "playerMarkerMinAlphaPercent" -> { return String.valueOf(def.playerMarkerMinAlphaPercent()); }
            case "showWaypoints" -> { return String.valueOf(def.showWaypoints()); }
            case "maxVisibleWaypoints" -> { return String.valueOf(def.maxVisibleWaypoints()); }
            case "showDeathWaypoint" -> { return String.valueOf(def.showDeathWaypoint()); }
            default -> { return ""; }
        }
    }

    private static int setStyle(CommandContext<CommandSourceStack> ctx, LocatorBarStyle style) {
        ServerSettings settings = LocatorBarServerConfig.get();
        ServerSettings newSettings = new ServerSettings(
                style,
                settings.showCoordinates(),
                settings.showDays(),
                settings.showWorldDirections(),
                settings.playerMarkerType(),
                settings.maxVisiblePlayers(),
                settings.playerMarkerFadeStartDistance(),
                settings.playerMarkerFadeToMinDistance(),
                settings.playerMarkerHideDistance(),
                settings.playerMarkerMinAlphaPercent(),
                settings.showWaypoints(),
                settings.maxVisibleWaypoints(),
                settings.showDeathWaypoint()
        );
        updateSettings(ctx, newSettings, "style", style.name());
        return 1;
    }

    private static int setShowCoordinates(CommandContext<CommandSourceStack> ctx) {
        boolean val = BoolArgumentType.getBool(ctx, "value");
        ServerSettings settings = LocatorBarServerConfig.get();
        ServerSettings newSettings = new ServerSettings(
                settings.style(),
                val,
                settings.showDays(),
                settings.showWorldDirections(),
                settings.playerMarkerType(),
                settings.maxVisiblePlayers(),
                settings.playerMarkerFadeStartDistance(),
                settings.playerMarkerFadeToMinDistance(),
                settings.playerMarkerHideDistance(),
                settings.playerMarkerMinAlphaPercent(),
                settings.showWaypoints(),
                settings.maxVisibleWaypoints(),
                settings.showDeathWaypoint()
        );
        updateSettings(ctx, newSettings, "showCoordinates", val);
        return 1;
    }

    private static int setShowDays(CommandContext<CommandSourceStack> ctx) {
        boolean val = BoolArgumentType.getBool(ctx, "value");
        ServerSettings settings = LocatorBarServerConfig.get();
        ServerSettings newSettings = new ServerSettings(
                settings.style(),
                settings.showCoordinates(),
                val,
                settings.showWorldDirections(),
                settings.playerMarkerType(),
                settings.maxVisiblePlayers(),
                settings.playerMarkerFadeStartDistance(),
                settings.playerMarkerFadeToMinDistance(),
                settings.playerMarkerHideDistance(),
                settings.playerMarkerMinAlphaPercent(),
                settings.showWaypoints(),
                settings.maxVisibleWaypoints(),
                settings.showDeathWaypoint()
        );
        updateSettings(ctx, newSettings, "showDays", val);
        return 1;
    }

    private static int setShowWorldDirections(CommandContext<CommandSourceStack> ctx) {
        boolean val = BoolArgumentType.getBool(ctx, "value");
        ServerSettings settings = LocatorBarServerConfig.get();
        ServerSettings newSettings = new ServerSettings(
                settings.style(),
                settings.showCoordinates(),
                settings.showDays(),
                val,
                settings.playerMarkerType(),
                settings.maxVisiblePlayers(),
                settings.playerMarkerFadeStartDistance(),
                settings.playerMarkerFadeToMinDistance(),
                settings.playerMarkerHideDistance(),
                settings.playerMarkerMinAlphaPercent(),
                settings.showWaypoints(),
                settings.maxVisibleWaypoints(),
                settings.showDeathWaypoint()
        );
        updateSettings(ctx, newSettings, "showWorldDirections", val);
        return 1;
    }

    private static int setPlayerMarkerType(CommandContext<CommandSourceStack> ctx, PlayerMarkerType type) {
        ServerSettings settings = LocatorBarServerConfig.get();
        ServerSettings newSettings = new ServerSettings(
                settings.style(),
                settings.showCoordinates(),
                settings.showDays(),
                settings.showWorldDirections(),
                type,
                settings.maxVisiblePlayers(),
                settings.playerMarkerFadeStartDistance(),
                settings.playerMarkerFadeToMinDistance(),
                settings.playerMarkerHideDistance(),
                settings.playerMarkerMinAlphaPercent(),
                settings.showWaypoints(),
                settings.maxVisibleWaypoints(),
                settings.showDeathWaypoint()
        );
        updateSettings(ctx, newSettings, "playerMarkerType", type.name());
        return 1;
    }

    private static int setMaxVisiblePlayers(CommandContext<CommandSourceStack> ctx) {
        int val = IntegerArgumentType.getInteger(ctx, "value");
        ServerSettings settings = LocatorBarServerConfig.get();
        ServerSettings newSettings = new ServerSettings(
                settings.style(),
                settings.showCoordinates(),
                settings.showDays(),
                settings.showWorldDirections(),
                settings.playerMarkerType(),
                val,
                settings.playerMarkerFadeStartDistance(),
                settings.playerMarkerFadeToMinDistance(),
                settings.playerMarkerHideDistance(),
                settings.playerMarkerMinAlphaPercent(),
                settings.showWaypoints(),
                settings.maxVisibleWaypoints(),
                settings.showDeathWaypoint()
        );
        updateSettings(ctx, newSettings, "maxVisiblePlayers", val);
        return 1;
    }

    private static int setPlayerMarkerFadeStartDistance(CommandContext<CommandSourceStack> ctx) {
        float val = FloatArgumentType.getFloat(ctx, "value");
        ServerSettings settings = LocatorBarServerConfig.get();
        ServerSettings newSettings = new ServerSettings(
                settings.style(),
                settings.showCoordinates(),
                settings.showDays(),
                settings.showWorldDirections(),
                settings.playerMarkerType(),
                settings.maxVisiblePlayers(),
                val,
                settings.playerMarkerFadeToMinDistance(),
                settings.playerMarkerHideDistance(),
                settings.playerMarkerMinAlphaPercent(),
                settings.showWaypoints(),
                settings.maxVisibleWaypoints(),
                settings.showDeathWaypoint()
        );
        updateSettings(ctx, newSettings, "playerMarkerFadeStartDistance", val);
        return 1;
    }

    private static int setPlayerMarkerFadeToMinDistance(CommandContext<CommandSourceStack> ctx) {
        float val = FloatArgumentType.getFloat(ctx, "value");
        ServerSettings settings = LocatorBarServerConfig.get();
        ServerSettings newSettings = new ServerSettings(
                settings.style(),
                settings.showCoordinates(),
                settings.showDays(),
                settings.showWorldDirections(),
                settings.playerMarkerType(),
                settings.maxVisiblePlayers(),
                settings.playerMarkerFadeStartDistance(),
                val,
                settings.playerMarkerHideDistance(),
                settings.playerMarkerMinAlphaPercent(),
                settings.showWaypoints(),
                settings.maxVisibleWaypoints(),
                settings.showDeathWaypoint()
        );
        updateSettings(ctx, newSettings, "playerMarkerFadeToMinDistance", val);
        return 1;
    }

    private static int setPlayerMarkerHideDistance(CommandContext<CommandSourceStack> ctx) {
        float val = FloatArgumentType.getFloat(ctx, "value");
        ServerSettings settings = LocatorBarServerConfig.get();
        ServerSettings newSettings = new ServerSettings(
                settings.style(),
                settings.showCoordinates(),
                settings.showDays(),
                settings.showWorldDirections(),
                settings.playerMarkerType(),
                settings.maxVisiblePlayers(),
                settings.playerMarkerFadeStartDistance(),
                settings.playerMarkerFadeToMinDistance(),
                val,
                settings.playerMarkerMinAlphaPercent(),
                settings.showWaypoints(),
                settings.maxVisibleWaypoints(),
                settings.showDeathWaypoint()
        );
        updateSettings(ctx, newSettings, "playerMarkerHideDistance", val);
        return 1;
    }

    private static int setPlayerMarkerHideDistanceInf(CommandContext<CommandSourceStack> ctx) {
        ServerSettings settings = LocatorBarServerConfig.get();
        ServerSettings newSettings = new ServerSettings(
                settings.style(),
                settings.showCoordinates(),
                settings.showDays(),
                settings.showWorldDirections(),
                settings.playerMarkerType(),
                settings.maxVisiblePlayers(),
                settings.playerMarkerFadeStartDistance(),
                settings.playerMarkerFadeToMinDistance(),
                LocatorBarServerConfig.INFINITE_PLAYER_HEAD_DISTANCE,
                settings.playerMarkerMinAlphaPercent(),
                settings.showWaypoints(),
                settings.maxVisibleWaypoints(),
                settings.showDeathWaypoint()
        );
        updateSettings(ctx, newSettings, "playerMarkerHideDistance", "inf");
        return 1;
    }

    private static int setPlayerMarkerMinAlphaPercent(CommandContext<CommandSourceStack> ctx) {
        float val = FloatArgumentType.getFloat(ctx, "value");
        ServerSettings settings = LocatorBarServerConfig.get();
        ServerSettings newSettings = new ServerSettings(
                settings.style(),
                settings.showCoordinates(),
                settings.showDays(),
                settings.showWorldDirections(),
                settings.playerMarkerType(),
                settings.maxVisiblePlayers(),
                settings.playerMarkerFadeStartDistance(),
                settings.playerMarkerFadeToMinDistance(),
                settings.playerMarkerHideDistance(),
                val,
                settings.showWaypoints(),
                settings.maxVisibleWaypoints(),
                settings.showDeathWaypoint()
        );
        updateSettings(ctx, newSettings, "playerMarkerMinAlphaPercent", val);
        return 1;
    }

    private static int setShowWaypoints(CommandContext<CommandSourceStack> ctx) {
        boolean val = BoolArgumentType.getBool(ctx, "value");
        ServerSettings settings = LocatorBarServerConfig.get();
        ServerSettings newSettings = new ServerSettings(
                settings.style(),
                settings.showCoordinates(),
                settings.showDays(),
                settings.showWorldDirections(),
                settings.playerMarkerType(),
                settings.maxVisiblePlayers(),
                settings.playerMarkerFadeStartDistance(),
                settings.playerMarkerFadeToMinDistance(),
                settings.playerMarkerHideDistance(),
                settings.playerMarkerMinAlphaPercent(),
                val,
                settings.maxVisibleWaypoints(),
                settings.showDeathWaypoint()
        );
        updateSettings(ctx, newSettings, "showWaypoints", val);
        return 1;
    }

    private static int setMaxVisibleWaypoints(CommandContext<CommandSourceStack> ctx) {
        int val = IntegerArgumentType.getInteger(ctx, "value");
        ServerSettings settings = LocatorBarServerConfig.get();
        ServerSettings newSettings = new ServerSettings(
                settings.style(),
                settings.showCoordinates(),
                settings.showDays(),
                settings.showWorldDirections(),
                settings.playerMarkerType(),
                settings.maxVisiblePlayers(),
                settings.playerMarkerFadeStartDistance(),
                settings.playerMarkerFadeToMinDistance(),
                settings.playerMarkerHideDistance(),
                settings.playerMarkerMinAlphaPercent(),
                settings.showWaypoints(),
                val,
                settings.showDeathWaypoint()
        );
        updateSettings(ctx, newSettings, "maxVisibleWaypoints", val);
        return 1;
    }

    private static int setShowDeathWaypoint(CommandContext<CommandSourceStack> ctx) {
        boolean val = BoolArgumentType.getBool(ctx, "value");
        ServerSettings settings = LocatorBarServerConfig.get();
        ServerSettings newSettings = new ServerSettings(
                settings.style(),
                settings.showCoordinates(),
                settings.showDays(),
                settings.showWorldDirections(),
                settings.playerMarkerType(),
                settings.maxVisiblePlayers(),
                settings.playerMarkerFadeStartDistance(),
                settings.playerMarkerFadeToMinDistance(),
                settings.playerMarkerHideDistance(),
                settings.playerMarkerMinAlphaPercent(),
                settings.showWaypoints(),
                settings.maxVisibleWaypoints(),
                val
        );
        updateSettings(ctx, newSettings, "showDeathWaypoint", val);
        return 1;
    }

    private static boolean isMultiplayerOrLan(CommandSourceStack source) {
        var server = source.getServer();
        if (server == null) {
            return false;
        }
        return server.isDedicatedServer() || server.isPublished();
    }
}