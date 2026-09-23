package pl.fuzjajadrowa.locatorbar.config

import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.*
import pl.fuzjajadrowa.locatorbar.config.LocatorBarServerConfig.ServerSettings
import pl.fuzjajadrowa.locatorbar.config.LocatorBarEnums.PlayerMarkerType

class ConfigTest {
    private val config = Path.of("config")
    private val client = config.resolve("locatorbar.json")
    private val server = config.resolve("locatorbar-server.json")
    private val legacy = config.resolve("locatorbar-server.toml")

    @BeforeTest fun setup() {
        Files.createDirectories(config)
        Files.deleteIfExists(client)
        Files.deleteIfExists(server)
        Files.deleteIfExists(legacy)
        LocatorBarConfig.clearServerSettings()
    }

    private fun assertDistances(start: Float, end: Float, hide: Float) {
        assertTrue(start.isFinite() && end.isFinite() && hide.isFinite())
        assertTrue(0 <= start && start <= end && end <= hide && hide <= 60_000_000, "$start <= $end <= $hide")
    }

    @Test fun serverDistancesAtEveryEntryPoint() {
        val cases = listOf(
            floatArrayOf(50f, 125f, 500f), floatArrayOf(-1f, -5f, -10f),
            floatArrayOf(90_000_000f, 80_000_000f, 70_000_000f),
            floatArrayOf(300f, 100f, 500f), floatArrayOf(50f, 500f, 100f),
            floatArrayOf(90_000_000f, -20f, Float.NaN),
            floatArrayOf(Float.NaN, Float.NEGATIVE_INFINITY, Float.POSITIVE_INFINITY)
        )
        for ((start, end, hide) in cases) {
            val settings = ServerSettings.defaults().update {
                it.playerMarkerFadeStartDistance = start
                it.playerMarkerFadeToMinDistance = end
                it.playerMarkerHideDistance = hide
            }
            assertDistances(settings.playerMarkerFadeStartDistance(), settings.playerMarkerFadeToMinDistance(), settings.playerMarkerHideDistance())
            LocatorBarServerConfig.set(settings)
            LocatorBarServerConfig.save()
            LocatorBarServerConfig.load()
            assertEquals(settings, LocatorBarServerConfig.get())
            LocatorBarConfig.applyServerSettings(settings)
            assertDistances(LocatorBarConfig.getPlayerMarkerFadeStartDistance(), LocatorBarConfig.getPlayerMarkerFadeToMinDistance(), LocatorBarConfig.getPlayerMarkerHideDistance())
        }
        val defaults = ServerSettings.defaults()
        assertEquals(defaults, defaults.update { })
        assertEquals(defaults.showDays().not(), defaults.update { it.showDays = !it.showDays }.showDays())
    }

    @Test fun clientCorruptedValuesAndSetter() {
        Files.writeString(client, """{"version":2,"scale":-5,"viewAngle":900,"playerMarkerType":null,
            "playerMarkerFadeStartDistance":90000000,"playerMarkerFadeToMinDistance":-3,"playerMarkerHideDistance":-5,
            "waypoints":{"00000000-0000-0000-0000-000000000001":null}}""")
        LocatorBarConfig.load()
        assertEquals(0.5f, LocatorBarConfig.getScale())
        assertEquals(180f, LocatorBarConfig.getViewAngle())
        assertEquals(PlayerMarkerType.HEADS, LocatorBarConfig.getPlayerMarkerType())
        assertTrue(LocatorBarConfig.getWaypoints().isEmpty())
        assertDistances(LocatorBarConfig.getPlayerMarkerFadeStartDistance(), LocatorBarConfig.getPlayerMarkerFadeToMinDistance(), LocatorBarConfig.getPlayerMarkerHideDistance())
        LocatorBarConfig.setPlayerMarkerFadeStartDistance(Float.NaN)
        assertDistances(LocatorBarConfig.getPlayerMarkerFadeStartDistance(), LocatorBarConfig.getPlayerMarkerFadeToMinDistance(), LocatorBarConfig.getPlayerMarkerHideDistance())
    }

    @Test fun rawJsonDistancesNormalizeSequentially() {
        for (values in listOf("50,125,500", "-1,-2,-3", "90000000,80000000,70000000",
            "300,100,500", "50,500,100", "90000000,-20,-50")) {
            val (start, end, hide) = values.split(',')
            val json = """{"version":2,"playerMarkerFadeStartDistance":$start,
                "playerMarkerFadeToMinDistance":$end,"playerMarkerHideDistance":$hide}"""
            Files.writeString(server, json)
            LocatorBarServerConfig.load()
            val settings = LocatorBarServerConfig.get()
            assertDistances(settings.playerMarkerFadeStartDistance(), settings.playerMarkerFadeToMinDistance(), settings.playerMarkerHideDistance())
            Files.writeString(client, json)
            LocatorBarConfig.load()
            assertDistances(LocatorBarConfig.getPlayerMarkerFadeStartDistance(), LocatorBarConfig.getPlayerMarkerFadeToMinDistance(), LocatorBarConfig.getPlayerMarkerHideDistance())
            assertEquals(settings.playerMarkerFadeStartDistance(), LocatorBarConfig.getPlayerMarkerFadeStartDistance())
            assertEquals(settings.playerMarkerFadeToMinDistance(), LocatorBarConfig.getPlayerMarkerFadeToMinDistance())
            assertEquals(settings.playerMarkerHideDistance(), LocatorBarConfig.getPlayerMarkerHideDistance())
        }
    }

    @Test fun legacyClientMigration() {
        for (version in listOf("\"version\":1,", "")) {
            Files.writeString(client, """{$version"showPlayerHeads":false,"playerHeadsScale":1.5,"playerHeadOutline":true}""")
            LocatorBarConfig.load()
            assertEquals(PlayerMarkerType.OFF, LocatorBarConfig.getPlayerMarkerType())
            assertEquals(1.5f, LocatorBarConfig.getPlayerMarkersScale())
            assertTrue(LocatorBarConfig.isPlayerMarkerOutline())
            LocatorBarConfig.load()
            assertEquals(PlayerMarkerType.OFF, LocatorBarConfig.getPlayerMarkerType())
        }
    }

    @Test fun legacyServerMigrationAndInvariants() {
        Files.writeString(legacy, """
            showPlayerHeads = false
            playerHeadFadeStartDistance = 90000000
            playerHeadFadeToMinDistance = -12
            playerHeadHideDistance = "inf"
        """.trimIndent())
        LocatorBarServerConfig.load()
        val settings = LocatorBarServerConfig.get()
        assertEquals(PlayerMarkerType.OFF, settings.playerMarkerType())
        assertDistances(settings.playerMarkerFadeStartDistance(), settings.playerMarkerFadeToMinDistance(), settings.playerMarkerHideDistance())
        assertTrue(Files.exists(server))
        assertFalse(Files.exists(legacy))
    }

    @Test fun damagedJsonIsBackedUpAndDefaultsLoaded() {
        val broken = "{ broken json"
        Files.writeString(client, broken)
        LocatorBarConfig.load()
        assertEquals(1f, LocatorBarConfig.getScale())
        Files.list(config).use { files ->
            assertTrue(files.anyMatch { it.fileName.toString().endsWith(".bak") && Files.readString(it) == broken })
        }
        Files.writeString(server, broken)
        LocatorBarServerConfig.load()
        assertEquals(ServerSettings.defaults(), LocatorBarServerConfig.get())
    }

    @Test fun failedReadOrWriteDoesNotDestroyOriginal() {
        val directory = Files.createTempDirectory("config-test")
        val target = directory.resolve("settings.json")
        Files.createDirectory(target)
        Files.writeString(target.resolve("keep"), "original")
        val file = ConfigFile(target, ServerSettings::class.java)
        assertEquals(ServerSettings.defaults(), file.read(ServerSettings::defaults))
        assertFalse(file.save(ServerSettings.defaults()))
        assertEquals("original", Files.readString(target.resolve("keep")))
        val failedWrite = ConfigFile(target, ServerSettings::class.java)
        assertFalse(failedWrite.save(ServerSettings.defaults()))
        Files.list(directory).use { files -> assertEquals(1L, files.count()) }
    }
}